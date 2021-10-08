package com.souvenir.notificationmanager;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class AppListItemAdapter extends BaseAdapter {
    static class ViewHolder {
        TextView packageName;
        ImageView packageIcon;
        TextView packageState;
    }

    Context context;
    List<String> packageInfos;
    LayoutInflater inflter;
    PackageManager pm;

    public AppListItemAdapter(Context applicationContext, List<String> packageInfos, PackageManager pm) {
        this.context = applicationContext;
        this.packageInfos = packageInfos;
        this.pm = pm;
        inflter = (LayoutInflater.from(applicationContext));
    }

    @Override
    public int getCount() {
        return packageInfos.size();
    }

    @Override
    public Object getItem(int i) {
        return packageInfos.get(i);
    }

    @Override
    public long getItemId(int i) {
        return packageInfos.get(i).hashCode();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if (view == null) {
            view = inflter.inflate(R.layout.activity_app_list_view_item, null);
            holder = new ViewHolder();

            holder.packageIcon = (ImageView) view.findViewById(R.id.packageIcon);
            holder.packageName = (TextView) view.findViewById(R.id.packageName);
            holder.packageState = (TextView) view.findViewById(R.id.packageState) ;
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        try {
            PackageInfo item = pm.getPackageInfo((String) this.getItem(i), 0);
            holder.packageIcon.setImageDrawable(item.applicationInfo.loadIcon(pm));
            holder.packageName.setText(item.applicationInfo.loadLabel(pm));

            AppData appData = NotificationManagement.GetInstance(context).GetAppData(item.packageName);

            if (item != null && item.packageName == item.packageName) {
                String state = "";
                if (appData.appNotificationData.mode == AppNotificationMode.NONE) {
                    state = "不拦截";
                } else if (appData.appNotificationData.mode == AppNotificationMode.USE_WHITE_LIST) {
                    state = "白名单模式";
                } else if (appData.appNotificationData.mode == AppNotificationMode.USE_BLACK_LIST) {
                    state = "黑名单模式";
                }

                long lastTime = 0;
                int blockNumber = 0;
                for (SingleNotification notification:
                     appData.singleNotifications) {
                    if (notification.isBlocked) {
                        blockNumber ++;
                    }
                    if (notification.sendTime > lastTime) {
                        lastTime = notification.sendTime;
                    }
                }

                String times = "无";

                if (lastTime != 0) {
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    times = format.format(new Date(lastTime));
                }

                holder.packageState.setText("当前模式:" + state + "\n上次通知时间:" + times + "\n拦截次数:" + blockNumber);
            }

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return view;
    }
}
