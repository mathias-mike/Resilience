package com.psyclone.resilience.messages;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.psyclone.resilience.models.Message;

import java.util.ArrayList;

public class GroupMessageViewModel extends ViewModel {

    private GroupMessageRepository groupMessageRepository;

    public GroupMessageViewModel () {
        groupMessageRepository = GroupMessageRepository.getInstance();
    }

    public LiveData<ArrayList<Message>> getMessages() {
        return groupMessageRepository.getMessages();
    }

    public void fetchMessageApi(String topic, String token){
        groupMessageRepository.fetchMessageApi(topic, token);
    }

    public boolean isNotFetching(){
        return !groupMessageRepository.isFetching();
    }

    public void setFetching (boolean isFetching){
        groupMessageRepository.setFetching(isFetching);
    }

    public void cancelRequest (){
        groupMessageRepository.cancelRequest();
    }

    public LiveData<Boolean> isFetchMessageRequestTimedOut(){
        return groupMessageRepository.isFetchMessageRequestTimedOut();
    }

    public void sendMessage (Message message) {
        groupMessageRepository.sendMessage(message);
    }

    public LiveData<Message> getSendMessageResponse(){
        return groupMessageRepository.getSendMessageResponse();
    }

    public boolean isSMRObservationPermitted() {
        return groupMessageRepository.isSMRObservationPermitted();
    }

    public void setSMRObservationPermitted(boolean SMRObservationPermitted) {
        groupMessageRepository.setSMRObservationPermitted(SMRObservationPermitted);
    }

}
