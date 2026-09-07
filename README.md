# AyanTech Version Control SDK for Android

An Android SDK for managing application version checking, automatic/manual updates, direct APK downloading and installation, market store redirection, and app sharing across Android applications.

---

## 📦 Adding the SDK to Your Project

### 1. Configure Repositories

Add JitPack in your `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

### 2. Add Dependency

Add the SDK dependency to your app module's `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.github.ayantech:versioncontrol:LAST_VERSION")
}
```

### 3. Permissions

Ensure internet permission is declared in your `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

---

## 🚀 How to Use

### 1. Check for New Version

```kotlin
VersionControlCore.getInstance("BASE_URL")
    .setApplicationName("myApplicationName")
    .setCategoryName("market")
    .checkForNewVersion(activity)
```

### 2. Custom Typeface (Optional)

Apply a custom font to the version control update dialog:

```kotlin
val customTypeface = Typeface.createFromAsset(assets, "fonts/my_custom_font.ttf")

VersionControlCore.getInstance("BASE_URL")
    .setApplicationName("myApplicationName")
    .setCategoryName("market")
    .setTypeface(customTypeface)
    .checkForNewVersion(this)
```

### 3. Share Application

Launch the native sharing intent with the application store link:

```kotlin
VersionControlCore.getInstance("BASE_URL")
    .setApplicationName("myApplicationName")
    .setCategoryName("market")
    .shareApp(context)
```

---

## 🛡️ ProGuard / R8 Rules

ProGuard rules are bundled with the library. If custom obfuscation is active, ensure these rules are included:

```proguard
-keep public class ir.ayantech.versioncontrol.** { *; }
-keep class ir.ayantech.versioncontrol.data.remote.dto.** { *; }
-keep class ir.ayantech.versioncontrol.model.** { *; }
-keep class ir.ayantech.versioncontrol.domain.model.** { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
```
