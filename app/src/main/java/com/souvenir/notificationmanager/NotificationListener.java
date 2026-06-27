package com.souvenir.notificationmanager;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import java.lang.reflect.Field;
import java.util.Date;

public class NotificationListener extends NotificationListenerService {

    private static final String CHANNEL_ID = "notification_manager_foreground";
    private static final int FOREGROUND_NOTIFICATION_ID = 1001;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForegroundService();
    }

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        startForegroundService();
    }

    @Override
    public void onListenerDisconnected() {
        super.onListenerDisconnected();
        requestRebind(android.content.ComponentName.createRelative(
                getPackageName(), ".NotificationListener"));
    }

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

    @Override
    public void onDestroy() {
        stopForeground(true);
        super.onDestroy();
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                getString(R.string.foreground_channel_name),
                NotificationManager.IMPORTANCE_LOW);
        channel.setDescription(getString(R.string.foreground_channel_desc));
        channel.setShowBadge(false);
        NotificationManager nm = getSystemService(NotificationManager.class);
        if (nm != null) {
            nm.createNotificationChannel(channel);
        }
    }

    private void startForegroundService() {
        try {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new Notification.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.foreground_notification_title))
                .setContentText(getString(R.string.foreground_notification_text))
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();

        startForeground(FOREGROUND_NOTIFICATION_ID, notification);
        } catch (Exception e) {
            // Ignore if notification permission not granted yet
        }
    }
}
