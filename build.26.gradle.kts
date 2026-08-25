// Stonecutter's central script for 26.2 specifically (see
// settings.gradle.kts): Minecraft removed Java Edition obfuscation starting
// with the 26.x (year.drop) versions
// (minecraft.net/en-us/article/removing-obfuscation-in-java-edition), so
// there's nothing left for Loom to remap. `dev.architectury.loom-no-remap`
// is the currently-documented workaround for architectury-loom not finding
// "official mappings" for these versions (architectury/architectury-loom#328)
// - see README.md. Otherwise this mirrors build.gradle.kts.

plugins {
    id("dev.architectury.loom-no-remap")
    id("architectury-plugin")
}

val minecraft = stonecutter.current.version

val modId: String by project
val modVersion: String by project

version = "$modVersion+$minecraft"
base { archivesName.set("$modId-common") }

architectury.common(stonecutter.tree.branches.mapNotNull {
    if (stonecutter.current.project !in it) null
    else it.project.findProperty("loom.platform") as String?
})

val fabricLoaderVersion: String by project

dependencies {
    // No `mappings(...)` here - see the comment above.
    minecraft("net.minecraft:minecraft:$minecraft")

    // We depend on Fabric Loader here only to use its @Environment /
    // @EnvironmentInterface annotations. Do NOT use any other Fabric
    // Loader class from common code.
    implementation("net.fabricmc:fabric-loader:$fabricLoaderVersion")
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
