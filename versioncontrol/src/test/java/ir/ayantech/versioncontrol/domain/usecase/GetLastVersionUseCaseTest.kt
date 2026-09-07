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

class GetLastVersionUseCaseTest {

    private val repository: VersionControlRepository = mockk()
    private val getLastVersionUseCase = GetLastVersionUseCase(repository)
    private val config = VersionControlConfig(baseUrl = "https://example.com/api/", applicationName = "testApp")

    @Test
    fun `invoke returns UpdateInfo when repository call succeeds`() = runTest {
        // Arrange
        val expectedInfo = UpdateInfo(title = "New Version", body = "Improvements")
        coEvery { repository.getLastVersion(config) } returns Result.success(expectedInfo)

        // Act
        val result = getLastVersionUseCase(config)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(expectedInfo, result.getOrNull())
    }

    @Test
    fun `invoke returns Failure when repository call fails`() = runTest {
        // Arrange
        val exception = IllegalStateException("Server Unavailable")
        coEvery { repository.getLastVersion(config) } returns Result.failure(exception)

        // Act
        val result = getLastVersionUseCase(config)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Server Unavailable", result.exceptionOrNull()?.message)
    }
}
