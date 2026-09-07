package ir.ayantech.testcases

import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import ir.ayantech.versioncontrol.VersionControlCore

class TestcaseListActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_testcase_list)
        bindButtons()
    }

    private fun bindButtons() {
        findViewById<View>(R.id.testcase1Button).setOnClickListener(this)
        findViewById<View>(R.id.testcase2Button).setOnClickListener(this)
        findViewById<View>(R.id.testcase3Button).setOnClickListener(this)
        findViewById<View>(R.id.testcase4Button).setOnClickListener(this)
        findViewById<View>(R.id.testcase5Button).setOnClickListener(this)

        findViewById<View>(R.id.shareBtn).setOnClickListener {
            VersionControlCore.getInstance("https://versioncontrol.infra.ayantech.ir/WebServices/App.svc/").shareApp(this@TestcaseListActivity)
        }
    }

    override fun onClick(v: View) {
        val testcaseNumber = getTestcaseNumber(v)
        val testcaseName = getTestcaseName(testcaseNumber)
        runVersionControlTestcase(testcaseName)
    }

    private fun getTestcaseNumber(v: View): Int {
        return when (v.id) {
            R.id.testcase1Button -> 1
            R.id.testcase2Button -> 2
            R.id.testcase3Button -> 3
            R.id.testcase4Button -> 4
            R.id.testcase5Button -> 5
            else -> 0
        }
    }

    private fun getTestcaseName(testcaseNumber: Int): String {
        return "testcase$testcaseNumber"
    }

    private fun runVersionControlTestcase(testcaseName: String) {
        VersionControlCore.getInstance("https://versioncontrol.infra.ayantech.ir/WebServices/App.svc/")
            .setApplicationName(testcaseName)
            .setCategoryName("cafebazaar")
            .setTypeface(Typeface.createFromAsset(assets, "fonts/iransans-ultralight.ttf"))
            .checkForNewVersion(this)
    }
}
