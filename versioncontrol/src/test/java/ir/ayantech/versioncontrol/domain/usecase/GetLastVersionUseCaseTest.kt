package ir.ayantech.versioncontrol.domain.usecase

import io.mockk.coEvery
import io.mockk.mockk
import ir.ayantech.versioncontrol.VersionControlConfig
import ir.ayantech.versioncontrol.data.remote.dto.GetLastVersionResponseDto
import ir.ayantech.versioncontrol.data.remote.dto.VCStatusDto
import ir.ayantech.versioncontrol.domain.repository.VersionControlRepository
import ir.ayantech.versioncontrol.domain.usecase.impl.GetLastVersionUseCaseImpl
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetLastVersionUseCaseTest {

    private val repository: VersionControlRepository = mockk()
    private val getLastVersionUseCase: GetLastVersionUseCase = GetLastVersionUseCaseImpl(repository)
    private val config = VersionControlConfig(baseUrl = "https://example.com/api/", applicationName = "testApp")

    @Test
    fun `invoke returns UpdateInfo when repository call succeeds with valid status`() = runTest {
        // Arrange
        val response = GetLastVersionResponseDto(
            parameters = GetLastVersionResponseDto.Parameters(title = "New Version", body = "Improvements"),
            status = VCStatusDto("G00000", "Success")
        )
        coEvery { repository.getLastVersion(config.baseUrl, any()) } returns Result.success(response)

        // Act
        val result = getLastVersionUseCase(config)

        // Assert
        assertTrue(result.isSuccess)
        val updateInfo = result.getOrNull()
        assertEquals("New Version", updateInfo?.title)
        assertEquals("Improvements", updateInfo?.body)
    }

    @Test
    fun `invoke returns Failure when status code is error`() = runTest {
        // Arrange
        val response = GetLastVersionResponseDto(
            parameters = null,
            status = VCStatusDto("E10002", "Version not found")
        )
        coEvery { repository.getLastVersion(config.baseUrl, any()) } returns Result.success(response)

        // Act
        val result = getLastVersionUseCase(config)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Version not found", result.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke returns Failure when repository call fails`() = runTest {
        // Arrange
        val exception = IllegalStateException("Server Unavailable")
        coEvery { repository.getLastVersion(config.baseUrl, any()) } returns Result.failure(exception)

        // Act
        val result = getLastVersionUseCase(config)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Server Unavailable", result.exceptionOrNull()?.message)
    }
}
