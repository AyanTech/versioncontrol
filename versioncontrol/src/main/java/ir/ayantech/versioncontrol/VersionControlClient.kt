package ir.ayantech.versioncontrol

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.Proxy
import java.util.concurrent.TimeUnit

object VersionControlClient {

    private var retrofit: Retrofit? = null
    private const val VERSION_CONTROL_BASE_URL = "https://versioncontrol.infra.ayantech.ir/WebServices/App.svc/"
    private var okHttpClient: OkHttpClient? = null

    @JvmStatic
    fun getOkHttpClient(): OkHttpClient {
        if (okHttpClient == null) {
            okHttpClient = OkHttpClient.Builder()
                .readTimeout(12, TimeUnit.SECONDS)
                .connectTimeout(12, TimeUnit.SECONDS)
                .proxy(Proxy.NO_PROXY)
                .retryOnConnectionFailure(false)
                .build()
        }
        return okHttpClient!!
    }

    @JvmStatic
    fun getClient(baseUrl: String?): Retrofit {
        if (retrofit == null) {
            retrofit = Retrofit.Builder()
                .client(getOkHttpClient())
                .baseUrl(baseUrl ?: VERSION_CONTROL_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!
    }
}
