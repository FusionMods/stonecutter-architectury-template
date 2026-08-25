// Used for 1.20.1 and 1.21.1 - see fabric/build.26.gradle.kts for 26.2.

plugins {
    id("dev.architectury.loom")
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

loom {
    silentMojangMappingsLicense()
}

// javaVersion/fabricLoaderVersion/fabricApiVersion/architecturyApiVersion/
// clothConfigVersion live in versions/<mcVersion>/gradle.properties, which -
// unlike this repo's root gradle.properties - is only an ancestor directory
// of the `common` project, not of `fabric`/`forge`/`neoforge`. Route through
// the sibling `common` project to read them.
val fabricLoaderVersion = common.property("fabricLoaderVersion") as String
val fabricApiVersion = common.property("fabricApiVersion") as String
val architecturyApiVersion = common.property("architecturyApiVersion") as String
val clothConfigVersion = common.property("clothConfigVersion") as String

dependencies {
    minecraft("com.mojang:minecraft:$minecraft")
    mappings(loom.officialMojangMappings())

    modImplementation("net.fabricmc:fabric-loader:$fabricLoaderVersion")
    modImplementation("net.fabricmc.fabric-api:fabric-api:$fabricApiVersion")
    modImplementation("dev.architectury:architectury-fabric:$architecturyApiVersion")

    // Optional/soft dependency: compile against it, and have it on the dev
    // run's classpath, but don't bundle it or require it at runtime - see
    // the "cloth-config" entry in fabric.mod.json.
    modCompileOnly("me.shedaniel.cloth:cloth-config-fabric:$clothConfigVersion")
    modLocalRuntime("me.shedaniel.cloth:cloth-config-fabric:$clothConfigVersion")

    commonBundle(project(common.path, "namedElements")) { isTransitive = false }
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

tasks.remapJar {
    input.set(tasks.shadowJar.flatMap { it.archiveFile })
    dependsOn(tasks.shadowJar)
}

tasks.jar {
    archiveClassifier.set("dev")
}

tasks.build {
    group = "versioned"
    description = "Must run through 'chiseledBuild' - see stonecutter.gradle.kts"
}

tasks.register<Copy>("buildAndCollect") {
    group = "versioned"
    from(tasks.remapJar.get().archiveFile, tasks.remapSourcesJar.get().archiveFile)
    into(rootProject.layout.buildDirectory.file("libs/$modVersion/fabric"))
    dependsOn("build")
}
