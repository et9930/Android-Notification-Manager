package com.souvenir.notificationmanager;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "app_notification_data_table")
public class AppNotificationData {
    @PrimaryKey
    @ColumnInfo(name = "package_name")
    @NonNull
    public String packageName;

    public int mode;

    @ColumnInfo(name = "white_list")
    public String whiteList;

    @ColumnInfo(name = "black_list")
    public String blackList;
}
