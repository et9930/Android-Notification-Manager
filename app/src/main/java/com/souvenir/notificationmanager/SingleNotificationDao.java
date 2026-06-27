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

    @Query("SELECT * FROM single_notification_table WHERE notification_key == :key")
    SingleNotification[] getNotificationByKey(String key);

    @Query("SELECT * FROM single_notification_table WHERE notification_key == :key AND send_time / 1000 == :second")
    SingleNotification[] getNotificationByKeyAndSecond(String key, long second);

    @Query("SELECT COUNT(*) FROM single_notification_table WHERE package_name == :pkg AND is_blocked == 1 AND is_read == 0")
    int countUnreadBlocked(String pkg);

    @Query("UPDATE single_notification_table SET is_read = 1 WHERE package_name == :pkg AND is_read == 0")
    void markAllRead(String pkg);
}
