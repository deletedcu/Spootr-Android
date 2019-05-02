package com.ottawa.spootr2.model;

/**
 * Created by king on 28/01/16.
 */
public class PopupItem {
    private int nId;
    private String strName;

    public PopupItem(int id, String name) {
        setnId(id);
        setStrName(name);
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
}
