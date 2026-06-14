package com.souvenir.notificationmanager;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainViewModel extends AndroidViewModel {

    private final MutableLiveData<List<String>> appList = new MutableLiveData<>(new ArrayList<>());
    private final NotificationManagement nm;
    private final PackageManager pm;

    private String searchWord = "";
    private int sortType = 0;

    private final Handler loadHandler = new Handler(msg -> {
        if (msg.what == 1) {
            refreshList();
        }
        return true;
    });

    public MainViewModel(@NonNull Application application) {
        super(application);
        nm = NotificationManagement.GetInstance(application);
        pm = application.getPackageManager();
    }

    public LiveData<List<String>> getAppList() {
        return appList;
    }

    public void loadData() {
        nm.LoadAllDataFromDb(loadHandler);
    }

    public void setSearchWord(String word) {
        this.searchWord = word != null ? word.trim() : "";
        refreshList();
    }

    public void setSortType(int sortType) {
        this.sortType = sortType;
        refreshList();
    }

    private void refreshList() {
        Set<String> packageNamesSet = new HashSet<>();

        for (ApplicationInfo info : pm.getInstalledApplications(0)) {
            if (matchesFilter(info.packageName, info.loadLabel(pm).toString())) {
                packageNamesSet.add(info.packageName);
            }
        }
        for (PackageInfo info : pm.getInstalledPackages(0)) {
            if (matchesFilter(info.packageName, info.applicationInfo.loadLabel(pm).toString())) {
                packageNamesSet.add(info.packageName);
            }
        }

        List<String> list = new ArrayList<>(packageNamesSet);

        if (sortType == 0) {
            Collections.sort(list, (o1, o2) -> {
                AppData d1 = nm.GetAppData(o1);
                AppData d2 = nm.GetAppData(o2);
                long t1 = 0, t2 = 0;
                for (SingleNotification sn : d1.singleNotifications) {
                    if (sn.sendTime > t1) t1 = sn.sendTime;
                }
                for (SingleNotification sn : d2.singleNotifications) {
                    if (sn.sendTime > t2) t2 = sn.sendTime;
                }
                return Long.compare(t2, t1);
            });
        } else if (sortType == 1) {
            Collections.sort(list, (o1, o2) -> {
                AppData d1 = nm.GetAppData(o1);
                AppData d2 = nm.GetAppData(o2);
                int b1 = 0, b2 = 0;
                for (SingleNotification sn : d1.singleNotifications) {
                    if (sn.isBlocked) b1++;
                }
                for (SingleNotification sn : d2.singleNotifications) {
                    if (sn.isBlocked) b2++;
                }
                return Integer.compare(b2, b1);
            });
        }

        appList.setValue(list);
    }

    private boolean matchesFilter(String packageName, String label) {
        if (searchWord.isEmpty()) return true;
        return packageName.contains(searchWord) || label.contains(searchWord);
    }
}
