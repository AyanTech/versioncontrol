package ir.ayantech.versioncontrolsample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import ir.ayantech.versioncontrol.VersionControlCore;

public class MainActivity extends AppCompatActivity {

    private VersionControlCore versionControlCore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        versionControlCore = new VersionControlCore(this)
                .setApplicationName(getApplicationName())
                .setApplicationType(getApplicationType())
                .setCategoryName("CafeBazar")
                .checkForNewVersion();
    }

    public String getApplicationName() {
        return "testcase5";
    }

    public String getApplicationType() {
        return "Android";
    }

    public void shareApp() {
        versionControlCore.shareApp();
    }
}
