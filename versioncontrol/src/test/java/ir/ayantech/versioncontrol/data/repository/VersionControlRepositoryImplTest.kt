package ir.ayantech.versioncontrol.data.repository

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import ir.ayantech.versioncontrol.data.downloader.ApkDownloader
import ir.ayantech.versioncontrol.data.remote.VersionControlRemoteDataSource
import ir.ayantech.versioncontrol.data.remote.dto.CheckVersionInputDto
import ir.ayantech.versioncontrol.data.remote.dto.CheckVersionResponseDto
import ir.ayantech.versioncontrol.data.remote.dto.EndpointDto
import ir.ayantech.versioncontrol.data.remote.dto.GetApplicationColocationConfigParametersDto
import ir.ayantech.versioncontrol.data.remote.dto.GetApplicationColocationConfigResponseDto
import ir.ayantech.versioncontrol.data.remote.dto.GetLastVersionInputDto
import ir.ayantech.versioncontrol.data.remote.dto.GetLastVersionResponseDto
import ir.ayantech.versioncontrol.data.remote.dto.VCStatusDto
import ir.ayantech.versioncontrol.domain.model.DownloadState
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

    private val baseUrl = "https://example.com/api/"
    private val iranBaseUrl = "https://iran.example.com/App.svc/"
    private val internationalBaseUrl = "https://international.example.com/App.svc/"

    @Test
    fun `checkVersion returns raw response DTO on remote success`() = runTest(testDispatcher) {
        // Arrange
        val inputDto = CheckVersionInputDto("testApp", "android", "cafebazaar", "1.0.0", null)
        val responseDto = CheckVersionResponseDto(
            parameters = CheckVersionResponseDto.Parameters("Mandatory"),
            status = VCStatusDto("G00000", "Success")
        )
        coEvery { remoteDataSource.checkVersion(baseUrl, any()) } returns responseDto

        // Act
        val result = repository.checkVersion(baseUrl, inputDto)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(responseDto, result.getOrNull())
    }

    @Test
    fun `checkVersion returns failure when remote dataSource throws IOException`() = runTest(testDispatcher) {
        // Arrange
        val inputDto = CheckVersionInputDto("testApp", "android", "cafebazaar", "1.0.0", null)
        coEvery { remoteDataSource.checkVersion(baseUrl, any()) } throws IOException("HTTP Error 500")

        // Act
        val result = repository.checkVersion(baseUrl, inputDto)

        // Assert
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IOException)
    }

    @Test
    fun `getLastVersion returns raw response DTO on remote success`() = runTest(testDispatcher) {
        // Arrange
        val inputDto = GetLastVersionInputDto("testApp", "android", "cafebazaar", "1.0.0", null)
        val responseDto = GetLastVersionResponseDto(
            parameters = GetLastVersionResponseDto.Parameters(
                title = "Title",
                body = "Body",
                textToShare = "Share Text"
            ),
            status = VCStatusDto("G00000", "Success")
        )
        coEvery { remoteDataSource.getLastVersion(baseUrl, any()) } returns responseDto

        // Act
        val result = repository.getLastVersion(baseUrl, inputDto)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(responseDto, result.getOrNull())
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

    @Test
    fun `getApplicationColocationConfig tries Iran1 first and returns response DTO`() = runTest(testDispatcher) {
        // Arrange
        val responseDto = GetApplicationColocationConfigResponseDto(
            parameters = GetApplicationColocationConfigParametersDto(
                endpointList = listOf(
                    EndpointDto(name = "VersionControl", url = "https://iran.example.com/vc/")
                )
            ),
            status = VCStatusDto("G00000", "Success")
        )
        coEvery {
            remoteDataSource.getApplicationColocationConfig(
                baseUrl = iranBaseUrl,
                request = match { it.parameters?.colocationType == "Iran1" }
            )
        } returns responseDto

        // Act
        val result = repository.getApplicationColocationConfig(
            "testApp",
            "android",
            "1.0.0",
            iranBaseUrl,
            internationalBaseUrl
        )

        // Assert
        assertTrue(result.isSuccess)
        val response = result.getOrNull()!!
        assertEquals(responseDto, response)
        coVerify(exactly = 0) {
            remoteDataSource.getApplicationColocationConfig(
                baseUrl = internationalBaseUrl,
                request = any()
            )
        }
    }

    @Test
    fun `getApplicationColocationConfig uses custom Iran and International base URLs when provided`() = runTest(testDispatcher) {
        // Arrange
        val customIranUrl = "https://custom-iran.example.com/App.svc/"
        val customIntlUrl = "https://custom-intl.example.com/App.svc/"
        val responseDto = GetApplicationColocationConfigResponseDto(
            parameters = GetApplicationColocationConfigParametersDto(
                endpointList = listOf(
                    EndpointDto(name = "VersionControl", url = customIranUrl)
                )
            ),
            status = VCStatusDto("G00000", "Success")
        )
        coEvery {
            remoteDataSource.getApplicationColocationConfig(
                baseUrl = customIranUrl,
                request = match { it.parameters?.colocationType == "Iran1" }
            )
        } returns responseDto

        // Act
        val result = repository.getApplicationColocationConfig(
            applicationName = "testApp",
            applicationType = "android",
            applicationVersion = "1.0.0",
            iranBaseUrl = customIranUrl,
            internationalBaseUrl = customIntlUrl
        )

        // Assert
        assertTrue(result.isSuccess)
        val response = result.getOrNull()!!
        assertEquals(responseDto, response)
    }

    @Test
    fun `getApplicationColocationConfig falls back to International if Iran returns G00001`() = runTest(testDispatcher) {
        // Arrange
        val iranResponseDto = GetApplicationColocationConfigResponseDto(
            parameters = null,
            status = VCStatusDto(
                "G00001",
                "متاسفانه مشکلی پیش آمده است.  لطفا لحظاتی دیگر مجددا تلاش کنید."
            )
        )
        coEvery {
            remoteDataSource.getApplicationColocationConfig(
                baseUrl = iranBaseUrl,
                request = any()
            )
        } returns iranResponseDto

        val intlResponseDto = GetApplicationColocationConfigResponseDto(
            parameters = GetApplicationColocationConfigParametersDto(
                endpointList = listOf(
                    EndpointDto(name = "VersionControl", url = "https://example.com/xyz/")
                )
            ),
            status = VCStatusDto("G00000", "Success")
        )
        coEvery {
            remoteDataSource.getApplicationColocationConfig(
                baseUrl = internationalBaseUrl,
                request = match { it.parameters?.colocationType == "International" }
            )
        } returns intlResponseDto

        // Act
        val result = repository.getApplicationColocationConfig(
            "testApp",
            "android",
            "1.0.0",
            iranBaseUrl,
            internationalBaseUrl
        )

        // Assert
        assertTrue(result.isSuccess)
        val response = result.getOrNull()!!
        assertEquals(intlResponseDto, response)
        coVerifyOrder {
            remoteDataSource.getApplicationColocationConfig(
                baseUrl = iranBaseUrl,
                request = any()
            )
            remoteDataSource.getApplicationColocationConfig(
                baseUrl = internationalBaseUrl,
                request = any()
            )
        }
        coVerify(exactly = 1) {
            remoteDataSource.getApplicationColocationConfig(
                baseUrl = iranBaseUrl,
                request = any()
            )
        }
        coVerify(exactly = 1) {
            remoteDataSource.getApplicationColocationConfig(
                baseUrl = internationalBaseUrl,
                request = any()
            )
        }
    }

    @Test
    fun `getApplicationColocationConfig falls back to International if Iran throws exception`() = runTest(testDispatcher) {
        // Arrange
        coEvery {
            remoteDataSource.getApplicationColocationConfig(
                baseUrl = iranBaseUrl,
                request = any()
            )
        } throws IOException("Iran unavailable")

        val responseDtoInt = GetApplicationColocationConfigResponseDto(
            parameters = GetApplicationColocationConfigParametersDto(
                endpointList = listOf(
                    EndpointDto(name = "VersionControl", url = "https://international.example.com/vc/")
                )
            ),
            status = VCStatusDto("G00000", "Success")
        )

        coEvery {
            remoteDataSource.getApplicationColocationConfig(
                baseUrl = internationalBaseUrl,
                request = match { it.parameters?.colocationType == "International" }
            )
        } returns responseDtoInt

        // Act
        val result = repository.getApplicationColocationConfig(
            "testApp",
            "android",
            "1.0.0",
            iranBaseUrl,
            internationalBaseUrl
        )

        // Assert
        assertTrue(result.isSuccess)
        val response = result.getOrNull()!!
        assertEquals(responseDtoInt, response)
    }

    @Test
    fun `getApplicationColocationConfig passes error from International when International returns non-G00000 response`() = runTest(testDispatcher) {
        // Arrange
        coEvery {
            remoteDataSource.getApplicationColocationConfig(
                baseUrl = iranBaseUrl,
                request = any()
            )
        } throws IOException("Iran unreachable")

        val intlErrorResponse = GetApplicationColocationConfigResponseDto(
            parameters = null,
            status = VCStatusDto("E99999", "International error description")
        )
        coEvery {
            remoteDataSource.getApplicationColocationConfig(
                baseUrl = internationalBaseUrl,
                request = any()
            )
        } returns intlErrorResponse

        // Act
        val result = repository.getApplicationColocationConfig(
            "testApp",
            "android",
            "1.0.0",
            iranBaseUrl,
            internationalBaseUrl
        )

        // Assert
        assertTrue(result.isFailure)
        assertEquals("International error description", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getApplicationColocationConfig passes exception from International when International throws exception`() = runTest(testDispatcher) {
        // Arrange
        coEvery {
            remoteDataSource.getApplicationColocationConfig(
                baseUrl = iranBaseUrl,
                request = any()
            )
        } throws IOException("Iran service error")

        coEvery {
            remoteDataSource.getApplicationColocationConfig(
                baseUrl = internationalBaseUrl,
                request = any()
            )
        } throws IOException("International service error")

        // Act
        val result = repository.getApplicationColocationConfig(
            "testApp",
            "android",
            "1.0.0",
            iranBaseUrl,
            internationalBaseUrl
        )

        // Assert
        assertTrue(result.isFailure)
        assertEquals("International service error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getApplicationColocationConfig rejects a missing Iran URL without making a request`() = runTest(testDispatcher) {
        val result = repository.getApplicationColocationConfig(
            "testApp",
            "android",
            "1.0.0",
            " ",
            internationalBaseUrl
        )

        assertTrue(result.isFailure)
        assertEquals(
            "Iran base URL must be provided by the application.",
            result.exceptionOrNull()?.message
        )
        coVerify(exactly = 0) {
            remoteDataSource.getApplicationColocationConfig(any(), any())
        }
    }

    @Test
    fun `getApplicationColocationConfig rejects a missing International URL without making a request`() = runTest(testDispatcher) {
        val result = repository.getApplicationColocationConfig(
            "testApp",
            "android",
            "1.0.0",
            iranBaseUrl,
            ""
        )

        assertTrue(result.isFailure)
        assertEquals(
            "International base URL must be provided by the application.",
            result.exceptionOrNull()?.message
        )
        coVerify(exactly = 0) {
            remoteDataSource.getApplicationColocationConfig(any(), any())
        }
    }
}
