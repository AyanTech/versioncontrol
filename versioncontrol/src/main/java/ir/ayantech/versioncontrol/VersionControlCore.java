package ir.ayantech.versioncontrol;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ShareCompat;
import android.util.Log;
import android.widget.Toast;

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

    private Context context;
    private String ApplicationName;
    private String ApplicationType;
    private String ApplicationVersion;
    private String CategoryName;
    private ExtraInfoModel ExtraInfo;

    public VersionControlCore(Context context) {
        this.context = context;
        initializeApplicationType();
        initializeApplicationName();
        initializeApplicationVersion(this.context);
        VersionControlAPIs.initialize();
    }

    private void initializeApplicationName() {
        try {
            this.ApplicationName = context.getPackageName().split("\\.")[2];
        } catch (Exception e) {
            Log.e("AyanVC:", "Package name is not well formatted.");
        }
    }

    private void initializeApplicationVersion(Context context) {
        this.setApplicationVersion(getApplicationVersion(context));
    }

    private void initializeApplicationType() {
        setApplicationType("android");
    }

    public static String getApplicationVersion(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
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

    public VersionControlCore setApplicationVersion(String applicationVersion) {
        ApplicationVersion = applicationVersion;
        return this;
    }

    public VersionControlCore setExtraInfo(ExtraInfoModel extraInfo) {
        ExtraInfo = extraInfo;
        return this;
    }

    public VersionControlCore checkForNewVersion() {
        VersionControlAPIs.checkVersion.callApi(this,
                new CheckVersion.CheckVersionInputModel(ApplicationName, ApplicationType, CategoryName, ApplicationVersion, ExtraInfo));
        return this;
    }

    @Override
    public void onSuccess(VersionControlAPI versionControlAPI, String message, @Nullable VCResponseModel responseModel) {
        if (versionControlAPI instanceof CheckVersion) {
            CheckVersion.CheckVersionResponse response = ((CheckVersion.CheckVersionResponse) responseModel);
            if (response.getParameters().getUpdateStatus().contentEquals(CheckVersion.UpdateStatus.NOT_REQUIRED))
                return;
            VersionControlAPIs.getLastVersion.callApi(this,
                    new GetLastVersion.GetLastVersionInputModel(ApplicationName, ApplicationType, CategoryName, getApplicationVersion(context), ExtraInfo));
        } else if (versionControlAPI instanceof GetLastVersion) {
            GetLastVersion.GetLastVersionResponseModel model = (GetLastVersion.GetLastVersionResponseModel) responseModel;
            Intent intent = new Intent(context, VersionControlActivity.class);
            intent.putExtra("title", model.getParameters().getTitle());
            intent.putExtra("message", model.getParameters().getBody());
            intent.putStringArrayListExtra("change_logs", model.getParameters().getChangeLogs());
            intent.putExtra("pos_btn", model.getParameters().getAcceptButtonText());
            intent.putExtra("neg_btn", model.getParameters().getRejectButtonText());
            intent.putExtra("update_status", VersionControlAPIs.checkVersion.getResponse().getParameters().getUpdateStatus());
            intent.putExtra("link_type", model.getParameters().getLinkType());
            intent.putExtra("link", model.getParameters().getLink());
            context.startActivity(intent);
        }
    }

    @Override
    public void onFail(VersionControlAPI versionControlAPI, String error, boolean canTry) {

    }

    public void shareApp() {
        if (VersionControlAPIs.getLastVersion.getResponse() != null) {
            share(context, VersionControlAPIs.getLastVersion.getResponse().getParameters().getTextToShare());
        } else {
            VersionControlAPIs.getLastVersion.callApi(new VCResponseStatus() {
                @Override
                public void onSuccess(VersionControlAPI versionControlAPI, String message, @Nullable VCResponseModel responseModel) {
                    share(context, VersionControlAPIs.getLastVersion.getResponse().getParameters().getTextToShare());
                }

                @Override
                public void onFail(VersionControlAPI versionControlAPI, String error, boolean canTry) {
                    Toast.makeText(context, "لطفا اتصال اینترنت خود را بررسی کرده و دوباره تلاش نمایید.", Toast.LENGTH_LONG).show();
                }
            }, new GetLastVersion.GetLastVersionInputModel(ApplicationName, ApplicationType, CategoryName, getApplicationVersion(context), ExtraInfo));
        }
    }

    private void share(Context context, String shareBody) {
        ShareCompat.IntentBuilder.from((Activity) context)
                .setText(shareBody)
                .setType("text/plain")
                .setChooserTitle("به اشتراک گذاری از طریق:")
                .startChooser();
    }
}
