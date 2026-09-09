package ir.ayantech.versioncontrol.domain.usecase

import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockk
import ir.ayantech.versioncontrol.domain.model.DownloadState
import ir.ayantech.versioncontrol.domain.repository.VersionControlRepository
import ir.ayantech.versioncontrol.domain.usecase.impl.DownloadApkUseCaseImpl
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class DownloadApkUseCaseTest {

    private val repository: VersionControlRepository = mockk()
    private val downloadApkUseCase: DownloadApkUseCase = DownloadApkUseCaseImpl(repository)

    @Test
    fun `invoke delegates download flow to repository`() = runTest {
        // Arrange
        val url = "https://example.com/app.apk"
        val destination = "/path/to/apk"
        val expectedStates = flowOf(
            DownloadState.Idle,
            DownloadState.Downloading(50),
            DownloadState.Success(destination)
        )
        every { repository.downloadApk(url, destination) } returns expectedStates

        // Act
        val flow = downloadApkUseCase(url, destination)

        // Assert
        flow.test {
            assertEquals(DownloadState.Idle, awaitItem())
            assertEquals(DownloadState.Downloading(50), awaitItem())
            assertEquals(DownloadState.Success(destination), awaitItem())
            awaitComplete()
        }
    }
}
