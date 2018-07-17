package ir.ayantech.versioncontrol.api;

/**
 * Created by Administrator on 11/5/2017.
 */

public class VersionControlAPIs {
    public static CheckVersion checkVersion;
    public static GetLastVersion getLastVersion;

    public static void initialize() {
        checkVersion = new CheckVersion();
        getLastVersion = new GetLastVersion();
    }
}
