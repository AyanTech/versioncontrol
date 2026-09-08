package ir.ayantech.versioncontrolsample

import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import ir.ayantech.testcases.R
import ir.ayantech.versioncontrol.VersionControl
import ir.ayantech.versioncontrol.VersionControlConfig
import ir.ayantech.versioncontrol.VersionControlCore
import ir.ayantech.versioncontrol.model.ExtraInfoModel
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private val baseUrl = "MY_BASE_URL"
    private val iranColocationBaseUrl = "MY_BASE_URL"
    private val internationalColocationBaseUrl = "MY_BASE_URL"
    private val applicationName = "testcase2"
    private val market = "cafebazaar"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        enableEdgeToEdge()

        findViewById<Button>(R.id.testcase1Button)?.setOnClickListener(this)
        findViewById<Button>(R.id.testcase2Button)?.setOnClickListener(this)
        findViewById<Button>(R.id.testcase3Button)?.setOnClickListener(this)
        findViewById<Button>(R.id.testcase4Button)?.setOnClickListener(this)
        findViewById<Button>(R.id.testcase5Button)?.setOnClickListener(this)

        findViewById<Button>(R.id.shareAppBtn)?.setOnClickListener {
            shareApp()
        }

        findViewById<Button>(R.id.getLastVersionBtn)?.setOnClickListener {
            testGetLastVersion()
        }

        findViewById<Button>(R.id.getColocationConfigBtn)?.setOnClickListener {
            testGetColocationConfig()
        }
    }

    override fun onClick(v: View) {
        val testcaseName = getTestcaseName(v)
        runVersionControlTestcase(testcaseName)
    }

    fun getTestcaseName(v: View): String {
        val id = when (v.id) {
            R.id.testcase1Button -> 1
            R.id.testcase2Button -> 2
            R.id.testcase3Button -> 3
            R.id.testcase4Button -> 4
            R.id.testcase5Button -> 5
            else -> 0
        }
        return "testcase$id"
    }

    fun getApplicationType(): String {
        return VersionControlConfig.DEFAULT_APPLICATION_TYPE
    }

    private fun runVersionControlTestcase(testcaseName: String) {
        val typeface = try {
            Typeface.createFromAsset(assets, "fonts/iransans-ultralight.ttf")
        } catch (_: Exception) {
            null
        }

        val appVersion = VersionControlCore.getApplicationVersion(this)

        VersionControlCore.getInstance(baseUrl)
            .setApplicationName(testcaseName)
            .setApplicationType(getApplicationType())
            .setCategoryName(market)
            .setApplicationVersion(appVersion)
            .setExtraInfo(ExtraInfoModel())
            .setTypeface(typeface)
            .checkForNewVersion(this)
    }

    fun shareApp() {
        VersionControlCore.getInstance(baseUrl)
            .setApplicationName(applicationName)
            .setApplicationType(getApplicationType())
            .setCategoryName(market)
            .shareApp(this)
    }

    private fun testGetLastVersion() {
        val typeface = try {
            Typeface.createFromAsset(assets, "fonts/iransans-ultralight.ttf")
        } catch (_: Exception) {
            null
        }

        val config = VersionControlConfig(
            baseUrl = baseUrl,
            applicationName = applicationName,
            applicationType = getApplicationType(),
            categoryName = market,
            applicationVersion = VersionControlCore.getApplicationVersion(this),
            extraInfo = ExtraInfoModel(),
            typeface = typeface
        )

        val versionControl = VersionControl.create(this)
        lifecycleScope.launch {
            val result = versionControl.getLastVersion(config)
            result.fold(
                onSuccess = { updateInfo ->
                    Toast.makeText(
                        this@MainActivity,
                        "Last version: ${updateInfo.title}",
                        Toast.LENGTH_LONG
                    ).show()
                },
                onFailure = { error ->
                    Toast.makeText(
                        this@MainActivity,
                        "Error: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            )
        }
    }

    private fun testGetColocationConfig() {
        val appVersion = VersionControlCore.getApplicationVersion(this)

        lifecycleScope.launch {
            val result = VersionControlCore.getInstance(baseUrl)
                .setIranBaseUrl(iranColocationBaseUrl)
                .setInternationalBaseUrl(internationalColocationBaseUrl)
                .setApplicationName(applicationName)
                .setApplicationType(getApplicationType())
                .setApplicationVersion(appVersion)
                .getApplicationColocationConfig(this@MainActivity)

            result.fold(
                onSuccess = { colocationResult ->
                    val endpointsSummary = colocationResult.endpointList.joinToString("\n") {
                        "${it.name}: ${it.url}"
                    }
                    Toast.makeText(
                        this@MainActivity,
                        "Lane: ${colocationResult.versionControlBaseUrl}\nEndpoints:\n$endpointsSummary",
                        Toast.LENGTH_LONG
                    ).show()
                },
                onFailure = { error ->
                    Toast.makeText(
                        this@MainActivity,
                        "Colocation error: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            )
        }
    }
}
