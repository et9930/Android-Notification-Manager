package com.souvenir.notificationmanager;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface AppNotificationDataDao {
    // app notification data
    @Insert
    void addAppNotificationData(AppNotificationData... data);

    @Update
    void updateAppNotificationData(AppNotificationData... data);

    @Delete
    void deleteAppNotificationData(AppNotificationData... data);

    @Query("SELECT * FROM app_notification_data_table")
    AppNotificationData[] getAllAppNotificationData();

    @Query("SELECT * FROM app_notification_data_table WHERE package_name == :packageName")
    AppNotificationData[] getAppNotificationDataByPackageName(String packageName);



}
