package ir.ayantech.versioncontrol.api

import ir.ayantech.versioncontrol.CallVCApi
import ir.ayantech.versioncontrol.VersionControlClient
import ir.ayantech.versioncontrol.VersionControlInterface
import ir.ayantech.versioncontrol.model.VCInputModel
import ir.ayantech.versioncontrol.model.VCResponseModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

abstract class VersionControlAPI<Request : VCInputModel, ResponseModel : VCResponseModel> :
    VCReasonModel(), CallVCApi<Request> {

    companion object {
        private var apiService: VersionControlInterface? = null

        @JvmStatic
        fun getApiService(baseUrl: String?): VersionControlInterface {
            if (apiService == null) {
                apiService = VersionControlClient.getClient(baseUrl).create(VersionControlInterface::class.java)
            }
            return apiService!!
        }
    }

    var response: ResponseModel? = null
        private set

    var isRunning: Boolean = false
        private set

    private val wrappedRequests: MutableList<WrappedRequest> = ArrayList()

    open fun isCallSuccessful(errorCode: String?): Boolean {
        return errorCode == "G00000"
    }

    open fun showSuccessMessage(errorCode: String?): Boolean {
        return errorCode != VCErrorCode.RESULT_SUCCESS
    }

    override fun callApi(status: VCResponseStatus, inputModel: Request) {
        val wrappedRequest = WrappedRequest(status, inputModel)
        wrappedRequests.add(wrappedRequest)
        resumeCalls()
    }

    open fun resumeCalls() {
        if (wrappedRequests.isNotEmpty()) {
            if (!isRunning) {
                wrappedRequests[wrappedRequests.size - 1].call()
            }
        }
    }

    protected abstract fun getApi(inputModel: Request?): Call<ResponseModel>

    open fun cancelCall() {
        for (wrappedRequest in wrappedRequests) {
            wrappedRequest.responseModelCall.cancel()
        }
        wrappedRequests.clear()
    }

    inner class WrappedRequest(
        var responseStatus: VCResponseStatus,
        var inputModel: Request
    ) : Callback<ResponseModel> {

        var responseModelCall: Call<ResponseModel> = getApi(inputModel)

        fun call() {
            isRunning = true
            responseModelCall.clone().enqueue(this)
        }

        override fun onResponse(call: Call<ResponseModel>, response: Response<ResponseModel>) {
            try {
                if (wrappedRequests.isNotEmpty()) {
                    wrappedRequests.removeAt(wrappedRequests.size - 1)
                }
                isRunning = false
                this@VersionControlAPI.response = response.body()
                val currentResponse = this@VersionControlAPI.response
                if (currentResponse != null) {
                    handleCallback(currentResponse)
                    if (wrappedRequests.isNotEmpty()) {
                        resumeCalls()
                    }
                } else {
                    onFailure(call, Throwable("Response body is null"))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun onFailure(call: Call<ResponseModel>, t: Throwable) {
            isRunning = false
            handleError(this@VersionControlAPI, t, responseStatus)
        }

        private fun <P> handleCallback(response: P) {
            val responseModel = response as VCResponseModel
            val status = responseModel.status
            val code = status?.code
            val description = status?.description
            if (isCallSuccessful(code)) {
                if (showSuccessMessage(code)) {
                    responseStatus.onSuccess(this@VersionControlAPI, description, responseModel)
                } else {
                    responseStatus.onSuccess(this@VersionControlAPI, "", responseModel)
                }
            } else {
                responseStatus.onFail(this@VersionControlAPI, description, false)
            }
        }
    }
}
