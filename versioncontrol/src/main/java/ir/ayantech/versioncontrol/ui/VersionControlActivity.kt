package ir.ayantech.versioncontrol.ui

import android.os.Bundle
import android.view.Window
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity

class VersionControlActivity : AppCompatActivity() {

    private fun getVCTitle(): String? = intent.getStringExtra("title")
    private fun getVCMessage(): String? = intent.getStringExtra("message")
    private fun getVCPositiveButton(): String? = intent.getStringExtra("pos_btn")
    private fun getVCNegativeButton(): String? = intent.getStringExtra("neg_btn")
    private fun getVCLinkType(): String? = intent.getStringExtra("link_type")
    private fun getVCLink(): String? = intent.getStringExtra("link")
    private fun getVCUpdateStatus(): String? = intent.getStringExtra("update_status")
    private fun getVCChangeLogs(): ArrayList<String>? = intent.getStringArrayListExtra("change_logs")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Prevent back press
            }
        })

        val dialog = VersionControlDialog(
            activity = this,
            title = getVCTitle(),
            message = getVCMessage(),
            positiveButton = getVCPositiveButton(),
            negativeButton = getVCNegativeButton(),
            changeLogs = getVCChangeLogs(),
            linkType = getVCLinkType(),
            link = getVCLink(),
            updateStatus = getVCUpdateStatus(),
            typeface = null
        )
        dialog.setOnDismissListener {
            if (!isFinishing) {
                finish()
            }
        }
        dialog.show()
    }
}