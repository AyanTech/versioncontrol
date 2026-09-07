package ir.ayantech.versioncontrol.api

import com.google.gson.Gson
import ir.ayantech.versioncontrol.model.VCResponseModel
import retrofit2.Response
import java.io.IOException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeoutException

abstract class VCReasonModel {

    companion object {
        private const val NO_INTERNET = "دستگاه شما به اینترنت متصل نیست. لطفا بعد از بررسی دوباره تلاش نمایید."
        private const val NO_HOST = "ارتباط با سرور برقرار نشد. لطفا دوباره تلاش نمایید."
    }

    protected open fun handleError(
        offerAPI: VersionControlAPI<*, *>,
        t: Throwable?,
        status: VCResponseStatus
    ) {
        var callback = true
        var canTry = true
        val message: String
        when (t) {
            is UnknownHostException -> message = NO_INTERNET
            is TimeoutException, is SocketTimeoutException -> message = NO_HOST
            is SocketException -> {
                canTry = false
                message = ""
                callback = false
            }
            is IOException -> {
                canTry = false
                message = ""
                callback = true
            }
            else -> message = NO_HOST
        }
        if (callback) {
            status.onFail(offerAPI, message, canTry)
        }
    }

    open fun isCodeOk(code: Int): Boolean {
        return code == 200
    }

    open fun <T : VCResponseModel> convertJsonStringToObject(json: String?, objectClass: Class<T>): T {
        return Gson().fromJson(json, objectClass)
    }

    @Throws(IOException::class)
    open fun <T : VCResponseModel> handleResponse(response: Response<T>, tClass: Class<T>): T? {
        return if (isCodeOk(response.code())) {
            response.body()
        } else {
            val error = response.errorBody()?.string()
            convertJsonStringToObject(error, tClass)
        }
    }
}
