package com.psyclone.resilience.services;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.psyclone.resilience.models.Message;
import com.psyclone.resilience.utils.GsonSingleton;

import java.util.Map;

import static com.psyclone.resilience.utils.Constants.MESSAGE;
import static com.psyclone.resilience.utils.Constants.MESSAGING_UPDATE;

public class FCMMessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Map<String, String> data = remoteMessage.getData();

        String messageStr = data.get("object");
        if(messageStr != null) {
            Message newMessage = GsonSingleton.getInstance().fromJson(messageStr, Message.class);
            Intent messageBroadcast = new Intent(MESSAGING_UPDATE);
            messageBroadcast.putExtra(MESSAGE, newMessage);
            LocalBroadcastManager.getInstance(this).sendBroadcast(messageBroadcast);
        }
    }
}
