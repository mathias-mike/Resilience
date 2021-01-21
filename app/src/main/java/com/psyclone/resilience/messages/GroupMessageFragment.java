package com.psyclone.resilience.messages;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.psyclone.resilience.R;
import com.psyclone.resilience.databinding.FragmentGroupMessagesBinding;
import com.psyclone.resilience.models.Message;
import com.psyclone.resilience.models.User;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;

import static com.psyclone.resilience.utils.Constants.MESSAGE;
import static com.psyclone.resilience.utils.Constants.MESSAGING_UPDATE;

public class GroupMessageFragment extends Fragment implements GroupMessageAdapter.RetryMessageClickListener {

    private FragmentGroupMessagesBinding binding;

    private GroupMessageAdapter adapter;

    private User me;

    private String topic, token;

    private GroupMessageViewModel groupMessageViewModel;

    private static MutableLiveData<String> observableTopic = new MutableLiveData<>();

    private ArrayList<Message> messages;

    private MessagingUpdateReceiver messagingUpdateReceiver;

    private Handler UIHandler = new Handler();

    public GroupMessageFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_group_messages, container, false);

        assert getArguments() != null;
        token = getArguments().getString("token");
        me = (User) getArguments().getSerializable("me");
        topic = getArguments().getString("topic");

        observableTopic.setValue(topic);

        groupMessageViewModel = new ViewModelProvider(this).get(GroupMessageViewModel.class);
        groupMessageViewModel.setFetching(true);
        groupMessageViewModel.setSMRObservationPermitted(false);

        groupMessageViewModel.getMessages().observe(getViewLifecycleOwner(), messages -> {
            if(groupMessageViewModel.isNotFetching()) {
                if(messages != null) {
                    this.messages = messages;
                    adapter.setMessages(this.messages);
                    if (adapter.getItemCount() > 1)
                        binding.messageRecyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);

                    binding.loading.setVisibility(View.GONE);
                    binding.loadingView.setVisibility(View.GONE);
                } else {
                    Toast.makeText(getContext(), "Oops! An error occurred, check network setting.",
                            Toast.LENGTH_LONG).show();
                    requireActivity().onBackPressed();
                }
            }
        });

        groupMessageViewModel.isFetchMessageRequestTimedOut()
                .observe(getViewLifecycleOwner(), isFetchMessagingRequestTimedOut -> {
                    if(groupMessageViewModel.isNotFetching() && isFetchMessagingRequestTimedOut) {
                        Toast.makeText(getContext(), "Request timed out! Check network setting.",
                                Toast.LENGTH_LONG).show();
                        requireActivity().onBackPressed();
                    }
                });

        groupMessageViewModel.getSendMessageResponse().observe(getViewLifecycleOwner(), sendMessageResponse -> {
            if(groupMessageViewModel.isSMRObservationPermitted()){
                if(!sendMessageResponse.isSent()) {
                    Toast.makeText(getContext(), "Oops! An error occurred, check network setting.",
                            Toast.LENGTH_LONG).show();
                    new Thread(() -> {
                        int index = messages.indexOf(sendMessageResponse);
                        messages.set(index, sendMessageResponse);
                        UIHandler.post(() -> adapter.notifyItemChanged(index));
                    }).start();
                }
            }
        });

        adapter = new GroupMessageAdapter(getActivity(), me, this);
        binding.messageRecyclerView.setAdapter(adapter);

        binding.sendMessage.setOnClickListener(v -> {
            String msg = binding.startAMessage.getText().toString();
            if(!TextUtils.isEmpty(msg) && me.get_id() != null){
                Message message = new Message();
                message.setMessage(msg);
                message.setTopic(topic);
                message.setUser(me);
                message.setTimestamp(Calendar.getInstance().getTimeInMillis());

                groupMessageViewModel.sendMessage(message);

                binding.startAMessage.setText(null);
                messages.add(message);
                adapter.notifyItemInserted(messages.size() - 1);
                if(messages.size() > 1) adapter.notifyItemChanged(messages.size() - 2);
                binding.messageRecyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);
            }
        });

        return binding.getRoot();
    }

    public static LiveData<String> getObservableTopic() {
        return observableTopic;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Fetch message from server
        fetchTopicMessage(topic, token);

        // Registering network broadcast receiver
        messagingUpdateReceiver = new MessagingUpdateReceiver(this);
        LocalBroadcastManager.getInstance(requireContext())
                .registerReceiver(messagingUpdateReceiver, new IntentFilter(MESSAGING_UPDATE));
    }

    private void fetchTopicMessage(String topic, String token){
        groupMessageViewModel.fetchMessageApi(topic, token);
    }

    @Override
    public void onRetryMessageClicked(Message message) {
        message.setSent(true);
        groupMessageViewModel.sendMessage(message);
        new Thread(() -> {
            int index = messages.indexOf(message);
            messages.remove(index);
            UIHandler.post(() -> adapter.notifyItemRemoved(index));
            messages.add(message);
            UIHandler.post(() -> {
                adapter.notifyItemInserted(messages.size() - 1);
                if(messages.size() > 1) adapter.notifyItemChanged(messages.size() - 2);
                binding.messageRecyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);
            });
        }).start();
    }

    private static class MessagingUpdateReceiver extends BroadcastReceiver {

        private GroupMessageFragment msgFragment;

        public MessagingUpdateReceiver (GroupMessageFragment msgFragment){
            this.msgFragment = msgFragment;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Message message = (Message) intent.getSerializableExtra(MESSAGE);
            if (message != null && message.getTopic().equals(msgFragment.topic)
                    && !message.getUser().get_id().equals(msgFragment.me.get_id())) {
                msgFragment.messages.add(message);
                msgFragment.adapter.notifyItemInserted(msgFragment.messages.size() - 1);
                if(msgFragment.messages.size() > 1)
                    msgFragment.adapter.notifyItemChanged(msgFragment.messages.size() - 2);
                msgFragment.binding.messageRecyclerView.smoothScrollToPosition(msgFragment.adapter.getItemCount() - 1);
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if(messagingUpdateReceiver != null) LocalBroadcastManager.getInstance(requireContext())
                .unregisterReceiver(messagingUpdateReceiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        groupMessageViewModel.cancelRequest();
    }

}