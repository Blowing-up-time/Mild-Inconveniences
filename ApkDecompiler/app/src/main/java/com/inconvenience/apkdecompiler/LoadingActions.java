package com.inconvenience.apkdecompiler;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.util.Pair;
import android.widget.TextView;

import java.util.ArrayList;

public enum LoadingActions {;
    public static class AppListLoadingAction implements Parcelable {
        private Context context;
        private Activity activity;
        protected AppListLoadingAction(Parcel in) {
        }

        public AppListLoadingAction(Context _c, Activity _a) {
            context = _c;
            activity = _a;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
        }

        @Override
        public int describeContents() {
            return 0;
        }
        public void setBasics(Context c, Activity a) {
            context = c;
            activity = a;
        }


        public static final Creator<AppListLoadingAction> CREATOR = new Creator<AppListLoadingAction>() {
            @Override
            public AppListLoadingAction createFromParcel(Parcel in) {
                return new AppListLoadingAction(in);
            }

            @Override
            public AppListLoadingAction[] newArray(int size) {
                return new AppListLoadingAction[size];
            }
        };
    }
    public static class SecondLoadingAction implements Parcelable {

        protected SecondLoadingAction(Parcel in) {
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<SecondLoadingAction> CREATOR = new Creator<SecondLoadingAction>() {
            @Override
            public SecondLoadingAction createFromParcel(Parcel in) {
                return new SecondLoadingAction(in);
            }

            @Override
            public SecondLoadingAction[] newArray(int size) {
                return new SecondLoadingAction[size];
            }
        };
    }
}
