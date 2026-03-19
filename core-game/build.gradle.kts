plugins {
    id("org.jetbrains.kotlin.jvm")
    application
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

application {
    mainClass.set("org.jjgame.sudoku.PreviewMainKt")
}

tasks.register<JavaExec>("runPreview") {
    group = "application"
    description = "Runs a tiny console preview that prints one generated Sudoku board."
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("org.jjgame.sudoku.PreviewMainKt")
}


