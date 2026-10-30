# Regles ProGuard par defaut pour TransportTN
-keepattributes Signature
-keepattributes *Annotation*

# Retrofit / Gson : conserver les modeles pour la (de)serialisation JSON
-keep class com.transporttn.app.models.** { *; }
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Firebase
-keep class com.google.firebase.** { *; }
