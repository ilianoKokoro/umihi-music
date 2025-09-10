import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val versionMajor = 1
val versionMinor = 1
val versionPatch = 0

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.jetbrains.kotlin.serialization)
    alias(libs.plugins.ksp)
}

android {

    namespace = "ca.ilianokokoro.umihi.music"
    compileSdk = 36

    defaultConfig {
        applicationId = "ca.ilianokokoro.umihi.music"
        minSdk = 23
        targetSdk = 36
        versionCode = versionMajor * 10000 + versionMinor * 100 + versionPatch
        versionName = "${versionMajor}.${versionMinor}.${versionPatch}"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    // Universal APK only
    splits {
        abi {
            isEnable = false
            isUniversalApk = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
        isCoreLibraryDesugaringEnabled = true
    }
    kotlin {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_21
        }
    }

    buildFeatures {
        compose = true
    }
}


dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Desugaring
    coreLibraryDesugaring(libs.desugar.jdk.libs.nio)

    // Navigation 3
    implementation(libs.nav3.runtime)
    implementation(libs.nav3.ui)
    implementation(libs.androidx.lifecycle.viewmodel.nav3)

    // Serialization
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.kotlinx.serialization.json)

    // Viewmodel
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Coil (images)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    // Exoplayer
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.session)
    implementation(libs.androidx.media3.datasource)
    // Additional playback support
    implementation(libs.androidx.media3.exoplayer.dash)
    implementation(libs.androidx.media3.exoplayer.rtsp)
    implementation(libs.androidx.media3.exoplayer.hls)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // Fuel (HTTP)
    implementation(libs.fuel.android)
    implementation(libs.fuel.json)

    // WebKit
    implementation(libs.androidx.webkit)

    // Icons
    implementation(libs.androidx.material.icons.extended)

    // New Valve (NewPipeExtractor)
    implementation(libs.newvalve)

    // Room
    implementation(libs.androidx.room.runtime)
    annotationProcessor(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
}