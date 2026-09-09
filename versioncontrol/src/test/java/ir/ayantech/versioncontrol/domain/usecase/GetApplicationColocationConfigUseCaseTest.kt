package ir.ayantech.versioncontrol.domain.usecase

import io.mockk.coEvery
import io.mockk.mockk
import ir.ayantech.versioncontrol.data.remote.dto.EndpointDto
import ir.ayantech.versioncontrol.data.remote.dto.GetApplicationColocationConfigParametersDto
import ir.ayantech.versioncontrol.data.remote.dto.GetApplicationColocationConfigResponseDto
import ir.ayantech.versioncontrol.data.remote.dto.VCStatusDto
import ir.ayantech.versioncontrol.domain.repository.VersionControlRepository
import ir.ayantech.versioncontrol.domain.usecase.impl.GetApplicationColocationConfigUseCaseImpl
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetApplicationColocationConfigUseCaseTest {

    private val repository: VersionControlRepository = mockk()
    private val useCase: GetApplicationColocationConfigUseCase =
        GetApplicationColocationConfigUseCaseImpl(repository)
    private val iranBaseUrl = "https://iran.example.com/App.svc/"
    private val internationalBaseUrl = "https://international.example.com/App.svc/"

    @Test
    fun `invoke returns mapped ColocationConfigResult when repository succeeds`() = runTest {
        // Arrange
        val response = GetApplicationColocationConfigResponseDto(
            parameters = GetApplicationColocationConfigParametersDto(
                endpointList = listOf(
                    EndpointDto(name = "VersionControl", url = "https://vc.example.com/"),
                    EndpointDto(name = "CoreApi", url = "https://core.example.com/")
                )
            ),
            status = VCStatusDto("G00000", "Success")
        )
        coEvery {
            repository.getApplicationColocationConfig(
                "testApp",
                "android",
                "1.0.0",
                iranBaseUrl,
                internationalBaseUrl
            )
        } returns Result.success(response)

        // Act
        val result = useCase(
            "testApp",
            "android",
            "1.0.0",
            iranBaseUrl,
            internationalBaseUrl
        )

        // Assert
        assertTrue(result.isSuccess)
        val configResult = result.getOrNull()!!
        assertEquals("https://vc.example.com/", configResult.versionControlBaseUrl)
        assertEquals(2, configResult.endpointList.size)
    }

    @Test
    fun `invoke passes custom base URLs to repository`() = runTest {
        // Arrange
        val customIran = "https://custom-iran.example.com/"
        val customIntl = "https://custom-intl.example.com/"
        val response = GetApplicationColocationConfigResponseDto(
            parameters = GetApplicationColocationConfigParametersDto(
                endpointList = listOf(
                    EndpointDto(name = "VersionControl", url = "https://vc.example.com/")
                )
            ),
            status = VCStatusDto("G00000", "Success")
        )
        coEvery {
            repository.getApplicationColocationConfig("testApp", "android", "1.0.0", customIran, customIntl)
        } returns Result.success(response)

        // Act
        val result = useCase(
            applicationName = "testApp",
            applicationType = "android",
            applicationVersion = "1.0.0",
            iranBaseUrl = customIran,
            internationalBaseUrl = customIntl
        )

        // Assert
        assertTrue(result.isSuccess)
        val configResult = result.getOrNull()!!
        assertEquals("https://vc.example.com/", configResult.versionControlBaseUrl)
    }

    @Test
    fun `invoke returns failure when response status code is not successful`() = runTest {
        // Arrange
        val response = GetApplicationColocationConfigResponseDto(
            parameters = null,
            status = VCStatusDto("E50000", "Service Error")
        )
        coEvery {
            repository.getApplicationColocationConfig(
                "testApp",
                "android",
                "1.0.0",
                iranBaseUrl,
                internationalBaseUrl
            )
        } returns Result.success(response)

        // Act
        val result = useCase(
            "testApp",
            "android",
            "1.0.0",
            iranBaseUrl,
            internationalBaseUrl
        )

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Service Error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke returns failure when endpointList is empty`() = runTest {
        // Arrange
        val response = GetApplicationColocationConfigResponseDto(
            parameters = GetApplicationColocationConfigParametersDto(endpointList = emptyList()),
            status = VCStatusDto("G00000", "Success")
        )
        coEvery {
            repository.getApplicationColocationConfig(
                "testApp",
                "android",
                "1.0.0",
                iranBaseUrl,
                internationalBaseUrl
            )
        } returns Result.success(response)

        // Act
        val result = useCase(
            "testApp",
            "android",
            "1.0.0",
            iranBaseUrl,
            internationalBaseUrl
        )

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Empty endpoint list", result.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke returns failure when repository call fails`() = runTest {
        // Arrange
        val exception = IllegalStateException("Failed to connect")
        coEvery {
            repository.getApplicationColocationConfig(
                "testApp",
                "android",
                "1.0.0",
                iranBaseUrl,
                internationalBaseUrl
            )
        } returns Result.failure(exception)

        // Act
        val result = useCase(
            "testApp",
            "android",
            "1.0.0",
            iranBaseUrl,
            internationalBaseUrl
        )

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Failed to connect", result.exceptionOrNull()?.message)
    }
}
