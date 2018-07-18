# AyanTech Version Control Module

Use this module in order to enable version control for your "AyanTech" application.
# Adding version control to your project

Add jitpack maven repository to your project level gradle so the allprojects section be look like this:
```
allprojects {
    repositories {
        google()
        jcenter()
        maven { url 'https://jitpack.io' }
    }
}
```
Then put this lines in your module level gradle:
```
implementation 'com.google.code.gson:gson:2.8.2'
implementation 'com.squareup.retrofit2:converter-gson:2.3.0'
implementation 'com.squareup.okhttp3:okhttp:3.10.0'
implementation 'com.coolerfall:android-http-download-manager:1.6.1'
implementation 'com.github.ayantech:VersionControl:0.2.9'
```

# Check for new versions

Anywhere in your app, when you want to check for updates:
```
new VersionControlCore({yourActivity})
                .setCategoryName({applicationCategory})
                .checkForNewVersion();
```

If you have to send ExtraInfo object (optional due to your project):
1. Create a class that extends ExtraInfoModel class.
2. Put anything you should send to server in it.
3. Call setExtraInfo() method in the builder.

In this case, your ExtraInfo model will be something like this:
```
public class AppExtraInfo extends ExtraInfoModel {
    private String Option1 = "x;
    private String Option2 = "y";
}
```

And checking for new version code will be like this:
```
new VersionControlCore({yourActivity})
                .setCategoryName({applicationCategory})
                .setExtraInfo(new AppExtraInfo())
                .checkForNewVersion();
```
