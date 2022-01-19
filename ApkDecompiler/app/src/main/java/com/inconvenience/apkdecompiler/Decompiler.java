package com.inconvenience.apkdecompiler;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import jadx.api.JadxArgs;
import jadx.api.JadxDecompiler;
import jadx.api.JavaClass;

public class Decompiler {
    public File apkFile;
    private Activity activity;
    private Context context;
    private String apkLabel;
    private File projectHome;

    public Decompiler(File _apkFile, String appname, Context _c, Activity _a) {
        apkFile = _apkFile;
        context = _c;
        activity = _a;
        apkLabel = appname;
    }

    public void decompile() {
        StaticShit.context = context;
        StaticShit.activity = activity;
        StaticShit.res = context.getResources();

        projectHome = new File(StaticShit.jadxHome.getAbsolutePath() + "/" + apkLabel);
        if (!projectHome.exists()) {
            projectHome.mkdirs();
        } else {
            projectHome.delete();
            projectHome.mkdirs();
        }

        copyApk();

        JadxArgs args = new JadxArgs();
        args.getInputFiles().add(apkFile);
        args.setOutDir(projectHome);
        args.setDeobfuscationOn(true);
        args.setUseSourceNameAsClassAlias(true);
        args.setDebugInfo(true);
        Thread process = new Thread(() -> {
            Looper.prepare();
            try (JadxDecompiler jadx = new JadxDecompiler(args)) {
                jadx.load();

                jadx.save(1000, (done, total) -> {
                    int progress = (int) (done * 100.0 / total);
                    Log.d("Decompiler Progress", String.format("INFO  - progress: %d of %d (%d%%)\r", done, total, progress));

                });
                int errorsCount = jadx.getErrorsCount();
                if (errorsCount != 0) {
                    Toast.makeText(context, String.format("Finished with errors, count: %d", errorsCount), Toast.LENGTH_LONG).show();
                    Toast.makeText(context, "Finished decompiling your app, you can go see the results under the folder JadxDecompiler/" + apkLabel, Toast.LENGTH_LONG).show();
                    tryDeleteTempApk();
                } else {
                    Toast.makeText(context, "Done decompiling", Toast.LENGTH_LONG).show();
                    Toast.makeText(context, "Finished decompiling your app, you can go see the results under the folder JadxDecompiler/" + apkLabel, Toast.LENGTH_LONG).show();
                    tryDeleteTempApk();
                }
            }
        });
        try {
            process.start();
            process.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    private void tryDeleteTempApk() {
        File toDelete = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/.temp.apk");
        toDelete.delete();
    }
    private void copyApk() {
        // lol didn't want this screwing with me later so put it here to easily disable it but why do you need to know this?
        try {
            FileInputStream in;
            FileOutputStream out;
            File newApkFile = new File(projectHome.getAbsolutePath() + "/" + "originalApk.apk");
            newApkFile.createNewFile();
            out = new FileOutputStream(newApkFile);
            in = new FileInputStream(apkFile);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
