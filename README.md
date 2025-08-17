<div align="center">

<img src="./packages/forgeworks/icon.png" alt="Modpack Icon" width="300" />

# Forgeworks

**Modpack containing magic, engineering and a few QOL mods.**

[![CurseForge Downloads](https://img.shields.io/curseforge/dt/1185057?style=for-the-badge&label=CurseForge&color=orange&logoColor=orange&labelColor=black&logo=curseforge)](https://www.curseforge.com/minecraft/modpacks/forgeworks)

</div>

---

## About

This modpack is a magic/engineering modpack used on my Minecraft server with friends.
It's focused on creating factories and using magic, but also contains lots of food items, more bows, hammers for mining bigger areas (3x3, 5x5, etc.) and a few quality of life mods.

Other than that the modpack contains mods like [Sodium](https://www.curseforge.com/minecraft/mc-mods/sodium) to improve performance on the client-side as well as server-side.

## Contributing

### KubeJS Setup

To get typescript support in kubejs scripts, you will need to generate typings using [ProbeJS](https://www.curseforge.com/minecraft/mc-mods/probejs). To do this, follow the steps below.

1. Download the latest version of this modpack.
2. Enable ProbeJS in the modpack if it hasn't been enabled already.
3. Start up Minecraft with this modpack.
4. Create a new world and enter it. This should generate KubeJS typings with ProbeJS.
5. Go into the instance folder of your modpack.
6. Copy the .probe folder and paste it in the lumiosecraft folder.

The resulting folder structure should look like the following:

```
<PROJECT_ROOT_DIRECTORY>
├── .probe
│   ├── classes.txt
│   ├── client
│   │   └── probe-types
│   │       ├── global
│   │       └── packages
│   ├── decompiled
│   ├── images
│   ├── server
│   │   └── probe-types
│   │       ├── global
│   │       └── packages
│   └── startup
│       └── probe-types
│           ├── global
│           └── packages
├── .vscode
├── packages
└── ...
```
