# Architectury Multiversion Multiloader Template

A Minecraft mod template built on [Architectury](https://architectury.org/), targeting Minecraft versions
from **1.20.1** through **26.2** - across **Fabric**, **NeoForge**, and (1.20.1 only) **Forge** - from one
shared codebase.

| Minecraft | Fabric | NeoForge | Forge |
|---|---|---|---|
| 1.20.1  | ✅ | - | ✅ |
| 1.20.2  | ✅ | ✅ | - |
| 1.20.4  | ✅ | ✅ | - |
| 1.20.5  | ✅ | ✅ | - |
| 1.20.6  | ✅ | ✅ | - |
| 1.21.1  | ✅ | ✅ | - |
| 1.21.2  | ✅ | ✅ | - |
| 1.21.3  | ✅ | ✅ | - |
| 1.21.4  | ✅ | ✅ | - |
| 1.21.5  | ✅ | ✅ | - |
| 1.21.6  | ✅ | ✅ | - |
| 1.21.7  | ✅ | ✅ | - |
| 1.21.8  | ✅ | ✅ | - |
| 1.21.9  | ✅ | ✅ | - |
| 1.21.10 | ✅ | ✅ | - |
| 26.1    | ✅ | ✅ | - |
| 26.1.1  | ✅ | ✅ | - |
| 26.1.2  | ✅ | ✅ | - |
| 26.2    | ✅ | ✅ | - |

1.20.3 is deliberately absent: Architectury API never published a real (Fabric API + NeoForge-backed)
release for it.

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

Building all:
```sh
./gradlew chiseledBuild --no-parallel
```

Publishing all:
```sh
./gradlew publishAllMods -Ppublish.dryRun=false
```
Ensure that you have set the `modrinthProjectId` and `curseforgeProjectId` in `gradle.properties`, and that you have the `MODRINTH_TOKEN` and `CURSEFORGE_TOKEN` environment variables set before running the publish command.


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
   stays shared across all three loaders. See "Adding content" below.

## Using this template

Everything is currently named after the placeholder `ExampleMod`/`examplemod`/`com.example.examplemod`.
Rename it before writing any real code, so names don't shift under you:

```sh
python3 setup.py                # interactive prompts for modId/modGroup/modName/...
python3 setup.py --dry-run      # preview the plan first, change nothing
```

This automates everything a rename touches:

1. **`gradle.properties`**: sets `modId`, `modGroup`, `modVersion`, `modName`, `modDescription`,
   `modAuthors`. These flow into `fabric.mod.json`/`mods.toml`/`neoforge.mods.toml` at build time - no
   other file needs your mod's display name or description.
2. **`settings.gradle.kts`**: updates `rootProject.name`.
3. **The package**: moves `com/example/examplemod/` to your own package in all four source trees
   (`src/`, `fabric/src/`, `forge/src/`, `neoforge/src/`) and updates every `package`/`import` line to
   match, including the `block`/`client`/`registry` sub-packages under `src/`.
4. **The classes**: renames `ExampleMod`/`ExampleModFabric`/`ExampleModForge`/`ExampleModNeoForge` and
   their `*Client` counterparts to whatever you like, and updates the one place each is referenced by
   string: the fabric `entrypoints` in `fabric/src/main/resources/fabric.mod.json`, and `MOD_ID` in
   the renamed main class, kept in sync with `modId`.

It's a plain-stdlib Python script (no dependencies), prints its full plan before touching anything, and
is safe to re-run (fields you don't change are no-ops). Run it without `--yes` and it asks for
confirmation once you've reviewed the plan; `git status` first if you want an easy way to review/undo
the result afterward.

Still worth doing by hand, since neither the script nor anything else here can reasonably automate them:

5. Pick a real license (see below) and replace this README with one about your actual mod.
6. Check `versions/<mcVersion>/gradle.properties` for stale dependency numbers before your first real
   build (see "Building" below).

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
├── build.gradle.kts            # "common" build script for 1.20.1 & 1.21.1 (26.1+ uses build.26.gradle.kts)
├── fabric/build.gradle.kts     # Fabric build script for 1.20.1 & 1.21.1 (26.1+ uses fabric/build.26.gradle.kts)
├── forge/build.gradle.kts      # Forge build script (1.20.1 only)
├── neoforge/build.gradle.kts   # NeoForge build script for 1.21.1 (26.1+ uses neoforge/build.26.gradle.kts)
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

**1. 26.1+ needs a different Loom variant, because Minecraft removed obfuscation.** 
Starting with the 1.21.11 versions, Java Edition [ships without
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

## Adding content

`src/main/java/com/example/examplemod/registry/` has one `Mod*` class per content type
(`ModItems`, `ModBlocks`, `ModBlockEntities`, `ModEntities`, `ModSounds`), each wrapping an
[Architectury API `DeferredRegister`](https://docs.architectury.dev/api/registry) - one
`register(...)` call reaches Fabric, Forge and NeoForge alike, no per-loader copies needed.
`ModRegistries.init()` binds all of them to whichever loader is actually running, and is
already called from `ExampleMod.init()`. To add something:

- **An item**: `ModItems.registerSimple("my_item", new Item.Properties())`.
- **A block**: `ModBlocks.registerWithItem("my_block", () -> new Block(...), new Item.Properties())`
  registers the block and its `BlockItem` together (the common case); `registerBlockOnly` skips
  the item, for the rare block that shouldn't have one.
- **A block entity, entity, or sound**: `ModBlockEntities`/`ModEntities`/`ModSounds` follow the
  same pattern - see their Javadoc.

`com.example.examplemod.block.ExampleBlock`/`ExampleBlockEntity` are a worked example tying
this together end to end: a block that remembers which way it's facing and stores one extra
value on its block entity. A few things worth knowing going in:

- **Blockstate properties (like `FACING`) and NBT persistence are plain vanilla Minecraft
  APIs** - Architectury API doesn't (and doesn't need to) get involved, since they're identical
  on every loader. Where they *do* differ is across Minecraft **versions**, and this template's
  1.20.1-26.2 range hits a real cluster of them in `BlockEntity`'s save/load alone: a
  `HolderLookup.Provider` parameter appeared in 1.20.5, `CompoundTag#getInt` started returning
  `Optional<Integer>` in 1.21.5, and 1.21.6 replaced `CompoundTag` there entirely with a new
  `ValueInput`/`ValueOutput` abstraction. `Block#use` similarly split into `useItemOn`/
  `useWithoutItem` in 1.20.5, and `Level#isClientSide` went from a public field to a private
  one (with an accessor method) in 1.21.2. Each is a Stonecutter `//? if` block (see
  `ExampleBlockEntity`/`ExampleBlock`), not a new abstraction - `ModSounds` has a smaller third
  example (`ResourceLocation`'s constructor going private in 1.21, then the class itself being
  renamed to `Identifier` in 26.1). Reach for one only where you've actually hit a real
  difference, the same as everywhere else in this template.
- **`BlockEntityType` construction is the one spot with no shared answer at all.** 1.21.2
  removed `BlockEntityType.Builder` and privatized `BlockEntityType`'s constructor, with no
  vanilla public replacement; Fabric API and NeoForge each patch in their own widened path
  instead (Fabric API's `FabricBlockEntityTypeBuilder`, NeoForge's own access-transformed
  constructor - see [the Fabric](https://docs.fabricmc.net/develop/blocks/block-entities) and
  [NeoForge](https://docs.neoforged.net/docs/blockentities/) docs), and neither is reachable
  from shared `common` code. `ModBlockEntities.newType` reaches through reflection for 1.21.2+
  instead of duplicating registration per loader - see its Javadoc. Worth knowing this exists
  before you build much on top of it.
- **Arbitrary extra data beyond blockstate/NBT** (Forge Capabilities, NeoForge Data Attachments,
  Fabric's own attachment API) is deliberately *not* abstracted here - the three loaders'
  systems differ enough that Architectury API doesn't unify them either. Plain BlockEntity/Entity
  NBT (or, from 1.20.5, ItemStack `DataComponent`s) covers the vast majority of cases and is
  fully portable; reach for a loader's native system directly only if you outgrow that.

Client-only registration (renderers, render types, ...) is written once, in
`com.example.examplemod.client.ExampleModClient` (Architectury API's client registries are
loader-agnostic too - only the entry point calling them differs), and invoked from each
loader's own client entry point: `ExampleModFabricClient` (a Fabric `ClientModInitializer`,
registered in `fabric.mod.json`'s `"client"` entrypoints) and
`ExampleModForgeClient`/`ExampleModNeoForgeClient` (a `Dist.CLIENT`-gated
`@EventBusSubscriber` listening for `FMLClientSetupEvent`). It has no real call to make yet
(`ExampleBlock` needs no special rendering) - `RenderTypeRegistry`'s own render-layer type was
itself mid-rewrite across this template's newest supported versions (`RenderType` became
`ChunkSectionLayer` in 1.21.2, and that API kept moving at least as far as 1.21.11), so a call
here would mean chasing a moving target for no real benefit. Add one (with a Stonecutter split
if it still differs across your supported versions) once you have a block or entity that
actually needs a renderer.

**Not covered here**: adding items to an existing vanilla creative-mode tab. It's one of the
more version-volatile corners of the API across this template's Minecraft range, so it's left
as a follow-up rather than a shaky abstraction - check
[Architectury's `CreativeTabRegistry`](https://docs.architectury.dev/) and the vanilla
`BuildCreativeModeTabContentsEvent`/`ItemGroupEvents` for the current state per loader when you
need it.

## Data generation

Block/item models, blockstates, recipes and loot tables all live in `datagen/`, a small,
standalone Gradle module - deliberately **not** part of the `stonecutter {}` block in
`settings.gradle.kts`, so it's outside the version/loader matrix entirely and never touches
`chiseledBuild`. That's a deliberate choice, not an oversight:

- **Datagen is a dev-time-only tool.** It produces static JSON once; that JSON then ships,
  completely unmodified, as ordinary resources in every Minecraft version this template
  targets. It never needs to run again just because you're building for a different version.
- **The JSON formats are stable**; the *generator Java API* that produces them isn't. Building
  this module surfaced real churn on top of everything else in this README - block/item model
  generation alone moved packages between older versions and 26.x. Making datagen code live in
  Stonecutter's shared `src/` trees (compiled once per Minecraft version, like everything else
  here) would mean fighting that churn across models, blockstates, recipes *and* loot tables,
  for no real benefit, since the output doesn't change. `datagen/` is pinned to one fixed
  version (currently 1.21.1, matching `stonecutter active` - see `datagen/build.gradle.kts`,
  which reads it from `versions/1.21.1/gradle.properties` rather than duplicating it) and never
  needs a `//? if`.
- **The output is loader-agnostic vanilla data**, so there's no need for three separate
  (Fabric/Forge/NeoForge) datagen implementations either. `datagen/` uses Fabric's
  well-documented, Gradle-integrated tooling (`fabricApi { configureDataGeneration() }` +
  `FabricRecipeProvider`/`FabricModelProvider`/`FabricBlockLootTableProvider`) purely because
  it's the most convenient way to produce the JSON - Forge/NeoForge don't need their own copy
  of it.

Running it:

```sh
./gradlew :datagen:runDatagen      # writes JSON into datagen/src/main/generated/ (gitignored)
./gradlew :datagen:copyGenerated   # runs the above, then copies it into fabric/, forge/ and
                                    # neoforge/'s real (committed) resources - the one piece of
                                    # automation replacing a manual "copy it three times" step
```

`datagen/src/main/java/.../datagen/DatagenContent.java` is the one thing worth understanding
before extending this: since `datagen/` is standalone, it has no dependency on the real,
per-version `common` project, so it can't reference `ModBlocks.EXAMPLE_BLOCK` etc. directly.
Instead it registers its own throwaway `Block`/`Item` instances under the exact same ids
(`examplemod:example_block`, `examplemod:example_item`) purely so the provider APIs have
something to point at - **not** the real registered content. Add a matching entry there
whenever you add real content that needs data generation, and a provider method alongside the
existing `example_block`/`example_item` ones to generate its model/recipe/loot table.

Two things that tripped this up and are worth knowing:

- `datagen/`'s own `fabric.mod.json` deliberately shares the real mod's `modId` (harmless -
  this module never ships and never runs alongside the real one) - without that, Fabric's
  datagen tooling namespaces generated files under *this module's own* id
  (`examplemod-datagen`) instead of the real mod's, which would generate everything under a
  namespace nothing ever loads.
- The throwaway `Block`/`Item` instances need to be registered *early* - referencing
  `DatagenContent` for the first time only when a provider actually runs (rather than earlier,
  during normal mod init) hits `IllegalStateException: This registry can't create intrusive
  holders`, because by then vanilla's registries have already frozen that pathway. That's why
  `ExampleModDataGenerator` also implements plain `ModInitializer` and touches
  `DatagenContent` there, not just from inside `onInitializeDataGenerator`.

**Textures aren't data-generated** - datagen only ever produces JSON, never images - so
`example_block`'s and `example_item`'s placeholder textures
(`assets/examplemod/textures/{block,item}/example_*.png`) are hand-placed directly in each
loader's resources, not part of `datagen/`'s output.

**After renaming this template** (see "Using this template" above): `setup.py` renames
`datagen/`'s source and its own `fabric.mod.json` along with everything else, but it doesn't
touch already-generated resources sitting in `fabric/`/`forge/`/`neoforge/` - re-run
`./gradlew :datagen:runDatagen :datagen:copyGenerated` afterward so they pick up the new id.

## A `//? if` editing gotcha

The Minecraft version set active via `stonecutter active "..."` in `stonecutter.gradle.kts`
(currently `1.21.1`) compiles its `:<version>` (and `fabric:<version>`/`neoforge:<version>`/
`forge:<version>`) project **directly from the raw `src/main/java/...` tree** - unlike every
*other* version, which gets its own copy generated into
`versions/<mc>/build/generated/stonecutter/...`, with `//? if` conditionals correctly
re-evaluated against that target version regardless of which branch happens to be textually
"live" (uncommented) in the shared source.

So: when hand-authoring a new `//? if <condition> { live } else { /* dead */ }` block directly
in an editor (rather than through Stonecutter's own "switch active version" mechanism), **the
branch left uncommented has to match whatever's true for the currently active version**, or
`./gradlew :<activeVersion>:compileJava` (and therefore `chiseledBuild`) fails on just that one
project - while every other version's generated copy still builds correctly, since the
generator re-derives the right branch from the condition text regardless of what's live in the
source. This is easy to misdiagnose as a comment-syntax problem (`else if` support, nesting,
...) when it's actually just this active-project staleness quirk; `else if` chains inside a
`//? if` block do work correctly once generated for a non-active version.

Practically: after adding or editing a `//? if` block by hand, check which branch is true for
whatever version is currently active and make sure *that* one is uncommented before building -
or just run the full build (`./gradlew chiseledBuild --no-parallel --continue` surfaces every
broken version at once instead of stopping at the first one) and fix whichever single
active-version project fails.

## Vanilla API breaks by Minecraft version (1.20.1 → 26.2)

Building the worked example above (`ExampleBlock`/`ExampleBlockEntity`/`ModBlockEntities`/
`ModSounds`) across this template's full version range surfaced more vanilla API churn than
expected - each cutoff below was confirmed against the real compiler, not just changelogs, and
is already encoded as a `//? if` in the corresponding file. Reuse these boundaries (don't
re-derive them) for any new content that touches the same APIs:

- **1.20.5**: `BlockEntity#saveAdditional`/`loadAdditional` gained a `HolderLookup.Provider`
  parameter. `Block#use` split into `useItemOn` (item in hand) and `useWithoutItem` (empty
  hand, no `InteractionHand` param).
- **~1.21 (before 1.21.2)**: `ResourceLocation`'s constructor went private in favour of
  `ResourceLocation.fromNamespaceAndPath(...)`.
- **1.21.2**: `Level#isClientSide` went from a public field to a private one with an
  `isClientSide()` accessor method instead. `BlockEntityType.Builder` was removed *and*
  `BlockEntityType`'s constructor was privatized, with **no vanilla public replacement** -
  Fabric API (`FabricBlockEntityTypeBuilder`) and NeoForge (its own access-transformed
  constructor) each patch in their own loader-specific path; there's no call shared `common`
  code can make. `ModBlockEntities.newType` reaches through reflection
  (`BlockEntityType.class.getDeclaredConstructor(...).setAccessible(true)`) instead for
  1.21.2+, to keep block entity registration in one shared call rather than splitting it per
  loader - worth knowing this exists before building much on top of it.
- **1.21.5**: `CompoundTag#getInt` (and presumably sibling getters) started returning
  `Optional<Integer>` instead of a plain `int`.
- **1.21.6**: `BlockEntity#saveAdditional`/`loadAdditional` fully replaced the
  `CompoundTag`-based signature with `ValueOutput`/`ValueInput`
  (`net.minecraft.world.level.storage`) - `putInt`/`getIntOr` instead of
  `tag.putInt`/`tag.getInt`.
- **26.1** (the first non-obfuscated release, following 1.21.11 which this template doesn't
  target directly): `ResourceLocation` was renamed to `Identifier` everywhere.
- **~1.21.2 onward, still moving as of 1.21.11**: `RenderType` (terrain/block render layers)
  was replaced by `ChunkSectionLayer`, and that API kept changing at least through
  1.21.10→1.21.11 (`CUTOUT_MIPPED` removed, naming changes) - see `ExampleModClient` for why
  that's deliberately left uncalled here rather than chased further.

Also worth knowing before searching for it: Architectury API's registry package is
`dev.architectury.registry.registries` (**plural** "registries") as of API 9.x-21.x (this
template's whole range) - `dev.architectury.registry.registry` (singular) doesn't exist,
despite showing up in some older docs/tutorials that predate the rename.
