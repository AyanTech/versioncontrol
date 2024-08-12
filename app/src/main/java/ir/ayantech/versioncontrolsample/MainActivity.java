package ir.ayantech.versioncontrolsample;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;

import ir.ayantech.versioncontrol.VersionControlClient;
import ir.ayantech.versioncontrol.VersionControlCore;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


//        VersionControlCore.getInstance("https://versioncontrolinfra.ayanco.ae/WebServices/App.svc/")
//        VersionControlCore.getInstance()
//                .setApplicationName(getApplicationName())
//                .setApplicationType(getApplicationType())
//                .setCategoryName("cafebazaar")
//                .checkForNewVersion(this);

        findViewById(R.id.shareAppBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                checkForNewVersion();
                shareApp();
            }
        });
    }

    private void checkForNewVersion() {
        VersionControlCore.getInstance("https://versioncontrolinfra.ayanco.ae/WebServices/App.svc/")
                .setApplicationName(getApplicationName())
                .setApplicationType(getApplicationType())
                .setCategoryName("cafebazaar")
                .checkForNewVersion(MainActivity.this);
    }

    public String getApplicationName() {
        return "testcase2";
    }

    public String getApplicationType() {
        return "Android";
    }

    public void shareApp() {
        VersionControlCore.getInstance("https://versioncontrolinfra.ayanco.ae/WebServices/App.svc/")
                .setApplicationName(getApplicationName())
                .setApplicationType(getApplicationType())
                .setCategoryName("cafebazaar").shareApp(this);
    }
}
