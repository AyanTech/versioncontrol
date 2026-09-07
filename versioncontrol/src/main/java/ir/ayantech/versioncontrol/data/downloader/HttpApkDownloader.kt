package ir.ayantech.versioncontrol.data.downloader

import android.content.Context
import com.coolerfall.download.DownloadCallback
import com.coolerfall.download.DownloadManager
import com.coolerfall.download.DownloadRequest
import com.coolerfall.download.OkHttpDownloader
import com.coolerfall.download.Priority
import ir.ayantech.versioncontrol.domain.model.DownloadState
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.concurrent.TimeUnit

class HttpApkDownloader(
    private val context: Context
) : ApkDownloader {

    private val downloadManager: DownloadManager by lazy {
        DownloadManager.Builder()
            .context(context.applicationContext)
            .downloader(OkHttpDownloader.create())
            .threadPoolSize(2)
            .build()
    }

    override fun downloadApk(url: String, destinationPath: String): Flow<DownloadState> = callbackFlow {
        trySend(DownloadState.Idle)

        val request = DownloadRequest.Builder()
            .url(url)
            .retryTime(5)
            .retryInterval(2, TimeUnit.SECONDS)
            .progressInterval(100, TimeUnit.MILLISECONDS)
            .priority(Priority.HIGH)
            .destinationFilePath(destinationPath)
            .downloadCallback(object : DownloadCallback() {
                override fun onProgress(downloadId: Int, bytesWritten: Long, totalBytes: Long) {
                    if (totalBytes > 0) {
                        val progressPercent = ((bytesWritten * 100) / totalBytes).toInt()
                        trySend(DownloadState.Downloading(progressPercent))
                    }
                }

                override fun onSuccess(downloadId: Int, filePath: String) {
                    trySend(DownloadState.Success(filePath))
                    channel.close()
                }

                override fun onFailure(downloadId: Int, statusCode: Int, errMsg: String?) {
                    trySend(DownloadState.Error(errMsg ?: "Download failed with status $statusCode"))
                    channel.close()
                }
            })
            .build()

        val downloadId = downloadManager.add(request)

        awaitClose {
            if (downloadId != -1) {
                downloadManager.cancel(downloadId)
            }
        }
    }
}
