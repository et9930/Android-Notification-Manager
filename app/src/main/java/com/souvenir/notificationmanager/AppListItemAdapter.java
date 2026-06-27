package com.souvenir.notificationmanager;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AppListItemAdapter extends BaseAdapter {

    static class ViewHolder {
        ImageView packageIcon;
        TextView packageName;
        TextView packageState;
    }

    Context context;
    List<String> packageInfos;
    LayoutInflater inflater;
    PackageManager pm;

    public AppListItemAdapter(Context context, List<String> packageInfos, PackageManager pm) {
        this.context = context;
        this.packageInfos = packageInfos;
        this.pm = pm;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return packageInfos != null ? packageInfos.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return packageInfos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return packageInfos.get(position).hashCode();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.activity_app_list_view_item, parent, false);
            holder = new ViewHolder();
            holder.packageIcon = convertView.findViewById(R.id.packageIcon);
            holder.packageName = convertView.findViewById(R.id.packageName);
            holder.packageState = convertView.findViewById(R.id.packageState);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        try {
            PackageInfo item = pm.getPackageInfo((String) getItem(position), 0);
            holder.packageIcon.setImageDrawable(item.applicationInfo.loadIcon(pm));
            holder.packageName.setText(item.applicationInfo.loadLabel(pm));

            AppData appData = NotificationManagement.GetInstance(context).GetAppData(item.packageName);

            String stateText;
            int mode = appData.appNotificationData.mode;
            if (mode == AppNotificationMode.USE_WHITE_LIST) {
                stateText = "白名单模式";
            } else if (mode == AppNotificationMode.USE_BLACK_LIST) {
                stateText = "黑名单模式";
            } else {
                stateText = "不拦截";
            }

            long lastTime = 0;
            int blockNumber = 0;
            for (SingleNotification sn : appData.singleNotifications) {
                if (sn.isBlocked) blockNumber++;
                if (sn.sendTime > lastTime) lastTime = sn.sendTime;
            }

            String times = lastTime != 0
                    ? new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date(lastTime))
                    : "N/A";

            int unreadBlocked = NotificationManagement.GetInstance(context)
                    .countUnreadBlocked(item.packageName);
            String unreadInfo = unreadBlocked > 0 ? "\n未读已拦截:" + unreadBlocked : "";
            holder.packageState.setText("当前模式:" + stateText
                    + "\n上次通知时间:" + times
                    + "\n拦截次数:" + blockNumber + unreadInfo);

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return convertView;
    }
}
