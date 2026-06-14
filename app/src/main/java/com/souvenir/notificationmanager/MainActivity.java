package com.souvenir.notificationmanager;

import android.Manifest;
import android.app.AlertDialog;
import android.net.Uri;
import android.os.PowerManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private ListView appListView;
    private AppListItemAdapter appListItemAdapter;
    private EditText searchText;
    private Spinner sortSpinner;

    private MainViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        appListView = findViewById(R.id.AppList);
        appListItemAdapter = new AppListItemAdapter(getApplicationContext(),
                viewModel.getAppList().getValue(), getPackageManager());
        appListView.setAdapter(appListItemAdapter);

        viewModel.getAppList().observe(this, list -> {
            appListItemAdapter.packageInfos = list;
            appListItemAdapter.notifyDataSetChanged();
        });

        appListView.setOnItemClickListener((parent, view, position, id) -> {
            String packageName = viewModel.getAppList().getValue().get(position);
            Intent intent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putSerializable("packageName", packageName);
            intent.putExtra("packageName", bundle);
            intent.setClass(MainActivity.this, AppDetail.class);
            startActivity(intent);
        });

        searchText = findViewById(R.id.searchText);
        searchText.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(android.text.Editable s) {
                viewModel.setSearchWord(s.toString());
            }
        });

        sortSpinner = findViewById(R.id.sortType);
        sortSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view,
                                       int position, long id) {
                viewModel.setSortType(position);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkPermission();
        checkBatteryOptimization();
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.loadData();
    }

    private void checkBatteryOptimization() {
        PowerManager pm = getSystemService(PowerManager.class);
        if (pm != null && !pm.isIgnoringBatteryOptimizations(getPackageName())) {
            new AlertDialog.Builder(this)
                    .setTitle("电池优化")
                    .setMessage("为了确保通知拦截服务稳定运行，请关闭电池优化限制。")
                    .setPositiveButton("去设置", (d, w) -> {
                        Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                        intent.setData(Uri.parse("package:" + getPackageName()));
                        startActivity(intent);
                    })
                    .setNegativeButton("暂不", null)
                    .show();
        }
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.QUERY_ALL_PACKAGES)
                == PackageManager.PERMISSION_DENIED) {
            Log.w(TAG, "QUERY_ALL_PACKAGES denied, requesting");
            requestPermissions(new String[]{Manifest.permission.QUERY_ALL_PACKAGES}, 0);
        }

        toggleNotificationListenerService(this);

        if (!isNotificationListenersEnabled()) {
            Log.w(TAG, "Notification listener not enabled");
            gotoNotificationAccessSetting(this);
        }
    }

    private boolean isNotificationListenersEnabled() {
        String listeners = Settings.Secure.getString(getContentResolver(),
                "enabled_notification_listeners");
        if (!TextUtils.isEmpty(listeners)) {
            String[] list = listeners.split(":");
            for (String s : list) {
                if (s.contains(getPackageName())) return true;
            }
        }
        return false;
    }

    private void gotoNotificationAccessSetting(Context context) {
        try {
            Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(context, "对不起，您的手机暂不支持", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public static void toggleNotificationListenerService(Context context) {
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(
                new ComponentName(context, NotificationListener.class),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
        pm.setComponentEnabledSetting(
                new ComponentName(context, NotificationListener.class),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }
}
