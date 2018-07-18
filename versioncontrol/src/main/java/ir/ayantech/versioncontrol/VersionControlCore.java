package ir.ayantech.versioncontrol;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.annotation.Nullable;
import android.util.Log;

import ir.ayantech.versioncontrol.api.CheckVersion;
import ir.ayantech.versioncontrol.api.GetLastVersion;
import ir.ayantech.versioncontrol.api.VCResponseStatus;
import ir.ayantech.versioncontrol.api.VersionControlAPI;
import ir.ayantech.versioncontrol.api.VersionControlAPIs;
import ir.ayantech.versioncontrol.model.ExtraInfoModel;
import ir.ayantech.versioncontrol.model.VCResponseModel;

/**
 * Created by Administrator on 11/5/2017.
 */

public class VersionControlCore implements VCResponseStatus {

    private Activity activity;
    private String ApplicationName;
    private String ApplicationType;
    private String CategoryName;
    private ExtraInfoModel ExtraInfo;

    public VersionControlCore(Activity activity) {
        this.activity = activity;
        initializeApplicationType();
        initializeApplicationName();
        VersionControlAPIs.initialize();
    }

    private void initializeApplicationName() {
        try {
            this.ApplicationName = activity.getPackageName().split("\\.")[2];
        } catch (Exception e) {
            Log.e("AyanVC:", "Package name is not well formatted.");
        }
    }

    private void initializeApplicationType() {
        setApplicationType("android");
    }

    public String getApplicationVersion() {
        try {
            return activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    public VersionControlCore setApplicationName(String applicationName) {
        ApplicationName = applicationName;
        return this;
    }

    public VersionControlCore setApplicationType(String applicationType) {
        ApplicationType = applicationType;
        return this;
    }

    public VersionControlCore setCategoryName(String categoryName) {
        CategoryName = categoryName;
        return this;
    }

    public VersionControlCore setExtraInfo(ExtraInfoModel extraInfo) {
        ExtraInfo = extraInfo;
        return this;
    }

    public void checkForNewVersion() {
        VersionControlAPIs.checkVersion.callApi(this,
                new CheckVersion.CheckVersionInputModel(ApplicationName, ApplicationType, CategoryName, getApplicationVersion(), ExtraInfo));
    }

    @Override
    public void onSuccess(VersionControlAPI versionControlAPI, String message, @Nullable VCResponseModel responseModel) {
        if (versionControlAPI instanceof CheckVersion) {
            CheckVersion.CheckVersionResponse response = ((CheckVersion.CheckVersionResponse) responseModel);
            if (response.getParameters().getUpdateStatus().contentEquals(CheckVersion.UpdateStatus.NOT_REQUIRED))
                return;
            VersionControlAPIs.getLastVersion.callApi(this,
                    new GetLastVersion.GetLastVersionInputModel(ApplicationName, ApplicationType, CategoryName, getApplicationVersion(), ExtraInfo));
        } else if (versionControlAPI instanceof GetLastVersion) {
            GetLastVersion.GetLastVersionResponseModel model = (GetLastVersion.GetLastVersionResponseModel) responseModel;
            new VersionControlDialog(activity,
                    model.getParameters().getTitle(),
                    model.getParameters().getBody(),
                    model.getParameters().getChangeLogs(),
                    model.getParameters().getAcceptButtonText(),
                    model.getParameters().getRejectButtonText(),
                    VersionControlAPIs.checkVersion.getResponse().getParameters().getUpdateStatus(),
                    model.getParameters().getLinkType(),
                    model.getParameters().getLink()).show();
        }
    }

    @Override
    public void onFail(VersionControlAPI versionControlAPI, String error, boolean canTry) {

    }
}
