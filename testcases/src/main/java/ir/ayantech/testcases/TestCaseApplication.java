package ir.ayantech.testcases;

import android.app.Application;

import ir.ayantech.versioncontrol.VersionControlCore;

public class TestCaseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        runVersionControlTestcase("testcase4");
    }

    private void runVersionControlTestcase(String testcaseName) {
        new VersionControlCore(this)
                .setApplicationName(testcaseName)
                .setCategoryName("cafebazar")
                .checkForNewVersion();
    }
}
