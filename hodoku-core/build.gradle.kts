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

