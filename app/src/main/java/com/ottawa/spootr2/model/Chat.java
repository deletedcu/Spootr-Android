package com.ottawa.spootr2.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by King on 5/10/2016.
 */
public class Chat implements Serializable {

    private int nRoomId;
    private int nUserId;
    private String strUserName;
    private String strPictureName;
    private String strMessage;
    private Date chatTime;
    private boolean isNew;
    private boolean fromme;
    private int nMessageCount;

    public int getnRoomId() {
        return nRoomId;
    }

    public void setnRoomId(int nRoomId) {
        this.nRoomId = nRoomId;
    }

    public int getnUserId() {
        return nUserId;
    }

    public void setnUserId(int nUserId) {
        this.nUserId = nUserId;
    }

    public String getStrUserName() {
        return strUserName;
    }

    public void setStrUserName(String strUserName) {
        this.strUserName = strUserName;
    }

    public String getStrPictureName() {
        return strPictureName;
    }

    public void setStrPictureName(String strPictureName) {
        this.strPictureName = strPictureName;
    }

    public String getStrMessage() {
        return strMessage;
    }

    public void setStrMessage(String strMessage) {
        this.strMessage = strMessage;
    }

    public Date getChatTime() {
        return chatTime;
    }

    public void setChatTime(Date chatTime) {
        this.chatTime = chatTime;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean aNew) {
        isNew = aNew;
    }

    public boolean isFromme() {
        return fromme;
    }

    public void setFromme(boolean fromme) {
        this.fromme = fromme;
    }

    public int getnMessageCount() {
        return nMessageCount;
    }

    public void setnMessageCount(int nMessageCount) {
        this.nMessageCount = nMessageCount;
    }
}
