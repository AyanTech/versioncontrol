package ir.ayantech.versioncontrol.api

import com.google.gson.annotations.SerializedName
import ir.ayantech.versioncontrol.model.ExtraInfoModel
import ir.ayantech.versioncontrol.model.VCInputModel
import ir.ayantech.versioncontrol.model.VCRequestModel
import ir.ayantech.versioncontrol.model.VCResponseModel
import ir.ayantech.versioncontrol.model.VCStatusModel
import retrofit2.Call

class GetLastVersion(var baseUrl: String?) : VersionControlAPI<GetLastVersion.GetLastVersionInputModel, GetLastVersion.GetLastVersionResponseModel>() {

    override fun getApi(inputModel: GetLastVersionInputModel?): Call<GetLastVersionResponseModel> {
        return getApiService(baseUrl).getLastVersion(VCRequestModel(inputModel))
    }

    open class GetLastVersionInputModel(
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

    class GetLastVersionResponseModel(
        @SerializedName("Parameters")
        var parameters: GetLastVersionOutputModel? = null,
        status: VCStatusModel? = null
    ) : VCResponseModel(status)

    open class GetLastVersionOutputModel(
        @SerializedName("ChangeLogs")
        var changeLogs: ArrayList<String>? = null,
        @SerializedName("Link")
        var link: String? = null,
        @SerializedName("LinkType")
        var linkType: String? = null,
        @SerializedName("TextToShare")
        var textToShare: String? = null,
        @SerializedName("Title")
        var title: String? = null,
        @SerializedName("Body")
        var body: String? = null,
        @SerializedName("AcceptButtonText")
        var acceptButtonText: String? = null,
        @SerializedName("RejectButtonText")
        var rejectButtonText: String? = null
    )

    object LinkType {
        const val DIRECT = "direct"
        const val PAGE = "page"
    }
}
