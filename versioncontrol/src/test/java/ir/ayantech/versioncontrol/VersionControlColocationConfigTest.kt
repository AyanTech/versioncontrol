package ir.ayantech.versioncontrol

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import ir.ayantech.versioncontrol.data.remote.dto.EndpointDto
import ir.ayantech.versioncontrol.data.remote.dto.GetApplicationColocationConfigParametersDto
import ir.ayantech.versioncontrol.data.remote.dto.GetApplicationColocationConfigResponseDto
import ir.ayantech.versioncontrol.data.remote.dto.VCStatusDto
import ir.ayantech.versioncontrol.domain.repository.VersionControlRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VersionControlColocationConfigTest {

    private val repository: VersionControlRepository = mockk()
    private val versionControl = VersionControl(repository)
    private val iranBaseUrl = "https://custom-iran.example.com/App.svc/"
    private val internationalBaseUrl = "https://custom-international.example.com/App.svc/"
    private val response = GetApplicationColocationConfigResponseDto(
        parameters = GetApplicationColocationConfigParametersDto(
            endpointList = listOf(
                EndpointDto(
                    name = "VersionControl",
                    url = "https://vc.example.com/"
                )
            )
        ),
        status = VCStatusDto("G00000", "Success")
    )

    @Test
    fun `direct API forwards application origin and custom base URLs`() = runTest {
        stubRepository("direct-origin")

        val result = versionControl.getApplicationColocationConfig(
            applicationName = "testApp",
            applicationVersion = "1.0.0",
            iranBaseUrl = iranBaseUrl,
            internationalBaseUrl = internationalBaseUrl,
            applicationOrigin = "direct-origin"
        )

        assertTrue(result.isSuccess)
        assertEquals("https://vc.example.com/", result.getOrThrow().versionControlBaseUrl)
        verifyRepositoryCall("direct-origin")
    }

    @Test
    fun `config API forwards application origin and custom base URLs`() = runTest {
        stubRepository("config-origin")
        val config = VersionControlConfig(
            baseUrl = "https://unused.example.com/",
            applicationName = "testApp",
            applicationVersion = "1.0.0",
            iranBaseUrl = iranBaseUrl,
            internationalBaseUrl = internationalBaseUrl,
            applicationOrigin = "config-origin"
        )

        val result = versionControl.getApplicationColocationConfig(config)

        assertTrue(result.isSuccess)
        assertEquals("https://vc.example.com/", result.getOrThrow().versionControlBaseUrl)
        verifyRepositoryCall("config-origin")
    }

    @Test
    fun `direct API forwards an empty origin`() = runTest {
        stubRepository("")

        val result = versionControl.getApplicationColocationConfig(
            applicationName = "testApp",
            applicationType = "android",
            applicationVersion = "1.0.0",
            iranBaseUrl = iranBaseUrl,
            internationalBaseUrl = internationalBaseUrl,
            applicationOrigin = ""
        )

        assertTrue(result.isSuccess)
        verifyRepositoryCall("")
    }

    @Test
    fun `config API retains empty origin when omitted`() = runTest {
        stubRepository("")
        val config = VersionControlConfig(
            baseUrl = "https://unused.example.com/",
            applicationName = "testApp",
            applicationVersion = "1.0.0",
            iranBaseUrl = iranBaseUrl,
            internationalBaseUrl = internationalBaseUrl
        )

        val result = versionControl.getApplicationColocationConfig(config)

        assertTrue(result.isSuccess)
        verifyRepositoryCall("")
    }

    private fun stubRepository(applicationOrigin: String) {
        coEvery {
            repository.getApplicationColocationConfig(
                "testApp", "android", applicationOrigin, "1.0.0", iranBaseUrl, internationalBaseUrl
            )
        } returns Result.success(response)
    }

    private fun verifyRepositoryCall(applicationOrigin: String) {
        coVerify(exactly = 1) {
            repository.getApplicationColocationConfig(
                "testApp", "android", applicationOrigin, "1.0.0", iranBaseUrl, internationalBaseUrl
            )
        }
    }
}
