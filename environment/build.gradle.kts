import groovy.json.JsonSlurper

plugins {
    id("java-library")
    id("net.neoforged.moddev") version ("2.0.137")
    id("idea")
}

abstract class JsonObjectMapper<T> {
    abstract fun from(map: Map<*, *>): T

    fun from(file: File): T {
        return from(JsonSlurper().parse(file) as Map<*, *>)
    }
}

class Pakku(val name: String, val version: String) {
    companion object : JsonObjectMapper<Pakku>() {
        @Suppress("UNCHECKED_CAST")
        override fun from(map: Map<*, *>): Pakku {
            if (!validate(map)) {
                throw IllegalArgumentException("Invalid Pakku configuration")
            }
            return Pakku(
                name = map["name"] as String,
                version = map["version"] as String
            )
        }

        private fun validate(map: Map<*, *>): Boolean {
            if (!map.containsKey("name") || map["name"] !is String) return false
            if (!map.containsKey("version") || map["version"] !is String) return false
            return true
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
        override fun from(map: Map<*, *>): PakkuLock {
            if (!validate(map)) {
                throw IllegalArgumentException("Invalid PakkuLock configuration")
            }
            return PakkuLock(
                target = map["target"] as String,
                mcVersions = map["mc_versions"] as List<String>,
                loaders = map["loaders"] as Map<String, String>,
                lockfileVersion = map["lockfile_version"] as Int
            )
        }

        fun validate(map: Map<*, *>): Boolean {
            if (!map.keys.all { it is String }) return false
            if (!map.containsKey("target") || map["target"] !is String) return false
            if (!map.containsKey("mc_versions") || map["mc_versions"] !is List<*>) return false
            if (!(map["mc_versions"] as List<*>).all { it is String }) return false
            if (!map.containsKey("loaders") || map["loaders"] !is Map<*, *>) return false
            if (!(map["loaders"] as Map<*, *>).keys.all { it is String }) return false
            if (!(map["loaders"] as Map<*, *>).values.all { it is String }) return false
            if (!map.containsKey("lockfile_version") || map["lockfile_version"] !is Int) return false
            return true
        }
    }
}

val pakku = Pakku.from(file("../modpack/pakku.json"))
val pakkuLock = PakkuLock.from(file("../modpack/pakku-lock.json"))
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
