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
