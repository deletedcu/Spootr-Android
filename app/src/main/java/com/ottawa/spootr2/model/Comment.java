package com.ottawa.spootr2.model;

import android.graphics.drawable.Drawable;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by king on 21/01/16.
 */
public class Comment implements Serializable{

    private int nId;
    private String strContent;
    private Date commentDate;
    private String pictureName;
    private String strUserName;
    private Drawable drawable;

    public Comment(int nId, String content, Date date, String pictureName, String strUserName, Drawable drawable) {
        setnId(nId);
        setStrContent(content);
        setCommentDate(date);
        setPictureName(pictureName);
        setStrUserName(strUserName);
        setDrawable(drawable);
    }

    public int getnId() {
        return nId;
    }

    public void setnId(int nId) {
        this.nId = nId;
    }

    public String getStrContent() {
        return strContent;
    }

    public void setStrContent(String strContent) {
        this.strContent = strContent;
    }

    public Date getCommentDate() {
        return commentDate;
    }

    public void setCommentDate(Date commentDate) {
        this.commentDate = commentDate;
    }

    public String getPictureName() {
        return pictureName;
    }

    public void setPictureName(String pictureName) {
        this.pictureName = pictureName;
    }

    public String getStrUserName() {
        return strUserName;
    }

    public void setStrUserName(String strUserName) {
        this.strUserName = strUserName;
    }

    public Drawable getDrawable() {
        return drawable;
    }

    public void setDrawable(Drawable drawable) {
        this.drawable = drawable;
    }
}
