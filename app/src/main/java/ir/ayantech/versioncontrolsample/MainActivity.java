package ir.ayantech.versioncontrolsample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import ir.ayantech.versioncontrol.VersionControlCore;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new VersionControlCore(this)
                .setApplicationName(getApplicationName())
                .setApplicationType(getApplicationType())
                .setCategoryName("CafeBazar")
                .setApplicationVersion("2")
                .checkForNewVersion();
    }

    public String getApplicationName() {
        return "testcase5";
    }

    public String getApplicationType() {
        return "Android";
    }
}
