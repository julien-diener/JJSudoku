plugins {
    id("org.jetbrains.kotlin.jvm")
}

group = "org.jjgame"
version = "0.1.0"

kotlin {
    jvmToolchain(17)
}

dependencies {
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

