package ir.ayantech.versioncontrol.data.downloader

import ir.ayantech.versioncontrol.domain.model.DownloadState
import kotlinx.coroutines.flow.Flow

interface ApkDownloader {
    fun downloadApk(url: String, destinationPath: String): Flow<DownloadState>
}
