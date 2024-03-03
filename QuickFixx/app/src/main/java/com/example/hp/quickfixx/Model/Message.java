package com.example.hp.quickfixx.Model;

/**
 * Created by HP on 3/30/2018.
 */

public class Message {
    String sendBy;
    String sendTo;
    String message;

    public String getTimeStamp2() {
        return timeStamp;
    }

    public void setTimeStamp2(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    String timeStamp;

    public String getSendBy() {
        return sendBy;
    }

    public void setSendBy(String sendBy) {
        this.sendBy = sendBy;
    }

    public String getSendTo() {
        return sendTo;
    }

    public void setSendTo(String sendTo) {
        this.sendTo = sendTo;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
