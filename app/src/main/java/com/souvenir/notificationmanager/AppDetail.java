package com.souvenir.notificationmanager;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AppDetail extends AppCompatActivity {
    private static final String TAG = "AppDetail";

    private String packageName;
    private ImageView icon;
    private TextView name;
    private TextView state;
    private RadioGroup modeGroup;
    private EditText blackList;
    private EditText whiteList;
    private ListView historyList;
    private List<SingleNotification> notificationList;
    private HistoryNotificationAdapter historyNotificationAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_detail);

        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(
                getWindow(), getWindow().getDecorView());
        controller.setAppearanceLightStatusBars(true);

        icon = findViewById(R.id.icon);
        name = findViewById(R.id.localName);
        state = findViewById(R.id.stateText);
        modeGroup = findViewById(R.id.modeGroup);
        blackList = findViewById(R.id.blackListInput);
        whiteList = findViewById(R.id.whiteListInput);
        historyList = findViewById(R.id.historyNotifications);

        notificationList = new ArrayList<>();
        historyNotificationAdapter = new HistoryNotificationAdapter(this, notificationList);
        historyList.setAdapter(historyNotificationAdapter);

        PackageManager pm = getPackageManager();

        Intent myIntent = getIntent();
        Bundle myBundle = myIntent.getBundleExtra("packageName");
        packageName = (String) myBundle.getSerializable("packageName");

        try {
            PackageInfo item = pm.getPackageInfo(packageName, 0);
            icon.setImageDrawable(item.applicationInfo.loadIcon(pm));
            name.setText(item.applicationInfo.loadLabel(pm));

            NotificationManagement nm = NotificationManagement.GetInstance(getApplicationContext());
            AppData appData = nm.GetAppData(item.packageName);

            int mode = appData.appNotificationData.mode;
            if (mode == AppNotificationMode.NONE) {
                modeGroup.check(R.id.noneMode);
            } else if (mode == AppNotificationMode.USE_BLACK_LIST) {
                modeGroup.check(R.id.blackMode);
            } else if (mode == AppNotificationMode.USE_WHITE_LIST) {
                modeGroup.check(R.id.whiteMode);
            }

            blackList.setText(appData.appNotificationData.blackList != null
                    ? appData.appNotificationData.blackList : "");
            whiteList.setText(appData.appNotificationData.whiteList != null
                    ? appData.appNotificationData.whiteList : "");

            long lastTime = 0;
            int blockNumber = 0;
            for (SingleNotification notification : appData.singleNotifications) {
                if (notification.isBlocked) blockNumber++;
                if (notification.sendTime > lastTime) lastTime = notification.sendTime;
            }

            String times = lastTime != 0
                    ? new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date(lastTime))
                    : "N/A";

            state.setText("上次通知时间:" + times + "\n拦截次数:" + blockNumber);

            notificationList.clear();
            notificationList.addAll(appData.singleNotifications);
            Collections.sort(notificationList, (o1, o2) -> Long.compare(o2.sendTime, o1.sendTime));
            historyNotificationAdapter.notifyDataSetChanged();

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        modeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            int mode;
            if (checkedId == R.id.noneMode) {
                mode = AppNotificationMode.NONE;
            } else if (checkedId == R.id.blackMode) {
                mode = AppNotificationMode.USE_BLACK_LIST;
            } else if (checkedId == R.id.whiteMode) {
                mode = AppNotificationMode.USE_WHITE_LIST;
            } else {
                return;
            }
            NotificationManagement.GetInstance(getApplicationContext()).SetAppMode(packageName, mode);
        });

        blackList.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                NotificationManagement.GetInstance(getApplicationContext())
                        .SetBlackList(packageName, blackList.getText().toString());
            }
        });

        whiteList.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                NotificationManagement.GetInstance(getApplicationContext())
                        .SetWhiteList(packageName, whiteList.getText().toString());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        NotificationManagement nm = NotificationManagement.GetInstance(getApplicationContext());
        nm.SetBlackList(packageName, blackList.getText().toString());
        nm.SetWhiteList(packageName, whiteList.getText().toString());
    }
}
