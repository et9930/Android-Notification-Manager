package com.souvenir.notificationmanager;


import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = { SingleNotification.class }, version = 1,exportSchema = false)

public abstract class SingleNotificationDatabase extends RoomDatabase {
    private static final String DB_NAME = "SingleNotificationDatabase.db";
    private static volatile SingleNotificationDatabase instance;

    static synchronized SingleNotificationDatabase getInstance(Context context) {
        if (instance == null) {
            instance = create(context);
        }
        return instance;
    }

    private static SingleNotificationDatabase create(final Context context) {
        return Room.databaseBuilder(
                context,
                SingleNotificationDatabase.class,
                DB_NAME).build();
    }

    public abstract SingleNotificationDao getSingleNotificationDao();
}
