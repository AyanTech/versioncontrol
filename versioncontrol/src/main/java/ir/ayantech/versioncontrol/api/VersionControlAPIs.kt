package ir.ayantech.versioncontrol.api

object VersionControlAPIs {
    @JvmField
    var checkVersion: CheckVersion? = null

    @JvmField
    var getLastVersion: GetLastVersion? = null

    @JvmStatic
    fun initialize(baseUrl: String?) {
        checkVersion = CheckVersion(baseUrl)
        getLastVersion = GetLastVersion(baseUrl)
    }
}
