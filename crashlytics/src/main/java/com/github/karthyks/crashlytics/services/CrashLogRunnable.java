package com.github.karthyks.crashlytics.services;


import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import com.github.karthyks.crashlytics.dao.EventDAO;
import com.github.karthyks.crashlytics.model.EventModel;
import com.github.karthyks.crashlytics.provider.LocalStoreContract;
import com.github.karthyks.crashlytics.transaction.EventTransaction;
import com.github.karthyks.crashlytics.utils.CrashUtils;

import java.util.LinkedList;
import java.util.List;

public class CrashLogRunnable implements Runnable {

  private static final String TAG = CrashLogRunnable.class.getSimpleName();
  private Context context;
  private String company;
  private String username;
  private String tag;
  private String info;
  private long time;

  public CrashLogRunnable(Context context, String company, String username, String tag,
                          String info, long time) {
    this.context = context;
    this.company = company;
    this.username = username;
    this.tag = tag;
    this.info = info;
    this.time = time;
  }

  @Override
  public void run() {
    android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
    EventModel eventModel = new EventModel();
    eventModel.setAppName(CrashUtils.getApplicationName(context));
    eventModel.setAppVersion(CrashUtils.getAppVersion(context));
    eventModel.setAndroidVersion(CrashUtils.getOsVersion());
    eventModel.setDeviceModel(CrashUtils.getDeviceMake());
    eventModel.setUsername(username);
    eventModel.setCompany(company);
    eventModel.setTag(tag);
    eventModel.setInfo(info);
    eventModel.setTime(time);
    pushToCloud(eventModel);
  }

  private void pushToCloud(EventModel eventModel) {
    List<EventModel> eventModelList = new LinkedList<>();
    eventModelList.add(eventModel);
    EventTransaction eventTransaction = new EventTransaction();
    try {
      eventTransaction.postEvent(eventModelList);
    } catch (Exception e) {
      saveLocal(eventModel);
      e.printStackTrace();
    }
  }

  private void saveLocal(EventModel eventModel) {
    String authority = LocalStoreContract.getAuthority(context);
    ContentResolver contentResolver = context.getContentResolver();
    Uri eventUri = LocalStoreContract.getContentUri(authority,
        LocalStoreContract.EventStore.TABLE_NAME);
    EventDAO eventDAO = new EventDAO(authority, contentResolver, eventUri);
    eventDAO.insertOne(eventModel);
  }
}
