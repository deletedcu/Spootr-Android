package com.ottawa.spootr2.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.ottawa.spootr2.R;
import com.ottawa.spootr2.common.Constants;
import com.ottawa.spootr2.common.SharedData;
import com.ottawa.spootr2.gcm.GCMClientManager;
import com.ottawa.spootr2.location.MyBroadcastManager;
import com.ottawa.spootr2.location.MyLocationService;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by king on 21/01/16.
 */
public class LocationActivity extends Activity {

    public static Activity sharedActivity;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_location);

        preferences = getSharedPreferences(Constants.PREFS_NAME, 0);
        editor = getSharedPreferences(Constants.PREFS_NAME, 0).edit();

        boolean isSetting = preferences.getBoolean(Constants.IS_SETTING, false);
        if (!isSetting)
            saveNotificationSetting();

        String deviceToken = preferences.getString(Constants.DEVICE_TOKEN, "");
        if (deviceToken.equals(""))
            registerPushClient();

        Button okButton = (Button) findViewById(R.id.ok_button);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startLocationUpdate();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        sharedActivity = this;
    }

    @Override
    protected void onStop() {
        super.onStop();
        sharedActivity = null;
    }

    /***********************************************************************************************
     ***********************                Internal Methods                ************************
     **********************************************************************************************/

    private void startLocationUpdate() {

        MyBroadcastManager.getInstance(this).receiveBroadcast(this, MyLocationService.BROADCAST_LOCATION_UPDATED, new MyBroadcastManager.MyBroadcastReceiver() {
            @Override
            public void onReceive(Intent intent) {
                Location location = (Location) intent.getExtras().get("data");

                SharedPreferences.Editor editor = getSharedPreferences(Constants.PREFS_NAME, 0).edit();
                editor.putFloat(Constants.LATITUDE, (float) location.getLatitude());
                editor.putFloat(Constants.LONGITUDE, (float) location.getLongitude());
                editor.commit();

//                MyBroadcastManager.getInstance(LocationActivity.this).stopBroadcast(LocationActivity.this);

            }
        });

        Intent locationService = new Intent(this, MyLocationService.class);
        startService(locationService);

        Boolean signUp = SharedData.getInstance().isSignUp;
        if (signUp) {
            SharedData.getInstance().isSignUp = true;
            showEmojiActivity();
        } else {
            showMainActivity();
        }

    }

    private void showMainActivity() {
        Intent intent = new Intent(LocationActivity.this, MainActivity.class);
        startActivity(intent);

        finish();
    }

    private void showEmojiActivity() {
        Intent intent = new Intent(LocationActivity.this, SignEmojiActivity.class);
        startActivity(intent);

        finish();
    }

    @Override
    public void onBackPressed() {
        showFinishAlert();
    }

    private void showFinishAlert() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.close_alert)
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        return;
                    }
                }).show();
    }

    /***********************************************************************************************
     *************                    Web api functions                         ********************
     **********************************************************************************************/
    /**
     * api method: saveNotificationSetting
     * parameter: user_id, like_post, comment_post, inapp_vibration
     * return: json(status - 200: success, else: failed)
     */
    private void saveNotificationSetting() {
        int nUserId = preferences.getInt(Constants.USER_ID, 0);
        final SharedData sharedData = SharedData.getInstance();
        RequestParams params = new RequestParams();
        params.put(Constants.ACTION, "saveNotificationSetting");
        params.put(Constants.USER_ID, nUserId);
        params.put(Constants.LIKE_POST, true);
        params.put(Constants.COMMENT_POST, true);
        params.put(Constants.CHAT_NOTIFICATION, true);

        sharedData.httpClient.post(LocationActivity.this, Constants.WEBSERVICE_URL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] response) {
                try {
                    String strResponse = new String(response, "UTF-8");
                    JSONArray jsonArray = new JSONArray(strResponse);
                    String status = jsonArray.getJSONObject(0).getString(Constants.STATUS);
                    if (status.equals(Constants.STATUS_SUCCESS)) {
                        editor.putBoolean(Constants.IS_SETTING, true);
                        editor.putBoolean(Constants.LIKE_POST, true);
                        editor.putBoolean(Constants.COMMENT_POST, true);
                        editor.putBoolean(Constants.CHAT_NOTIFICATION, false);
                        editor.commit();

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] response, Throwable throwable) {
                Log.i("Notification: ", "failed");
                Toast.makeText(LocationActivity.this, "Notification saving was failed", Toast.LENGTH_SHORT);
            }
        });
    }

    /**
     * api method: saveDeviceToken
     * parameter: user_id, device_token, device_type
     * return: json(status - 200: success, else: fail)
     */
    private void saveDeviceToken() {
        SharedData sharedData = SharedData.getInstance();

        int nUserId = preferences.getInt(Constants.USER_ID, 0);
        String registerID = preferences.getString(Constants.DEVICE_TOKEN, "");
        RequestParams params = new RequestParams();
        params.put(Constants.ACTION, "saveDeviceToken");
        params.put(Constants.USER_ID, nUserId);
        params.put(Constants.DEVICE_TOKEN, registerID);
        params.put(Constants.DEVICE_TYPE, Constants.DEVICE_ANDROID);

        sharedData.httpClient.post(LocationActivity.this, Constants.WEBSERVICE_URL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] response) {

                try {
                    String strResponse = new String(response, "UTF-8");
                    JSONArray jsonArray = new JSONArray(strResponse);
                    String status = jsonArray.getJSONObject(0).getString(Constants.STATUS);
                    if (status.equals(Constants.STATUS_SUCCESS)) {
                        Log.d("RegisterID", "success");
                    } else {
                        Log.d("RegisterID", "status_fail");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                Log.d("RegisterID", "failure");
            }
        });
    }

    /***********************************************************************************************
     *******************                       Internal Methods                 ********************
     **********************************************************************************************/

    private void registerPushClient() {
        String projectNumber = getResources().getString(R.string.project_number);
        GCMClientManager pushManager = new GCMClientManager(this, projectNumber);
        pushManager.registerIfNeeded(new GCMClientManager.RegistrationCompletedHandler() {
            @Override
            public void onSuccess(String registrationId, boolean isNewRegistration) {
                Log.d("Register ID", registrationId);
                editor.putString(Constants.DEVICE_TOKEN, registrationId);
                editor.commit();
                saveDeviceToken();
            }

            @Override
            public void onFailure(String ex) {
                super.onFailure(ex);
            }
        });

    }



}
