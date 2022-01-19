package com.inconvenience.apkdecompiler;

import java.util.Comparator;
import java.util.Locale;

import com.inconvenience.apkdecompiler.AppListItem;

public class SortOptions {

    public Comparator<AppListItem> AppListItemDefault = new Comparator<AppListItem>() {
        @Override
        public int compare(AppListItem o1, AppListItem o2) {
            return o1.appName.toLowerCase().compareTo(o2.appName.toLowerCase());
        }
    };
}
