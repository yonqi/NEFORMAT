plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.neformat"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.neformat"
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
}

dependencies {
    // Навигация
    implementation ("androidx.navigation:navigation-fragment:2.7.0")
    implementation ("androidx.navigation:navigation-ui:2.7.0")

    // Material Bottom Navigation
    implementation ("com.google.android.material:material:1.10.0")

    implementation(platform("com.google.firebase:firebase-bom:33.12.0"))
    implementation("com.google.firebase:firebase-auth")

    // Firebase UI (для удобства загрузки)
    implementation ("com.firebaseui:firebase-ui-database:8.0.2")
    implementation ("com.google.firebase:firebase-database:20.3.0")

    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.android.volley:volley:1.2.1")



// Библиотека для загрузки изображений
    implementation ("com.squareup.picasso:picasso:2.71828")

    implementation ("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.16.0")

    implementation ("com.squareup.okhttp3:okhttp:4.9.1")
    implementation ("androidx.activity:activity-ktx:1.4.0")
    implementation ("com.google.android.material:material:1.11.0")




    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)
    implementation(libs.firebase.auth)
    implementation(libs.credentials)
    implementation(libs.credentials.play.services.auth)
    implementation(libs.googleid)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}


