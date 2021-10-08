package com.souvenir.notificationmanager;


import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = { AppNotificationData.class }, version = 1,exportSchema = false)

public abstract class AppNotificationDatabase extends RoomDatabase {
    private static final String DB_NAME = "AppNotificationDatabase.db";
    private static volatile AppNotificationDatabase instance;

    static synchronized AppNotificationDatabase getInstance(Context context) {
        if (instance == null) {
            instance = create(context);
        }
        return instance;
    }

    private static AppNotificationDatabase create(final Context context) {
        return Room.databaseBuilder(
                context,
                AppNotificationDatabase.class,
                DB_NAME).build();
    }

    public abstract AppNotificationDataDao getAppNotificationDataDao();
}
