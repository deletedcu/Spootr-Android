package com.ottawa.spootr2.model;

/**
 * Created by king on 23/01/16.
 */
public class MenuItem {
    private int nId;
    private String strName;
    private int nResourceId;

    public MenuItem(int nId, String strName, int nResourceId) {
        setnId(nId);
        setStrName(strName);
        setnResourceId(nResourceId);
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

    public int getnResourceId() {
        return nResourceId;
    }

    public void setnResourceId(int nResourceId) {
        this.nResourceId = nResourceId;
    }
}
