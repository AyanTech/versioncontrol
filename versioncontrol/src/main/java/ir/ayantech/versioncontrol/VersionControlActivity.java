package ir.ayantech.versioncontrol;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
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

public class VersionControlActivity extends AppCompatActivity {

    int id = -1;
    DownloadManager manager;

    @Override
    public void onBackPressed() {
    }

    private String getVCTitle() {
        return getIntent().getStringExtra("title");
    }

    private String getVCMessage() {
        return getIntent().getStringExtra("message");
    }

    private String getVCPositiveButton() {
        return getIntent().getStringExtra("pos_btn");
    }

    private String getVCNegativeButton() {
        return getIntent().getStringExtra("neg_btn");
    }

    private String getVCLinkType() {
        return getIntent().getStringExtra("link_type");
    }

    private String getVCLink() {
        return getIntent().getStringExtra("link");
    }

    private String getVCUpdateStatus() {
        return getIntent().getStringExtra("update_status");
    }

    private List<String> getVCChangeLogs() {
        return getIntent().getStringArrayListExtra("change_logs");
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_version_control);
        this.setFinishOnTouchOutside(false);

        ((TextView) findViewById(R.id.titleTv)).setText(getVCTitle());
        ((TextView) findViewById(R.id.messageTv)).setText(getVCMessage());
        ((TextView) findViewById(R.id.positiveTv)).setText(getVCPositiveButton());
        ((TextView) findViewById(R.id.negativeTv)).setText(getVCNegativeButton());
        if (getVCChangeLogs() == null)
            findViewById(R.id.changeLogTv).setVisibility(View.GONE);
        else if (getVCChangeLogs().isEmpty())
            findViewById(R.id.changeLogTv).setVisibility(View.GONE);
        else {
            StringBuilder changeLog = new StringBuilder();
            for (String s : getVCChangeLogs()) {
                changeLog.append(s).append("\n");
            }
            ((TextView) findViewById(R.id.changeLogTv)).setText(changeLog);
        }
        manager = new DownloadManager.Builder().context(this)
                .downloader(OkHttpDownloader.create())
                .threadPoolSize(2)
                .build();
        findViewById(R.id.positiveTv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (GetLastVersion.LinkType.DIRECT.contentEquals(getVCLinkType())) {
                        if (getRootDirPath(VersionControlActivity.this) == null) {
                            openUrl(VersionControlActivity.this, getVCLink());
                            return;
                        }
                        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                        findViewById(R.id.progressTv).setVisibility(View.VISIBLE);
                        String destPath = getRootDirPath(VersionControlActivity.this) + "/newversion" + String.valueOf(new Date().getTime()) + ".apk";
                        DownloadRequest request = new DownloadRequest.Builder()
                                .url(getVCLink())
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
                                            installApp(VersionControlActivity.this, filePath);
                                            if (CheckVersion.UpdateStatus.MANDATORY.contentEquals(getVCUpdateStatus())) {
                                                finish();
                                                endApplication();
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    @Override
                                    public void onFailure(int downloadId, int statusCode, String errMsg) {
                                        Log.e("AyanVC:", errMsg);
                                        finish();
                                        openUrl(VersionControlActivity.this, getVCLink());
                                    }
                                })
                                .build();

                        id = manager.add(request);
                    } else if (GetLastVersion.LinkType.PAGE.contentEquals(getVCLinkType())) {
                        finish();
                        openUrl(VersionControlActivity.this, getVCLink());
                        if (CheckVersion.UpdateStatus.MANDATORY.contentEquals(getVCUpdateStatus()))
                            endApplication();
                    }
                } catch (Exception e) {
                    finish();
                    openUrl(VersionControlActivity.this, getVCLink());
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
                finish();
                if (CheckVersion.UpdateStatus.MANDATORY.contentEquals(getVCUpdateStatus()))
                    endApplication();
            }
        });
    }

    private void endApplication() {
        android.os.Process.sendSignal(android.os.Process.myPid(), android.os.Process.SIGNAL_KILL);
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
