import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "org.jjgame.sudokuapp"
    compileSdk = 35

    sourceSets {
        getByName("main") {
            java.setSrcDirs(listOf("src/main/java", "src/main/kotlin"))
        }
        getByName("test") {
            java.setSrcDirs(listOf("src/test/java", "src/test/kotlin"))
        }
        getByName("androidTest") {
            java.setSrcDirs(listOf("src/androidTest/java", "src/androidTest/kotlin"))
        }
    }

    defaultConfig {
        applicationId = "org.jjgame.sudokuapp"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(project(":core-game"))

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("com.google.android.material:material:1.12.0")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.json:json:20240303")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    implementation(kotlin("stdlib-jdk8"))
}

tasks.register("deployDebug") {
    group = "install"
    description = "Assembles the debug APK and installs it on the connected device via adb."
    dependsOn("assembleDebug")
    doLast {
        val apk = layout.buildDirectory.file("outputs/apk/debug/app-debug.apk").get().asFile
        providers.exec {
            commandLine("adb", "install", "-r", apk.absolutePath)
        }.result.get()
    }
}
kotlin {
    jvmToolchain(17)
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}