# Vendored dependencies

This directory is a small, in-repo Maven repository (`file://${project.basedir}/libs`)
that is declared **first** in `pom.xml`.

It hosts compile-time-only (`provided`) plugin APIs from projects that are no
longer maintained. Because they are never bundled into the final jar, only the
bare APIs are needed to compile the integration code in
`io.github.bakedlibs.dough.protection`. Their upstream repositories may
disappear at any time, so a copy is kept here to keep the build self-contained.

Nothing in here is shaded into `Slimefun v4.9-UNOFFICIAL.jar`.

| Artifact | Project | Notes |
| --- | --- | --- |
| `com.github.elBukkit:PreciousStones:1.17.2` | PreciousStones | unmaintained |
| `de.diddiz:logblock:1.17.0.0-SNAPSHOT` | LogBlock | unmaintained (snapshot) |
| `com.github.marcelo-mason:SimpleClans:7c3db52796` | SimpleClans | unmaintained |
| `com.massivecraft:Factions:1.6.9.5-4.1.4-STABLE` | MassiveCraft Factions | unmaintained |
| `com.github.fubira:Lockette:9dac96e8f8` | Lockette | unmaintained |
| `com.github.angeschossen:ChestProtectAPI:3.9.1` | ChestProtect | unmaintained |
| `net.dzikoysk.funnyguilds:plugin:4.12.0` | FunnyGuilds 4.x | superseded by 5.x |
| `org.panda-lang:expressible:1.3.1` | Expressible | needed by FunnyGuilds 4.x API |
| `de.epiceric:ShopChest:1.13-SNAPSHOT` | ShopChest | unmaintained (snapshot) |
| `org.popcraft:bolt-bukkit:1.0.580` | Bolt | archived |
| `org.popcraft:bolt-common:1.0.580` | Bolt | archived |
| `me.minebuilders:clearlag-core:3.1.6` | ClearLag | discontinued |

To refresh an artifact, replace the files in its directory (Maven layout:
`<groupId as path>/<artifactId>/<version>/`) and update the checksums if added.
