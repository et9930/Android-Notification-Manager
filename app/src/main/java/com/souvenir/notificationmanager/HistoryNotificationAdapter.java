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

    private static final SimpleDateFormat FORMAT =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    static class GroupHolder {
        TextView timeText;
        TextView countText;
        TextView titleText;
        TextView contentText;
        TextView stateText;
    }

    static class DetailHolder {
        TextView detailTime;
        TextView detailState;
    }

    Context context;
    List<DisplayItem> items;
    LayoutInflater inflater;

    public HistoryNotificationAdapter(Context context, List<DisplayItem> items) {
        this.context = context;
        this.items = items;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return items != null ? items.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).type;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        DisplayItem item = items.get(position);
        if (item.type == DisplayItem.TYPE_GROUP) {
            return getGroupView(item, convertView, parent);
        } else {
            return getDetailView(item, convertView, parent);
        }
    }

    private View getGroupView(DisplayItem item, View convertView, ViewGroup parent) {
        GroupHolder holder;
        if (convertView == null || convertView.getTag() instanceof DetailHolder) {
            convertView = inflater.inflate(R.layout.activity_history_notification, parent, false);
            holder = new GroupHolder();
            holder.timeText = convertView.findViewById(R.id.TimeText);
            holder.countText = convertView.findViewById(R.id.CountText);
            holder.titleText = convertView.findViewById(R.id.TitleText);
            holder.contentText = convertView.findViewById(R.id.ContentText);
            holder.stateText = convertView.findViewById(R.id.StateText);
            convertView.setTag(holder);
        } else {
            holder = (GroupHolder) convertView.getTag();
        }

        SingleNotification sn = item.notification;
        holder.timeText.setText(FORMAT.format(sn.sendTime));
        holder.titleText.setText(sn.title);
        holder.contentText.setText(sn.content);
        holder.stateText.setText(sn.isBlocked ? "\u5df2\u62e6\u622a" : "\u672a\u62e6\u622a");
        if (item.count > 1) {
            holder.countText.setVisibility(View.VISIBLE);
            holder.countText.setText("(x" + item.count + ")");
        } else {
            holder.countText.setVisibility(View.GONE);
        }
        return convertView;
    }

    private View getDetailView(DisplayItem item, View convertView, ViewGroup parent) {
        DetailHolder holder;
        if (convertView == null || convertView.getTag() instanceof GroupHolder) {
            convertView = inflater.inflate(R.layout.item_time_detail, parent, false);
            holder = new DetailHolder();
            holder.detailTime = convertView.findViewById(R.id.DetailTime);
            holder.detailState = convertView.findViewById(R.id.DetailState);
            convertView.setTag(holder);
        } else {
            holder = (DetailHolder) convertView.getTag();
        }

        holder.detailTime.setText(FORMAT.format(item.notification.sendTime));
        holder.detailState.setText(item.notification.isBlocked ? "\u5df2\u62e6\u622a" : "\u672a\u62e6\u622a");
        return convertView;
    }
}
