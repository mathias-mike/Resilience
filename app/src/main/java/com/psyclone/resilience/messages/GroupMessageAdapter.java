package com.psyclone.resilience.messages;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.psyclone.resilience.R;
import com.psyclone.resilience.models.Message;
import com.psyclone.resilience.models.User;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import io.github.inflationx.calligraphy3.CalligraphyUtils;

public class GroupMessageAdapter extends RecyclerView.Adapter<GroupMessageAdapter.GroupMessageViewHolder> {

    private Context context;
    private User me;
    private List<Message> messages;
    private RetryMessageClickListener retryMessageClickListener;

    public interface RetryMessageClickListener{
        void onRetryMessageClicked(Message message);
    }

    public GroupMessageAdapter (Context context, User me, RetryMessageClickListener retryMessageClickListener){
        this.context = context;
        this.me = me;
        this.retryMessageClickListener = retryMessageClickListener;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GroupMessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.recycler_group_message_item, parent, false);
        return new GroupMessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupMessageViewHolder holder, int position) {
        Message message = messages.get(position);
        String formattedTime = sortTime(message.getTimestamp());

        if(message.getUser().get_id().equals(me.get_id())){
            holder.senderName.setVisibility(View.GONE);
            holder.receivedMessage.setVisibility(View.GONE);
            holder.receivedMessageTimestamp.setVisibility(View.GONE);
            holder.senderImage.setVisibility(View.GONE);
            holder.sentMessage.setVisibility(View.VISIBLE);
            holder.sentMessageTimestamp.setVisibility(View.VISIBLE);

            holder.sentMessage.setText(message.getMessage());
            holder.sentMessageTimestamp.setText(formattedTime);
        } else {
            holder.receivedMessage.setVisibility(View.VISIBLE);
            holder.receivedMessageTimestamp.setVisibility(View.VISIBLE);
            holder.senderName.setVisibility(View.VISIBLE);
            holder.senderImage.setVisibility(View.VISIBLE);
            if(position > 0) {
                Message preMessage = messages.get(position - 1);
                if (preMessage.getUser().get_id().equals(message.getUser().get_id())) {
                    // Same user, hence don't show user name and image. Just show message
                    holder.senderName.setVisibility(View.GONE);
                    holder.senderImage.setVisibility(View.GONE);
                }
            }

            holder.receivedMessage.setText(message.getMessage());
            holder.receivedMessageTimestamp.setText(formattedTime);
            holder.senderName.setText(message.getUser().getName());
            Glide.with(context)
                    .load(message.getUser().getUrl())
                    .centerCrop()
                    .placeholder(R.color.gray)
                    .into(holder.senderImage);

            holder.sentMessage.setVisibility(View.GONE);
            holder.sentMessageTimestamp.setVisibility(View.GONE);
        }

        if(position < messages.size() - 1) {
            String message_time = new SimpleDateFormat("MM/dd/yyyy HH:mm z",
                    Locale.UK).format(new Date(messages.get(position).getTimestamp()));

            String next_message_time = new SimpleDateFormat("MM/dd/yyyy HH:mm z",
                    Locale.UK).format(new Date(messages.get(position + 1).getTimestamp()));

            if (messages.get(position + 1).getUser().get_id().equals(message.getUser().get_id())
                    && message_time.equals(next_message_time)) {

                if(message.getUser().get_id().equals(me.get_id())){
                    holder.sentMessageTimestamp.setVisibility(View.GONE);
                } else {
                    holder.receivedMessageTimestamp.setVisibility(View.GONE);
                }
            }
        }

        // Handling sent status
        if(!message.isSent()) {
            holder.sentMessageTimestamp.setText(("Retry"));
            CalligraphyUtils.applyFontToTextView(context, holder.sentMessageTimestamp,
                    "fonts/CerealMedium.ttf");
            holder.sentMessageTimestamp.setTextColor(context.getResources().getColor(R.color.red));
            holder.itemView.setOnClickListener(v -> retryMessageClickListener.onRetryMessageClicked(message));
        } else {
            CalligraphyUtils.applyFontToTextView(context, holder.sentMessageTimestamp,
                    "fonts/CerealLight.ttf");
            holder.sentMessageTimestamp.setTextColor(context.getResources().getColor(R.color.text_color));
            holder.itemView.setOnClickListener(v -> {});
        }
    }

    @Override
    public int getItemCount() {
        if(messages != null) return messages.size();
        else return 0;
    }

    public static class GroupMessageViewHolder extends RecyclerView.ViewHolder{

        TextView sentMessage, sentMessageTimestamp;
        TextView senderName, receivedMessage, receivedMessageTimestamp;
        CircleImageView senderImage;

        public GroupMessageViewHolder(@NonNull View itemView) {
            super(itemView);

            sentMessage = itemView.findViewById(R.id.sent_message);
            sentMessageTimestamp = itemView.findViewById(R.id.sent_message_timestamp);
            senderName = itemView.findViewById(R.id.sender_name);
            senderImage = itemView.findViewById(R.id.sender_image);
            receivedMessage = itemView.findViewById(R.id.received_message);
            receivedMessageTimestamp = itemView.findViewById(R.id.received_message_timestamp);
        }
    }

    private String sortTime(long timestamp){
        long currentTime = Calendar.getInstance().getTimeInMillis();
        long MILLISECONDS_IN_A_MINUTE = 60 * 1000;
        long MILLISECONDS_IN_AN_HOUR = MILLISECONDS_IN_A_MINUTE * 60;
        long MILLISECONDS_IN_A_DAY = MILLISECONDS_IN_AN_HOUR * 24;

        String timeStampStringFormat = new SimpleDateFormat("MMM/dd/yyyy HH:mm:ss z", Locale.UK)
                .format(timestamp);
        String nextDaytimeStampStringFormat = new SimpleDateFormat("MMM/dd/yyyy HH:mm:ss z", Locale.UK)
                .format(timestamp + MILLISECONDS_IN_A_DAY);
        String currentTimeStringFormat = new SimpleDateFormat("MMM/dd/yyyy HH:mm:ss z", Locale.UK)
                .format(currentTime);

        String time;
        if(currentTimeStringFormat.substring(0, 11).equals(timeStampStringFormat.substring(0, 11))){
            // We are still on the day style was posted.
            time = new SimpleDateFormat("hh:mm aa", Locale.UK).format(timestamp);
        } else if (currentTimeStringFormat.substring(0, 11).equals(nextDaytimeStampStringFormat.substring(0, 11))) {
            // This means we are on the next day
            time = "Yesterday at " + new SimpleDateFormat("hh:mm aa", Locale.UK).format(timestamp);
        } else if (((currentTime - timestamp) / MILLISECONDS_IN_A_DAY) < 6) {
            // Still in the same week
            time = new SimpleDateFormat("EEE", Locale.UK).format(timestamp)
                    + " at " + new SimpleDateFormat("hh:mm aa", Locale.UK).format(timestamp);
        } else if (currentTimeStringFormat.substring(7, 11).equals(timeStampStringFormat.substring(7, 11))) {
            // This means we are still in the year style was posted
            time = new SimpleDateFormat("MMM dd", Locale.UK).format(timestamp)
                    + " at " + new SimpleDateFormat("hh:mm aa", Locale.UK).format(timestamp);
        } else {
            // This means we are still in some other year other than that style was posted
            time = new SimpleDateFormat("MMM dd, yyyy", Locale.UK).format(timestamp)
                    + " at " + new SimpleDateFormat("hh:mm aa", Locale.UK).format(timestamp);
        }
        return time;
    }
}
