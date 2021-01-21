package com.psyclone.resilience.models;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Objects;

public class Message implements Serializable {
    @SerializedName("_id")
    private String _id;
    @SerializedName("message")
    private String message;
    @SerializedName("topic")
    private String topic;
    @SerializedName("user")
    private User user;
    @SerializedName("timestamp")
    private long timestamp;

    private boolean sent = true;

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isSent() {
        return sent;
    }

    public void setSent(boolean sent) {
        this.sent = sent;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        Message message = (Message) obj;
        return this.getTimestamp() == Objects.requireNonNull(message).getTimestamp();
    }
}
