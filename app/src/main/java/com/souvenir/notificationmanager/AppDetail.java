package com.souvenir.notificationmanager;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class AppDetail extends AppCompatActivity {
    private final static String TAG = "AppDetail";

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

    private PackageManager pm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_detail);

        icon = findViewById(R.id.icon);
        name = findViewById(R.id.localName);
        state = findViewById(R.id.stateText);
        modeGroup = findViewById(R.id.modeGroup);
        blackList = findViewById(R.id.blackListInput);
        whiteList = findViewById(R.id.whiteListInput);
        historyList = findViewById(R.id.historyNotifications);
        notificationList = new ArrayList<>();
        historyNotificationAdapter = new HistoryNotificationAdapter(getApplicationContext(), notificationList);
        historyList.setAdapter(historyNotificationAdapter);
        pm = getPackageManager();

        Intent myIntend = getIntent();
        Bundle myBundle = myIntend.getBundleExtra("packageName");
        packageName = (String) myBundle.getSerializable("packageName");
        try {
            PackageInfo item = pm.getPackageInfo(packageName, 0);
            icon.setImageDrawable(item.applicationInfo.loadIcon(pm));
            name.setText(item.applicationInfo.loadLabel(pm));

            AppData appData = NotificationManagement.GetInstance(getApplicationContext()).GetAppData(item.packageName);

            if(appData.appNotificationData.mode == AppNotificationMode.NONE) {
                modeGroup.check(R.id.noneMode);
            } else if (appData.appNotificationData.mode == AppNotificationMode.USE_BLACK_LIST) {
                modeGroup.check(R.id.blackMode);
            } else if (appData.appNotificationData.mode == AppNotificationMode.USE_WHITE_LIST) {
                modeGroup.check(R.id.whiteMode);
            }

            if (appData.appNotificationData.blackList != null) {
                blackList.setText(appData.appNotificationData.blackList);
            } else {
                blackList.setText("");
            }

            if (appData.appNotificationData.whiteList != null) {
                whiteList.setText(appData.appNotificationData.whiteList);
            } else {
                whiteList.setText("");
            }

            long lastTime = 0;
            int blockNumber = 0;
            for (SingleNotification notification:
                    appData.singleNotifications) {
                if (notification.isBlocked) {
                    blockNumber ++;
                }
                if (notification.sendTime > lastTime) {
                    lastTime = notification.sendTime;
                }
            }

            String times = "无";

            if (lastTime != 0) {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                times = format.format(new Date(lastTime));
            }

            state.setText("上次通知时间:" + times + "\n拦截次数:" + blockNumber);

            this.notificationList.clear();
            this.notificationList.addAll(appData.singleNotifications);

            Collections.sort(this.notificationList, new Comparator<SingleNotification>() {
                public int compare(SingleNotification o1, SingleNotification o2) {
                    return o2.sendTime >= o1.sendTime ? 1 : -1;
                }
            });

            historyNotificationAdapter.notifyDataSetChanged();
            historyList.setAdapter(historyNotificationAdapter);

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        modeGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                switch (checkedId){
                    case R.id.noneMode:
                        NotificationManagement.GetInstance(getApplicationContext()).SetAppMode(packageName, AppNotificationMode.NONE);
                        break;
                    case R.id.blackMode:
                        NotificationManagement.GetInstance(getApplicationContext()).SetAppMode(packageName, AppNotificationMode.USE_BLACK_LIST);
                        break;
                    case R.id.whiteMode:
                        NotificationManagement.GetInstance(getApplicationContext()).SetAppMode(packageName, AppNotificationMode.USE_WHITE_LIST);
                        break;
                    default:
                        break;
                }
            }
        });

        blackList.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    NotificationManagement.GetInstance(getApplicationContext()).SetBlackList(packageName, blackList.getText().toString());
                }
            }
        });

        whiteList.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    NotificationManagement.GetInstance(getApplicationContext()).SetWhiteList(packageName, whiteList.getText().toString());
                }
            }
        });


    }

    @Override
    protected void onPause() {
        super.onPause();
        historyNotificationAdapter.notifyDataSetChanged();
        historyList.setAdapter(historyNotificationAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        NotificationManagement.GetInstance(getApplicationContext()).SetBlackList(packageName, blackList.getText().toString());
        NotificationManagement.GetInstance(getApplicationContext()).SetWhiteList(packageName, whiteList.getText().toString());
    }
}