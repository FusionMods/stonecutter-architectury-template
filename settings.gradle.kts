// This project's structure is a modernized version of the (now archived)
// official https://github.com/stonecutter-versioning/stonecutter-template-architectury :
// Stonecutter handles the *Minecraft version* axis (it turns the root
// `common`/`fabric`/`forge`/`neoforge` projects into one copy per targeted
// version under versions/<mcVersion>/<loader>/), while those four projects
// use the classic Architectury multiloader split for the *loader* axis.
// See README.md for the reasoning and for the alternative, non-Architectury
// https://github.com/stonecutter-versioning/stonecutter-template-multiloader
// approach this project deliberately did not take.
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.fabricmc.net/") { name = "Fabric" }
        maven("https://maven.architectury.dev/") { name = "Architectury" }
        maven("https://maven.minecraftforge.net/") { name = "Forge" }
        maven("https://maven.neoforged.net/releases/") { name = "NeoForge" }
        maven("https://maven.kikugie.dev/releases") { name = "Stonecutter" }
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.9.7"
    // 1.20.1 needs Java 17, 26.2 needs Java 25 - none of them may be locally
    // installed, so let Gradle download whichever JDK each version needs.
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "examplemod"

// A plain, standalone Gradle module - deliberately NOT part of the stonecutter {} block
// below, so it's outside the version/loader matrix entirely. See datagen/build.gradle.kts
// and README.md's "Data generation" section for why: it's a dev-time-only tool pinned to
// one fixed Minecraft version, producing static JSON shared by every version/loader rather
// than needing to compile against all of them.
include("datagen")

stonecutter {
    centralScript = "build.gradle.kts"
    kotlinController = true

    create(rootProject) {
        versions(
            "1.20.1", "1.20.2", "1.20.4", "1.20.5", "1.20.6",
            // 1.20.3 is deliberately skipped - Architectury API never published
            // a Fabric API/NeoForge-backed release for it (only a Fabric-only
            // beta, since 1.20.4 followed two days later), so it fails this
            // template's "needs supported Fabric + NeoForge + Architectury"
            // bar. See README.md.
            "1.21.1", "1.21.2", "1.21.3", "1.21.4", "1.21.5", "1.21.6", "1.21.7", "1.21.8", "1.21.9", "1.21.10",
        )
        versions("26.1", "26.1.1", "26.1.2", "26.2").buildscript("build.26.gradle.kts")

        branch("fabric") {
            inherit()
            versions("26.1", "26.1.1", "26.1.2", "26.2").buildscript("build.26.gradle.kts")
        }

        branch("neoforge") {
            versions(
                "1.20.2", "1.20.4", "1.20.5", "1.20.6",
                "1.21.1", "1.21.2", "1.21.3", "1.21.4", "1.21.5", "1.21.6", "1.21.7", "1.21.8", "1.21.9", "1.21.10",
            )
            versions("26.1", "26.1.1", "26.1.2", "26.2").buildscript("build.26.gradle.kts")
        }

        branch("forge") { versions("1.20.1") }

        vcsVersion = "1.21.1"
    }
}
