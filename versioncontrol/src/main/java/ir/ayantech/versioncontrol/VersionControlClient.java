package ir.ayantech.versioncontrol;

import java.net.Proxy;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by shadoWalker on 5/9/17.
 */

public class VersionControlClient {

    private static Retrofit retrofit = null;

    private static final String VERSION_CONTROL_BASE_URL = "http://versioncontrol.infra.ayantech.ir/WebServices/App.svc/";

    private static OkHttpClient okHttpClient;

    public static OkHttpClient getOkHttpClient() {
        if (okHttpClient == null) {
            okHttpClient = new OkHttpClient.Builder()
                    .readTimeout(12, TimeUnit.SECONDS)
                    .connectTimeout(12, TimeUnit.SECONDS)
                    .proxy(Proxy.NO_PROXY)
                    .retryOnConnectionFailure(false)
                    .build();
        }
        return okHttpClient;
    }

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .client(getOkHttpClient())
                    .baseUrl(VERSION_CONTROL_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
