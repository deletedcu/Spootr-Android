package com.ottawa.spootr2.location;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.support.v4.content.LocalBroadcastManager;

import java.util.HashMap;

public class MyBroadcastManager {

    private static MyBroadcastManager instance;

    public static MyBroadcastManager getInstance(Context context) {
        if (instance == null) {
            instance = new MyBroadcastManager(context);
        }

        instance.setContext(context);

        return instance;
    }

    private Context mContext;

    private HashMap<Activity, BroadcastReceiver> mBroadcastMap = new HashMap();

    private MyBroadcastManager(Context context) {

    }

    private void setContext(Context context) {
        mContext = context;
    }

    public void sendBroadcast(String broadcast) {
        Intent intent = new Intent(broadcast);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    public void sendBroadcast(String broadcast, String data) {
        Intent intent = new Intent(broadcast);
        intent.putExtra("data", data);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    public void sendBroadcast(String broadcast, Location location) {
        Intent intent = new Intent(broadcast);
        intent.putExtra("data", location);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }


    public void receiveBroadcast(Activity activity, String broadcast, final MyBroadcastReceiver broadcastReceiver) {
        BroadcastReceiver br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (broadcastReceiver != null) {
                    broadcastReceiver.onReceive(intent);
                }
            }
        };

        mBroadcastMap.put(activity, br);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(br, new IntentFilter(broadcast));
    }

    public void stopBroadcast(Activity activity) {
        BroadcastReceiver receiver = mBroadcastMap.get(activity);
        if (receiver != null) {
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(receiver);
        }
    }

    public interface MyBroadcastReceiver {
        void onReceive(Intent intent);
    }
}
