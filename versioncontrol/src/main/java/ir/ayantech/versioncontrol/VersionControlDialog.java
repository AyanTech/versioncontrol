package ir.ayantech.versioncontrol;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.coolerfall.download.DownloadCallback;
import com.coolerfall.download.DownloadManager;
import com.coolerfall.download.DownloadRequest;
import com.coolerfall.download.OkHttpDownloader;
import com.coolerfall.download.Priority;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import ir.ayantech.versioncontrol.api.CheckVersion;
import ir.ayantech.versioncontrol.api.GetLastVersion;

public class VersionControlDialog extends Dialog {

    private int id = -1;
    private DownloadManager manager;

    public VersionControlDialog(@NonNull final Activity activity,
                                String title,
                                String message,
                                String positiveButton,
                                String negativeButton,
                                ArrayList<String> changeLogs,
                                final String linkType,
                                final String link,
                                final String updateStatus,
                                final Typeface typeface) {
        super(activity);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_version_control);
        this.setCancelable(false);
        this.setCanceledOnTouchOutside(false);

        ((TextView) findViewById(R.id.titleTv)).setText(title);
        ((TextView) findViewById(R.id.messageTv)).setText(message);
        ((TextView) findViewById(R.id.positiveTv)).setText(positiveButton);
        ((TextView) findViewById(R.id.negativeTv)).setText(negativeButton);
        if (changeLogs == null)
            findViewById(R.id.changeLogTv).setVisibility(View.GONE);
        else if (changeLogs.isEmpty())
            findViewById(R.id.changeLogTv).setVisibility(View.GONE);
        else {
            StringBuilder changeLog = new StringBuilder();
            for (String s : changeLogs) {
                changeLog.append(s).append("\n");
            }
            ((TextView) findViewById(R.id.changeLogTv)).setText(changeLog);
        }
        manager = new DownloadManager.Builder().context(getContext())
                .downloader(OkHttpDownloader.create())
                .threadPoolSize(2)
                .build();
        findViewById(R.id.positiveTv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (GetLastVersion.LinkType.DIRECT.contentEquals(linkType)) {
                        if (getRootDirPath(getContext()) == null) {
                            openUrl(getContext(), link);
                            return;
                        }
                        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                        findViewById(R.id.progressTv).setVisibility(View.VISIBLE);
                        String destPath = getRootDirPath(getContext()) + "/newversion" + String.valueOf(new Date().getTime()) + ".apk";
                        DownloadRequest request = new DownloadRequest.Builder()
                                .url(link)
                                .retryTime(5)
                                .retryInterval(2, TimeUnit.SECONDS)
                                .progressInterval(100, TimeUnit.MILLISECONDS)
                                .priority(Priority.HIGH)
                                .destinationFilePath(destPath)
                                .downloadCallback(new DownloadCallback() {
                                    @Override
                                    public void onStart(int downloadId, long totalBytes) {
                                    }

                                    @Override
                                    public void onRetry(int downloadId) {
                                    }

                                    @Override
                                    public void onProgress(int downloadId, long bytesWritten, long totalBytes) {
                                        long progressPercent = bytesWritten * 100 / totalBytes;
                                        ((ProgressBar) findViewById(R.id.progressBar)).setProgress((int) progressPercent);
                                        ((TextView) findViewById(R.id.progressTv)).setText(String.format("%%%s", String.valueOf(progressPercent)));
                                    }

                                    @Override
                                    public void onSuccess(int downloadId, String filePath) {
                                        try {
                                            installApp(getContext(), filePath);
                                            dismiss();
                                            if (CheckVersion.UpdateStatus.MANDATORY.contentEquals(updateStatus)) {
                                                activity.finish();
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    @Override
                                    public void onFailure(int downloadId, int statusCode, String errMsg) {
                                        Log.e("AyanVC:", errMsg);
                                        dismiss();
                                        openUrl(getContext(), link);
                                    }
                                })
                                .build();

                        id = manager.add(request);
                    } else if (GetLastVersion.LinkType.PAGE.contentEquals(linkType)) {
                        dismiss();
                        openUrl(getContext(), link);
                        if (CheckVersion.UpdateStatus.MANDATORY.contentEquals(updateStatus))
                            activity.finish();
                    }
                } catch (Exception e) {
                    dismiss();
                    openUrl(getContext(), link);
                }
            }
        });
        findViewById(R.id.negativeTv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    manager.cancel(id);
                } catch (Exception e) {
                }
                dismiss();
                if (CheckVersion.UpdateStatus.MANDATORY.contentEquals(updateStatus))
                    activity.finish();
            }
        });

        if (typeface != null) {
            LinearLayout mainContentLl = findViewById(R.id.mainContentLl);
            for (int i = 0; i < mainContentLl.getChildCount(); i++) {
                if (mainContentLl.getChildAt(i) instanceof TextView) {
                    ((TextView) mainContentLl.getChildAt(i)).setTypeface(typeface);
                }
            }
            ((TextView) findViewById(R.id.positiveTv)).setTypeface(typeface);
            ((TextView) findViewById(R.id.negativeTv)).setTypeface(typeface);
        }
    }

    @Override
    public void onBackPressed() {
    }

    private void installApp(Context context, String path) {
        File toInstall = new File(path);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                Uri apkUri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", toInstall);
                Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
                intent.setData(apkUri);
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                context.startActivity(intent);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        } else {
            Uri apkUri = Uri.fromFile(toInstall);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(intent);
        }
    }

    private void openUrl(Context context, String url) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        context.startActivity(browserIntent);
    }

    private String getRootDirPath(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            return context.getFilesDir().getAbsolutePath();
        else if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()))
            return context.getExternalFilesDir(null).getAbsolutePath();
        else
            return null;
    }
}
