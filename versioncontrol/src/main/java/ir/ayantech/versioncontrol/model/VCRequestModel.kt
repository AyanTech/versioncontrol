package ir.ayantech.versioncontrol.model

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

open class VCRequestModel(
    @SerializedName("Parameters")
    var parameters: VCInputModel? = null
) {
    override fun toString(): String {
        return Gson().toJson(this)
    }
}
