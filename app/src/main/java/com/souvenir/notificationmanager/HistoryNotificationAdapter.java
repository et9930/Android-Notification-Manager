package com.souvenir.notificationmanager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class HistoryNotificationAdapter extends BaseAdapter {

    static class ViewHolder {
        TextView timeText;
        TextView titleText;
        TextView contentText;
        TextView stateText;
    }

    Context context;
    List<SingleNotification> notifications;
    LayoutInflater inflater;

    public HistoryNotificationAdapter(Context context, List<SingleNotification> notifications) {
        this.context = context;
        this.notifications = notifications;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return notifications != null ? notifications.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return notifications.get(position);
    }

    @Override
    public long getItemId(int position) {
        return notifications.get(position).hashCode();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.activity_history_notification, parent, false);
            holder = new ViewHolder();
            holder.timeText = convertView.findViewById(R.id.TimeText);
            holder.titleText = convertView.findViewById(R.id.TitleText);
            holder.contentText = convertView.findViewById(R.id.ContentText);
            holder.stateText = convertView.findViewById(R.id.StateText);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        SingleNotification notification = (SingleNotification) getItem(position);

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        holder.timeText.setText(format.format(notification.sendTime));
        holder.titleText.setText(notification.title);
        holder.contentText.setText(notification.content);
        holder.stateText.setText(notification.isBlocked ? "已拦截" : "未拦截");

        return convertView;
    }
}
