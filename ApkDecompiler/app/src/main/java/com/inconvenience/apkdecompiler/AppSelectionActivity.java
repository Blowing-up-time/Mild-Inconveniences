package com.inconvenience.apkdecompiler;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AppSelectionActivity extends AppCompatActivity {
    AppSelectionAdapter adapter;
    ListView listView;
    ArrayList<AppListItem> listItems = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_selection);
        loadList();
        launchList();

    }

    public void loadList() {
        PackageManager pm = this.getPackageManager();
        for (ApplicationInfo info : pm.getInstalledApplications(0)) {
            String name = pm.getApplicationLabel(info).toString();
            listItems.add(new AppListItem(
                            pm.getApplicationIcon(info),
                            name,
                            info.packageName
                    )

            );
        }
        listItems.sort(new SortOptions().AppListItemDefault);
        adapter = new AppSelectionAdapter(this, listItems);
    }


    public void launchList() {
        PackageManager pm = getPackageManager();
        listView = findViewById(R.id.applist);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            PackageManager packageManager = getPackageManager();

            try {
                ApplicationInfo info = packageManager.getApplicationInfo(listItems.get(position).Package, PackageManager.GET_META_DATA);
                StringBuilder contents = new StringBuilder();
                File f = new File(info.sourceDir);
                BufferedReader br = new BufferedReader(new FileReader(f));
                String line;
                while ((line = br.readLine()) != null) {
                    contents.append(line);
                    contents.append("\n");
                }

                Decompiler d = new Decompiler(f, pm.getApplicationLabel(info).toString(), this, this);
                Toast.makeText(this, "Decompiling now, please do not touch anything until this process is complete", Toast.LENGTH_LONG).show();
                Thread t = new Thread(d::decompile);
                t.start();
            } catch (PackageManager.NameNotFoundException | IOException e) {
                e.printStackTrace();
            }

        });
    }
}

class AppSelectionAdapter extends BaseAdapter {
    Context context;
    List<AppListItem> itemNames;
    LayoutInflater inflater;
    public AppSelectionAdapter(Context context1, List<AppListItem> items) {
        context = context1;
        itemNames = items;
    }

    @Override
    public int getCount() {
        return itemNames.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String fuckThisWarning = Context.LAYOUT_INFLATER_SERVICE;
        inflater = (LayoutInflater) context.getSystemService(fuckThisWarning);
        View itemView = inflater.inflate(R.layout.selection_row, parent, false);
        ((TextView) itemView.findViewById(R.id.applistname)).setText(itemNames.get(position).appName);
        ((ImageView) itemView.findViewById(R.id.applistimage)).setImageDrawable(itemNames.get(position).icon);
        return itemView;
    }
}

class AppListItem {
    public Drawable icon;
    public String appName;
    public String Package;
    public AppListItem(Drawable icon1, String appName1, String packageName) {
        icon = icon1;
        appName = appName1;
        Package = packageName;
    }
}