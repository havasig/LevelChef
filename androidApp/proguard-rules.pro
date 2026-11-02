# R8 rules for the LevelChef app. Libraries ship their own consumer rules (kotlinx.serialization,
# Koin, Ktor, SQLDelight, Compose); this file only covers what they don't.

# Keep line numbers for readable crash stack traces (Kermit/CrashLogWriter), hide source file names.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Ktor references JVM-only classes that don't exist on Android.
-dontwarn java.lang.management.**
-dontwarn org.slf4j.**
-dontwarn io.ktor.utils.io.jvm.nio.**
