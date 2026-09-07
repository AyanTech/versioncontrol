package ir.ayantech.versioncontrol

import ir.ayantech.versioncontrol.api.CheckVersion
import ir.ayantech.versioncontrol.api.GetLastVersion
import ir.ayantech.versioncontrol.model.VCRequestModel
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface VersionControlInterface {
    @POST("checkVersion")
    fun checkVersion(@Body requestModel: VCRequestModel?): Call<CheckVersion.CheckVersionResponse>

    @POST("getLastVersion")
    fun getLastVersion(@Body requestModel: VCRequestModel?): Call<GetLastVersion.GetLastVersionResponseModel>
}
