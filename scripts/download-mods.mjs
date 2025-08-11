import { existsSync } from "node:fs";
import { mkdir, readFile, glob, unlink } from "node:fs/promises";
import { join, relative } from "node:path";
import { spawn } from "node:child_process";
import { fileURLToPath } from "node:url";
import toml from "toml";

const baseDir = new URL("../", import.meta.url);
const installDir = new URL("install/", baseDir);
const packageDir = new URL(`file:///${process.cwd()}/`);
const modpackDir = new URL("modpack/", packageDir);

if (!existsSync(installDir)) await mkdir(installDir);

const packFile = await readFile(new URL("pack.toml", modpackDir), {
	encoding: "utf-8"
});
const indexFilePath = toml.parse(packFile).index.file;
const indexFile = await readFile(new URL(indexFilePath, modpackDir), {
	encoding: "utf-8"
});
const indexFiles = toml.parse(indexFile).files.map((file) => new URL(file.file, modpackDir));

const downloadedFiles = [];

for (let batchIndex = 0; batchIndex < indexFiles.length; batchIndex += 100) {
	const batch = indexFiles.slice(batchIndex, batchIndex + 100);

	console.log("Downloading files...");
	await Promise.all(
		batch.map(async (file) => {
			const dir = new URL("./", file);

			if (!file.pathname.endsWith(".pw.toml")) {
				return;
			}

			const content = await readFile(file, { encoding: "utf-8" });
			const fileToml = toml.parse(content);
			const { side, filename } = fileToml;

			const relativeDir = relative(modpackDir.pathname, dir.pathname);
			const destDir = new URL(join(side, relativeDir, "/"), installDir);
			const downloading = new URL(filename, destDir);

			downloadedFiles.push(downloading.pathname);

			if (existsSync(downloading)) {
				return;
			}
			if (!existsSync(destDir)) {
				await mkdir(destDir, { recursive: true });
			}

			function downloadFile(url, dest) {
				if (dest instanceof URL) {
					dest = fileURLToPath(dest);
				}
				console.log(`Downloading file from ${url}`);
				return new Promise((resolve, reject) => {
					const curl = spawn("curl", ["-fL", "-4", "--retry", "3", "-o", dest, url]);

					curl.on("close", (code) => {
						if (code === 0) {
							resolve();
						} else {
							reject(new Error(`curl failed with exit code ${code}`));
						}
					});
				});
			}

			if (fileToml.update?.curseforge) {
				const projectId = fileToml.update.curseforge["project-id"];
				const fileId = fileToml.update.curseforge["file-id"];
				await downloadFile(
					`https://www.curseforge.com/api/v1/mods/${projectId}/files/${fileId}/download`,
					downloading
				);
			} else if (fileToml.update?.modrinth) {
				const modId = fileToml.update.modrinth["mod-id"];
				const version = fileToml.update.modrinth.version;
				await downloadFile(
					`https://cdn.modrinth.com/data/${modId}/versions/${version}/${encodeURIComponent(filename)}`,
					downloading
				);
			}
		})
	);
}

console.log("Removing old files...");
for await (const file of glob(join(installDir.pathname.substring(1), "**", "*.*"))) {
	const fileURL = new URL(`file://${file}`);
	if (!downloadedFiles.includes(fileURL.pathname)) {
		await unlink(fileURL);
	}
}
