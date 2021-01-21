package com.psyclone.resilience.networking;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.psyclone.resilience.AppExecutors;
import com.psyclone.resilience.models.Message;
import com.psyclone.resilience.utils.GsonSingleton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Response;

import static com.psyclone.resilience.utils.Constants.NETWORK_TIMEOUT;
import static com.psyclone.resilience.utils.Constants.TAG;

@SuppressWarnings("rawtypes")
public class GroupMessageApiClient {

    private MutableLiveData<ArrayList<Message>> mMessages;

    private static GroupMessageApiClient instance;

    private FetchMessagesRunnable fetchMessagesRunnable;

    private SendMessageRunnable sendMessageRunnable;

    private MutableLiveData<Boolean> fetchMessageRequestTimeout = new MutableLiveData<>();

    private MutableLiveData<Message> sendMessageResponse = new MutableLiveData<>();

    private boolean isFetching;

    private boolean isSMRObservationPermitted;

    private Future fetchMessageFuture;

    private Future sendMessageFuture;

    public boolean isFetching() {
        return isFetching;
    }

    public void setFetching(boolean isFetching) {
        this.isFetching = isFetching;
    }

    public boolean isSMRObservationPermitted() {
        return isSMRObservationPermitted;
    }

    public void setSMRObservationPermitted(boolean SMRObservationPermitted) {
        isSMRObservationPermitted = SMRObservationPermitted;
    }

    public static GroupMessageApiClient getInstance() {
        if(instance == null) instance = new GroupMessageApiClient();
        return instance;
    }

    private GroupMessageApiClient () {
        mMessages = new MutableLiveData<>();
    }

    public LiveData<ArrayList<Message>> getmMessages(){
        return mMessages;
    }

    public LiveData<Boolean> isFetchMessageRequestTimedOut(){
        return fetchMessageRequestTimeout;
    }

    public LiveData<Message> getSendMessageResponse(){
        return sendMessageResponse;
    }

    public void fetchMessages(String topic, String token) {
        if(fetchMessagesRunnable != null) fetchMessagesRunnable = null;
        fetchMessagesRunnable = new FetchMessagesRunnable(topic, token);
        final Future handler = AppExecutors.getInstance().networkIO().submit(fetchMessagesRunnable);

        fetchMessageFuture = AppExecutors.getInstance().networkIO().schedule(() -> {
            if(isFetching) {
                isFetching = false;
                fetchMessageRequestTimeout.postValue(true);
                handler.cancel(true);
            }
        }, NETWORK_TIMEOUT, TimeUnit.MILLISECONDS);
    }

    public void sendMessage(Message message) {
        if(sendMessageRunnable != null) sendMessageRunnable = null;
        sendMessageRunnable = new SendMessageRunnable(message);
        final Future handler = AppExecutors.getInstance().networkIO().submit(sendMessageRunnable);

        sendMessageFuture = AppExecutors.getInstance().networkIO().schedule(() -> {
            if(!isSMRObservationPermitted) {
                isSMRObservationPermitted = true;
                message.setSent(false);
                sendMessageResponse.postValue(message);
                handler.cancel(true);
            }
        }, NETWORK_TIMEOUT, TimeUnit.MILLISECONDS);
    }

    private class FetchMessagesRunnable implements Runnable {
        String topic;
        String token;
        boolean cancelRequest;

        public FetchMessagesRunnable(String topic, String token) {
            this.topic = topic;
            this.token = token;
            cancelRequest = false;
        }

        @Override
        public void run() {
            try {
                Response<ArrayList<Message>> response = getMessages(topic, token).execute();
                // Notify that message has completed fetch
                isFetching = false;
                if(cancelRequest){
                    Log.w(TAG, "Request cancelled!");
                    return;
                }

                if(response.code() == 200){
                    ArrayList<Message> messages = response.body();
                    mMessages.postValue(messages);
                } else {
                    String error = response.errorBody().string();
                    Log.e(TAG, "run: " + error);
                    mMessages.postValue(null);
                }
            } catch (IOException e) {
                isFetching = false;
                Log.e(TAG, "run: " + e);
                mMessages.postValue(null);
            }
        }

        private Call<ArrayList<Message>> getMessages (String topic, String token) {
            return ServiceGenerator.getGroupMessageApi().getAllTopicMessage(topic, token);
        }

        private void cancelRequest() {
            cancelRequest = true;
        }
    }

    private class SendMessageRunnable implements Runnable {
        Message message;
        boolean cancelRequest;

        public SendMessageRunnable(Message message) {
            this.message = message;
            cancelRequest = false;
        }

        @Override
        public void run() {
            try {
                Response<Message> response = sendMessage(message.getMessage(), message.getTopic(),
                        GsonSingleton.getInstance().toJson(message.getUser()), message.getTimestamp()).execute();
                // Notify that message has completed fetch
                isSMRObservationPermitted = true;
                if(cancelRequest){
                    Log.w(TAG, "Request cancelled!");
                    return;
                }

                if(response.code() == 200){
                    sendMessageResponse.postValue(response.body());
                } else {
                    String error = response.errorBody().string();
                    Log.e(TAG, "run: " + error);
                    message.setSent(false);
                    sendMessageResponse.postValue(message);
                }
            } catch (IOException e) {
                isSMRObservationPermitted = true;
                Log.e(TAG, "run: " + e);
                message.setSent(false);
                sendMessageResponse.postValue(message);
            }
        }

        private Call<Message> sendMessage (String message, String topic, String user, long timestamp) {
            return ServiceGenerator.getGroupMessageApi().sendMessage(message, topic, user, timestamp);
        }

        private void cancelRequest() {
            cancelRequest = true;
        }
    }

    public void cancelRequests() {
        if(fetchMessagesRunnable != null) {
            fetchMessagesRunnable.cancelRequest();
            fetchMessageFuture.cancel(true);
        }
        if(sendMessageRunnable != null) {
            sendMessageRunnable.cancelRequest();
            sendMessageFuture.cancel(true);
        }
    }

}
