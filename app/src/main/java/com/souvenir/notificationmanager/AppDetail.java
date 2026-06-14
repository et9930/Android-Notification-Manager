package com.souvenir.notificationmanager;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

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
        
                historyList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                        @Override
                        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                                showKeywordPicker(notificationList.get(position));
                                return true;
                        }
                });
        
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
                        for (SingleNotification n : appData.singleNotifications) {
                                if (n.isBlocked) blockNumber++;
                                if (n.sendTime > lastTime) lastTime = n.sendTime;
                        }
                        String times = lastTime != 0
                                ? new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date(lastTime))
                                : "N/A";
                        state.setText("\u4e0a\u6b21\u901a\u77e5\u65f6\u95f4:" + times + "\n\u62e6\u622a\u6b21\u6570:" + blockNumber);
            
                        notificationList.clear();
                        notificationList.addAll(appData.singleNotifications);
                        Collections.sort(notificationList, (o1, o2) -> Long.compare(o2.sendTime, o1.sendTime));
                        historyNotificationAdapter.notifyDataSetChanged();
                } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                }
        
                modeGroup.setOnCheckedChangeListener((group, checkedId) -> {
                        int mode;
                        if (checkedId == R.id.noneMode) mode = AppNotificationMode.NONE;
                        else if (checkedId == R.id.blackMode) mode = AppNotificationMode.USE_BLACK_LIST;
                        else if (checkedId == R.id.whiteMode) mode = AppNotificationMode.USE_WHITE_LIST;
                        else return;
                        NotificationManagement.GetInstance(getApplicationContext()).SetAppMode(packageName, mode);
                });
        
                blackList.setOnFocusChangeListener((v, hasFocus) -> {
                        if (!hasFocus) NotificationManagement.GetInstance(getApplicationContext())
                                .SetBlackList(packageName, blackList.getText().toString());
                });
        
                whiteList.setOnFocusChangeListener((v, hasFocus) -> {
                        if (!hasFocus) NotificationManagement.GetInstance(getApplicationContext())
                                .SetWhiteList(packageName, whiteList.getText().toString());
                });
        }

        private List<String> tokenize(String text) {
                Set<String> tokens = new LinkedHashSet<>();
                if (text == null) return new ArrayList<>(tokens);
                String[] parts = text.split("[\\p{Punct}\\s]+");
                for (String part : parts) {
                        part = part.trim();
                        if (part.isEmpty()) continue;
                        for (int i = 0; i < part.length(); i++) {
                                char c = part.charAt(i);
                                if (Character.isIdeographic(c)) tokens.add(String.valueOf(c));
                        }
                }
                return new ArrayList<>(tokens);
        }

        private void showKeywordPicker(SingleNotification notification) {
                String combined = (notification.title != null ? notification.title : "")
                        + " " + (notification.content != null ? notification.content : "");
                List<String> keywords = tokenize(combined);
                if (keywords.isEmpty()) {
                        Toast.makeText(this, "\u65e0\u53ef\u9009\u5173\u952e\u8bcd", Toast.LENGTH_SHORT).show();
                        return;
                }
                String[] items = keywords.toArray(new String[0]);
                boolean[] checked = new boolean[items.length];
                new AlertDialog.Builder(this)
                        .setTitle("\u9009\u62e9\u5173\u952e\u8bcd")
                        .setMultiChoiceItems(items, checked, (dialog, which, isChecked) -> checked[which] = isChecked)
                        .setPositiveButton("\u6dfb\u52a0\u5230\u9ed1\u540d\u5355", (dialog, which) -> appendKeywords(checked, items, true))
                        .setNeutralButton("\u6dfb\u52a0\u5230\u767d\u540d\u5355", (dialog, which) -> appendKeywords(checked, items, false))
                        .setNegativeButton("\u53d6\u6d88", null)
                        .show();
        }

        private void appendKeywords(boolean[] checked, String[] items, boolean toBlacklist) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < checked.length; i++) {
                        if (checked[i]) sb.append(items[i]);
                }
                if (sb.length() == 0) {
                        Toast.makeText(this, "\u8bf7\u5148\u9009\u62e9\u5173\u952e\u8bcd", Toast.LENGTH_SHORT).show();
                        return;
                }
                EditText target = toBlacklist ? blackList : whiteList;
                String existing = target.getText().toString().trim();
                String newText = existing.isEmpty() ? sb.toString() : existing + "." + sb;
                target.setText(newText);
                NotificationManagement nm = NotificationManagement.GetInstance(getApplicationContext());
                if (toBlacklist) nm.SetBlackList(packageName, newText);
                else nm.SetWhiteList(packageName, newText);
                Toast.makeText(this, "\u5df2\u6dfb\u52a0\u5173\u952e\u8bcd: " + sb.toString(),
                        Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onDestroy() {
                super.onDestroy();
                NotificationManagement nm = NotificationManagement.GetInstance(getApplicationContext());
                nm.SetBlackList(packageName, blackList.getText().toString());
                nm.SetWhiteList(packageName, whiteList.getText().toString());
        }
}
