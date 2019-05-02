package com.ottawa.spootr2.model;

import android.graphics.drawable.Drawable;

import java.io.Serializable;

/**
 * Created by king on 17/02/16.
 */
public class Emoji implements Serializable {
    private String pictureName;
    private Drawable drawable;

    public String getPictureName() {
        return pictureName;
    }

    public void setPictureName(String pictureName) {
        this.pictureName = pictureName;
    }

    public Drawable getDrawable() {
        return drawable;
    }

    public void setDrawable(Drawable drawable) {
        this.drawable = drawable;
    }
}
