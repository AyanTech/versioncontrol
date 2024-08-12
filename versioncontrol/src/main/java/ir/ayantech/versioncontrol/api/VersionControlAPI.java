package ir.ayantech.versioncontrol.api;

import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import ir.ayantech.versioncontrol.CallVCApi;
import ir.ayantech.versioncontrol.VersionControlClient;
import ir.ayantech.versioncontrol.VersionControlInterface;
import ir.ayantech.versioncontrol.model.VCInputModel;
import ir.ayantech.versioncontrol.model.VCRequestModel;
import ir.ayantech.versioncontrol.model.VCResponseModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Administrator on 6/10/2017.
 */

public abstract class VersionControlAPI<Request extends VCInputModel, ResponseModel extends VCResponseModel>
        extends VCReasonModel implements CallVCApi<Request> {

    private static VersionControlInterface apiService;
    private ResponseModel responseModel;
    private boolean isRunning;
    private List<WrappedRequest> wrappedRequests;

    public static VersionControlInterface getApiService(@Nullable String baseUrl) {
        if (apiService == null)
            apiService = VersionControlClient.getClient(baseUrl).create(VersionControlInterface.class);
        return apiService;
    }

    public VersionControlAPI() {
        this.wrappedRequests = new ArrayList<>();
    }

    public boolean isCallSuccessful(String errorCode) {
        return errorCode.contentEquals("G00000");
    }

    public boolean showSuccessMessage(String errorCode) {
        return !errorCode.contentEquals(VCErrorCode.RESULT_SUCCESS);
    }

    @Override
    public void callApi(VCResponseStatus status, Request inputModel) {
        WrappedRequest wrappedRequest = new WrappedRequest(status, inputModel);
        wrappedRequests.add(wrappedRequest);
        resumeCalls();
    }

    public void resumeCalls() {
        if (!wrappedRequests.isEmpty()) {
            if (!isRunning())
                wrappedRequests.get(wrappedRequests.size() - 1).call();
        }
    }

    protected abstract Call<ResponseModel> getApi(Request inputModel);

    public ResponseModel getResponse() {
        return responseModel;
    }

    public void cancelCall() {
        if (wrappedRequests == null)
            return;
        for (WrappedRequest wrappedRequest : wrappedRequests) {
            wrappedRequest.getResponseModelCall().cancel();
        }
        wrappedRequests.clear();
    }

    public boolean isRunning() {
        return isRunning;
    }

    public class WrappedRequest implements Callback<ResponseModel> {
        private VCResponseStatus responseStatus;
        private Request inputModel;
        private Call<ResponseModel> responseModelCall;

        public WrappedRequest(VCResponseStatus responseStatus, Request inputModel) {
            this.responseStatus = responseStatus;
            this.inputModel = inputModel;
            this.responseModelCall = getApi(inputModel);
        }

        public VCResponseStatus getResponseStatus() {
            return responseStatus;
        }

        public void setResponseStatus(VCResponseStatus responseStatus) {
            this.responseStatus = responseStatus;
        }

        public Request getInputModel() {
            return inputModel;
        }

        public void setInputModel(Request inputModel) {
            this.inputModel = inputModel;
        }

        public Call<ResponseModel> getResponseModelCall() {
            return responseModelCall;
        }

        public void setResponseModelCall(Call<ResponseModel> responseModelCall) {
            this.responseModelCall = responseModelCall;
        }

        public void call() {
            isRunning = true;
            getResponseModelCall().clone().enqueue(this);
        }

        @Override
        public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {
            try {
                wrappedRequests.remove(wrappedRequests.size() - 1);
                isRunning = false;
                responseModel = response.body();
                if (responseModel != null) {
                    handleCallback(responseModel);
                    if (!wrappedRequests.isEmpty())
                        resumeCalls();
                } else {
                    onFailure(call, null);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(Call<ResponseModel> call, Throwable t) {
            isRunning = false;
            handleError(VersionControlAPI.this, t, getResponseStatus());
        }

        private <P> void handleCallback(P response) {
            VCResponseModel responseModel = (VCResponseModel) response;
            if (isCallSuccessful(responseModel.getStatus().getCode())) {
                if (showSuccessMessage(responseModel.getStatus().getCode()))
                    getResponseStatus().onSuccess(VersionControlAPI.this, responseModel.getStatus().getDescription(), responseModel);
                else
                    getResponseStatus().onSuccess(VersionControlAPI.this, "", responseModel);
            } else {
                getResponseStatus().onFail(VersionControlAPI.this, responseModel.getStatus().getDescription(), false);
            }
        }
    }
}
