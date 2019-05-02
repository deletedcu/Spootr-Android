package com.ottawa.spootr2.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.View;
import android.widget.ImageButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.ottawa.spootr2.R;
import com.ottawa.spootr2.common.Constants;
import com.ottawa.spootr2.common.SharedData;
import com.ottawa.spootr2.fragment.ChatFragment;
import com.ottawa.spootr2.fragment.HomeFragment;
import com.ottawa.spootr2.fragment.ProfileFragment;
import com.ottawa.spootr2.fragment.SearchFragment;
import com.startsmake.mainnavigatetabbar.widget.MainNavigateTabBar;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by King on 01/05/2016.
 */
public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private MainNavigateTabBar mainNavigateTabBar;
    private GoogleMap googleMap;
    private SharedPreferences preferences;
    private MainNavigateTabBar.TabParam chatParam;
    private MainNavigateTabBar.TabParam chatParamNotification;
    private MainNavigateTabBar.TabParam profileParam;
    private MainNavigateTabBar.TabParam profileparamNotification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        registerReceiver(messageReceiver, new IntentFilter("message"));
        registerReceiver(notificationReceiver, new IntentFilter("notification"));

        chatParam = new MainNavigateTabBar.TabParam(R.drawable.chat, R.drawable.chat_selected, "4");
        chatParamNotification = new MainNavigateTabBar.TabParam(R.drawable.chat_notification, R.drawable.chat_selected_notification, "4");
        profileParam = new MainNavigateTabBar.TabParam(R.drawable.account, R.drawable.account_selected, "5");
        profileparamNotification = new MainNavigateTabBar.TabParam(R.drawable.account_notification, R.drawable.account_notification_selected, "5");

        mainNavigateTabBar = (MainNavigateTabBar)findViewById(R.id.mainTabBar);
        mainNavigateTabBar.onRestoreInstanceState(savedInstanceState);

        mainNavigateTabBar.addTab(HomeFragment.class, new MainNavigateTabBar.TabParam(R.drawable.home, R.drawable.home_selected, "1"));
        mainNavigateTabBar.addTab(SearchFragment.class, new MainNavigateTabBar.TabParam(R.drawable.globe, R.drawable.globe_selected, "2"));
        mainNavigateTabBar.addTab(null, new MainNavigateTabBar.TabParam(0, 0, "3"));
        mainNavigateTabBar.addTab(ChatFragment.class, chatParam);
        mainNavigateTabBar.addTab(ProfileFragment.class, profileParam);


        ImageButton postButton = (ImageButton)findViewById(R.id.button_main_post);
        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PostActivity.class);
                startActivity(intent);
            }
        });

        preferences = getSharedPreferences(Constants.PREFS_NAME, 0);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        SharedData sharedData = SharedData.getInstance();
        sharedData.image_width = size.x;

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map_main);
        mapFragment.getMapAsync(this);

        googleMap = mapFragment.getMap();

        getNotificationCount();

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mainNavigateTabBar.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(messageReceiver);
        unregisterReceiver(notificationReceiver);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        updateMap();
    }

    private void updateMap() {
        Float latitude = preferences.getFloat(Constants.LATITUDE, 0);
        Float longitude = preferences.getFloat(Constants.LONGITUDE, 0);

        LatLng latLng = new LatLng(latitude, longitude);

        googleMap.setMyLocationEnabled(true);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        googleMap.addMarker(new MarkerOptions().position(latLng));
    }

    private BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int messageCount = SharedData.getInstance().messageCount;
            if (messageCount > 0) {
                mainNavigateTabBar.setViewHolder(2, chatParamNotification);
            } else {
                mainNavigateTabBar.setViewHolder(2, chatParam);
            }
        }
    };

    private BroadcastReceiver notificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int notificationCount = SharedData.getInstance().notificationCount;
            if (notificationCount > 0) {
                mainNavigateTabBar.setViewHolder(3, profileparamNotification);
            } else {
                mainNavigateTabBar.setViewHolder(3, profileParam);
            }
        }
    };

    private void getNotificationCount() {

        final SharedData sharedData = SharedData.getInstance();
        int userId = preferences.getInt(Constants.USER_ID, 0);
        RequestParams params = new RequestParams();
        params.put(Constants.ACTION, "getNotificationCount");
        params.put(Constants.USER_ID, userId);

        sharedData.httpClient.post(MainActivity.this, Constants.WEBSERVICE_URL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] response) {

                try {
                    String strResponse = new String(response, "UTF-8");
                    JSONArray jsonArray = new JSONArray(strResponse);
                    String status = jsonArray.getJSONObject(0).getString(Constants.STATUS);
                    if (status.equals(Constants.STATUS_SUCCESS)) {
                        JSONObject object = jsonArray.getJSONObject(1);
                        int notificationCount = object.getInt("notificationCount");
                        int messageCount = object.getInt("messageCount");
                        sharedData.notificationCount = notificationCount;
                        sharedData.messageCount = messageCount;

                        if (messageCount > 0) {
                            mainNavigateTabBar.setViewHolder(2, chatParamNotification);
                        } else {
                            mainNavigateTabBar.setViewHolder(2, chatParam);
                        }

                        if (notificationCount > 0) {
                            mainNavigateTabBar.setViewHolder(3, profileparamNotification);
                        } else {
                            mainNavigateTabBar.setViewHolder(3, profileParam);
                        }

                    } else {
                        sharedData.notificationCount = 0;
                        sharedData.messageCount = 0;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {

            }
        });

    }
}
