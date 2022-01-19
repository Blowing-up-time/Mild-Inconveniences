package com.inconvenience.apkdecompiler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * <b>
 *     jfunny
 * </b>
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener, ActivityCompat.OnRequestPermissionsResultCallback {
    private String TAG = "APKDECOMPILER";
    private String[] requiredPermissions = new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE };
    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_selectApp).setOnClickListener(this);
        findViewById(R.id.btn_selectFile).setOnClickListener(this);
        checkPerms();
    }



    @RequiresApi(api = Build.VERSION_CODES.R)
    private void checkPerms() {
        requestPermissions(requiredPermissions, 1);
        if (!Environment.isExternalStorageManager()) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage("You must enable aces to all files otherwise i cant do the cool shit i could normally do");
            dialog.setTitle("uwu perms pls owo");
            dialog.setPositiveButton("Okay!", (dialog1, which) -> {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            });
            dialog.setNegativeButton("No", (dialog12, which) -> {
                showToast("won't work now :(");
                finishAndRemoveTask();
            });
            AlertDialog d = dialog.create();
            d.show();

        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_selectApp:
                Intent newIntent = new Intent(getApplicationContext(), AppSelectionActivity.class);
                newIntent.putExtra("fromLoad", false);
                startActivity(newIntent);
                break;
            case R.id.btn_selectFile:
                Intent fileIntent = new Intent(Intent.ACTION_GET_CONTENT);
                fileIntent.setType("*/*");
                fileIntent = Intent.createChooser(fileIntent, "Big");
                startActivityForResult(fileIntent, 1);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri fileUri = data.getData();
        File apk = fromUriToFile(fileUri);
        PackageInfo info = getPackageManager().getPackageArchiveInfo(apk.getAbsolutePath(), 0);
        String appName = getPackageManager().getApplicationLabel(info.applicationInfo).toString();
        Decompiler d = new Decompiler(apk, appName, this, this);
        Toast.makeText(this, "Decompiling now, please do not touch anything until this process is complete", Toast.LENGTH_LONG).show();
        Thread t = new Thread(d::decompile);
        t.start();
    }

    public File fromUriToFile(Uri uri) {
        File tempApk = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/.temp.apk");
        try {
            InputStream in = getContentResolver().openInputStream(uri);
            FileOutputStream out = new FileOutputStream(tempApk);
            tempApk.createNewFile();
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            out.flush();
            out.close();

        } catch (FileNotFoundException e) {
            showToast("something fucked up try again");
            e.printStackTrace();
        } catch (IOException e) {
            showToast("something fucked up try again");
            e.printStackTrace();
        }
        return tempApk;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length != 1 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    showToast("won't work now :(");
                }
        }
    }

    public void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
    public void showToastLong(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
}