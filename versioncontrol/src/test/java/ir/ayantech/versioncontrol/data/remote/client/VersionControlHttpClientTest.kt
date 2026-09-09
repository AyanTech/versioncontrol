package ir.ayantech.versioncontrol.data.remote.client

import com.google.gson.annotations.SerializedName
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class VersionControlHttpClientTest {

    private val okHttpClient: OkHttpClient = mockk()
    private val httpClient = VersionControlHttpClient(client = okHttpClient)

    private data class DummyRequest(val name: String)
    private data class DummyResponse(@SerializedName("status") val status: String)

    @Test
    fun `post successfully deserializes response on HTTP 200`() = runTest {
        // Arrange
        val baseUrl = "https://example.com/api"
        val endpoint = "testEndpoint"
        val requestBody = DummyRequest("test")
        val responseJson = """{"status":"ok"}"""

        val call: Call = mockk()
        every { okHttpClient.newCall(any()) } returns call

        val fakeOkHttpResponse = Response.Builder()
            .request(Request.Builder().url("https://example.com/api/testEndpoint").build())
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(responseJson.toResponseBody("application/json".toMediaType()))
            .build()

        every { call.execute() } returns fakeOkHttpResponse

        // Act
        val result: DummyResponse = httpClient.post(baseUrl, endpoint, requestBody)

        // Assert
        assertEquals("ok", result.status)
    }

    @Test
    fun `post throws IOException on HTTP error`() = runTest {
        // Arrange
        val baseUrl = "https://example.com/api"
        val endpoint = "testEndpoint"
        val requestBody = DummyRequest("test")

        val call: Call = mockk()
        every { okHttpClient.newCall(any()) } returns call

        val fakeOkHttpResponse = Response.Builder()
            .request(Request.Builder().url("https://example.com/api/testEndpoint").build())
            .protocol(Protocol.HTTP_1_1)
            .code(500)
            .message("Internal Server Error")
            .body("Internal Error".toResponseBody("text/plain".toMediaType()))
            .build()

        every { call.execute() } returns fakeOkHttpResponse

        // Act & Assert
        runCatching {
            httpClient.post<DummyRequest, DummyResponse>(baseUrl, endpoint, requestBody)
        }.onSuccess {
            assertTrue("Expected exception was not thrown", false)
        }.onFailure { throwable ->
            assertTrue(throwable is IOException)
            assertTrue(throwable.message?.contains("HTTP Error 500") == true)
        }
    }
}
