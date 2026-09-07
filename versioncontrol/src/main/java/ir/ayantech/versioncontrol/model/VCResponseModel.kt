package ir.ayantech.versioncontrol.model

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

open class VCResponseModel(
    @SerializedName("Status")
    var status: VCStatusModel? = null
) {
    override fun toString(): String {
        return Gson().toJson(this)
    }
}
