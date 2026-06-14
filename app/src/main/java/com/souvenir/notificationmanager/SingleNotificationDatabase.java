package com.souvenir.notificationmanager;


import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = { SingleNotification.class }, version = 2, exportSchema = false)

public abstract class SingleNotificationDatabase extends RoomDatabase {
    private static final String DB_NAME = "SingleNotificationDatabase.db";
    private static volatile SingleNotificationDatabase instance;

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE single_notification_table ADD COLUMN notification_key TEXT");
        }
    };

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
                DB_NAME)
                .addMigrations(MIGRATION_1_2)
                .build();
    }

    public abstract SingleNotificationDao getSingleNotificationDao();
}
