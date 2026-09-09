package ir.ayantech.versioncontrol.data.remote.client

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.net.Proxy
import java.util.concurrent.TimeUnit

class VersionControlHttpClient(
    private val client: OkHttpClient = defaultOkHttpClient,
    private val gson: Gson = Gson()
) {

    suspend fun <T, R> post(
        baseUrl: String,
        endpoint: String,
        requestBody: T,
        responseClass: Class<R>
    ): R = withContext(Dispatchers.IO) {
        val fullUrl = buildUrl(baseUrl, endpoint)
        val json = gson.toJson(requestBody)
        val body = json.toRequestBody(JSON_MEDIA_TYPE)

        val request = Request.Builder()
            .url(fullUrl)
            .post(body)
            .build()

        val response = runCatching {
            client.newCall(request).execute()
        }.getOrElse { throwable ->
            throw IOException("Network error while connecting to $fullUrl", throwable)
        }

        response.use { resp ->
            if (!resp.isSuccessful) {
                throw IOException("HTTP Error ${resp.code}: ${resp.message}")
            }

            val bodyString = resp.body?.string()
                ?: throw IOException("Response body is null")

            try {
                gson.fromJson(bodyString, responseClass)
                    ?: throw IOException("Parsed response object is null")
            } catch (e: Exception) {
                if (e is IOException) throw e
                throw IOException("Failed to parse JSON response: ${e.localizedMessage}", e)
            }
        }
    }

    suspend inline fun <reified T : Any, reified R : Any> post(
        baseUrl: String,
        endpoint: String,
        requestBody: T
    ): R {
        return post(baseUrl, endpoint, requestBody, R::class.java)
    }

    private fun buildUrl(baseUrl: String, endpoint: String): String {
        val cleanBaseUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        val cleanEndpoint = endpoint.removePrefix("/")
        return "$cleanBaseUrl$cleanEndpoint"
    }

    companion object {
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

        val defaultOkHttpClient: OkHttpClient by lazy {
            OkHttpClient.Builder()
                .readTimeout(12, TimeUnit.SECONDS)
                .connectTimeout(12, TimeUnit.SECONDS)
                .proxy(Proxy.NO_PROXY)
                .retryOnConnectionFailure(false)
                .build()
        }
    }
}
