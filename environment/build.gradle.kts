import groovy.json.JsonSlurper

plugins {
    id("java-library")
    id("net.neoforged.moddev") version ("2.0.140")
    id("idea")
}

abstract class JsonObjectMapper<T> {
    protected abstract fun create(map: Map<*, *>): T
    protected abstract fun validate(map: Map<*, *>)

    fun from(path: String) = from(File(path))
    fun from(file: File): T {
        val parsed = JsonSlurper().parse(file)
        if (parsed !is Map<*, *>)
            throw IllegalArgumentException("Expected JSON object in file: ${file.path}")
        return from(parsed)
    }
    fun from(map: Map<*, *>): T {
        validate(map)
        return create(map)
    }
}

class Pakku(val name: String, val version: String) {
    companion object : JsonObjectMapper<Pakku>() {
        @Suppress("UNCHECKED_CAST")
        override fun create(map: Map<*, *>): Pakku {
            return Pakku(
                name = map["name"] as String,
                version = map["version"] as String
            )
        }

        override fun validate(map: Map<*, *>) {
            if (!map.containsKey("name") || map["name"] !is String)
                throw IllegalArgumentException("Invalid or missing 'name' field in Pakku JSON")
            if (!map.containsKey("version") || map["version"] !is String)
                throw IllegalArgumentException("Invalid or missing 'version' field in Pakku JSON")
        }
    }
}

class PakkuLock(
    val target: String,
    val mcVersions: List<String>,
    val loaders: Map<String, String>,
    val lockfileVersion: Int
) {
    companion object : JsonObjectMapper<PakkuLock>() {
        @Suppress("UNCHECKED_CAST")
        override fun create(map: Map<*, *>): PakkuLock {
            return PakkuLock(
                target = map["target"] as String,
                mcVersions = map["mc_versions"] as List<String>,
                loaders = map["loaders"] as Map<String, String>,
                lockfileVersion = map["lockfile_version"] as Int
            )
        }

        override fun validate(map: Map<*, *>) {
            if (!map.containsKey("target") || map["target"] !is String)
                throw IllegalArgumentException("Invalid or missing 'target' field in Pakku JSON")
            if (!map.containsKey("mc_versions") || map["mc_versions"] !is List<*>)
                throw IllegalArgumentException("Invalid or missing 'mc_versions' field in Pakku JSON")
            if (!(map["mc_versions"] as List<*>).all { it is String })
                throw IllegalArgumentException("'mc_versions' field must be a list of strings in Pakku JSON")
            if (!map.containsKey("loaders") || map["loaders"] !is Map<*, *>)
                throw IllegalArgumentException("Invalid or missing 'loaders' field in Pakku JSON")
            if (!(map["loaders"] as Map<*, *>).keys.all { it is String })
                throw IllegalArgumentException("'loaders' field must have string keys in Pakku JSON")
            if (!(map["loaders"] as Map<*, *>).values.all { it is String })
                throw IllegalArgumentException("'loaders' field must have string values in Pakku JSON")
            if (!map.containsKey("lockfile_version") || map["lockfile_version"] !is Int)
                throw IllegalArgumentException("Invalid or missing 'lockfile_version' field in Pakku JSON")
        }
    }
}

val pakku = Pakku.from("../modpack/pakku.json")
val pakkuLock = PakkuLock.from("../modpack/pakku-lock.json")
val neoVersion = pakkuLock.loaders["neoforge"]

tasks.named<Wrapper>("wrapper").configure {
    distributionType = Wrapper.DistributionType.BIN
}

java.toolchain.languageVersion = JavaLanguageVersion.of(21)

neoForge {
    version = neoVersion

    runs {
        val client = create("client")
        client.client()
        client.gameDirectory = file("run/client")

        val server = create("server")
        server.server()
        server.gameDirectory = file("run/server")
        server.programArgument("--nogui")
    }
}

dependencies {
    runtimeOnly("net.neoforged:neoforge:${neoVersion}")

    val modsDir = File("../modpack/mods")
    if (!modsDir.exists()) {
        println("The mods directory does not exist. Did you run 'pakku fetch'?")
    } else {
        val mods = modsDir.listFiles().filter { it.isFile && it.name.endsWith(".jar") }
        if (mods.isEmpty()) {
            println("The mods directory does not contain any mods.")
        }
        for (file in mods) {
            runtimeOnly(files(file))
        }
    }
}

idea {
    module {
        for (fileName in listOf("build", "run", "run-data", "out", "logs")) {
            excludeDirs.add(file(fileName))
        }
    }
}
