package com.ottawa.spootr2.model;

import java.util.Date;

/**
 * Created by King on 5/11/2016.
 */
public class Message {

    private String strMessage;
    private Date messageDate;
    private Emoji emoji;
    private boolean fromme;
    private int status = 0; // 0: normal, 1: sending, 2: failed

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getStrMessage() {
        return strMessage;
    }

    public void setStrMessage(String strMessage) {
        this.strMessage = strMessage;
    }

    public Date getMessageDate() {
        return messageDate;
    }

    public void setMessageDate(Date messageDate) {
        this.messageDate = messageDate;
    }

    public Emoji getEmoji() {
        return emoji;
    }

    public void setEmoji(Emoji emoji) {
        this.emoji = emoji;
    }

    public boolean isFromme() {
        return fromme;
    }

    public void setFromme(boolean fromme) {
        this.fromme = fromme;
    }
}
