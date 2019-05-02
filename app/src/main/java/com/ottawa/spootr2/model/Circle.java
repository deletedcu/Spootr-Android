package com.ottawa.spootr2.model;

import java.io.Serializable;

/**
 * Created by king on 21/01/16.
 */
public class Circle implements Serializable {

    private int nId;
    private String strName;
    private int nUserCount;
    private boolean isJoined;
    private boolean isHeader;
    private boolean isTrending;

    public Circle(int id, String name, boolean isTrending) {
        setnId(id);
        setStrName(name);
        setIsTrending(isTrending);
        setIsHeader(false);
    }

    public Circle(String strName, boolean isHeader) {
        setStrName(strName);
        setIsHeader(isHeader);
    }

    public int getnId() {
        return nId;
    }

    public void setnId(int nId) {
        this.nId = nId;
    }

    public String getStrName() {
        return strName;
    }

    public void setStrName(String strName) {
        this.strName = strName;
    }

    public int getnUserCount() {
        return nUserCount;
    }

    public void setnUserCount(int nUserCount) {
        this.nUserCount = nUserCount;
    }

    public boolean isJoined() {
        return isJoined;
    }

    public void setIsJoined(boolean isJoined) {
        this.isJoined = isJoined;
    }

    public boolean isHeader() {
        return isHeader;
    }

    public void setIsHeader(boolean isHeader) {
        this.isHeader = isHeader;
    }

    public boolean isTrending() {
        return isTrending;
    }

    public void setIsTrending(boolean isTrending) {
        this.isTrending = isTrending;
    }
}
