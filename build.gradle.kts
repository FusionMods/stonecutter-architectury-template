// Stonecutter's "central script" for every targeted version through 1.21.11 (see
// settings.gradle.kts's "Supported versions" - 26.1+ uses build.26.gradle.kts instead):
// copied once per version into versions/<mcVersion>/, where it becomes that version's
// `common` project - the classic Architectury common module, shared by
// that version's fabric/forge/neoforge projects (see their own
// build.gradle.kts). 26.2 uses build.26.gradle.kts instead - see
// settings.gradle.kts and README.md.

plugins {
    id("dev.architectury.loom")
    id("architectury-plugin")
}

val minecraft = stonecutter.current.version

val modId: String by project
val modVersion: String by project

version = "$modVersion+$minecraft"
base { archivesName.set("$modId-common") }

// Tells the Architectury plugin which loaders this Minecraft version
// actually builds for, by asking Stonecutter which loader branches include
// the version currently being evaluated - so 1.20.1 resolves to
// [fabric, forge, neoforge] while 1.21.1 resolves to [fabric, neoforge],
// with no per-version conditionals needed here.
architectury.common(stonecutter.tree.branches.mapNotNull {
    if (stonecutter.current.project !in it) null
    else it.project.findProperty("loom.platform") as String?
})

loom {
    silentMojangMappingsLicense()
}

val fabricLoaderVersion: String by project
val architecturyApiVersion: String by project

dependencies {
    minecraft("com.mojang:minecraft:$minecraft")
    mappings(loom.officialMojangMappings())

    // We depend on Fabric Loader here only to use its @Environment /
    // @EnvironmentInterface annotations, which Loom remaps to the correct
    // equivalent on each platform. Do NOT use any other Fabric Loader class
    // from common code.
    modImplementation("net.fabricmc:fabric-loader:$fabricLoaderVersion")

    // The loader-agnostic Architectury API (registries, events, networking,
    // ...) - fabric/forge/neoforge each additionally pull in their own
    // dev.architectury:architectury-<loader> implementation of this.
    modImplementation("dev.architectury:architectury:$architecturyApiVersion")
}

java {
    withSourcesJar()
    val javaVersion: String by project
    val java = JavaVersion.toVersion(javaVersion)
    sourceCompatibility = java
    targetCompatibility = java
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

tasks.build {
    group = "versioned"
    description = "Must run through 'chiseledBuild' - see stonecutter.gradle.kts"
}
