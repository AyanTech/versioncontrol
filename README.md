# AyanTech Version Control Module

Use this module in order to enable version control for your "AyanTech" application.

# Adding version control to your project

Add jitpack maven repository to your project-level build.gradle so the `allprojects` section be look like this:
```
allprojects {
    repositories {
        google()
        jcenter()
        maven { url 'https://jitpack.io' }
    }
}
```
Then put this lines in your module-level build.gradle:
```
implementation 'com.google.code.gson:gson:2.8.2'
implementation 'com.squareup.retrofit2:converter-gson:2.3.0'
implementation 'com.squareup.okhttp3:okhttp:3.12.0'
implementation 'com.coolerfall:android-http-download-manager:1.6.1'
implementation 'com.github.ayantech:VersionControl:0.5.0'
```

# Checking for new versions

You have to initialize VersionControlCore class with proper values. So, it is recommended to do it like this in your application class:
```
VersionControlCore.getInstance().setCategoryName({applicationCategory})
```

You can customize more properties with relevant setter methods. You can find some of them in the example applications.

Then anywhere in your app, when you want to check for updates:
```
VersionControlCore.getInstance().checkForNewVersion({yourActivity});
```
* Attention: You have to pass Activity context to this method.

# Share the app
Anywhere in your app, when you want to share it:
```
VersionControlCore.getInstance().shareApp({yourContext});
```

If you have to send ExtraInfo object (optional due to your project):
1. Create a class that extends `ExtraInfoModel` class.
2. Put anything you should send to server in it.
3. Call `setExtraInfo()` method in the builder.

In this case, your ExtraInfo model will be something like this:
```
public class AppExtraInfo extends ExtraInfoModel {
    private String Option1 = "x";
    private String Option2 = "y";
}
```

And initializing will be like this:
```
VersionControlCore.getInstance()
                .setCategoryName({applicationCategory})
                .setExtraInfo(new AppExtraInfo())
```

# Proguard rules:
If you are using proguard, you need to exclude this module from obfuscation. In order to do that, add this line in your proguard rules file:

```
-keep public class ir.ayantech.versioncontrol.** { *; }
```
