plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.jaynestv.max"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.jaynestv.max"
        minSdk = 23
        targetSdk = 34
        versionCode = 4
        versionName = "4.0"

        buildConfigField("String", "SUPABASE_URL",  "\"https://dablnrggyfcddmdeiqxi.supabase.co\"")
        buildConfigField("String", "SUPABASE_KEY",  "\"sb_publishable_d8mzJ3iulCU7YdlV_lrdQw_32pOzDXc\"")
        buildConfigField("String", "BASE_URL",      "\"https://mq.xo.je/\"")
        buildConfigField("String", "FALLBACK_URL",  "\"https://dde.ct.ws/\"")
    }

    signingConfigs {
        create("release") {
            storeFile     = file(System.getenv("CM_KEYSTORE_PATH") ?: "debug.keystore")
            storePassword = System.getenv("CM_KEYSTORE_PASSWORD") ?: "android"
            keyAlias      = System.getenv("CM_KEY_ALIAS")         ?: "androiddebugkey"
            keyPassword   = System.getenv("CM_KEY_PASSWORD")      ?: "android"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled   = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
        debug {
            applicationIdSuffix = ".debug"
        }
    }

    buildFeatures {
        viewBinding = true
        buildConfig  = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }

    applicationVariants.all {
        outputs.all {
            (this as com.android.build.gradle.internal.api.BaseVariantOutputImpl)
                .outputFileName = "JaynesMaxTV-v${versionName}-${buildType.name}.apk"
        }
    }
}

dependencies {
    // AndroidX core
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("androidx.viewpager2:viewpager2:1.0.0")

    // ViewModel + LiveData
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.activity:activity-ktx:1.8.2")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Retrofit + OkHttp (REST API calls)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Gson
    implementation("com.google.code.gson:gson:2.10.1")

    // ExoPlayer (Media3) — HLS + DASH/MPD support
    implementation("androidx.media3:media3-exoplayer:1.3.0")
    implementation("androidx.media3:media3-exoplayer-hls:1.3.0")
    implementation("androidx.media3:media3-exoplayer-dash:1.3.0")
    implementation("androidx.media3:media3-ui:1.3.0")
    implementation("androidx.media3:media3-common:1.3.0")
    implementation("androidx.media3:media3-session:1.3.0")

    // Glide (image loading)
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // Lottie animation
    implementation("com.airbnb.android:lottie:6.4.0")

    // Shimmer loading effect
    implementation("com.facebook.shimmer:shimmer:0.5.0")

    // Dot indicator for ViewPager2
    implementation("com.tbuonomo:dotsindicator:5.0")
}
