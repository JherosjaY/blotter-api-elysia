# ============================================
# BLOTTER MANAGEMENT SYSTEM - PROGUARD RULES
# ============================================

# Keep line numbers for crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ============================================
# KOTLIN
# ============================================
-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

# ============================================
# JETPACK COMPOSE
# ============================================
-keep class androidx.compose.** { *; }
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.** { *; }
-dontwarn androidx.compose.**

# ============================================
# ROOM DATABASE
# ============================================
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Keep Room entities
-keep class com.example.blottermanagementsystem.data.entity.** { *; }
-keep class com.example.blottermanagementsystem.data.dao.** { *; }

# ============================================
# RETROFIT & OKHTTP
# ============================================
-keepattributes Signature
-keepattributes *Annotation*
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn retrofit2.**
-dontwarn okhttp3.**
-dontwarn okio.**

# ============================================
# GSON
# ============================================
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

# ============================================
# APACHE POI (Excel)
# ============================================
-keep class org.apache.poi.** { *; }
-dontwarn org.apache.poi.**
-dontwarn org.apache.commons.**
-dontwarn org.apache.xmlbeans.**

# ============================================
# COIL (Image Loading)
# ============================================
-keep class coil.** { *; }
-dontwarn coil.**

# ============================================
# ZXING (QR Code)
# ============================================
-keep class com.google.zxing.** { *; }
-dontwarn com.google.zxing.**

# ============================================
# CAMERAX
# ============================================
-keep class androidx.camera.** { *; }
-dontwarn androidx.camera.**

# ============================================
# ITEXT PDF
# ============================================
-keep class com.itextpdf.** { *; }
-dontwarn com.itextpdf.**

# ============================================
# MPANDROIDCHART
# ============================================
-keep class com.github.mikephil.charting.** { *; }
-dontwarn com.github.mikephil.charting.**

# ============================================
# LOTTIE
# ============================================
-keep class com.airbnb.lottie.** { *; }
-dontwarn com.airbnb.lottie.**

# ============================================
# ACCOMPANIST
# ============================================
-keep class com.google.accompanist.** { *; }
-dontwarn com.google.accompanist.**

# ============================================
# EXOPLAYER
# ============================================
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# ============================================
# GENERAL OPTIMIZATIONS
# ============================================
# Remove logging in release
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# Optimize
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep custom views
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}

# Keep Parcelable
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Keep Serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# ============================================
# NEW FEATURES OPTIMIZATIONS (15 Features)
# ============================================

# WorkManager (Offline Sync)
-keep class androidx.work.** { *; }
-dontwarn androidx.work.**

# Google Maps & Location
-keep class com.google.android.gms.maps.** { *; }
-keep class com.google.android.gms.location.** { *; }
-dontwarn com.google.android.gms.**

# Calendar Library
-keep class com.kizitonwose.calendar.** { *; }
-dontwarn com.kizitonwose.calendar.**

# Biometric
-keep class androidx.biometric.** { *; }
-dontwarn androidx.biometric.**

# Speech Recognition (Voice-to-Text)
-keep class android.speech.** { *; }
-dontwarn android.speech.**