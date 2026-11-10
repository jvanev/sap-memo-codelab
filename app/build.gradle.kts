import java.io.FileNotFoundException
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.hilt)
    alias(libs.plugins.google.ksp)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.sap.codelab"

    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.sap.codelab"
        minSdk = 27
        targetSdk = 37
        versionCode = 1
        versionName = "0.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val keyName = "GOOGLE_MAPS_API_KEY"
        val properties = Properties().apply {
            val localPropertiesFile = file("../local.properties")

            if (!localPropertiesFile.exists()) {
                throw FileNotFoundException("Couldn't find file local.properties")
            }

            localPropertiesFile.inputStream().use(::load)
        }
        manifestPlaceholders[keyName] = properties.getProperty(keyName, null)
            ?: throw IllegalArgumentException("Couldn't find property $keyName in local.properties")

    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material.icons)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.adaptive)
    implementation(libs.androidx.compose.material3.windowsizeclass)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.datastore)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.androidx.room3.runtime)
    implementation(libs.androidx.work)
    implementation(libs.google.maps)
    implementation(libs.google.play.location)
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation)

    ksp(libs.androidx.room3.compiler)
    ksp(libs.hilt.compiler)

    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)

    testImplementation(libs.junit)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}
