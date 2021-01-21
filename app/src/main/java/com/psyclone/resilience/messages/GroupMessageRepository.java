package com.psyclone.resilience.messages;

import androidx.lifecycle.LiveData;

import com.psyclone.resilience.models.Message;
import com.psyclone.resilience.networking.GroupMessageApiClient;

import java.util.ArrayList;

public class GroupMessageRepository {

    private GroupMessageApiClient groupMessageApiClient;

    private static GroupMessageRepository instance;

    public static GroupMessageRepository getInstance(){
        if(instance == null) instance = new GroupMessageRepository();
        return instance;
    }

    private GroupMessageRepository () {
        groupMessageApiClient = GroupMessageApiClient.getInstance();
    }

    public LiveData<ArrayList<Message>> getMessages (){
        return groupMessageApiClient.getmMessages();
    }

    public void fetchMessageApi(String topic, String token){
        groupMessageApiClient.fetchMessages(topic, token);
    }

    public boolean isFetching(){
        return groupMessageApiClient.isFetching();
    }

    public void setFetching (boolean isFetching) {
        groupMessageApiClient.setFetching(isFetching);
    }

    public void cancelRequest (){
        groupMessageApiClient.cancelRequests();
    }

    public LiveData<Boolean> isFetchMessageRequestTimedOut(){
        return groupMessageApiClient.isFetchMessageRequestTimedOut();
    }

    public void sendMessage (Message message) {
        groupMessageApiClient.sendMessage(message);
    }

    public LiveData<Message> getSendMessageResponse(){
        return groupMessageApiClient.getSendMessageResponse();
    }

    public boolean isSMRObservationPermitted() {
        return groupMessageApiClient.isSMRObservationPermitted();
    }

    public void setSMRObservationPermitted(boolean SMRObservationPermitted) {
        groupMessageApiClient.setSMRObservationPermitted(SMRObservationPermitted);
    }

}
