package ir.ayantech.versioncontrol.api;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import ir.ayantech.versioncontrol.model.ExtraInfoModel;
import ir.ayantech.versioncontrol.model.VCInputModel;
import ir.ayantech.versioncontrol.model.VCRequestModel;
import ir.ayantech.versioncontrol.model.VCResponseModel;
import ir.ayantech.versioncontrol.model.VCStatusModel;
import retrofit2.Call;

/**
 * Created by Administrator on 11/5/2017.
 */

public class GetLastVersion extends VersionControlAPI<GetLastVersion.GetLastVersionInputModel, GetLastVersion.GetLastVersionResponseModel> {

    @Nullable
    String BaseUrl;

    public GetLastVersion(@Nullable String baseUrl) {
        this.BaseUrl = baseUrl;
    }

    @Override
    protected Call<GetLastVersionResponseModel> getApi(GetLastVersionInputModel inputModel) {
        return getApiService(BaseUrl).getLastVersion(new VCRequestModel(inputModel));
    }

    public static class GetLastVersionInputModel extends VCInputModel {
        private String ApplicationName;
        private String ApplicationType;
        private String CategoryName;
        private String CurrentApplicationVersion;
        private ExtraInfoModel ExtraInfo;

        public GetLastVersionInputModel(String applicationName, String applicationType, String categoryName, String currentApplicationVersion, ExtraInfoModel extraInfo) {
            ApplicationName = applicationName;
            ApplicationType = applicationType;
            CategoryName = categoryName;
            CurrentApplicationVersion = currentApplicationVersion;
            ExtraInfo = extraInfo;
        }
    }

    public class GetLastVersionResponseModel extends VCResponseModel {

        private GetLastVersionOutputModel Parameters;

        public GetLastVersionResponseModel(GetLastVersionOutputModel Parameters, VCStatusModel status) {
            super(status);
            this.Parameters = Parameters;
        }

        public GetLastVersionOutputModel getParameters() {
            return Parameters;
        }

        public void setParameters(GetLastVersionOutputModel parameters) {
            this.Parameters = parameters;
        }
    }

    public class GetLastVersionOutputModel {
        private ArrayList<String> ChangeLogs;
        private String Link;
        private String LinkType;
        private String TextToShare;
        private String Title;
        private String Body;
        private String AcceptButtonText;
        private String RejectButtonText;

        public ArrayList<String> getChangeLogs() {
            return ChangeLogs;
        }

        public void setChangeLogs(ArrayList<String> changeLogs) {
            ChangeLogs = changeLogs;
        }

        public String getLink() {
            return Link;
        }

        public void setLink(String link) {
            Link = link;
        }

        public String getLinkType() {
            return LinkType;
        }

        public void setLinkType(String linkType) {
            LinkType = linkType;
        }

        public String getTitle() {
            return Title;
        }

        public String getBody() {
            return Body;
        }

        public String getAcceptButtonText() {
            return AcceptButtonText;
        }

        public String getRejectButtonText() {
            return RejectButtonText;
        }

        public String getTextToShare() {
            return TextToShare;
        }

        public void setTextToShare(String textToShare) {
            TextToShare = textToShare;
        }
    }

    public class LinkType {
        public static final String DIRECT = "direct";
        public static final String PAGE = "page";
    }
}
