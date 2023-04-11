package ir.ayantech.versioncontrol;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ShareCompat;
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

public class VersionControlCore {

    private static VersionControlCore versionControlCoreInstance;

    private String ApplicationName = null;
    private String ApplicationType = null;
    private String ApplicationVersion = null;
    private String CategoryName = null;
    private ExtraInfoModel ExtraInfo = null;
    private Typeface typeface = null;

    public static VersionControlCore getInstance() {
        if (versionControlCoreInstance == null)
            versionControlCoreInstance = new VersionControlCore();
        return versionControlCoreInstance;
    }

    private VersionControlCore() {
        VersionControlAPIs.initialize();
    }

    private void initializeProperties(Context context) {
        initializeApplicationType();
        initializeApplicationName(context);
        initializeApplicationVersion(context);
    }

    private void initializeApplicationName(Context context) {
        if (ApplicationName != null)
            return;
        try {
            this.ApplicationName = context.getPackageName().split("\\.")[2];
        } catch (Exception e) {
        }
    }

    private void initializeApplicationVersion(Context context) {
        if (ApplicationVersion == null)
            this.setApplicationVersion(getApplicationVersion(context));
    }

    private void initializeApplicationType() {
        if (ApplicationType == null)
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

    public VersionControlCore setTypeface(Typeface typeface) {
        this.typeface = typeface;
        return this;
    }

    public void checkForNewVersion(final Activity activity) {
        initializeProperties(activity);
        VersionControlAPIs.checkVersion
                .callApi(new VCResponseStatus() {
                             @Override
                             public void onSuccess(VersionControlAPI versionControlAPI, String message, @Nullable VCResponseModel responseModel) {
                                 if (versionControlAPI instanceof CheckVersion) {
                                     CheckVersion.CheckVersionResponse response = ((CheckVersion.CheckVersionResponse) responseModel);
                                     if (response.getParameters().getUpdateStatus().contentEquals(CheckVersion.UpdateStatus.NOT_REQUIRED))
                                         return;
                                     VersionControlAPIs.getLastVersion.callApi(this,
                                             new GetLastVersion.GetLastVersionInputModel(ApplicationName, ApplicationType, CategoryName, getApplicationVersion(activity), ExtraInfo));
                                 } else if (versionControlAPI instanceof GetLastVersion) {
                                     GetLastVersion.GetLastVersionResponseModel model = (GetLastVersion.GetLastVersionResponseModel) responseModel;
                                     new VersionControlDialog(activity,
                                             model.getParameters().getTitle(),
                                             model.getParameters().getBody(),
                                             model.getParameters().getAcceptButtonText(),
                                             model.getParameters().getRejectButtonText(),
                                             model.getParameters().getChangeLogs(),
                                             model.getParameters().getLinkType(),
                                             model.getParameters().getLink(),
                                             VersionControlAPIs.checkVersion.getResponse().getParameters().getUpdateStatus(),
                                             typeface).show();
                                 }
                             }

                             @Override
                             public void onFail(VersionControlAPI versionControlAPI, String error, boolean canTry) {

                             }
                         },
                        new CheckVersion.CheckVersionInputModel(ApplicationName, ApplicationType, CategoryName, ApplicationVersion, ExtraInfo));
    }

    public void shareApp(final Context context) {
        initializeProperties(context);
        if (VersionControlAPIs.getLastVersion.getResponse() != null) {
            try {
                share(context, VersionControlAPIs.getLastVersion.getResponse().getParameters().getTextToShare());
            } catch (Exception e) {}
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
