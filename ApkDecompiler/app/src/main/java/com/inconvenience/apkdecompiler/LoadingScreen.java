package com.inconvenience.apkdecompiler;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Pair;
import android.widget.TextView;

import java.util.ArrayList;
/**
 * This is shitty old code, i got excited and wanted to do something specific and i can't remember what i wanted to do
 * this is here because i want to be abl to look back later in my career and see how far i've come, everyone loves a good arc.
 */
public class LoadingScreen extends AppCompatActivity {
    TextView status;
    private int statusProgress = 0;
    private Handler handler = new Handler();
    Context context;
    ArrayList<AppListItem> items;
    AppSelectionAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fullscreen_loading);

        status = findViewById(R.id.loading_status);
        context = this;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Pair<ArrayList<AppListItem>, AppSelectionAdapter> result;
                    ArrayList<AppListItem> listItems = new ArrayList<>();

                    handler.post(() -> status.setText("Loading list of applications"));
                    PackageManager pm = context.getPackageManager();
                    for (ApplicationInfo info : pm.getInstalledApplications(0)) {
                        handler.post(() -> status.setText("Added " + info.packageName));
                        String name = pm.getApplicationLabel(info).toString();
                        listItems.add(new AppListItem(
                                        pm.getApplicationIcon(info),
                                        name,
                                        info.packageName
                                )

                        );
                    }
                    handler.post(() -> status.setText("Done"));
                    listItems.sort(new SortOptions().AppListItemDefault);
                    items = listItems;
                }
                finally {
                    adapter = new AppSelectionAdapter(context, items);

                    Intent acttwo = new Intent(context, AppSelectionActivity.class);
                    //AppSelectionActivity.fuckersthatarestaticforitems = items;
                    //AppSelectionActivity.thisfuckerbutstaticadapter = adapter;
                    acttwo.putExtra("fromLoad", true);
                    startActivity(acttwo);
                }
            }
        }).start();


    }

}