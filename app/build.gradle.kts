import com.android.build.api.variant.BuildConfigField
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import java.io.FileInputStream
import java.util.Properties

val appVersionName: String = libs.versions.versionName
    .get()

val appVersionCode: Int = appVersionName
    .split(".")
    .let { parts ->
        parts[0].toInt() * 10000 +
                parts[1].toInt() * 100 +
                parts[2].toInt()
    }


val beta: Boolean = (project.findProperty("beta") as String?)?.toBoolean() ?: true
val keystorePropertiesFile: File = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}


plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.jetbrains.kotlin.serialization)
    alias(libs.plugins.ksp)
}

kotlinExtension.jvmToolchain {
    languageVersion.set(JavaLanguageVersion.of(17))
}

android {
    namespace = "ca.ilianokokoro.umihi.music"
    compileSdk {
        version = release(37)
    }
    ndkVersion = "30.0.14904198"
    buildToolsVersion = "37.0.0"

    defaultConfig {
        applicationId = "ca.ilianokokoro.umihi.music"
        minSdk = 24
        targetSdk = 37
        versionCode = appVersionCode
        versionName = "${appVersionName}${if (beta) "-beta" else ""}"

        buildConfigField("boolean", "IS_BETA", "$beta")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }


    signingConfigs {
        if (keystorePropertiesFile.exists()) {
            create("release") {
                keyAlias = keystoreProperties["keyAlias"] as String
                keyPassword = keystoreProperties["keyPassword"] as String
                storeFile = file(keystoreProperties["storeFile"] as String)
                storePassword = keystoreProperties["storePassword"] as String
            }
        }
    }


    flavorDimensions += "version"

    productFlavors {
        create("standalone") {
            isDefault = true
            dimension = "version"
            buildConfigField("boolean", "UPDATER_ENABLED", "true")
        }

        create("store") {
            versionNameSuffix = "-store"
            dimension = "version"
            buildConfigField("boolean", "UPDATER_ENABLED", "false")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfigs.findByName("release")?.let {
                signingConfig = it
            }
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
        isCoreLibraryDesugaringEnabled = true
    }


    buildFeatures {
        compose = true
        buildConfig = true
    }


    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }

    androidResources {
        @Suppress("UnstableApiUsage")
        generateLocaleConfig = true
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

androidComponents {
    val commitHash: String =
        (project.findProperty("gitHash") as String?) ?: "N/A"

    onVariants { variant ->
        variant.buildConfigFields?.put(
            "COMMIT_HASH",
            BuildConfigField(
                "String",
                "\"$commitHash\"",
                "commit hash"
            )
        )

        variant.outputs.forEach { output ->
            val flavor = variant.flavorName
            output.outputFileName.set(
                when (flavor) {
                    "standalone" -> "UmihiMusic.apk"
                    "store" -> "UmihiMusic-store.apk"
                    else -> "UmihiMusic-$flavor.apk"
                }
            )
        }
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
    debugImplementation(libs.androidx.ui.tooling)

    // Desugaring
    coreLibraryDesugaring(libs.desugar.jdk.libs.nio)

    // Navigation 3
    implementation(libs.nav3.runtime)
    implementation(libs.nav3.ui)
    implementation(libs.androidx.lifecycle.viewmodel.nav3)

    // Splash Screen
    implementation(libs.androidx.core.splashscreen)

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

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // WebKit
    implementation(libs.androidx.webkit)

    // Icons
    implementation(libs.androidx.material.icons.extended)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Custom Activity On Crash
    implementation(libs.customactivityoncrash)

    // Workers
    implementation(libs.androidx.work.runtime.ktx)

    // Reorderable list
    implementation(libs.reorderable)

    // New Pipe Extractor
    implementation(libs.newpipeextractor)

}
