# ObjectBox entities
-keep class com.zerosignal.cognitedge.data.database.entity.** { *; }
-keep class com.zerosignal.cognitedge.data.database.entity.MyObjectBox { *; }

# ObjectBox runtime
-keep class io.objectbox.** { *; }
-dontwarn io.objectbox.**

# Nexa SDK (when integrated)
-keep class ai.nexa.** { *; }
-dontwarn ai.nexa.**

# Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }
