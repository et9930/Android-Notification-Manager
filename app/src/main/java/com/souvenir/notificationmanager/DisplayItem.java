package com.souvenir.notificationmanager;

import java.util.List;

public class DisplayItem {
    public static final int TYPE_GROUP = 0;
    public static final int TYPE_DETAIL = 1;

    public int type;
    public SingleNotification notification;  // the first/latest notification
    public int count;
    public List<SingleNotification> allNotifications;  // all in this group
    public boolean expanded;

    public boolean isUnread;

    public DisplayItem(int type, SingleNotification notification, int count,
                       List<SingleNotification> all, boolean expanded, boolean isUnread) {
        this.type = type;
        this.notification = notification;
        this.count = count;
        this.allNotifications = all;
        this.expanded = expanded;
        this.isUnread = isUnread;
    }
}
