// Used for 1.21.11+ - see fabric/build.gradle.kts (and its header comment,
// and build.26.gradle.kts) for why this one differs.

plugins {
    id("dev.architectury.loom-no-remap")
    id("architectury-plugin")
    id("com.gradleup.shadow")
    id("me.modmuss50.mod-publish-plugin") version "2.2.0"
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

// See the longer comment in fabric/build.gradle.kts (and README.md's "Testing" section) -
// this needs to be kept in both build scripts, since whichever one is actually in use
// depends on which Minecraft version is `stonecutter active`. Not separately verified
// against this file's `dev.architectury.loom-no-remap` variant specifically (only ever
// exercised once a 26.1+ version is made active) - if it doesn't work here, that's the
// first thing to check.
if (stonecutter.current.isActive) {
    fabricApi {
        configureTests {
            createSourceSet = true
            enableGameTests = true
            eula = true
        }
    }
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

// There's no remap step on this no-remap branch (see the header comment), so - unlike
// fabric/build.gradle.kts, where remapJar takes shadowJar's output and becomes the actual
// distributable - shadowJar's output has to become the distributable directly here.
tasks.shadowJar {
    configurations = listOf(shadowBundle)
    archiveClassifier.set("")
}

tasks.jar {
    archiveClassifier.set("dev")
}

tasks.build {
    group = "versioned"
    description = "Must run through 'chiseledBuild' - see stonecutter.gradle.kts"
    dependsOn(tasks.shadowJar)
}

tasks.register<Copy>("buildAndCollect") {
    group = "versioned"
    from(tasks.shadowJar.get().archiveFile, tasks.named<Jar>("sourcesJar").get().archiveFile)
    into(rootProject.layout.buildDirectory.file("libs/$modVersion/fabric"))
    dependsOn("build")
}

// Publishes this exact version+loader's jar to Modrinth/CurseForge - see
// stonecutter.gradle.kts's publishAllMods for running every variant's at once, and
// gradle.properties for the project IDs/dry-run switch this reads.
val modrinthProjectId: String by project
val curseforgeProjectId: String by project

publishMods {
    file.set(tasks.shadowJar.flatMap { it.archiveFile })
    changelog.set(providers.environmentVariable("CHANGELOG").orElse("See the commit history."))
    type.set(STABLE)
    modLoaders.add("fabric")
    dryRun.set(providers.gradleProperty("publish.dryRun").map(String::toBoolean).orElse(true))

    modrinth {
        projectId.set(modrinthProjectId)
        accessToken.set(providers.environmentVariable("MODRINTH_TOKEN"))
        minecraftVersions.add(minecraft)
    }
    curseforge {
        projectId.set(curseforgeProjectId)
        accessToken.set(providers.environmentVariable("CURSEFORGE_TOKEN"))
        minecraftVersions.add(minecraft)
        client.set(true)
        server.set(true)
    }
}
