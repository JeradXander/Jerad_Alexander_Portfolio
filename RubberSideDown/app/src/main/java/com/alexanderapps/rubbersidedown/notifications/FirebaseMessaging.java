package com.alexanderapps.rubbersidedown.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.alexanderapps.rubbersidedown.activities.ChatActivity;
import com.alexanderapps.rubbersidedown.services.FirebaseMessengerService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Objects;

public class FirebaseMessaging  extends FirebaseMessengerService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        //get current user from Shared Preferences
        SharedPreferences sp = getSharedPreferences("SP_USER", MODE_PRIVATE);

        SharedPreferences.Editor editor = sp.edit();
        String savedCurrentUser = sp.getString("Current_USERID","none");

        String sent = remoteMessage.getData().get("sent");
        String user = remoteMessage.getData().get("user");
        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        if (fUser != null && Objects.requireNonNull(sent).equals(fUser.getUid())){
            if (!savedCurrentUser.equals(user)){
                if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O){
                    sendOAndAboveNotification(remoteMessage);
                }else {
                    sendNormalNotification(remoteMessage);
                }
            }
        }
    }

    private void sendNormalNotification(RemoteMessage remoteMessage) {
        String user = remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        int i = Integer.parseInt(Objects.requireNonNull(user).replaceAll("[\\D]",""));
        Intent intent = new Intent(this, ChatActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("uid", user);
        intent.putExtras(bundle);

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, i,intent,PendingIntent.FLAG_ONE_SHOT);

        Uri defSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(Integer.parseInt(Objects.requireNonNull(icon)))
                .setContentText(body)
                .setContentTitle(title)
                .setSound(defSoundUri)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        int j = 0;
        if (i>0){
            j=1;
        }
        Objects.requireNonNull(notificationManager).notify(j,builder.build());

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void sendOAndAboveNotification(RemoteMessage remoteMessage) {

        String user = remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        int i = Integer.parseInt(Objects.requireNonNull(user).replaceAll("[\\D]",""));
        Intent intent = new Intent(this, ChatActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("uid", user);
        intent.putExtras(bundle);

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, i,intent,PendingIntent.FLAG_ONE_SHOT);

        Uri defSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        OreoAndAboveNotification notification1 = new OreoAndAboveNotification(this,notificationManager);

        Notification.Builder builder = notification1.getNotifications(title,body,pendingIntent,defSoundUri,icon);


        int j = 0;
        if (i>0){
            j=1;
        }
        notification1.getManager().notify(j,builder.build());
    }
}
