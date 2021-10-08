package com.souvenir.notificationmanager;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationManagement {
    private static NotificationManagement instance;

    public static NotificationManagement GetInstance(Context context) {
        if (instance == null) {
            instance = new NotificationManagement(context);
        }
        return instance;
    }
    private static String TAG = "NotificationManagement";
    private AppNotificationDatabase appNotificationDatabase;
    private Map<String, AppNotificationData> appNotificationDataMap;
    public AppNotificationDataDao appNotificationDataDao;

    private SingleNotificationDatabase singleNotificationDatabase;
    private Map<String, List<SingleNotification>> singleNotificationsMap;
    public SingleNotificationDao singleNotificationDao;
    private boolean loaded = false;

    private NotificationManagement(Context context) {
        appNotificationDatabase = AppNotificationDatabase.getInstance(context);
        appNotificationDataDao = appNotificationDatabase.getAppNotificationDataDao();
        appNotificationDataMap = new HashMap<>();

        singleNotificationDatabase = SingleNotificationDatabase.getInstance(context);
        singleNotificationDao = singleNotificationDatabase.getSingleNotificationDao();
        singleNotificationsMap = new HashMap<>();
    }

    public synchronized void LoadAllDataFromDb(Handler handler) {
        if (loaded) {
            Log.i(TAG, "LoadAllDataFromDb: prev loaded");
            Message msg = new Message();
            msg.what = 1;
            handler.sendMessage(msg);
            return;
        }

        new Thread() {
            @Override
            public void run() {
                Log.i(TAG, "run: start LoadAllDataFromDb");
                AppNotificationData[] appNotificationDatas = appNotificationDataDao.getAllAppNotificationData();
                SingleNotification[] singleNotifications = singleNotificationDao.getAllNotifications();

                for (AppNotificationData appNotificationData:
                        appNotificationDatas) {
                    appNotificationDataMap.put(appNotificationData.packageName, appNotificationData);
                }

                for (SingleNotification singleNotification:
                        singleNotifications) {

                    if (!singleNotificationsMap.containsKey(singleNotification.packageName)) {
                        singleNotificationsMap.put(singleNotification.packageName, new ArrayList<>());
                    }
                    singleNotificationsMap.get(singleNotification.packageName).add(singleNotification);
                }
                loaded = true;

                Message msg = new Message();
                msg.what = 1;
                handler.sendMessage(msg);
            }
        }.start();
    }

    public AppData GetAppData(String packageName) {
        AppData appData = new AppData();

        appData.appNotificationData = appNotificationDataMap.get(packageName);
        appData.singleNotifications = singleNotificationsMap.get(packageName);

        if (appData.appNotificationData == null) {
            Log.i(TAG, "GetAppData: " + packageName + " insert to db");
            appData.appNotificationData = new AppNotificationData();
            appData.appNotificationData.packageName = packageName;
            appData.appNotificationData.mode = AppNotificationMode.NONE;
            appNotificationDataMap.put(packageName, appData.appNotificationData);

            new Thread(){
                @Override
                public synchronized void run() {
                    if (appNotificationDataDao.getAppNotificationDataByPackageName(packageName).length == 0) {
                        appNotificationDataDao.addAppNotificationData(appData.appNotificationData);
                    }
                }
            }.start();
        }

        if (appData.singleNotifications == null) {
            appData.singleNotifications = new ArrayList<>();
            singleNotificationsMap.put(packageName, appData.singleNotifications);
        }

        return appData;
    }

    public void SetAppMode(String packageName, int mode) {
        Log.i(TAG, "SetAppMode: " + packageName + " " + mode);

        AppNotificationData appNotificationData = appNotificationDataMap.get(packageName);
        if (appNotificationData.mode != mode) {
            appNotificationData.mode = mode;

            new Thread() {
                @Override
                public void run() {
                    appNotificationDataDao.updateAppNotificationData(appNotificationData);
                }
            }.start();
        }
    }

    public void SetBlackList(String packageName, String blackList) {
        Log.i(TAG, "SetBlackList: " + packageName + " " + blackList);
        AppNotificationData appNotificationData = appNotificationDataMap.get(packageName);
        if (appNotificationData.blackList == null || !appNotificationData.blackList.equals(blackList)) {
            appNotificationData.blackList = blackList;

            new Thread() {
                @Override
                public void run() {
                    appNotificationDataDao.updateAppNotificationData(appNotificationData);
                }
            }.start();
        }
    }

    public void SetWhiteList(String packageName, String whiteList) {
        Log.i(TAG, "SetWhiteList: " + packageName + " " + whiteList);
        AppNotificationData appNotificationData = appNotificationDataMap.get(packageName);
        if (appNotificationData.whiteList == null || !appNotificationData.whiteList.equals(whiteList)) {
            appNotificationData.whiteList = whiteList;

            new Thread() {
                @Override
                public void run() {
                    appNotificationDataDao.updateAppNotificationData(appNotificationData);
                }
            }.start();
        }
    }

    public void SaveNotification(String packageName, long time, String title, String content, boolean isBlocked) {
        Log.i(TAG, "SaveNotification: packageName:" + packageName + ", time:" + time + ", title:" + title + ", content:" + content + ", isBlocked:" + isBlocked);

        SingleNotification singleNotification = new SingleNotification();
        singleNotification.packageName = packageName;
        singleNotification.sendTime = time;
        singleNotification.title = title;
        singleNotification.content = content;
        singleNotification.isBlocked = isBlocked;

        if (!singleNotificationsMap.containsKey(packageName)) {
            singleNotificationsMap.put(packageName, new ArrayList<>());
        }

        singleNotificationsMap.get(packageName).add(singleNotification);

        new Thread() {
            @Override
            public void run() {
                singleNotificationDao.addNotifications(singleNotification);
            }
        }.start();
    }

    public boolean shouldBlocked(String packageName, String title, String content) {
        AppNotificationData appNotificationData = appNotificationDataMap.get(packageName);
        if (appNotificationData == null) {
            Log.i(TAG, "GetAppData: " + packageName + " insert to db");
            appNotificationData = new AppNotificationData();
            appNotificationData.packageName = packageName;
            appNotificationData.mode = AppNotificationMode.NONE;
            appNotificationDataMap.put(packageName, appNotificationData);

            AppNotificationData finalAppNotificationData = appNotificationData;
            new Thread(){
                @Override
                public synchronized void run() {
                    if (appNotificationDataDao.getAppNotificationDataByPackageName(packageName).length == 0) {
                        appNotificationDataDao.addAppNotificationData(finalAppNotificationData);
                    }
                }
            }.start();

            return false;
        }

        if (appNotificationData.mode == AppNotificationMode.NONE) {
            return false;
        }

        if (appNotificationData.mode == AppNotificationMode.USE_BLACK_LIST && appNotificationData.blackList != null) {
            String[] blackList = appNotificationData.blackList.split("\\.");
            for (String keyWord :
                    blackList) {
                if (title.contains(keyWord)) {
                    return true;
                }

                if (content.contains(keyWord)) {
                    return true;
                }
            }

            return false;
        }

        if (appNotificationData.mode == AppNotificationMode.USE_WHITE_LIST && appNotificationData.whiteList != null) {
            String[] whiteList = appNotificationData.whiteList.split("\\.");
            for (String keyWord :
                    whiteList) {
                if (title.contains(keyWord)) {
                    return false;
                }

                if (content.contains(keyWord)) {
                    return false;
                }
            }

            return true;
        }

        return false;
    }
}
