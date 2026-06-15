package com.souvenir.notificationmanager;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NotificationManagement {
    private static NotificationManagement instance;

    public static NotificationManagement GetInstance(Context context) {
        if (instance == null) {
            instance = new NotificationManagement(context);
        }
        return instance;
    }

    private static final String TAG = "NotificationManagement";

    private final AppNotificationDatabase appNotificationDatabase;
    private final Map<String, AppNotificationData> appNotificationDataMap;
    public final AppNotificationDataDao appNotificationDataDao;

    private final SingleNotificationDatabase singleNotificationDatabase;
    private final Map<String, List<SingleNotification>> singleNotificationsMap;
    public final SingleNotificationDao singleNotificationDao;

    private volatile boolean loaded = false;
    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();

    private NotificationManagement(Context context) {
        appNotificationDatabase = AppNotificationDatabase.getInstance(context);
        appNotificationDataDao = appNotificationDatabase.getAppNotificationDataDao();
        appNotificationDataMap = new ConcurrentHashMap<>();

        singleNotificationDatabase = SingleNotificationDatabase.getInstance(context);
        singleNotificationDao = singleNotificationDatabase.getSingleNotificationDao();
        singleNotificationsMap = new ConcurrentHashMap<>();
    }

    public synchronized void LoadAllDataFromDb(Handler handler) {
        if (loaded) {
            Message msg = new Message();
            msg.what = 1;
            handler.sendMessage(msg);
            return;
        }

        dbExecutor.execute(() -> {
            AppNotificationData[] appNotificationDatas = appNotificationDataDao.getAllAppNotificationData();
            SingleNotification[] singleNotifications = singleNotificationDao.getAllNotifications();

            for (AppNotificationData data : appNotificationDatas) {
                appNotificationDataMap.put(data.packageName, data);
            }

            for (SingleNotification sn : singleNotifications) {
                singleNotificationsMap
                        .computeIfAbsent(sn.packageName, k -> new ArrayList<>())
                        .add(sn);
            }
            loaded = true;

            Message msg = new Message();
            msg.what = 1;
            handler.sendMessage(msg);
        });
    }

    public AppData GetAppData(String packageName) {
        AppData appData = new AppData();

        appData.appNotificationData = appNotificationDataMap.computeIfAbsent(packageName, k -> {
            AppNotificationData data = new AppNotificationData();
            data.packageName = packageName;
            data.mode = AppNotificationMode.NONE;

            dbExecutor.execute(() -> {
                if (appNotificationDataDao.getAppNotificationDataByPackageName(packageName).length == 0) {
                    appNotificationDataDao.addAppNotificationData(data);
                }
            });
            return data;
        });

        appData.singleNotifications = singleNotificationsMap.computeIfAbsent(packageName, k -> new ArrayList<>());

        return appData;
    }

    public void SetAppMode(String packageName, int mode) {
        AppNotificationData data = appNotificationDataMap.get(packageName);
        if (data != null && data.mode != mode) {
            data.mode = mode;
            dbExecutor.execute(() -> appNotificationDataDao.updateAppNotificationData(data));
        }
    }

    public void SetBlackList(String packageName, String blackList) {
        AppNotificationData data = appNotificationDataMap.get(packageName);
        if (data != null && (data.blackList == null || !data.blackList.equals(blackList))) {
            data.blackList = blackList;
            dbExecutor.execute(() -> appNotificationDataDao.updateAppNotificationData(data));
        }
    }

    public void SetWhiteList(String packageName, String whiteList) {
        AppNotificationData data = appNotificationDataMap.get(packageName);
        if (data != null && (data.whiteList == null || !data.whiteList.equals(whiteList))) {
            data.whiteList = whiteList;
            dbExecutor.execute(() -> appNotificationDataDao.updateAppNotificationData(data));
        }
    }

    public void SaveNotification(String packageName, String notificationKey, long time,
                                  String title, String content, boolean isBlocked) {
        // Dedup: skip if notification with same key already in memory
        if (notificationKey != null) {
            List<SingleNotification> existing = singleNotificationsMap.get(packageName);
            if (existing != null) {
                for (SingleNotification sn : existing) {
                    if (notificationKey.equals(sn.notificationKey)
                            && time / 1000 == sn.sendTime / 1000) {
                        return;
                    }
                }
            }
        }

        SingleNotification singleNotification = new SingleNotification();
        singleNotification.packageName = packageName;
        singleNotification.notificationKey = notificationKey;
        singleNotification.sendTime = time;
        singleNotification.title = title;
        singleNotification.content = content;
        singleNotification.isBlocked = isBlocked;

        singleNotificationsMap
                .computeIfAbsent(packageName, k -> new ArrayList<>())
                .add(singleNotification);

        dbExecutor.execute(() -> {
            // DB-level dedup: skip if already persisted
            if (notificationKey == null
                    || singleNotificationDao.getNotificationByKeyAndSecond(notificationKey, time / 1000).length == 0) {
                singleNotificationDao.addNotifications(singleNotification);
            }
        });
    }

    public boolean shouldBlocked(String packageName, String title, String content) {
        AppNotificationData data = appNotificationDataMap.computeIfAbsent(packageName, k -> {
            AppNotificationData newData = new AppNotificationData();
            newData.packageName = packageName;
            newData.mode = AppNotificationMode.NONE;

            dbExecutor.execute(() -> {
                if (appNotificationDataDao.getAppNotificationDataByPackageName(packageName).length == 0) {
                    appNotificationDataDao.addAppNotificationData(newData);
                }
            });
            return newData;
        });

        if (data.mode == AppNotificationMode.NONE) {
            return false;
        }

        if (data.mode == AppNotificationMode.USE_BLACK_LIST) {
            if (data.blackList == null) return false;

            for (String keyWord : data.blackList.split("\\.")) {
                if (keyWord.trim().isEmpty()) continue;
                if (title.contains(keyWord) || content.contains(keyWord)) return true;
            }
            return false;
        }

        if (data.mode == AppNotificationMode.USE_WHITE_LIST) {
            if (data.whiteList == null) return true;

            for (String keyWord : data.whiteList.split("\\.")) {
                if (keyWord.trim().isEmpty()) continue;
                if (title.contains(keyWord) || content.contains(keyWord)) return false;
            }
            return true;
        }

        return false;
    }
}
