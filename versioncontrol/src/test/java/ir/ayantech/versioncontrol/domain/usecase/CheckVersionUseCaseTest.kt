package ir.ayantech.versioncontrol.domain.usecase

import io.mockk.coEvery
import io.mockk.mockk
import ir.ayantech.versioncontrol.VersionControlConfig
import ir.ayantech.versioncontrol.domain.model.UpdateInfo
import ir.ayantech.versioncontrol.domain.model.UpdateStatus
import ir.ayantech.versioncontrol.domain.model.VersionCheckResult
import ir.ayantech.versioncontrol.domain.repository.VersionControlRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CheckVersionUseCaseTest {

    private val repository: VersionControlRepository = mockk()
    private val checkVersionUseCase = CheckVersionUseCase(repository)
    private val config = VersionControlConfig(baseUrl = "https://example.com/api/", applicationName = "testApp")

    @Test
    fun `invoke returns UpToDate when status is NOT_REQUIRED`() = runTest {
        // Arrange
        coEvery { repository.checkVersion(config) } returns Result.success(UpdateStatus.NOT_REQUIRED)

        // Act
        val result = checkVersionUseCase(config)

        // Assert
        assertEquals(VersionCheckResult.UpToDate, result)
    }

    @Test
    fun `invoke returns UpdateAvailable with update details when update is MANDATORY`() = runTest {
        // Arrange
        val expectedInfo = UpdateInfo(title = "Title", body = "Body")
        coEvery { repository.checkVersion(config) } returns Result.success(UpdateStatus.MANDATORY)
        coEvery { repository.getLastVersion(config) } returns Result.success(expectedInfo)

        // Act
        val result = checkVersionUseCase(config)

        // Assert
        assertTrue(result is VersionCheckResult.UpdateAvailable)
        val updateAvailable = result as VersionCheckResult.UpdateAvailable
        assertEquals(UpdateStatus.MANDATORY, updateAvailable.updateInfo.updateStatus)
        assertEquals("Title", updateAvailable.updateInfo.title)
    }

    @Test
    fun `invoke returns Failure when checkVersion API call fails`() = runTest {
        // Arrange
        coEvery { repository.checkVersion(config) } returns Result.failure(Exception("Network error"))

        // Act
        val result = checkVersionUseCase(config)

        // Assert
        assertTrue(result is VersionCheckResult.Failure)
        val failure = result as VersionCheckResult.Failure
        assertEquals("Network error", failure.message)
    }
}
