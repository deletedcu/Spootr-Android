package com.ottawa.spootr2.gcm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.gcm.GcmListenerService;
import com.ottawa.spootr2.R;
import com.ottawa.spootr2.activity.MainActivity;
import com.ottawa.spootr2.common.Constants;
import com.ottawa.spootr2.common.SharedData;

/**
 * Created by king on 02/02/16.
 */
public class PushNotificationService extends GcmListenerService {
    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString("message");
        String type = data.getString("type");
        WakeLocker.acquire(getApplicationContext());
        WakeLocker.release();

        Intent intent = new Intent();
        if (type.equals("chat")) {
            int messageCount = SharedData.getInstance().messageCount;
            messageCount ++;
            SharedData.getInstance().messageCount = messageCount;
            String content = data.getString("content");
            int senderId = Integer.parseInt(data.getString("senderId"));
            intent.setAction("message");
            intent.putExtra("content", content);
            intent.putExtra("senderId", senderId);
            intent.putExtra("fromme", false);
        } else if (type.equals("react") || type.equals("comment")) {
            int notificationCount = SharedData.getInstance().notificationCount;
            notificationCount ++;
            SharedData.getInstance().notificationCount = notificationCount;
            intent.setAction("notification");
        }
        sendBroadcast(intent);
    }

    private void createNotification(String message) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("Spootr")
                .setContentText(message);

        Intent resultIntent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);

        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        mBuilder.setAutoCancel(true);

        mBuilder.setPriority(Notification.PRIORITY_HIGH);

        mBuilder.setDefaults(Notification.DEFAULT_VIBRATE);

        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(Constants.NOTIFICATION_ID, mBuilder.build());

        Intent intent = new Intent();
        intent.setAction("com.ottawa.spootr2.Broadcast");
        intent.putExtra("message", message);
        sendBroadcast(intent);

    }

}
