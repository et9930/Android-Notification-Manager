package com.souvenir.notificationmanager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    ListView appListView;
    AppListItemAdapter appListItemAdapter;
    private EditText searchText;
    private String searchWord = "";
    List<String> packageNames;
    private Handler myHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    ShowList();
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private Spinner sortSpinner;
    private int sort = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        appListView = (ListView) findViewById(R.id.AppList);
        packageNames = new ArrayList<>();
        appListItemAdapter = new AppListItemAdapter(getApplicationContext(), packageNames, getPackageManager());
        appListView.setAdapter(appListItemAdapter);
        searchText = (EditText) findViewById(R.id.searchText);
        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                searchWord = editable.toString().trim();
                NotificationManagement.GetInstance(getApplicationContext()).LoadAllDataFromDb(myHandler);
            }
        });

        sortSpinner = findViewById(R.id.sortType);
        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String[] sorts = getResources().getStringArray(R.array.sorts);
                sort = i;
                ShowList();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        CheckPermission();
    }

    @Override
    protected void onPause() {
        super.onPause();
        packageNames.clear();
        appListItemAdapter.notifyDataSetChanged();
        appListView.setAdapter(appListItemAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        ShowList();
        NotificationManagement.GetInstance(getApplicationContext()).LoadAllDataFromDb(myHandler);
    }

    private void CheckPermission() {
        if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.QUERY_ALL_PACKAGES) == PackageManager.PERMISSION_DENIED) {
            Log.w(TAG, "CheckPermission: QUERY_ALL_PACKAGES fail");
            requestPermissions(new String[] { Manifest.permission.QUERY_ALL_PACKAGES }, 0);
        }

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE) == PackageManager.PERMISSION_DENIED) {
            Log.w(TAG, "CheckPermission: BIND_NOTIFICATION_LISTENER_SERVICE fail");
            requestPermissions(new String[] { Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE }, 0);
        }

        toggleNotificationListenerService(getApplicationContext());
        if (!isNotificationListenersEnabled()){
            Log.w(TAG, "CheckPermission: isNotificationListenersEnabled false");
            gotoNotificationAccessSetting(getApplicationContext());
        }
    }

    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
    private boolean isNotificationListenersEnabled () {
        String pkgName = getPackageName();
        final String flat = Settings.Secure.getString(getContentResolver(),   ENABLED_NOTIFICATION_LISTENERS);
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (int i = 0; i < names.length; i++) {
                final ComponentName cn = ComponentName.unflattenFromString(names[i]);
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean gotoNotificationAccessSetting(Context context) {
        try {
            Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            return true;

        } catch (ActivityNotFoundException e) {//普通情况下找不到的时候需要再特殊处理找一次
            try {
                Intent intent = new Intent();
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                ComponentName cn = new ComponentName("com.android.settings", "com.android.settings.Settings$NotificationAccessSettingsActivity");
                intent.setComponent(cn);
                intent.putExtra(":settings:show_fragment", "NotificationAccessSettings");
                context.startActivity(intent);
                return true;
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            Toast.makeText(context, "对不起，您的手机暂不支持", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            return false;
        }
    }

    public static void toggleNotificationListenerService(Context context) {
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(new ComponentName(context, NotificationListener.class),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

        pm.setComponentEnabledSetting(new ComponentName(context, NotificationListener .class),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }


    private void ShowList() {
        try {
            Set<String> packageNamesSet = new HashSet<>();
            for (ApplicationInfo info : getPackageManager().getInstalledApplications(0)) {
                if (searchWord.isEmpty() || info.packageName.contains(searchWord) || info.loadLabel(getPackageManager()).toString().contains(searchWord)) {
                    packageNamesSet.add(info.packageName);
                }
            }
            for (PackageInfo info : getPackageManager().getInstalledPackages(0)) {
                if (searchWord.isEmpty() || info.packageName.contains(searchWord) || info.applicationInfo.loadLabel(getPackageManager()).toString().contains(searchWord)) {
                    packageNamesSet.add(info.packageName);
                }
            }
            packageNames.clear();
            packageNames.addAll(packageNamesSet);

            if (sort == 0) {
                Collections.sort(packageNames, new Comparator<String>() {
                    public int compare(String o1, String o2) {
                        AppData appData1 = NotificationManagement.GetInstance(getApplicationContext()).GetAppData(o1);
                        AppData appData2 = NotificationManagement.GetInstance(getApplicationContext()).GetAppData(o2);

                        long lastTime1 = 0;
                        long lastTime2 = 0;

                        for (SingleNotification notification:
                             appData1.singleNotifications) {
                            if (notification.sendTime > lastTime1) {
                                lastTime1 = notification.sendTime;
                            }
                        }

                        for (SingleNotification notification:
                                appData2.singleNotifications) {
                            if (notification.sendTime > lastTime2) {
                                lastTime2 = notification.sendTime;
                            }
                        }

                        return Long.compare(lastTime2, lastTime1);
                    }
                });
            } else if (sort == 1) {
                Collections.sort(packageNames, new Comparator<String>() {
                    public int compare(String o1, String o2) {
                        AppData appData1 = NotificationManagement.GetInstance(getApplicationContext()).GetAppData(o1);
                        AppData appData2 = NotificationManagement.GetInstance(getApplicationContext()).GetAppData(o2);

                        int blockNum1 = 0;
                        int blockNum2 = 0;

                        for (SingleNotification notification:
                                appData1.singleNotifications) {
                            if (notification.isBlocked) {
                                blockNum1++;
                            }
                        }

                        for (SingleNotification notification:
                                appData2.singleNotifications) {
                            if (notification.isBlocked) {
                                blockNum2++;
                            }
                        }

                        return Integer.compare(blockNum2, blockNum1);
                    }
                });
            }

            appListItemAdapter.notifyDataSetChanged();
            appListView.setAdapter(appListItemAdapter);
            appListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    String packageName = packageNames.get(i);
                    Intent intent = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("packageName", packageName);
                    intent.putExtra("packageName", bundle);
                    intent.setClass(MainActivity.this,AppDetail.class);
                    startActivity(intent);
                }
            });
        } catch (Throwable t) {
            t.printStackTrace();;
        }
    }
}