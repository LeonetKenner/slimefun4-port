# Vendored dependencies

This directory is a small, in-repo Maven repository (`file://${project.basedir}/libs`)
that is declared **first** in `pom.xml`.

It hosts compile-time-only (`provided`) plugin APIs that Slimefun still
integrates with but whose canonical Maven artifact is either unmaintained or
otherwise not reliably available. Because they are never bundled into the final
jar, only the bare API is needed to compile the integration code. The upstream
repositories may disappear at any time, so a copy is kept here to keep the build
self-contained.

Nothing in here is shaded into `Slimefun v4.9-UNOFFICIAL.jar`.

| Artifact | Project | Notes |
| --- | --- | --- |
| `com.massivecraft:Factions:1.6.9.5-4.1.4-STABLE` | MassiveCraft Factions | unmaintained; compile stub for the maintained FactionsUUID fork (same API) |
| `me.minebuilders:clearlag-core:3.1.6` | ClearLag | discontinued; Slimefun imports its API directly in `ClearLagIntegration` |

To refresh an artifact, replace the files in its directory (Maven layout:
`<groupId as path>/<artifactId>/<version>/`) and update the checksums if added.