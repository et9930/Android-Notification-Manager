package com.souvenir.notificationmanager;

import android.app.Notification;
import android.app.PendingIntent;
import android.os.Bundle;
import android.os.Handler;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.lang.reflect.Field;
import java.util.Date;

public class NotificationListener extends NotificationListenerService {
    private static final String TAG = "NotificationListenerService";

    @Override
    public void onCreate() {
        super.onCreate();
    }

    
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Log.i(TAG,"Notification posted");

        String packageName = sbn.getPackageName();
        Notification notification = sbn.getNotification();
        Bundle extras = notification.extras;

        if (packageName.equals("com.miui.systemAdSolution")) {
            Class cNotification = notification.getClass();
            try {
                Field fieldMiuiNotification = cNotification.getField("extraNotification");
                Class cMiuiNotification = fieldMiuiNotification.getClass();
                Field fieldMiuiNotificationTargetPkg = cMiuiNotification.getField("targetPkg");
                packageName = fieldMiuiNotificationTargetPkg.toString();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
        CharSequence tickerText = notification.tickerText;
        String title = extras.getString(Notification.EXTRA_TITLE); //通知title
        String content = extras.getString(Notification.EXTRA_TEXT); //通知内容
        PendingIntent pendingIntent = notification.contentIntent; //获取通知的PendingIntent


        if (sbn.isClearable()) {
            boolean block = NotificationManagement.GetInstance(getApplicationContext()).shouldBlocked(packageName, title, content);
            if (block) {
                Log.i(TAG, "onNotificationPosted: cancel notification");
                super.cancelNotification(sbn.getKey());
            }

            NotificationManagement.GetInstance(getApplicationContext()).SaveNotification(packageName, new Date().getTime(), title, content, block);
        }
    }
}
