# Architectury Multiversion Multiloader Template

A Minecraft mod template built on [Architectury](https://architectury.org/), targeting three Minecraft
versions at once - **1.20.1**, **1.21.1**, and **26.2** - across **Fabric**, **NeoForge**, and
(1.20.1 only) **Forge** - from one shared codebase.

| Minecraft | Fabric | NeoForge | Forge |
|---|---|---|---|
| 1.20.1 | ✅ | - | ✅ |
| 1.21.1 | ✅ | ✅ | - |
| 26.2   | ✅ | ✅ | - |

Every one of those six combinations has been built end-to-end as part of setting this template up (real
Minecraft/mappings download, real compile, real jar assembly) - it's not just config that parses.

## Getting started

**Prerequisites:** git, and a network connection for the first build (it downloads each targeted
Minecraft version's jar, mappings and, for Forge/NeoForge, patches). You do *not* need any particular JDK
pre-installed - `settings.gradle.kts` lets Gradle download whichever of Java 17/21/25 a given version
needs.

```sh
git clone <your fork's URL>
cd <your fork>
./gradlew build          # builds every loader for the "active" version - see stonecutter.gradle.kts
```

Then run it:

```sh
./gradlew :fabric:1.21.1:runClient      # launches a dev client with the mod loaded
./gradlew :neoforge:1.21.1:runClient
./gradlew :forge:1.20.1:runClient
```

(swap `runClient` for `runServer` for a dedicated server). You should see `Example Mod (examplemod)
initialized` in the log - that's `ExampleMod.init()`, in `src/main/java/com/example/examplemod/`, being
called by whichever loader you ran. From there:

1. **Rename it** - see "Using this template" below. Do this before writing real code so class/package
   names don't shift under you.
2. **Open it in an IDE.** [IntelliJ IDEA](https://www.jetbrains.com/idea/) plus the
   [Stonecutter plugin](https://plugins.jetbrains.com/plugin/24630-stonecutter) is the smoothest experience
   - the plugin adds a per-version "Set active" action so the IDE's indexing/error-checking follows
   whichever version you're actively editing (`stonecutter active "..."` in `stonecutter.gradle.kts` is
   what it edits). Any other IDE - including plain VS Code - works fine too, just via the `./gradlew`
   commands above/below instead of a visual switcher.
3. **Add real content** in `src/main/java/com/example/examplemod/`, using
   [Architectury API](https://docs.architectury.dev/)'s cross-loader registries/events/networking so it
   stays shared across all three loaders. See "What's deliberately not here yet" below.

## Using this template

Everything is currently named after the placeholder `ExampleMod`/`examplemod`/`com.example.examplemod`.
To rename it:

1. **`gradle.properties`**: set `modId`, `modGroup`, `modVersion`, `modName`, `modDescription`,
   `modAuthors`. These flow into `fabric.mod.json`/`mods.toml`/`neoforge.mods.toml` at build time - no
   other file needs your mod's display name or description.
2. **`settings.gradle.kts`**: change `rootProject.name`.
3. **The package**: move `com/example/examplemod/` to your own package in all four source trees
   (`src/`, `fabric/src/`, `forge/src/`, `neoforge/src/`) and update the `package`/`import` lines in the
   four Java files to match.
4. **The classes**: rename `ExampleMod`/`ExampleModFabric`/`ExampleModForge`/`ExampleModNeoForge` to
   whatever you like, and update the one place each is referenced by string: the fabric `entrypoints` in
   `fabric/src/main/resources/fabric.mod.json`, and `MOD_ID` in `ExampleMod.java` - keep that constant in
   sync with `modId` from step 1.
5. Pick a real license (see below) and replace this README with one about your actual mod.

A single project-wide search-and-replace for `examplemod`/`ExampleMod`/`com.example.examplemod` covers
essentially all of the above at once.

No `LICENSE` file is included - `modLicense` is left as the placeholder `ARR` (all rights reserved).
Pick a real license (MIT, LGPL-3.0, CC0, ...) via [choosealicense.com](https://choosealicense.com/) and
add both the file and the `modLicense` value before publishing anything.

## Project layout

```
.
├── src/main/java/com/example/examplemod/ExampleMod.java             # shared "common" code
├── fabric/     src/.../fabric/ExampleModFabric.java                 # Fabric entry point + fabric.mod.json
├── forge/      src/.../forge/ExampleModForge.java                   # Forge entry point + mods.toml (1.20.1 only)
├── neoforge/   src/.../neoforge/ExampleModNeoForge.java              # NeoForge entry point + neoforge.mods.toml
├── versions/<mcVersion>/gradle.properties   # per-version dependency numbers (see "Building" below)
├── build.gradle.kts            # "common" build script for 1.20.1 & 1.21.1 (26.2 uses build.26.gradle.kts)
├── fabric/build.gradle.kts     # Fabric build script for 1.20.1 & 1.21.1 (26.2 uses fabric/build.26.gradle.kts)
├── forge/build.gradle.kts      # Forge build script (1.20.1 only)
├── neoforge/build.gradle.kts   # NeoForge build script for 1.21.1 (26.2 uses neoforge/build.26.gradle.kts)
├── settings.gradle.kts         # declares the versions/loaders matrix - start reading here
└── stonecutter.gradle.kts      # shared plugin versions + the "active" version
```

There's no separate Gradle module per Minecraft version to maintain by hand - Stonecutter generates
`:fabric:1.20.1`, `:fabric:1.21.1`, `:fabric:26.2`, `:forge:1.20.1`, `:neoforge:1.21.1`, `:neoforge:26.2`
(and the matching `:1.20.1`/`:1.21.1`/`:26.2` common projects) from the settings.gradle.kts matrix and the
build scripts above.

## Why this structure

Getting one Minecraft version's worth of Fabric+Forge+NeoForge working from a single codebase is what
Architectury is for. Getting **three** Minecraft versions' worth working from one codebase is a separate,
newer problem, solved by a second tool: [Stonecutter](https://stonecutter.kikugie.dev/). This template
combines them the way the Stonecutter team's own (now-archived) reference did:

- **Stonecutter** turns `common/fabric/forge/neoforge` (the classic Architectury multiloader layout) into
  one copy per Minecraft version, under `versions/<mcVersion>/<loader>/` - each copy compiles against that
  version's real Minecraft/mappings/loader, but they all share the *same* source in `src/`, `fabric/src/`,
  `forge/src/`, `neoforge/src/`. That's what makes this genuinely "multiversion" rather than three
  unrelated copies of the same mod.
- **Architectury** (`architectury-plugin` + `architectury-loom` + the Architectury API) is what makes
  `common/` code loader-agnostic within each of those versions.

Two real, current repos informed this (both from the Stonecutter team):

- [`stonecutter-template-architectury`](https://github.com/stonecutter-versioning/stonecutter-template-architectury) -
  **archived**, but its `versions()`/`branch()` structure (Stonecutter for the MC-version axis, classic
  Architectury subprojects for the loader axis) is exactly what this template uses, updated to current
  tool versions and extended to 26.2.
- [`stonecutter-template-multiloader`](https://github.com/stonecutter-versioning/stonecutter-template-multiloader) -
  actively maintained, but deliberately **not** what this is based on: it drops Architectury entirely in
  favor of each loader's own native Gradle plugin (`dev.kikugie.loom-back-compat` for Fabric, NeoForge's
  own `net.neoforged.moddev` for NeoForge), which is the current path of least resistance for brand-new
  Minecraft versions but isn't "using Architectury". Worth switching to later if Architectury's own 26.x
  support (see below) doesn't mature.

## The two rough edges this hit

Both are called out inline in the affected build scripts too.

**1. 26.2 needs a different Loom variant, because Minecraft removed obfuscation.** Starting with the 26.x
(year.drop) versions, Java Edition [ships without
obfuscation](https://www.minecraft.net/en-us/article/removing-obfuscation-in-java-edition) - there's no
Mojang-mappings deobfuscation step left to run, which is exactly what Architectury Loom's ordinary
`officialMojangMappings()` path tries to do, and currently can't
([architectury/architectury-loom#328](https://github.com/architectury/architectury-loom/issues/328), still
open). `build.26.gradle.kts`, `fabric/build.26.gradle.kts` and `neoforge/build.26.gradle.kts` use the
`dev.architectury.loom-no-remap` plugin variant instead, with no `mappings(...)` dependency - the
documented workaround.

**2. NeoForge on 1.20.1 has to be configured as Forge.** NeoForge forked from Forge *at* 1.20.1, and for
that one version is still, internally, Forge (same SRG-based toolchain, same `net.minecraftforge` package
namespace, and it even still publishes under the `net.neoforged:forge` Maven coordinate it forked from).
Telling Architectury Loom `neoForge()` for this version crashes
([architectury/architectury-loom#289](https://github.com/architectury/architectury-loom/issues/289)); it
has to be told `forge()` instead, same as the real `forge/` project. Rather than carry that fragile
special case, **this template has no dedicated NeoForge build for 1.20.1** - and doesn't need one, because
1.20.1 is also the last Minecraft version where Forge and NeoForge mods are interchangeable, so the
`forge/` build already covers NeoForge users there. If you need a separate NeoForge-branded jar for
1.20.1 anyway, start from the `forge/` build and swap its `META-INF/mods.toml` for a
`META-INF/neoforge.mods.toml` (a template is in `neoforge/src/main/resources/`).

## Building

```sh
./gradlew build                    # builds the "active" version (see stonecutter.gradle.kts) for all its loaders
./gradlew :fabric:1.21.1:build      # builds one specific (version, loader) combination
./gradlew :forge:1.20.1:build
./gradlew :fabric:1.21.1:runClient  # launches a dev client for one specific combination
```

Built jars land in `<loader>/versions/<mcVersion>/build/libs/`.

Building **more than one Minecraft version in the same command** can intermittently fail with a mappings
cache error - this is a real, observed Architectury Loom quirk with running multiple versions' Minecraft
setup concurrently in one Gradle daemon, not a configuration problem. If you hit it, add `--max-workers=1`
or build one version at a time; it hasn't reappeared with either since.

The per-version dependency numbers (loader versions, Fabric API, Architectury API, ...) live in
`versions/<mcVersion>/gradle.properties` and were current as of when this template was set up - they will
drift. Check each loader's latest before a release build: Fabric Loader/API at
[fabricmc.net](https://fabricmc.net/develop/), NeoForge at [neoforged.net](https://neoforged.net/), Forge
at [files.minecraftforge.net](https://files.minecraftforge.net/), Architectury API at
[github.com/architectury/architectury-api](https://github.com/architectury/architectury-api/releases).

## Adding a fourth Minecraft version

1. Add a `versions/<mcVersion>/gradle.properties` with that version's `javaVersion` and loader/API
   versions (see the links just above for where to check current numbers).
2. Add it to the `versions(...)`/`branch(...)` calls in `settings.gradle.kts`.
3. If it needs the no-remap toolchain (anything from 26.1 onward, going by the obfuscation-removal article
   above) or NeoForge-as-Forge handling (unlikely for anything after 1.20.1), give it its own
   `.buildscript("build.<name>.gradle.kts")` the way 26.2 and 1.20.1's `forge/` do, copying the nearest
   existing `build.26.gradle.kts` as a starting point.
4. Where actual game code differs between versions, reach for a Stonecutter `//? if <condition> {}`
   comment in the shared source (see `stonecutter.kikugie.dev`'s
   [wiki](https://stonecutter.kikugie.dev/wiki/) for the syntax) rather than duplicating files - that's
   the entire point of this structure.

## What's deliberately not here yet

This is a template, not a mod: no items, blocks, or registries. `ExampleMod.init()` just logs. Add real
content in `src/main/java/com/example/examplemod/` using
[Architectury API](https://docs.architectury.dev/)'s cross-loader registries/events/networking so it stays
shared across all three loaders; reach for the loader-specific `fabric/`/`forge/`/`neoforge/` source trees
only for genuinely loader-specific code (client rendering registration, loader-specific events, and so
on).
