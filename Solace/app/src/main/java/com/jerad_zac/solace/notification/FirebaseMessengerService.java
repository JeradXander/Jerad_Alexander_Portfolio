package com.jerad_zac.solace.notification;

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
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.jerad_zac.solace.activities.StartingActivity;

import java.util.Objects;

import static android.content.ContentValues.TAG;

public class FirebaseMessengerService extends FirebaseMessagingService {
    public FirebaseMessengerService() {
    }

    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        //sendRegistrationToServer(token);
    }
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // ...

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            if (/* Check if data needs to be processed by long running job */ true) {
                // For long-running tasks (10 seconds or more) use Firebase Job Dispatcher.
              //  scheduleJob();
            } else {
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

        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }

    private void sendNormalNotification(RemoteMessage remoteMessage) {
        String user = remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        int i = Integer.parseInt(Objects.requireNonNull(user).replaceAll("[\\D]",""));
        Intent intent = new Intent(this, StartingActivity.class);
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
        Intent intent = new Intent(this, StartingActivity.class);
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
