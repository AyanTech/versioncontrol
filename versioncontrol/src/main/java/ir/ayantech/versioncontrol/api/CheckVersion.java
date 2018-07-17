package ir.ayantech.versioncontrol.api;

import ir.ayantech.versioncontrol.model.ExtraInfoModel;
import ir.ayantech.versioncontrol.model.VCInputModel;
import ir.ayantech.versioncontrol.model.VCRequestModel;
import ir.ayantech.versioncontrol.model.VCResponseModel;
import ir.ayantech.versioncontrol.model.VCStatusModel;
import retrofit2.Call;

/**
 * Created by Administrator on 11/5/2017.
 */

public class CheckVersion extends VersionControlAPI<CheckVersion.CheckVersionInputModel, CheckVersion.CheckVersionResponse> {

    @Override
    protected Call<CheckVersionResponse> getApi(CheckVersionInputModel inputModel) {
        return getApiService().checkVersion(new VCRequestModel(inputModel));
    }


    public static class CheckVersionInputModel extends VCInputModel {
        private String ApplicationName;
        private String ApplicationType;
        private String CategoryName;
        private String CurrentApplicationVersion;
        private ExtraInfoModel ExtraInfo;

        public CheckVersionInputModel(String applicationName, String applicationType, String categoryName, String currentApplicationVersion, ExtraInfoModel extraInfo) {
            ApplicationName = applicationName;
            ApplicationType = applicationType;
            CategoryName = categoryName;
            CurrentApplicationVersion = currentApplicationVersion;
            ExtraInfo = extraInfo;
        }
    }

    public class CheckVersionResponse extends VCResponseModel {

        private CheckVersionOutputModel Parameters;

        public CheckVersionResponse(CheckVersionOutputModel Parameters, VCStatusModel status) {
            super(status);
            this.Parameters = Parameters;
        }

        public CheckVersionOutputModel getParameters() {
            return Parameters;
        }
    }

    public class CheckVersionOutputModel {
        private String UpdateStatus;

        public CheckVersionOutputModel(String updateStatus) {
            UpdateStatus = updateStatus;
        }

        public String getUpdateStatus() {
            return UpdateStatus;
        }

        public void setUpdateStatus(String updateStatus) {
            UpdateStatus = updateStatus;
        }
    }

    public static class UpdateStatus {
        public static final String NOT_REQUIRED = "NotRequired";
        public static final String OPTIONAL = "Optional";
        public static final String MANDATORY = "Mandatory";
    }
}
