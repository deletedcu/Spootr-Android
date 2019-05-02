package com.ottawa.spootr2.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.ottawa.spootr2.R;
import com.ottawa.spootr2.common.Constants;
import com.ottawa.spootr2.common.SharedData;

import net.louislam.android.L;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by king on 24/01/16.
 */
public class SettingActivity extends Activity implements View.OnClickListener {

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private Switch likeSwitch;
    private Switch commentSwitch;
    private Switch chatSwitch;
    private boolean isNewPost;
    private boolean isLike;
    private boolean isComment;
    private boolean isChat;
    private ProgressDialog loadingDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = getSharedPreferences(Constants.PREFS_NAME, 0);
        editor = getSharedPreferences(Constants.PREFS_NAME, 0).edit();
        setContentView(R.layout.activity_menu_setting);
        initComponent();
    }

    /***********************************************************************************************
     ***********************                Internal Methods                ************************
     **********************************************************************************************/

    private void initComponent() {

        likeSwitch = (Switch)findViewById(R.id.switch_likes);
        likeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (isLike != b) {
                    isLike = b;
                    saveNotificationSetting(1);
                }

            }
        });

        commentSwitch = (Switch)findViewById(R.id.switch_comments);
        commentSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (isComment != b) {
                    isComment = b;
                    saveNotificationSetting(2);
                }

            }
        });

        chatSwitch = (Switch)findViewById(R.id.switch_chat);
        chatSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (isChat != b) {
                    isChat = b;
                    saveNotificationSetting(3);
                }

            }
        });

        ImageButton backButton = (ImageButton)findViewById(R.id.button_settings_back);
        backButton.setOnClickListener(this);

        Button facebookButton = (Button)findViewById(R.id.button_settings_facebook);
        facebookButton.setOnClickListener(this);

        Button twitterButton = (Button)findViewById(R.id.button_settings_twitter);
        twitterButton.setOnClickListener(this);

        Button appButton = (Button)findViewById(R.id.button_settings_app);
        appButton.setOnClickListener(this);

        Button privacyButton = (Button)findViewById(R.id.button_settings_privacy);
        privacyButton.setOnClickListener(this);

        Button termsButton = (Button)findViewById(R.id.button_settings_terms);
        termsButton.setOnClickListener(this);

        Button contactButton = (Button)findViewById(R.id.button_settings_contact);
        contactButton.setOnClickListener(this);

        Button blockButton = (Button)findViewById(R.id.button_settings_blocklist);
        blockButton.setOnClickListener(this);

        Button signoutButton = (Button)findViewById(R.id.button_settings_signout);
        signoutButton.setOnClickListener(this);

        Button deleteButton = (Button)findViewById(R.id.button_settings_delete);
        deleteButton.setOnClickListener(this);

        loadData();
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.button_settings_back:
                finish();
                break;
            case R.id.button_settings_facebook:
                intent = new Intent(SettingActivity.this, MyWebViewActivity.class);
                intent.putExtra("type", Constants.FACEBOOK_TYPE);
                startActivity(intent);
                break;
            case R.id.button_settings_twitter:
                intent = new Intent(SettingActivity.this, MyWebViewActivity.class);
                intent.putExtra("type", Constants.TWITTER_TYPE);
                startActivity(intent);
                break;
            case R.id.button_settings_app:
                intent = new Intent(SettingActivity.this, MyWebViewActivity.class);
                intent.putExtra("type", Constants.APP_TYPE);
                startActivity(intent);
                break;
            case R.id.button_settings_privacy:
                intent = new Intent(SettingActivity.this, PrivacyActivity.class);
                startActivity(intent);
                break;
            case R.id.button_settings_terms:
                Intent intent1 = new Intent(SettingActivity.this, TermsActivity.class);
                startActivity(intent1);
                break;
            case R.id.button_settings_contact:
                intent=new Intent(Intent.ACTION_SEND);
                String[] recipients={"contact@spootr.com"};
                intent.putExtra(Intent.EXTRA_EMAIL, recipients);
                intent.putExtra(Intent.EXTRA_SUBJECT,"From my app");
                intent.putExtra(Intent.EXTRA_TEXT,"Hello Spootr support team");
                intent.putExtra(Intent.EXTRA_CC,"");
                intent.setType("text/html");
                startActivity(Intent.createChooser(intent, "Send mail"));
                break;
            case R.id.button_settings_blocklist:
                intent = new Intent(SettingActivity.this, BlockListActivity.class);
                startActivity(intent);
                break;
            case R.id.button_settings_signout:
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
                builder.setTitle("Are you sure?")
                        .setMessage("Do you want to sign out?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                signOut(true);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                return;
                            }
                        }).show();
                break;
            case R.id.button_settings_delete:
                AlertDialog.Builder builder1 = new AlertDialog.Builder(SettingActivity.this);
                builder1.setTitle("Are you sure?")
                        .setMessage("Do you want to delete the user?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                deleteUser();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                return;
                            }
                        }).show();
                break;
        }
    }

    private void loadData() {
        boolean isSetting = preferences.getBoolean(Constants.IS_SETTING, false);
        if (isSetting) {
            isLike = preferences.getBoolean(Constants.LIKE_POST, false);
            isComment = preferences.getBoolean(Constants.COMMENT_POST, false);
            isChat = preferences.getBoolean(Constants.CHAT_NOTIFICATION, false);

            setComponentValue();
        } else {
            getNotificationSetting();
        }
    }

    private void setComponentValue() {
        likeSwitch.setChecked(isLike);
        commentSwitch.setChecked(isComment);
        chatSwitch.setChecked(isChat);
    }

    private void resetComponentValue(int index) {
        switch (index) {
            case 1:
                isLike = !isLike;
                likeSwitch.setChecked(isLike);
                break;
            case 2:
                isComment = !isComment;
                commentSwitch.setChecked(isComment);
                break;
            case 3:
                isChat = !isChat;
                chatSwitch.setChecked(isChat);
                break;
        }
    }


    /***********************************************************************************************
     *********************                      Web Api Functions               ********************
     **********************************************************************************************/

    /**
     * api method: getNotificationSetting
     * parameter: user_id
     * return: json(status, notification setting)
     */
    private void getNotificationSetting() {
        loadingDialog = L.progressDialog(SettingActivity.this, Constants.REQUEST_WAITING);
        SharedData sharedData = SharedData.getInstance();
        int nUserId = preferences.getInt(Constants.USER_ID, 0);

        RequestParams params = new RequestParams();
        params.put(Constants.ACTION, "getNotificationSetting");
        params.put(Constants.USER_ID, nUserId);

        sharedData.httpClient.post(SettingActivity.this, Constants.WEBSERVICE_URL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] response) {
                loadingDialog.dismiss();
                try {
                    String strResponse = new String(response, "UTF-8");
                    JSONArray jsonArray = new JSONArray(strResponse);
                    String status = jsonArray.getJSONObject(0).getString(Constants.STATUS);
                    if (status.equals(Constants.STATUS_SUCCESS)) {
                        JSONObject data = jsonArray.getJSONObject(1);

                        isNewPost = data.getBoolean(Constants.NEW_POST);
                        isLike = data.getBoolean(Constants.LIKE_POST);
                        isComment = data.getBoolean(Constants.COMMENT_POST);
                        isChat = data.getBoolean(Constants.CHAT_NOTIFICATION);

                        editor.putBoolean(Constants.IS_SETTING, true);
                        editor.putBoolean(Constants.NEW_POST, isNewPost);
                        editor.putBoolean(Constants.LIKE_POST, isLike);
                        editor.putBoolean(Constants.COMMENT_POST, isComment);
                        editor.putBoolean(Constants.CHAT_NOTIFICATION, isChat);
                        editor.commit();

                        setComponentValue();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                loadingDialog.dismiss();
            }
        });
    }

    /**
     * api method: saveNotificationSetting
     * parameter: user_id, like_post, comment_post, inapp_vibration
     * return: json(status)
     */
    private void saveNotificationSetting(final int index) {
        SharedData sharedData = SharedData.getInstance();
        int nUserId = preferences.getInt(Constants.USER_ID, 0);

        RequestParams params = new RequestParams();
        params.put(Constants.ACTION, "saveNotificationSetting");
        params.put(Constants.USER_ID, nUserId);
        params.put(Constants.LIKE_POST, isLike);
        params.put(Constants.COMMENT_POST, isComment);
        params.put(Constants.CHAT_NOTIFICATION, isChat);

        sharedData.httpClient.post(SettingActivity.this, Constants.WEBSERVICE_URL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] response) {
                try {
                    String strResponse = new String(response, "UTF-8");
                    JSONArray jsonArray = new JSONArray(strResponse);
                    String status = jsonArray.getJSONObject(0).getString(Constants.STATUS);
                    if (status.equals(Constants.STATUS_SUCCESS)) {
                        editor.putBoolean(Constants.IS_SETTING, true);
                        editor.putBoolean(Constants.LIKE_POST, isLike);
                        editor.putBoolean(Constants.COMMENT_POST, isComment);
                        editor.putBoolean(Constants.CHAT_NOTIFICATION, isChat);
                        editor.commit();
                    } else {
                        resetComponentValue(index);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    resetComponentValue(index);
                }
            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                resetComponentValue(index);
            }
        });
    }

    /**
     * api method: deleteUser
     * parameter: user_id
     * return: json(status)
     */
    private void deleteUser() {
        final ProgressDialog dialog = L.progressDialog(this, Constants.REQUEST_WAITING);
        SharedData sharedData = SharedData.getInstance();
        int nUserId = preferences.getInt(Constants.USER_ID, 0);
        RequestParams params = new RequestParams();
        params.put(Constants.ACTION, "deleteUser");
        params.put(Constants.USER_ID, nUserId);

        sharedData.httpClient.post(SettingActivity.this, Constants.WEBSERVICE_URL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] response) {
                dialog.dismiss();
                try {
                    String strResponse = new String(response, "UTF-8");
                    JSONArray jsonArray = new JSONArray(strResponse);
                    String status = jsonArray.getJSONObject(0).getString(Constants.STATUS);
                    if (status.equals(Constants.STATUS_SUCCESS))
                        signOut(false);
                    else
                        L.alert(SettingActivity.this, Constants.WEB_FAILED);
                } catch (Exception e) {
                    e.printStackTrace();
                    L.alert(SettingActivity.this, Constants.WEB_FAILED);
                }

            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                dialog.dismiss();
                L.alert(SettingActivity.this, Constants.WEB_FAILED);
            }
        });
    }

    private void signOut(boolean isSignOut) {
        SharedPreferences.Editor editor = getSharedPreferences(Constants.PREFS_NAME, 0).edit();
        String userName = preferences.getString(Constants.USER_NAME, "");
        String password = preferences.getString(Constants.PASSWORD, "");
        editor.clear();
        editor.commit();
        if (isSignOut) {
            editor.putString(Constants.USER_NAME, userName);
            editor.putString(Constants.PASSWORD, password);
            editor.commit();
        }

        Intent intent = new Intent(SettingActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

}
