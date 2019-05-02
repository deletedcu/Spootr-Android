package com.ottawa.spootr2.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by king on 21/01/16.
 */
public class Post implements Serializable{

    private int nId;
    private int nUserId;
    private String strContent;
    private Date postDate;
    private int nLikeCount;
    private int nCommentCount;
    private int nLikeType;
    private ArrayList likeArray;
    private boolean isCommented;
    private boolean isMine;
    private int notificationCount;
    private int notificationType;
    private Date notificationDate;
    private String strImageName;
    private String extraInfo;
    private boolean isTrending;

    public Post(int nId, int nUserId, String strContent, Date postDate, int nLikeCount,
                int nCommentCount, int nLikeType, boolean isCommented, boolean isMine,
                String strImageName, ArrayList likeArray, String extraInfo, boolean isTrending) {
        setnId(nId);
        setnUserId(nUserId);
        setStrContent(strContent);
        setPostDate(postDate);
        setnLikeCount(nLikeCount);
        setnCommentCount(nCommentCount);
        setnLikeType(nLikeType);
        setCommented(isCommented);
        setMine(isMine);
        setStrImageName(strImageName);
        setLikeArray(likeArray);
        setExtraInfo(extraInfo);
        setTrending(isTrending);
    }

    public Post(int nId, int nUserId, String strContent, Date postDate, int nLikeCount,
                int nCommentCount, int nLikeType, boolean isCommented, boolean isMine,
                String strImageName, ArrayList likeArray, String extraInfo, boolean isTrending,
                int notificationCount, int notificationType, Date notificationDate) {
        setnId(nId);
        setnUserId(nUserId);
        setStrContent(strContent);
        setPostDate(postDate);
        setnLikeCount(nLikeCount);
        setnCommentCount(nCommentCount);
        setnLikeType(nLikeType);
        setCommented(isCommented);
        setMine(isMine);
        setStrImageName(strImageName);
        setLikeArray(likeArray);
        setExtraInfo(extraInfo);
        setTrending(isTrending);
        setNotificationCount(notificationCount);
        setNotificationType(notificationType);
        setNotificationDate(notificationDate);
    }

    public int getnId() {
        return nId;
    }

    public void setnId(int nId) {
        this.nId = nId;
    }

    public int getnUserId() {
        return nUserId;
    }

    public void setnUserId(int nUserId) {
        this.nUserId = nUserId;
    }

    public String getStrContent() {
        return strContent;
    }

    public void setStrContent(String strContent) {
        this.strContent = strContent;
    }

    public Date getPostDate() {
        return postDate;
    }

    public void setPostDate(Date postDate) {
        this.postDate = postDate;
    }

    public int getnLikeCount() {
        return nLikeCount;
    }

    public void setnLikeCount(int nLikeCount) {
        this.nLikeCount = nLikeCount;
    }

    public int getnCommentCount() {
        return nCommentCount;
    }

    public void setnCommentCount(int nCommentCount) {
        this.nCommentCount = nCommentCount;
    }

    public int getnLikeType() {
        return nLikeType;
    }

    public void setnLikeType(int nLikeType) {
        this.nLikeType = nLikeType;
    }

    public ArrayList getLikeArray() {
        return likeArray;
    }

    public void setLikeArray(ArrayList likeArray) {
        this.likeArray = likeArray;
    }

    public boolean isCommented() {
        return isCommented;
    }

    public void setCommented(boolean isCommented) {
        this.isCommented = isCommented;
    }

    public boolean isMine() {
        return isMine;
    }

    public void setMine(boolean isMine) {
        this.isMine = isMine;
    }

    public int getNotificationCount() {
        return notificationCount;
    }

    public void setNotificationCount(int notificationCount) {
        this.notificationCount = notificationCount;
    }

    public int getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(int notificationType) {
        this.notificationType = notificationType;
    }

    public Date getNotificationDate() {
        return notificationDate;
    }

    public void setNotificationDate(Date notificationDate) {
        this.notificationDate = notificationDate;
    }

    public String getStrImageName() {
        return strImageName;
    }

    public void setStrImageName(String strImageName) {
        this.strImageName = strImageName;
    }

    public String getExtraInfo() {
        return extraInfo;
    }

    public void setExtraInfo(String extraInfo) {
        this.extraInfo = extraInfo;
    }

    public boolean isTrending() {
        return isTrending;
    }

    public void setTrending(boolean trending) {
        isTrending = trending;
    }
}
