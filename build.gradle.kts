// Top-level build file where you can add configuration options common to all subprojects/modules.
buildscript {
    dependencies {
        // Pin KGP/KSP above AGP 9's bundled 2.2.10 so the catalog Kotlin 2.4.20 is used.
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${libs.versions.kotlin.get()}")
        classpath("com.google.devtools.ksp:symbol-processing-gradle-plugin:${libs.versions.ksp.get()}")
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.devtools.ksp) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
}