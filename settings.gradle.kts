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

stonecutter {
    centralScript = "build.gradle.kts"
    kotlinController = true

    create(rootProject) {
        // The root src/ (this repo's `common` project) is copied once per
        // version listed here.
        versions("1.20.1", "1.21.1")
        // Minecraft removed Java Edition obfuscation starting with 26.x, so
        // there's nothing left for Loom to remap - common/, fabric/ and
        // neoforge/ all give the 26.2 node its own build.26.gradle.kts
        // using the `dev.architectury.loom-no-remap` variant instead of the
        // regular one. See build.26.gradle.kts and README.md.
        versions("26.2").buildscript("build.26.gradle.kts")

        // Each branch below is itself copied once per version it lists
        // (or every root version, if none are listed) - so e.g. `fabric`
        // becomes versions/1.20.1/fabric, versions/1.21.1/fabric, etc.
        branch("fabric") {
            inherit()
            versions("26.2").buildscript("build.26.gradle.kts")
        }
        // NeoForge only for 1.21.1 and 26.2: NeoForge on 1.20.1 is,
        // internally, still Forge on 1.20.1 (same SRG-based toolchain, pre-
        // dating NeoForge's own package/coordinate rename), and hitting an
        // open, unresolved Architectury Loom bug when set up that way
        // (architectury/architectury-loom#289) - see README.md. 1.20.1 is
        // also the last version where Forge and NeoForge mods are
        // interchangeable, so the forge/ build below already covers it.
        branch("neoforge") {
            versions("1.21.1")
            versions("26.2").buildscript("build.26.gradle.kts")
        }
        // Forge is wired up for 1.20.1 only: it's still fully interoperable
        // with NeoForge there, but lags behind on newer versions - see README.
        branch("forge") { versions("1.20.1") }

        vcsVersion = "1.21.1"
    }
}
