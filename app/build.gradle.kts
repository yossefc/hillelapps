plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.firebase.appdistribution)
}

android {
    namespace = "com.example.hillelapps"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.hillelapps"
        minSdk = 30
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            firebaseAppDistribution {
                releaseNotesFile="/path/to/releasenotes.txt"
                testers="yossefcohzar@gmail.com"
            }
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_19
        targetCompatibility = JavaVersion.VERSION_19
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
    kotlinOptions {
        jvmTarget = "19"
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.zmanim)
    implementation (libs.threetenabp)
    implementation (libs.wear.v120)
    implementation (platform(libs.firebase.bom.v3010))
    implementation (libs.firebase.config.ktx)
    implementation (libs.firebase.storage.ktx)
    implementation(libs.firebase.analytics.ktx)
    implementation(platform(libs.firebase.bom))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.compiler)
    implementation(libs.play.services.wearable)
    implementation(platform(libs.compose.bom))
    implementation(libs.ui)
    implementation(libs.ui.tooling.preview)
    implementation(libs.compose.foundation)
    implementation(libs.activity.compose)
    implementation(libs.core.splashscreen)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.firebase.crashlytics.buildtools)
    implementation(libs.androidx.appcompat)
    implementation(libs.wear)
    implementation(libs.compose.material.v131)
    implementation(libs.firebase.database.ktx)
    implementation(libs.firebase.database)
    implementation(libs.androidx.room.common)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.play.services.games)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.ui.test.junit4)
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)
    implementation(libs.androidx.room.runtime)
    annotationProcessor(libs.androidx.room.compiler)
    implementation (libs.google.firebase.firestore.ktx.v2491)



}