plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("org.jetbrains.kotlin.kapt")
    alias(libs.plugins.google.gms.services) // ¡AÑADIR ESTE!
    alias(libs.plugins.firebase.crashlytics) // ¡AÑADIR ESTE!
}

android {
    namespace = "com.dfmiguel.gopack"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.dfmiguel.gopack"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Room components
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx) // Kotlin Extensions and Coroutines support for Room
    kapt(libs.androidx.room.compiler)

    // Firebase BoM (Bill of Materials) - Importante para gestionar versiones de Firebase
    implementation(platform(libs.firebase.bom))

    // Firebase Analytics (KTX significa extensiones de Kotlin, recomendado)
    implementation(libs.firebase.analytics.ktx)

    // Firebase Crashlytics (KTX)
    implementation(libs.firebase.crashlytics.ktx)
}