package ir.ayantech.versioncontrol.model

import com.google.gson.annotations.SerializedName

open class VCStatusModel(
    @SerializedName("Code")
    var code: String? = null,
    @SerializedName("Description")
    var description: String? = null
)
