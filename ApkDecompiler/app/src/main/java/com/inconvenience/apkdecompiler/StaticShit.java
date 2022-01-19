package com.inconvenience.apkdecompiler;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Environment;

import java.io.File;

public class StaticShit {

    public static Context context;
    public static Activity activity;
    public static Resources res;
    public static File jadxHome = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/JadxDecompiler");


}
