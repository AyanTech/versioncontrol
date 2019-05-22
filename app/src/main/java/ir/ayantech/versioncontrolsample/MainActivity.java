package ir.ayantech.versioncontrolsample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import ir.ayantech.versioncontrol.VersionControlCore;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        VersionControlCore.getInstance()
                .setApplicationName(getApplicationName())
                .setApplicationType(getApplicationType())
                .setCategoryName("CafeBazar")
                .checkForNewVersion(this);

        findViewById(R.id.shareAppBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareApp();
            }
        });
    }

    public String getApplicationName() {
        return "testcase5";
    }

    public String getApplicationType() {
        return "Android";
    }

    public void shareApp() {
        VersionControlCore.getInstance().shareApp(this);
    }
}
