plugins {
    id("dev.kikugie.stonecutter")
    id("dev.architectury.loom") version "1.17-SNAPSHOT" apply false
    // As of writing, plain `dev.architectury.loom` can't find official
    // mappings for the new year.drop versions (26.1+) - this is a known,
    // still-open gap (architectury/architectury-loom#328). The `-no-remap`
    // variant is the documented workaround, so 26.2 uses that one instead -
    // see the `useNoRemap` branches in each build.gradle.kts, and README.md.
    id("dev.architectury.loom-no-remap") version "1.17-SNAPSHOT" apply false
    id("architectury-plugin") version "3.5-SNAPSHOT" apply false
    id("com.gradleup.shadow") version "9.4.3" apply false
}

// The version opened by default in the IDE / by a bare `./gradlew build`.
// Switch it with the Stonecutter IntelliJ plugin, or target a specific
// version's tasks directly, e.g. `./gradlew :1.20.1:forge:build`.
stonecutter active "1.21.1" /* [SC] DO NOT EDIT */

// Builds every Minecraft version x loader combo in one go - the jars land in each
// <loader>/versions/<mc>/build/libs/ (the un-classified one, e.g. alotofinterior-fabric-
// 0.1.0+1.21.1.jar, is the real distributable - the others are dev-only intermediates).
tasks.register("chiseledBuild") {
    group = "project"
    description = "Builds every Minecraft version x loader variant."
    dependsOn(stonecutter.tasks.named("build").map { it.values })
}

// Publishes every Minecraft version x loader variant to Modrinth/CurseForge in one go -
// see each loader's build.gradle.kts's publishMods block for what actually gets sent, and
// gradle.properties for the project IDs and dry-run switch. Defaults to a dry run (logs
// what it would've done, uploads nothing) until you set real project IDs, MODRINTH_TOKEN/
// CURSEFORGE_TOKEN, and pass -Ppublish.dryRun=false.
tasks.register("publishAllMods") {
    group = "project"
    description = "Publishes every Minecraft version x loader variant to Modrinth/CurseForge."
    // Only the fabric/forge/neoforge branches apply the publish plugin - the bare common
    // branch (empty branch id) never gets built into something that itself ships, so it
    // has no publishMods task to depend on.
    dependsOn(stonecutter.tasks.named("publishMods") { branch.id.isNotEmpty() }.map { it.values })
}
