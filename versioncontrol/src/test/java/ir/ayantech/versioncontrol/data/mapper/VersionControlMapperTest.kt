package ir.ayantech.versioncontrol.data.mapper

import ir.ayantech.versioncontrol.VersionControlConfig
import ir.ayantech.versioncontrol.data.remote.dto.CheckVersionResponseDto
import ir.ayantech.versioncontrol.data.remote.dto.GetLastVersionResponseDto
import ir.ayantech.versioncontrol.domain.model.LinkType
import ir.ayantech.versioncontrol.domain.model.UpdateStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VersionControlMapperTest {

    @Test
    fun `toCheckVersionInputDto creates correct DTO from config`() {
        // Arrange
        val config = VersionControlConfig(
            baseUrl = "https://example.com/api/",
            applicationName = "testApp",
            applicationType = "android",
            categoryName = "cafebazaar",
            applicationVersion = "1.0.0"
        )

        // Act
        val dto = config.toCheckVersionInputDto()

        // Assert
        assertEquals("testApp", dto.applicationName)
        assertEquals("android", dto.applicationType)
        assertEquals("cafebazaar", dto.categoryName)
        assertEquals("1.0.0", dto.currentApplicationVersion)
    }

    @Test
    fun `toUIModel maps raw CheckVersionResponseDto to UpdateStatus correctly`() {
        // Arrange
        val mandatoryDto = CheckVersionResponseDto(CheckVersionResponseDto.Parameters("Mandatory"), null)
        val optionalDto = CheckVersionResponseDto(CheckVersionResponseDto.Parameters("Optional"), null)
        val notRequiredDto = CheckVersionResponseDto(CheckVersionResponseDto.Parameters("NotRequired"), null)
        val invalidDto = CheckVersionResponseDto(CheckVersionResponseDto.Parameters("Unknown"), null)

        // Act
        val mandatoryStatus = mandatoryDto.toUIModel()
        val optionalStatus = optionalDto.toUIModel()
        val notRequiredStatus = notRequiredDto.toUIModel()
        val invalidStatus = invalidDto.toUIModel()

        // Assert
        assertEquals(UpdateStatus.MANDATORY, mandatoryStatus)
        assertEquals(UpdateStatus.OPTIONAL, optionalStatus)
        assertEquals(UpdateStatus.NOT_REQUIRED, notRequiredStatus)
        assertEquals(UpdateStatus.NOT_REQUIRED, invalidStatus)
    }

    @Test
    fun `toUIModel maps GetLastVersionResponseDto to UpdateInfo domain model`() {
        // Arrange
        val params = GetLastVersionResponseDto.Parameters(
            changeLogs = listOf("Bug fixes", "New features"),
            link = "https://example.com/app.apk",
            linkType = "direct",
            textToShare = "Download our app!",
            title = "New Update Available",
            body = "Please update to continue using the app",
            acceptButtonText = "Update",
            rejectButtonText = "Cancel"
        )
        val dto = GetLastVersionResponseDto(params, null)

        // Act
        val updateInfo = dto.toUIModel()

        // Assert
        assertEquals("New Update Available", updateInfo.title)
        assertEquals("Please update to continue using the app", updateInfo.body)
        assertEquals("Update", updateInfo.acceptButtonText)
        assertEquals("Cancel", updateInfo.rejectButtonText)
        assertEquals(2, updateInfo.changeLogs.size)
        assertEquals(LinkType.DIRECT, updateInfo.linkType)
        assertEquals("https://example.com/app.apk", updateInfo.link)
        assertEquals("Download our app!", updateInfo.textToShare)
    }

    @Test
    fun `isResponseSuccessful returns true for G00000 and s00000`() {
        // Arrange
        val codeSuccessG = "G00000"
        val codeSuccessS = "s00000"
        val codeError = "E12345"
        val codeNull: String? = null

        // Act
        val isSuccessG = codeSuccessG.isResponseSuccessful()
        val isSuccessS = codeSuccessS.isResponseSuccessful()
        val isError = codeError.isResponseSuccessful()
        val isNullSuccess = codeNull.isResponseSuccessful()

        // Assert
        assertTrue(isSuccessG)
        assertTrue(isSuccessS)
        assertFalse(isError)
        assertFalse(isNullSuccess)
    }
}
