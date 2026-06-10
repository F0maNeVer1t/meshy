@file:Suppress("UnstableApiUsage")

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    // TODO: migrate Dagger/Glide annotation processors from kapt to KSP when supported
    // alias(libs.plugins.ksp)
}

// apply(plugin = "witness")
// apply(from = "witness.gradle")
// NOTE: gradle-witness плагин не поддерживает Kotlin DSL напрямую.
// Рассмотрите миграцию на встроенную Gradle dependency verification:
// https://docs.gradle.org/current/userguide/dependency_verification.html

/**
 * Возвращает stdout команды или [defaultValue], если запуск упал или
 * процесс завершился с ненулевым exit code.
 */
fun getStdout(command: List<String>, defaultValue: String): String {
    return try {
        val process = ProcessBuilder(command)
            .redirectErrorStream(true)
            .start()
        val output = process.inputStream.bufferedReader().readText().trim()
        if (process.waitFor() == 0 && output.isNotEmpty()) output else defaultValue
    } catch (_: Exception) {
        defaultValue
    }
}

val gitHash: String = getStdout(
    listOf("git", "rev-parse", "--short=7", "HEAD"),
    "No commit hash"
)

val buildTimestamp: Long = getStdout(
    listOf("git", "log", "-n", "1", "--format=%ct"),
    (System.currentTimeMillis() / 1000).toString()
).toLongOrNull()?.times(1000L) ?: System.currentTimeMillis()

android {
    namespace = "ru.itis.meshy"
    compileSdk = libs.versions.compileSdk.get().toInt()
    buildToolsVersion = libs.versions.buildTools.get()

    packaging {
        jniLibs {
            keepDebugSymbols += listOf("**/*.so")
        }
        resources {
            excludes += listOf(
                "/META-INF/{AL2.0,LGPL2.1}",
                "/META-INF/LICENSE*",
                "/META-INF/NOTICE*",
                "DebugProbesKt.bin",
            )
        }
    }

    defaultConfig {
        applicationId = "ru.itis.meshy.android"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = libs.versions.versionCode.get().toInt()
        versionName = libs.versions.versionName.get()

        vectorDrawables.useSupportLibrary = true

        buildConfigField("String", "TorVersion", "\"${libs.versions.tor.get()}\"")
        buildConfigField("String", "GitHash", "\"$gitHash\"")
        buildConfigField("Long", "BuildTimestamp", "${buildTimestamp}L")

        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a")
        }
    }

    ndkVersion = "27.0.12077973"

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    buildTypes {
        release {
            isShrinkResources = false
            isMinifyEnabled = true
            isCrunchPngs = false
            proguardFiles(
                getDefaultProguardFile("proguard-android.txt"),
                "proguard-rules.txt"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    lint {
        warning += listOf(
            "MissingTranslation",
            "MissingDefaultResource",
            "ImpliedQuantity",
            "ExtraTranslation",
            "InvalidPackage"
        )
    }

    buildFeatures {
        buildConfig = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
        freeCompilerArgs.add("-Xjvm-default=all")
    }
}

kapt {
    correctErrorTypes = true
    useBuildCache = false
    arguments {
        arg("dagger.fastInit", "enabled")
    }
}

dependencies {
    // ── Project modules ─────────────────────────────────────────────
    implementation(project(":messaging-engine-core"))
    implementation(project(":messaging-engine-android"))
    implementation(project(":app-core"))

    // ── AndroidX ────────────────────────────────────────────────────
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.preference)
    implementation(libs.androidx.exifinterface)
    implementation(libs.bundles.androidx.lifecycle)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview.selection)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)

    // ── Google Material ─────────────────────────────────────────────
    implementation(libs.google.material)

    // ── Kotlin stdlib & coroutines ──────────────────────────────────
    implementation(libs.kotlin.stdlib)
    implementation(libs.bundles.kotlinx.coroutines)

    // ── Networking ──────────────────────────────────────────────────
    implementation(libs.okhttp)
    implementation(libs.jsoup)

    // ── Guardian Project (panic button) ─────────────────────────────
    implementation(libs.panic)

    // ── UI libraries ────────────────────────────────────────────────
    implementation(libs.circleimageview)
    implementation(libs.zxing.core)
    implementation(libs.material.tap.target.prompt)
    implementation(libs.emoji.google)
    implementation(libs.material.fab.speed.dial)
    implementation(libs.photoview)

    // ── Image loading (Glide) ───────────────────────────────────────
    implementation(libs.glide) {
        exclude(group = "com.android.support")
        exclude(module = "disklrucache")
    }

    // ── Local HTTP server ───────────────────────────────────────────
    implementation(libs.nanohttpd)

    // ── Annotation processors ────────────────────────────────────────
    kapt(libs.dagger.compiler)
    // Override shaded kotlin-metadata-jvm в Dagger, чтобы он понимал Kotlin 2.3.
    // Этот трюк работает только начиная с Dagger 2.57.
    kapt("org.jetbrains.kotlinx:kotlinx-metadata-jvm:0.9.0")
    kapt(libs.glide.compiler)

    // ── Compile-only ────────────────────────────────────────────────
    compileOnly(libs.jsr250.api)
}

project.evaluationDependsOn(
    requireNotNull(rootProject.findProject("messaging-engine-android")) {
        "Subproject ':messaging-engine-android' not found. Проверь settings.gradle.kts."
    }.path
)