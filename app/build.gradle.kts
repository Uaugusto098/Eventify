plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.eventlyapp"
    compileSdk {
        version = release(36)
    }
    defaultConfig {
        applicationId = "com.example.eventlyapp"
        minSdk = 24
        targetSdk = 34
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


    packaging {
        resources {
            excludes += setOf(
                "META-INF/NOTICE.md",
                "META-INF/LICENSE.md",
                "META-INF/NOTICE.txt",
                "META-INF/LICENSE.txt",
                "META-INF/notice.txt",
                "META-INF/license.txt"
            )
        }
    }

}



    dependencies {
        implementation(libs.appcompat)
        implementation(libs.material)
        implementation(libs.activity)
        implementation(libs.constraintlayout)
        implementation(libs.cardview)
        implementation(libs.firebase.auth)
        testImplementation(libs.junit)
        androidTestImplementation(libs.ext.junit)
        androidTestImplementation(libs.espresso.core)
        implementation(platform("com.google.firebase:firebase-bom:34.13.0"))
        implementation("com.google.firebase:firebase-analytics")
        implementation("com.google.firebase:firebase-database")
        implementation("com.google.android.material:material:1.12.0")
        implementation("com.google.android.gms:play-services-code-scanner:16.1.0")
        implementation("com.journeyapps:zxing-android-embedded:4.3.0")
        implementation("com.github.DantSu:ESCPOS-ThermalPrinter-Android:3.4.0")
        implementation("com.sun.mail:android-mail:1.6.7")
        implementation("com.sun.mail:android-activation:1.6.7")
    }


