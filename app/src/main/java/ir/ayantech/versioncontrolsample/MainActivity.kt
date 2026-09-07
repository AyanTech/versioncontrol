package ir.ayantech.versioncontrolsample

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import ir.ayantech.versioncontrol.VersionControlCore

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<View>(R.id.shareAppBtn).setOnClickListener {
            shareApp()
        }
    }

    private fun checkForNewVersion() {
        VersionControlCore.getInstance("https://versioncontrolinfra.ayanco.ae/WebServices/App.svc/")
            .setApplicationName(getApplicationName())
            .setApplicationType(getApplicationType())
            .setCategoryName("cafebazaar")
            .checkForNewVersion(this@MainActivity)
    }

    fun getApplicationName(): String {
        return "testcase2"
    }

    fun getApplicationType(): String {
        return "Android"
    }

    fun shareApp() {
        VersionControlCore.getInstance("https://versioncontrolinfra.ayanco.ae/WebServices/App.svc/")
            .setApplicationName(getApplicationName())
            .setApplicationType(getApplicationType())
            .setCategoryName("cafebazaar")
            .shareApp(this)
    }
}
