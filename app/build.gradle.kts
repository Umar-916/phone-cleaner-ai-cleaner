plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    // Optional, provides the @Serialize annotation for autogeneration of Serializers.
    alias(libs.plugins.jetbrains.kotlin.serialization)

    alias(libs.plugins.devtools.ksp)

    // Firebase plugins
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

android {
    namespace = "com.phonecleaner.aicleaner"
    compileSdk {
        version = release(37) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.phonecleaner.aicleaner"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0.0"

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
            resValue("string", "admob_id", "ca-app-pub-3940256099942544~3347511713")
            isDebuggable = false
            manifestPlaceholders["crashlyticsCollectionEnabled"] = true

        }
        debug {
            isDebuggable = true
            resValue("string", "admob_id", "ca-app-pub-3940256099942544~3347511713")
            isMinifyEnabled = false
            manifestPlaceholders["crashlyticsCollectionEnabled"] = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
        resValues = true
    }


    lint {
        checkReleaseBuilds = true
        abortOnError = true
        // AdActivity ships with the Google Mobile Ads SDK at runtime; the Scaffold padding is applied by the nav host.
        disable += setOf("MissingClass", "UnusedMaterial3ScaffoldPaddingParameter")
    }


    bundle {
        language {
            enableSplit = false
        }
    }
}

base {
    archivesName.set("Phone_Cleaner_AI_Cleaner_Pro-VC_${android.defaultConfig.versionCode}-VN_${android.defaultConfig.versionName}")
}
kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

configurations.configureEach {
    exclude(group = "com.google.android.gms", module = "play-services-ads")
    exclude(group = "com.google.android.gms", module = "play-services-ads-api")
    exclude(group = "com.google.android.gms", module = "play-services-ads-lite")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.exifinterface)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Jetpack Compose Navigation
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.kotlinx.serialization.core)

    implementation(libs.coil.network.okhttp)
    implementation(libs.coil.svg)
    implementation(libs.koin.androidd)
    implementation(libs.koin.androidx.compose)

    implementation(libs.androidx.lifecycle.viewmodelCompose)
    implementation(libs.androidx.lifecycle.runtimeCompose)

    implementation(libs.koin.core)
    implementation(libs.bundles.coil)

    implementation(libs.coil.kt.coil.compose)

    // Coroutines
    implementation(libs.jetbrains.kotlinx.coroutines.core)

    // Room dependencies
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // DataStore dependencies
    implementation(libs.androidx.datastore.preferences)

    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)

    // Lottie dependencies
    implementation(libs.lottie.compose)

    // Firebase dependencies
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.config)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.messaging)

    // Force Update
    implementation(libs.app.update.ktx)

    // SSP/SDP
    implementation(libs.sdp.ssp.compose.multiplatform)

    /** Ads */
    implementation(libs.ads.mobile.sdk)
    implementation(libs.user.messaging.platform)

    /** Billing */
    implementation(libs.funsol.billing.helper)
    implementation(libs.billing)

    /** Mediation */
    implementation(libs.pangle)
    implementation(libs.vungle)
    implementation(libs.applovin)
    implementation(libs.inmobi)
    implementation(libs.mintegral)
    implementation(libs.facebook)
    implementation(libs.yandex)
    implementation(libs.ironsource)
    implementation(libs.unity)
    implementation(libs.unity.ads)
    implementation(libs.fyber)
    implementation(libs.bidmachine)

    /** FunNotify SDK */
    implementation(libs.funnotify.sdk)

    /** App Flyer */
    implementation(libs.af.android.sdk)
    implementation(libs.adrevenue)
    implementation(libs.purchase.connector)

    /** GSON */
    implementation(libs.ktor.serialization.gson)
    implementation(libs.gson)
}