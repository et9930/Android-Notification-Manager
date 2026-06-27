package com.souvenir.notificationmanager;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "single_notification_table")
public class SingleNotification {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "package_name")
    public String packageName;

    @ColumnInfo(name = "send_time")
    public long sendTime;

    public String title;

    public String content;

    @ColumnInfo(name = "is_blocked")
    public boolean isBlocked;

    @ColumnInfo(name = "notification_key")
    public String notificationKey;

    @ColumnInfo(name = "is_read", defaultValue = "1")
    public boolean isRead;

}
