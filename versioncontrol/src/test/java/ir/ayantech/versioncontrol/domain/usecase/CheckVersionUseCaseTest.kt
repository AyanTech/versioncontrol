package ir.ayantech.versioncontrol.domain.usecase

import io.mockk.coEvery
import io.mockk.mockk
import ir.ayantech.versioncontrol.VersionControlConfig
import ir.ayantech.versioncontrol.data.remote.dto.CheckVersionResponseDto
import ir.ayantech.versioncontrol.data.remote.dto.GetLastVersionResponseDto
import ir.ayantech.versioncontrol.data.remote.dto.VCStatusDto
import ir.ayantech.versioncontrol.domain.model.UpdateStatus
import ir.ayantech.versioncontrol.domain.model.VersionCheckResult
import ir.ayantech.versioncontrol.domain.repository.VersionControlRepository
import ir.ayantech.versioncontrol.domain.usecase.impl.CheckVersionUseCaseImpl
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CheckVersionUseCaseTest {

    private val repository: VersionControlRepository = mockk()
    private val checkVersionUseCase: CheckVersionUseCase = CheckVersionUseCaseImpl(repository)
    private val config = VersionControlConfig(baseUrl = "https://example.com/api/", applicationName = "testApp")

    @Test
    fun `invoke returns UpToDate when status is NOT_REQUIRED`() = runTest {
        // Arrange
        val response = CheckVersionResponseDto(
            parameters = CheckVersionResponseDto.Parameters("NotRequired"),
            status = VCStatusDto("G00000", "Success")
        )
        coEvery { repository.checkVersion(config.baseUrl, any()) } returns Result.success(response)

        // Act
        val result = checkVersionUseCase(config)

        // Assert
        assertEquals(VersionCheckResult.UpToDate, result)
    }

    @Test
    fun `invoke returns UpdateAvailable with update details when update is MANDATORY`() = runTest {
        // Arrange
        val checkResponse = CheckVersionResponseDto(
            parameters = CheckVersionResponseDto.Parameters("Mandatory"),
            status = VCStatusDto("G00000", "Success")
        )
        val lastVersionResponse = GetLastVersionResponseDto(
            parameters = GetLastVersionResponseDto.Parameters(title = "Title", body = "Body"),
            status = VCStatusDto("G00000", "Success")
        )

        coEvery { repository.checkVersion(config.baseUrl, any()) } returns Result.success(checkResponse)
        coEvery { repository.getLastVersion(config.baseUrl, any()) } returns Result.success(lastVersionResponse)

        // Act
        val result = checkVersionUseCase(config)

        // Assert
        assertTrue(result is VersionCheckResult.UpdateAvailable)
        val updateAvailable = result as VersionCheckResult.UpdateAvailable
        assertEquals(UpdateStatus.MANDATORY, updateAvailable.updateInfo.updateStatus)
        assertEquals("Title", updateAvailable.updateInfo.title)
    }

    @Test
    fun `invoke returns Failure when status code indicates error`() = runTest {
        // Arrange
        val errorResponse = CheckVersionResponseDto(
            parameters = null,
            status = VCStatusDto("E10001", "Invalid application name")
        )
        coEvery { repository.checkVersion(config.baseUrl, any()) } returns Result.success(errorResponse)

        // Act
        val result = checkVersionUseCase(config)

        // Assert
        assertTrue(result is VersionCheckResult.Failure)
        val failure = result as VersionCheckResult.Failure
        assertEquals("Invalid application name", failure.message)
    }

    @Test
    fun `invoke returns Failure when checkVersion API call fails`() = runTest {
        // Arrange
        coEvery { repository.checkVersion(config.baseUrl, any()) } returns Result.failure(Exception("Network error"))

        // Act
        val result = checkVersionUseCase(config)

        // Assert
        assertTrue(result is VersionCheckResult.Failure)
        val failure = result as VersionCheckResult.Failure
        assertEquals("Network error", failure.message)
    }
}
