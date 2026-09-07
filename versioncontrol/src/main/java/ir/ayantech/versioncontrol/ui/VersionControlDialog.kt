package ir.ayantech.versioncontrol.ui

import android.app.Activity
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.TextView
import androidx.activity.ComponentDialog
import androidx.activity.OnBackPressedCallback
import ir.ayantech.versioncontrol.VersionControl
import ir.ayantech.versioncontrol.databinding.ActivityVersionControlBinding
import ir.ayantech.versioncontrol.domain.model.DownloadState
import ir.ayantech.versioncontrol.domain.model.LinkType
import ir.ayantech.versioncontrol.domain.model.UpdateInfo
import ir.ayantech.versioncontrol.domain.model.UpdateStatus
import ir.ayantech.versioncontrol.util.ApkInstaller
import ir.ayantech.versioncontrol.util.UrlLauncher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Locale

class VersionControlDialog @JvmOverloads constructor(
    private val activity: Activity,
    private val updateInfo: UpdateInfo,
    private val typeface: Typeface? = null,
    private val versionControl: VersionControl = VersionControl.create(activity)
) : ComponentDialog(activity) {

    constructor(
        activity: Activity,
        title: String?,
        message: String?,
        positiveButton: String?,
        negativeButton: String?,
        changeLogs: List<String>?,
        linkType: String?,
        link: String?,
        updateStatus: String?,
        typeface: Typeface?
    ) : this(
        activity = activity,
        updateInfo = UpdateInfo(
            title = title,
            body = message,
            acceptButtonText = positiveButton,
            rejectButtonText = negativeButton,
            changeLogs = changeLogs ?: emptyList(),
            linkType = LinkType.fromRawValue(linkType),
            link = link,
            updateStatus = UpdateStatus.fromRawValue(updateStatus)
        ),
        typeface = typeface
    )

    private lateinit var binding: ActivityVersionControlBinding
    private val dialogScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var downloadJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        binding = ActivityVersionControlBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setCancelable(false)
        setCanceledOnTouchOutside(false)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Prevent back press dismiss
            }
        })

        setupViews()
        applyTypeface()
    }

    private fun setupViews() {
        binding.titleTv.text = updateInfo.title
        binding.messageTv.text = updateInfo.body
        binding.positiveTv.text = updateInfo.acceptButtonText
        binding.negativeTv.text = updateInfo.rejectButtonText

        if (updateInfo.changeLogs.isEmpty()) {
            binding.changeLogTv.visibility = View.GONE
        } else {
            val changeLog = StringBuilder()
            for (s in updateInfo.changeLogs) {
                changeLog.append(s).append("\n")
            }
            binding.changeLogTv.text = changeLog
            binding.changeLogTv.visibility = View.VISIBLE
        }

        binding.positiveTv.setOnClickListener {
            handlePositiveButtonClick()
        }

        binding.negativeTv.setOnClickListener {
            handleNegativeButtonClick()
        }
    }

    private fun handlePositiveButtonClick() {
        if (updateInfo.linkType == LinkType.DIRECT) {
            handleDirectApkDownload()
        } else {
            handlePageNavigation()
        }
    }

    private fun handleDirectApkDownload() {
        val downloadDir = ApkInstaller.getDownloadDirectory(context)
        val downloadUrl = updateInfo.link

        if (downloadUrl.isNullOrEmpty() || downloadUrl.startsWith("http").not()) {
            dismiss()
            return
        }

        if (downloadDir == null) {
            handlePageNavigation()
            return
        }

        showProgressUi()
        val destinationPath = "$downloadDir/newversion${Date().time}.apk"
        startApkDownload(downloadUrl, destinationPath)
    }

    private fun startApkDownload(downloadUrl: String, destinationPath: String) {
        downloadJob?.cancel()
        downloadJob = dialogScope.launch {
            versionControl.downloadApk(downloadUrl, destinationPath).collectLatest { state ->
                handleDownloadState(state)
            }
        }
    }

    private fun handleDownloadState(state: DownloadState) {
        when (state) {
            is DownloadState.Idle -> {}
            is DownloadState.Downloading -> updateProgressPercent(state.progressPercent)
            is DownloadState.Success -> handleDownloadSuccess(state.filePath)
            is DownloadState.Error -> handleDownloadError()
        }
    }

    private fun handleDownloadSuccess(filePath: String) {
        ApkInstaller.installApk(context, filePath)
        dismiss()
        finishIfMandatory()
    }

    private fun handleDownloadError() {
        dismiss()
        UrlLauncher.openUrl(context, updateInfo.link)
    }

    private fun handlePageNavigation() {
        dismiss()
        UrlLauncher.openUrl(context, updateInfo.link)
        finishIfMandatory()
    }

    private fun handleNegativeButtonClick() {
        downloadJob?.cancel()
        dismiss()
        finishIfMandatory()
    }

    private fun finishIfMandatory() {
        if (updateInfo.updateStatus == UpdateStatus.MANDATORY) {
            activity.finish()
        }
    }

    private fun showProgressUi() {
        binding.progressBar.visibility = View.VISIBLE
        binding.progressTv.visibility = View.VISIBLE
    }

    private fun updateProgressPercent(progressPercent: Int) {
        binding.progressBar.progress = progressPercent
        binding.progressTv.text = String.format(Locale.getDefault(), "%%%d", progressPercent)
    }

    private fun applyTypeface() {
        val currentTypeface = typeface ?: return
        for (i in 0 until binding.mainContentLl.childCount) {
            val child = binding.mainContentLl.getChildAt(i)
            if (child is TextView) {
                child.typeface = currentTypeface
            }
        }
        binding.positiveTv.typeface = currentTypeface
        binding.negativeTv.typeface = currentTypeface
    }

    override fun onStop() {
        super.onStop()
        dialogScope.cancel()
    }
}
