package ir.ayantech.versioncontrol.domain.usecase

import io.mockk.coEvery
import io.mockk.mockk
import ir.ayantech.versioncontrol.VersionControlConfig
import ir.ayantech.versioncontrol.data.remote.dto.GetLastVersionResponseDto
import ir.ayantech.versioncontrol.data.remote.dto.VCStatusDto
import ir.ayantech.versioncontrol.domain.repository.VersionControlRepository
import ir.ayantech.versioncontrol.domain.usecase.impl.ShareAppUseCaseImpl
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ShareAppUseCaseTest {

    private val repository: VersionControlRepository = mockk()
    private val shareAppUseCase: ShareAppUseCase = ShareAppUseCaseImpl(repository)
    private val config = VersionControlConfig(baseUrl = "https://example.com/api/", applicationName = "testApp")

    @Test
    fun `invoke returns textToShare when getLastVersion succeeds`() = runTest {
        // Arrange
        val response = GetLastVersionResponseDto(
            parameters = GetLastVersionResponseDto.Parameters(textToShare = "Share this app!"),
            status = VCStatusDto("G00000", "Success")
        )
        coEvery { repository.getLastVersion(config.baseUrl, any()) } returns Result.success(response)

        // Act
        val result = shareAppUseCase(config)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals("Share this app!", result.getOrNull())
    }

    @Test
    fun `invoke returns failure when textToShare is null`() = runTest {
        // Arrange
        val response = GetLastVersionResponseDto(
            parameters = GetLastVersionResponseDto.Parameters(textToShare = null),
            status = VCStatusDto("G00000", "Success")
        )
        coEvery { repository.getLastVersion(config.baseUrl, any()) } returns Result.success(response)

        // Act
        val result = shareAppUseCase(config)

        // Assert
        assertTrue(result.isFailure)
    }
}
