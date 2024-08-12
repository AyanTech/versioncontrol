package ir.ayantech.versioncontrol.api;

import androidx.annotation.Nullable;

/**
 * Created by Administrator on 11/5/2017.
 */

public class VersionControlAPIs {
    public static CheckVersion checkVersion;
    public static GetLastVersion getLastVersion;

    public static void initialize(@Nullable String baseUrl) {
        checkVersion = new CheckVersion(baseUrl);
        getLastVersion = new GetLastVersion(baseUrl);
    }
}
