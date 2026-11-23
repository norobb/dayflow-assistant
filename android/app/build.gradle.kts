// CI passes its run number so every CI build is newer than the release it builds on.
val ciRun = System.getenv("DAYFLOW_CI_RUN")?.toIntOrNull() ?: 0
val baseVersionCode = 5
val baseVersionName = "2.1"

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.dayflow.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.dayflow.app"
        minSdk = 26
        targetSdk = 35
        versionCode = baseVersionCode * 10000 + ciRun
        versionName = if (ciRun > 0) "$baseVersionName-ci.$ciRun" else baseVersionName
        buildConfigField("int", "CI_RUN", ciRun.toString())

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // Release signing only when CI provides the key; local release builds stay unsigned as before.
    val keystore = System.getenv("DAYFLOW_KEYSTORE")?.let { file(it) }?.takeIf { it.exists() }
    signingConfigs {
        if (keystore != null) create("ci") {
            storeFile = keystore
            storePassword = System.getenv("DAYFLOW_KEYSTORE_PASSWORD")
            keyAlias = System.getenv("DAYFLOW_KEY_ALIAS")
            keyPassword = System.getenv("DAYFLOW_KEY_PASSWORD")
        }
    }

    buildTypes {
        release {
            if (keystore != null) signingConfig = signingConfigs.getByName("ci")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            applicationIdSuffix = ".debug"
            isDebuggable = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    // Test installs grant runtime permissions (the speech recognizer needs RECORD_AUDIO even for files).
    installation {
        installOptions += listOf("-g")
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.service)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)

    // Room persistence
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // DataStore preferences
    implementation(libs.androidx.datastore.preferences)

    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)

    // On-device OCR (bundled model, works offline)
    implementation(libs.mlkit.text.recognition)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Xposed API (provided by LSPosed/Vector at runtime) for the root-only notification button hook
    compileOnly("de.robv.android.xposed:api:82")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
