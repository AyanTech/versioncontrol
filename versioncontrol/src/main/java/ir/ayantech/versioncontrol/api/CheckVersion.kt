package ir.ayantech.versioncontrol.api

import com.google.gson.annotations.SerializedName
import ir.ayantech.versioncontrol.model.ExtraInfoModel
import ir.ayantech.versioncontrol.model.VCInputModel
import ir.ayantech.versioncontrol.model.VCRequestModel
import ir.ayantech.versioncontrol.model.VCResponseModel
import ir.ayantech.versioncontrol.model.VCStatusModel
import retrofit2.Call

class CheckVersion(var baseUrl: String?) : VersionControlAPI<CheckVersion.CheckVersionInputModel, CheckVersion.CheckVersionResponse>() {

    override fun getApi(inputModel: CheckVersionInputModel?): Call<CheckVersionResponse> {
        return getApiService(baseUrl).checkVersion(VCRequestModel(inputModel))
    }

    open class CheckVersionInputModel(
        @SerializedName("ApplicationName")
        var applicationName: String? = null,
        @SerializedName("ApplicationType")
        var applicationType: String? = null,
        @SerializedName("CategoryName")
        var categoryName: String? = null,
        @SerializedName("CurrentApplicationVersion")
        var currentApplicationVersion: String? = null,
        @SerializedName("ExtraInfo")
        var extraInfo: ExtraInfoModel? = null
    ) : VCInputModel()

    class CheckVersionResponse(
        @SerializedName("Parameters")
        var parameters: CheckVersionOutputModel? = null,
        status: VCStatusModel? = null
    ) : VCResponseModel(status)

    open class CheckVersionOutputModel(
        @SerializedName("UpdateStatus")
        var updateStatus: String? = null
    )

    object UpdateStatus {
        const val NOT_REQUIRED = "NotRequired"
        const val OPTIONAL = "Optional"
        const val MANDATORY = "Mandatory"
    }
}
