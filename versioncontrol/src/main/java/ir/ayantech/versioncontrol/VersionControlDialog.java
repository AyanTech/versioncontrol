package ir.ayantech.versioncontrol;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.coolerfall.download.DownloadCallback;
import com.coolerfall.download.DownloadManager;
import com.coolerfall.download.DownloadRequest;
import com.coolerfall.download.OkHttpDownloader;
import com.coolerfall.download.Priority;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import ir.ayantech.versioncontrol.api.CheckVersion;
import ir.ayantech.versioncontrol.api.GetLastVersion;

public class VersionControlDialog extends Dialog {

    int id = -1;
    DownloadManager manager;

    public VersionControlDialog(@NonNull final Activity context,
                                String title,
                                String message,
                                List<String> changeLogs,
                                String positiveButtonText,
                                String negativeButtonText,
                                final String updateStatus,
                                final String linkType,
                                final String link) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_version_control);
        this.setCancelable(false);
        this.setCanceledOnTouchOutside(false);

        ((TextView) findViewById(R.id.titleTv)).setText(title);
        ((TextView) findViewById(R.id.messageTv)).setText(message);
        ((TextView) findViewById(R.id.positiveTv)).setText(positiveButtonText);
        ((TextView) findViewById(R.id.negativeTv)).setText(negativeButtonText);
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
        manager = new DownloadManager.Builder().context(context)
                .downloader(OkHttpDownloader.create())
                .threadPoolSize(2)
                .build();
        findViewById(R.id.positiveTv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (GetLastVersion.LinkType.DIRECT.contentEquals(linkType)) {
                    if (getRootDirPath(context) == null) {
                        openUrl(context, link);
                        return;
                    }
                    findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                    findViewById(R.id.progressTv).setVisibility(View.VISIBLE);
                    String destPath = getRootDirPath(context) + "/newversion" + String.valueOf(new Date().getTime()) + ".apk";
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
                                        installApp(context, filePath);
                                        if (CheckVersion.UpdateStatus.MANDATORY.contentEquals(updateStatus)) {
                                            dismiss();
                                            context.finish();
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void onFailure(int downloadId, int statusCode, String errMsg) {
                                }
                            })
                            .build();

                    id = manager.add(request);
                } else if (GetLastVersion.LinkType.PAGE.contentEquals(linkType)) {
                    dismiss();
                    openUrl(context, link);
                    if (CheckVersion.UpdateStatus.MANDATORY.contentEquals(updateStatus))
                        context.finish();
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
                    context.finish();
            }
        });
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
