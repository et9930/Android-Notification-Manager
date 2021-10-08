package com.souvenir.notificationmanager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;

public class HistoryNotificationAdapter extends BaseAdapter {
    static class ViewHolder {
        TextView timeText;
        TextView titleText;
        TextView contentText;
        TextView stateText;
    }

    Context context;
    List<SingleNotification> notifications;
    LayoutInflater inflter;

    public HistoryNotificationAdapter(Context applicationContext, List<SingleNotification> notifications) {
        this.context = applicationContext;
        this.notifications = notifications;
        inflter = (LayoutInflater.from(applicationContext));
    }

    @Override
    public int getCount() {
        return notifications.size();
    }

    @Override
    public Object getItem(int i) {
        return notifications.get(i);
    }

    @Override
    public long getItemId(int i) {
        return notifications.get(i).hashCode();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if (view == null) {
            view = inflter.inflate(R.layout.activity_history_notification, null);
            holder = new ViewHolder();

            holder.timeText = (TextView) view.findViewById(R.id.TimeText);
            holder.titleText = (TextView) view.findViewById(R.id.TitleText);
            holder.contentText = (TextView) view.findViewById(R.id.ContentText) ;
            holder.stateText = (TextView) view.findViewById(R.id.StateText) ;

            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        SingleNotification notification = (SingleNotification) this.getItem(i);

        SimpleDateFormat format =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //设置格式
        holder.timeText.setText(format.format(notification.sendTime));                                //获得带格式的字符串
        holder.titleText.setText(notification.title);
        holder.contentText.setText(notification.content);
        holder.stateText.setText(notification.isBlocked ? "已拦截" : "未拦截");

        return view;
    }
}
