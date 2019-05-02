package com.ottawa.spootr2.common;

import android.app.Activity;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by king on 17/02/16.
 */
public class KeyUtil {
    private static KeyUtil instance;
    public static KeyUtil getInstance() {
        if (instance == null) {
            instance = new KeyUtil();
        }
        return instance;
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager)  activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }
}
