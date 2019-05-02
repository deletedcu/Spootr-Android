package com.ottawa.spootr2.common;

import com.loopj.android.http.AsyncHttpClient;
import com.ottawa.spootr2.model.Circle;

import java.util.ArrayList;

/**
 * Created by king on 21/01/16.
 */
public class SharedData {

    private static SharedData instance;

    public AsyncHttpClient httpClient;

    private ArrayList<Circle> suggestCircles;
    private ArrayList<Circle> trendingCircles;
    private Circle myCircle;

    public boolean isNotificationReceived;
    public int image_width;
    public boolean isSignUp;
    public int notificationCount;
    public int messageCount;

    private SharedData() {
        suggestCircles = new ArrayList<Circle>();
        trendingCircles = new ArrayList<Circle>();
        initHttpClient();
    }

    public static SharedData getInstance() {
        if (instance == null)
            instance = new SharedData();
        return instance;
    }

    private void initHttpClient() {
        try {
            httpClient = new AsyncHttpClient();
            httpClient.setTimeout(Constants.REQUEST_TIMEOUT_SECOND);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Circle getMyCircle() {
        return myCircle;
    }

    public void setMyCircle(Circle myCircle) {
        this.myCircle = myCircle;
    }

    public ArrayList<Circle> getSuggestCircles() {
        return suggestCircles;
    }

    public void setSuggestCircles(ArrayList<Circle> suggestCircles) {
        this.suggestCircles = suggestCircles;
    }

    public ArrayList<Circle> getTrendingCircles() {
        return trendingCircles;
    }

    public void setTrendingCircles(ArrayList<Circle> trendingCircles) {
        this.trendingCircles = trendingCircles;
    }
}
