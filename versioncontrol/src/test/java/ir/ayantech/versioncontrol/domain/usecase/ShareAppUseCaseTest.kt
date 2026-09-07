package ir.ayantech.versioncontrol.domain.usecase

import io.mockk.coEvery
import io.mockk.mockk
import ir.ayantech.versioncontrol.VersionControlConfig
import ir.ayantech.versioncontrol.domain.model.UpdateInfo
import ir.ayantech.versioncontrol.domain.repository.VersionControlRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ShareAppUseCaseTest {

    private val repository: VersionControlRepository = mockk()
    private val shareAppUseCase = ShareAppUseCase(repository)
    private val config = VersionControlConfig(baseUrl = "https://example.com/api/", applicationName = "testApp")

    @Test
    fun `invoke returns textToShare when getLastVersion succeeds`() = runTest {
        // Arrange
        val updateInfo = UpdateInfo(textToShare = "Share this app!")
        coEvery { repository.getLastVersion(config) } returns Result.success(updateInfo)

        // Act
        val result = shareAppUseCase(config)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals("Share this app!", result.getOrNull())
    }

    @Test
    fun `invoke returns failure when textToShare is null`() = runTest {
        // Arrange
        val updateInfo = UpdateInfo(textToShare = null)
        coEvery { repository.getLastVersion(config) } returns Result.success(updateInfo)

        // Act
        val result = shareAppUseCase(config)

        // Assert
        assertTrue(result.isFailure)
    }
}
