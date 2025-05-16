plugins {
   alias(libs.plugins.androidApplication)
   alias(libs.plugins.jetbrainsKotlinAndroid)
   alias(libs.plugins.googleAndroidLibrariesMapsplatformSecretsGradlePlugin)
}

android {
   namespace = "com.project.womensafety"
   compileSdk = 34

   defaultConfig {
      applicationId = "com.project.womensafety"
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
      sourceCompatibility = JavaVersion.VERSION_1_8
      targetCompatibility = JavaVersion.VERSION_1_8
   }
   kotlinOptions {
      jvmTarget = "1.8"
   }
   viewBinding {
      enable = true
   }
   buildFeatures {
      viewBinding = true
   }
}

dependencies {

   implementation(libs.androidx.core.ktx)
   implementation(libs.androidx.appcompat)
   implementation(libs.material)
   implementation(libs.androidx.activity)
   implementation(libs.androidx.constraintlayout)
   implementation(libs.androidx.navigation.fragment.ktx)
   implementation(libs.androidx.navigation.ui.ktx)
   implementation(libs.play.services.maps)
   testImplementation(libs.junit)
   androidTestImplementation(libs.androidx.junit)
   androidTestImplementation(libs.androidx.espresso.core)
   implementation(libs.retrofit2.converter.gson)
   implementation(libs.retrofit)
   implementation(libs.play.services.location)
   implementation(libs.okhttp)
   implementation("com.google.ai.client.generativeai:generativeai:0.9.0")
}