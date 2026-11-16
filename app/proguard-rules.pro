# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
# Remove verbose logging from release builds
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
}

# Navigation 3 keys and onboarding/remote data are serialized by name
-keepattributes *Annotation*, InnerClasses, Signature
-keep,includedescriptorclasses class com.phonecleaner.aicleaner.**$$serializer { *; }
-keepclassmembers class com.phonecleaner.aicleaner.** {
    *** Companion;
}
-keepclasseswithmembers class com.phonecleaner.aicleaner.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep class com.phonecleaner.aicleaner.presentation.navigation.AppDestination { *; }
-keep class com.phonecleaner.aicleaner.presentation.navigation.AppDestination$* { *; }
-keep class com.phonecleaner.aicleaner.core.utils.onboardingFlowHelper.entity.** { *; }
-keep class com.phonecleaner.aicleaner.core.advertisement.taichi.** { *; }
-keep class com.phonecleaner.aicleaner.data.remote.** { *; }
