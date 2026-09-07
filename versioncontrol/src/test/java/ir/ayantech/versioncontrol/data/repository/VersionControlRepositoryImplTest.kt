package ir.ayantech.versioncontrol.data.repository

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import ir.ayantech.versioncontrol.VersionControlConfig
import ir.ayantech.versioncontrol.data.downloader.ApkDownloader
import ir.ayantech.versioncontrol.data.remote.VersionControlRemoteDataSource
import ir.ayantech.versioncontrol.data.remote.dto.CheckVersionResponseDto
import ir.ayantech.versioncontrol.data.remote.dto.GetLastVersionResponseDto
import ir.ayantech.versioncontrol.data.remote.dto.VCStatusDto
import ir.ayantech.versioncontrol.domain.model.DownloadState
import ir.ayantech.versioncontrol.domain.model.UpdateStatus
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class VersionControlRepositoryImplTest {

    private val remoteDataSource: VersionControlRemoteDataSource = mockk()
    private val downloader: ApkDownloader = mockk()
    private val testDispatcher = StandardTestDispatcher()

    private val repository = VersionControlRepositoryImpl(
        remoteDataSource = remoteDataSource,
        apkDownloader = downloader,
        ioDispatcher = testDispatcher
    )

    private val config = VersionControlConfig(baseUrl = "https://example.com/api/", applicationName = "testApp")

    @Test
    fun `checkVersion returns UpdateStatus on HTTP success and valid code`() = runTest(testDispatcher) {
        // Arrange
        val responseDto = CheckVersionResponseDto(
            parameters = CheckVersionResponseDto.Parameters("Mandatory"),
            status = VCStatusDto("G00000", "Success")
        )
        coEvery { remoteDataSource.checkVersion(eq(config.baseUrl), any()) } returns responseDto

        // Act
        val result = repository.checkVersion(config)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(UpdateStatus.MANDATORY, result.getOrNull())
    }

    @Test
    fun `checkVersion returns failure when HTTP response fails`() = runTest(testDispatcher) {
        // Arrange
        coEvery { remoteDataSource.checkVersion(eq(config.baseUrl), any()) } throws IOException("HTTP Error 500: Internal Server Error")

        // Act
        val result = repository.checkVersion(config)

        // Assert
        assertTrue(result.isFailure)
    }

    @Test
    fun `getLastVersion returns UpdateInfo on HTTP success and valid code`() = runTest(testDispatcher) {
        // Arrange
        val responseDto = GetLastVersionResponseDto(
            parameters = GetLastVersionResponseDto.Parameters(
                title = "Title",
                body = "Body",
                textToShare = "Share Text"
            ),
            status = VCStatusDto("G00000", "Success")
        )
        coEvery { remoteDataSource.getLastVersion(eq(config.baseUrl), any()) } returns responseDto

        // Act
        val result = repository.getLastVersion(config)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals("Title", result.getOrNull()?.title)
        assertEquals("Share Text", result.getOrNull()?.textToShare)
    }

    @Test
    fun `downloadApk delegates flow to downloader`() = runTest(testDispatcher) {
        // Arrange
        val url = "https://example.com/app.apk"
        val destination = "/tmp/app.apk"
        val expectedFlow = flowOf(DownloadState.Idle, DownloadState.Downloading(100))
        every { downloader.downloadApk(url, destination) } returns expectedFlow

        // Act
        val resultFlow = repository.downloadApk(url, destination)

        // Assert
        resultFlow.test {
            assertEquals(DownloadState.Idle, awaitItem())
            assertEquals(DownloadState.Downloading(100), awaitItem())
            awaitComplete()
        }
    }
}
