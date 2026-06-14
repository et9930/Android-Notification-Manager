package com.souvenir.notificationmanager;

import android.app.Notification;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import java.lang.reflect.Field;
import java.util.Date;

public class NotificationListener extends NotificationListenerService {
    private static final String TAG = "NotificationListenerService";

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        String packageName = sbn.getPackageName();
        Notification notification = sbn.getNotification();
        Bundle extras = notification.extras;

        if ("com.miui.systemAdSolution".equals(packageName)) {
            try {
                Field fieldMiuiNotification = notification.getClass().getField("extraNotification");
                Object miuiNotification = fieldMiuiNotification.get(notification);
                Field fieldTargetPkg = miuiNotification.getClass().getField("targetPkg");
                packageName = (String) fieldTargetPkg.get(miuiNotification);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        String title = extras.getString(Notification.EXTRA_TITLE);
        String content = extras.getString(Notification.EXTRA_TEXT);

        if (sbn.isClearable()) {
            NotificationManagement nm = NotificationManagement.GetInstance(getApplicationContext());
            boolean blocked = nm.shouldBlocked(packageName, title, content);
            if (blocked) {
                cancelNotification(sbn.getKey());
            }
            nm.SaveNotification(packageName, sbn.getKey(), new Date().getTime(), title, content, blocked);
        }
    }
}
