plugins {
    id("org.jetbrains.kotlin.jvm")
    kotlin("plugin.serialization")
}

group = "org.jjgame"
version = "0.1.0"

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.register<JavaExec>("probeGenerationDistribution") {
    group = "verification"
    description = "Runs the HoDoKu generation distribution probe (default: 100 puzzles)."
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("org.jjgame.hodoku.tools.GenerationDistributionProbeMain")
}

tasks.register<JavaExec>("probeHintDataset") {
    group = "verification"
    description = "Builds a POC hint dataset grouped by SolutionCategory/SolutionType."
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("org.jjgame.hodoku.tools.HintDatasetProbeMain")
}

