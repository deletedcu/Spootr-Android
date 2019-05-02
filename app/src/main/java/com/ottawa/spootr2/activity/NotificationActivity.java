package com.ottawa.spootr2.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;

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
import com.ottawa.spootr2.listAdapter.NotificationListAdapter;
import com.ottawa.spootr2.model.Post;

import net.louislam.android.L;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by king on 18/03/16.
 */
public class NotificationActivity extends Activity implements OnMapReadyCallback{

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private ProgressDialog loadingDialog;
    private ListView listView;
    private ArrayList<Post> itemList;
    private NotificationListAdapter notificationAdatper;
    private SimpleDateFormat isoFormat;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = getSharedPreferences(Constants.PREFS_NAME, 0);
        editor = getSharedPreferences(Constants.PREFS_NAME, 0).edit();
        setContentView(R.layout.activity_menu_pushnotification);

        isoFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map_notification);
        mapFragment.getMapAsync(this);

        initData();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        Float latitude = preferences.getFloat(Constants.LATITUDE, 0);
        Float longitude = preferences.getFloat(Constants.LONGITUDE, 0);

        LatLng latLng = new LatLng(latitude, longitude);

        map.setMyLocationEnabled(true);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

        map.addMarker(new MarkerOptions().position(latLng));
    }

    /***********************************************************************************************
     ***********************                Internal Methods                ************************
     **********************************************************************************************/

    private void initData() {
        ImageButton backButton = (ImageButton)findViewById(R.id.button_pushnotification_back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_OK);
                finish();
            }
        });

        listView = (ListView)findViewById(R.id.listView_notification);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Post post = notificationAdatper.getItem(i);
                removeNotifications(post, i);

                Intent intent = new Intent(NotificationActivity.this, CommentActivity.class);
                intent.putExtra("post", post);
                startActivityForResult(intent, Constants.REQUEST_CODE_EDITPOST);
            }
        });

        itemList = new ArrayList<Post>();
        notificationAdatper = new NotificationListAdapter(this, 0, itemList);
        listView.setAdapter(notificationAdatper);

        getNotifications();
    }

    /***********************************************************************************************
     *******************                    Web Api Functions                   ********************
     **********************************************************************************************/

    /**
     * api method: getNotifcations
     * parameter: user_id
     * result: json(status, array of notification
     */
    private void getNotifications() {
        loadingDialog = L.progressDialog(this, Constants.REQUEST_WAITING);

        SharedData sharedData = SharedData.getInstance();
        final int nUserId = preferences.getInt(Constants.USER_ID, 0);
        float latitude = preferences.getFloat(Constants.LATITUDE, 0);
        float longitude = preferences.getFloat(Constants.LONGITUDE, 0);

        RequestParams params = new RequestParams();
        params.put(Constants.ACTION, "getNotifications");
        params.put(Constants.USER_ID, nUserId);
        params.put(Constants.LATITUDE, latitude);
        params.put(Constants.LONGITUDE, longitude);

        sharedData.httpClient.post(NotificationActivity.this, Constants.WEBSERVICE_URL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] response) {
                try {
                    String strResponse = new String(response, "UTF-8");
                    JSONArray jsonArray = new JSONArray(strResponse);
                    String status = jsonArray.getJSONObject(0).getString(Constants.STATUS);
                    if (status.equals(Constants.STATUS_SUCCESS)) {
                        JSONArray result = jsonArray.getJSONArray(1);
                        for (int k = 0; k < result.length(); k++) {
                            JSONObject item = result.getJSONObject(k);

                            int nId = item.getInt(Constants.ID);
                            Date date = isoFormat.parse(item.getString(Constants.POST_TIME));
                            int userId = item.getInt(Constants.USER_ID);
                            String strContent = item.getString(Constants.CONTENT);
                            int nLikeCount = item.getInt(Constants.LIKE_COUNT);
                            int nCommentCount = item.getInt(Constants.COMMENT_COUNT);
                            int nLikeType = item.getInt(Constants.LIKE_TYPE);
                            boolean isCommented = item.getInt(Constants.IS_COMMENTED) > 0 ? true : false;
                            boolean isMine = userId == nUserId ? true : false;
                            String strImageName = item.getString(Constants.POST_IMAGE);
                            int notificationCount = item.getInt(Constants.NOTIFICATION_COUNT);
                            int notificationType = item.getInt(Constants.NOTIFICATION_TYPE);
                            Date notificationDate = isoFormat.parse(item.getString(Constants.NOTIFICATION_TIME));
                            double distance = item.getDouble(Constants.DISTANCE);
                            String strExtraInfo = "";
                            if (distance < 0.1) {
                                strExtraInfo = "Near by";
                            } else {
                                strExtraInfo = String.format("%1$.1f km away", distance);
                            }

                            ArrayList likeArray = new ArrayList();
                            likeArray.add(item.getInt(Constants.LIKE1));
                            likeArray.add(item.getInt(Constants.LIKE2));
                            likeArray.add(item.getInt(Constants.LIKE3));
                            likeArray.add(item.getInt(Constants.LIKE4));
                            likeArray.add(item.getInt(Constants.LIKE5));

                            Post post = new Post(nId, userId, strContent, date, nLikeCount,
                                    nCommentCount, nLikeType, isCommented, isMine, strImageName,
                                    likeArray, strExtraInfo, false, notificationCount, notificationType, notificationDate);

                            itemList.add(post);
                        }

                        notificationAdatper.updateResults(itemList);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                loadingDialog.dismiss();
            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                loadingDialog.dismiss();
            }
        });
    }

    /**
     * api method: removeNotifications
     * param post: post_id, notification_type
     * return: json(status)
     */
    private void removeNotifications(final Post post, final int position) {
        final SharedData sharedData = SharedData.getInstance();

        RequestParams params = new RequestParams();
        params.put(Constants.ACTION, "removeNotifications");
        params.put(Constants.POST_ID, post.getnId());
        params.put(Constants.NOTIFICATION_TYPE, post.getNotificationType());

        sharedData.httpClient.post(NotificationActivity.this, Constants.WEBSERVICE_URL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] response) {
                try {
                    String strResponse = new String(response, "UTF-8");
                    JSONArray jsonArray = new JSONArray(strResponse);
                    String status = jsonArray.getJSONObject(0).getString(Constants.STATUS);
                    if (status.equals(Constants.STATUS_SUCCESS)) {
                        int count = sharedData.notificationCount;
                        count = count - post.getNotificationCount();
                        if (count < 0)
                            count = 0;
                        sharedData.notificationCount = count;

                        notificationAdatper.removeItem(position);

                        Intent intent = new Intent();
                        intent.setAction("notification");
                        sendBroadcast(intent);
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
