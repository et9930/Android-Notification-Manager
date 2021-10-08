package com.souvenir.notificationmanager;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface SingleNotificationDao {
    // single notification
    @Insert
    void addNotifications(SingleNotification... items);

    @Update
    void updateNotifications(SingleNotification... items);

    @Delete
    void deleteNotifications(SingleNotification... items);

    @Query("SELECT * FROM single_notification_table")
    SingleNotification[] getAllNotifications();

    @Query("SELECT * FROM single_notification_table WHERE package_name == :packageName")
    SingleNotification[] getPackageNotifications(String packageName);
}
