// Standalone datagen module - deliberately NOT part of the stonecutter {} block in
// settings.gradle.kts. See README.md's "Data generation" section for the full reasoning:
// datagen only ever needs to run once, and the JSON it produces is used, unmodified, by
// every Minecraft version/loader this template targets, so this module never needs to
// compile against the 20-version matrix the rest of the project does. Uses
// dev.architectury.loom (not plain net.fabricmc.fabric-loom, even though this module has
// no cross-platform need) purely because it's the Loom flavor already proven to work with
// Kotlin DSL in this exact repo/Gradle combination - no architectury-plugin/architectury{}
// block needed alongside it, since this is a single, pinned Fabric-only environment used
// purely as a dev tool, not a multiplatform project.
import java.util.Properties

plugins {
    id("dev.architectury.loom") version "1.17-SNAPSHOT"
}

val rootProperties = Properties().apply {
    rootProject.file("gradle.properties").inputStream().use(::load)
}
// Pinned to whichever version stonecutter.gradle.kts currently has active - read directly
// rather than duplicated here, so this never drifts out of sync on its own.
val pinnedVersion = "1.21.1"
val pinnedVersionProperties = Properties().apply {
    rootProject.file("versions/$pinnedVersion/gradle.properties").inputStream().use(::load)
}

val modId: String = rootProperties.getProperty("modId")
val modGroup: String = rootProperties.getProperty("modGroup")
val fabricLoaderVersion: String = pinnedVersionProperties.getProperty("fabricLoaderVersion")
val fabricApiVersion: String = pinnedVersionProperties.getProperty("fabricApiVersion")

group = modGroup
version = "datagen"
base { archivesName.set("$modId-datagen") }

repositories {
    mavenCentral()
}

loom {
    silentMojangMappingsLicense()
}

dependencies {
    minecraft("com.mojang:minecraft:$pinnedVersion")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:$fabricLoaderVersion")
    modImplementation("net.fabricmc.fabric-api:fabric-api:$fabricApiVersion")
}

java {
    val java = JavaVersion.toVersion(pinnedVersionProperties.getProperty("javaVersion"))
    sourceCompatibility = java
    targetCompatibility = java
}

fabricApi {
    configureDataGeneration {
        outputDirectory = file("src/main/generated")
        // Nothing from this module ships anywhere, so there's no point mirroring the
        // output into its own resources - copyGenerated below distributes it to the
        // three loaders that actually need it instead.
        addToResources = false
    }
}

// The one piece of automation replacing a manual "copy the output into every loader"
// step: the generated JSON is loader-agnostic vanilla data (confirmed while researching
// this - Fabric API's datagen tooling is just the most convenient way to produce it, not
// something Forge/NeoForge need their own copy of), so one run covers all three.
listOf("fabric", "forge", "neoforge").forEach { loader ->
    tasks.register<Copy>("copyGeneratedTo${loader.replaceFirstChar(Char::uppercase)}") {
        dependsOn("runDatagen")
        from("src/main/generated") {
            include("assets/**", "data/**")
        }
        into(rootProject.file("$loader/src/main/resources"))
    }
}

tasks.register("copyGenerated") {
    group = "datagen"
    description = "Runs datagen, then copies its output into fabric/, forge/ and neoforge/'s resources."
    dependsOn("copyGeneratedToFabric", "copyGeneratedToForge", "copyGeneratedToNeoforge")
}
