# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# ---------------------------------------------------------------------------
# Crash traces
# ---------------------------------------------------------------------------
# Keep file/line info so the uploaded mapping.txt can deobfuscate stack traces,
# but rename the source-file attribute (Play Console only needs the mapping).
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ---------------------------------------------------------------------------
# Reflection / generics metadata (needed by Gson, Kotlin reflection, etc.)
# ---------------------------------------------------------------------------
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations, AnnotationDefault

# ---------------------------------------------------------------------------
# App Fragments
# FragmentStateAdapter / CursorPagerAdapter instantiate fragments reflectively
# (fragmentClass.newInstance()) and the framework re-creates them from saved
# state, so keep the classes and their no-arg constructors.
# ---------------------------------------------------------------------------
-keep public class * extends androidx.fragment.app.Fragment {
    public <init>();
}

# ---------------------------------------------------------------------------
# App data layer
# Data models are picked by ETDataModelCreator and injected via Koin; DB
# helpers are injected via Koin and read/write JSON by column name. Keep them
# whole rather than chase individual reflective entry points.
# ---------------------------------------------------------------------------
-keep class com.watnapp.etipitaka.plus.model.** { *; }
-keep class com.watnapp.etipitaka.plus.helper.** { *; }

# ---------------------------------------------------------------------------
# WebView <-> JavaScript bridge
# No @JavascriptInterface methods today, but keep this so adding one later
# doesn't silently break in release builds.
# ---------------------------------------------------------------------------
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# ---------------------------------------------------------------------------
# Gson (used in BookDatabaseHelper for the roman/pivot mapping tables)
# ---------------------------------------------------------------------------
-keep class com.google.gson.** { *; }
-keep class * extends com.google.gson.reflect.TypeToken
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-dontwarn sun.misc.**

# ---------------------------------------------------------------------------
# Enums (preserve values()/valueOf for name-based lookups & serialization)
# ---------------------------------------------------------------------------
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ---------------------------------------------------------------------------
# Third-party libraries without bundled consumer rules
# ---------------------------------------------------------------------------
# SlidingMenu (com.jeremyfeinstein.slidingmenu)
-keep class com.jeremyfeinstein.slidingmenu.** { *; }
-dontwarn com.jeremyfeinstein.slidingmenu.**

# Koin
-dontwarn org.koin.**

# Ion HTTP library references GMS ProviderInstaller as an optional dependency
# that isn't on the classpath.
-dontwarn com.google.android.gms.security.ProviderInstaller
-dontwarn com.google.android.gms.**
-dontwarn com.koushikdutta.ion.conscrypt.**
