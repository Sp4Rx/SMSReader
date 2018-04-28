package io.github.sp4rx.smsreader.model;

import android.support.annotation.IntDef;

public class Message {
    public static final int HEADER = 1;
    public static final int MESSAGE = 2;

    @IntDef({HEADER, MESSAGE})
    public @interface MessageType {
    }

    private String body;
    private long date;
    private String address;
    private int type;

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getType() {
        return type;
    }

    public void setType(@MessageType int type) {
        this.type = type;
    }
}
