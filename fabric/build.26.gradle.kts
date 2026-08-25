// Used for 26.2 only - see fabric/build.gradle.kts (and its header comment,
// and build.26.gradle.kts) for why this one differs.

plugins {
    id("dev.architectury.loom-no-remap")
    id("architectury-plugin")
    id("com.gradleup.shadow")
}

val minecraft = stonecutter.current.version
val common = requireNotNull(stonecutter.node?.sibling("")?.project) { "No common project for $project" }

val modId: String by project
val modVersion: String by project

version = "$modVersion+$minecraft"
base { archivesName.set("$modId-fabric") }

architectury {
    platformSetupLoomIde()
    fabric()
}

val commonBundle: Configuration by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}
val shadowBundle: Configuration by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}
configurations {
    compileClasspath.get().extendsFrom(commonBundle)
    runtimeClasspath.get().extendsFrom(commonBundle)
    named("developmentFabric").get().extendsFrom(commonBundle)
}

repositories {
    maven("https://maven.shedaniel.me/") // Cloth Config
}

// See the comment in fabric/build.gradle.kts: these live in
// versions/26.2/gradle.properties, only an ancestor of `common`.
val fabricLoaderVersion = common.property("fabricLoaderVersion") as String
val fabricApiVersion = common.property("fabricApiVersion") as String
val architecturyApiVersion = common.property("architecturyApiVersion") as String
val clothConfigVersion = common.property("clothConfigVersion") as String

dependencies {
    // No `mappings(...)` here - see the header comment above.
    minecraft("net.minecraft:minecraft:$minecraft")

    implementation("net.fabricmc:fabric-loader:$fabricLoaderVersion")
    implementation("net.fabricmc.fabric-api:fabric-api:$fabricApiVersion")
    implementation("dev.architectury:architectury-fabric:$architecturyApiVersion")

    // Optional/soft dependency: compile against it, and have it on the dev
    // run's classpath, but don't bundle it or require it at runtime - see
    // the "cloth-config" entry in fabric.mod.json.
    compileOnly("me.shedaniel.cloth:cloth-config-fabric:$clothConfigVersion")
    localRuntime("me.shedaniel.cloth:cloth-config-fabric:$clothConfigVersion")

    commonBundle(project(common.path)) { isTransitive = false }
    shadowBundle(project(common.path, "transformProductionFabric")) { isTransitive = false }
}

java {
    withSourcesJar()
    val java = JavaVersion.toVersion(common.property("javaVersion") as String)
    sourceCompatibility = java
    targetCompatibility = java
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

tasks.processResources {
    val modName: String by project
    val modDescription: String by project
    val modAuthors: String by project
    val modLicense: String by project

    val props = mapOf(
        "mod_id" to modId,
        "mod_version" to modVersion,
        "mod_name" to modName,
        "mod_description" to modDescription,
        "mod_authors" to modAuthors,
        "mod_license" to modLicense,
        "minecraft_version" to minecraft,
        "loader_version" to fabricLoaderVersion,
    )
    inputs.properties(props)
    filesMatching("fabric.mod.json") { expand(props) }
}

tasks.shadowJar {
    configurations = listOf(shadowBundle)
    archiveClassifier.set("dev-shadow")
}

tasks.build {
    group = "versioned"
    description = "Must run through 'chiseledBuild' - see stonecutter.gradle.kts"
}
