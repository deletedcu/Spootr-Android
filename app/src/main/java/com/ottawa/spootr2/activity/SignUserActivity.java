package com.ottawa.spootr2.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.ottawa.spootr2.R;
import com.ottawa.spootr2.common.Constants;
import com.ottawa.spootr2.common.KeyUtil;
import com.ottawa.spootr2.common.SharedData;

import net.louislam.android.L;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by king on 17/02/16.
 */
public class SignUserActivity extends Activity {
    private ProgressDialog loadingDialog;
    private String email;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = getSharedPreferences(Constants.PREFS_NAME, 0);
        editor = getSharedPreferences(Constants.PREFS_NAME, 0).edit();

        setContentView(R.layout.activity_signuser);

        Bundle extra = getIntent().getExtras();
        email = extra.getString(Constants.EMAIL);

        LinearLayout layout = (LinearLayout)findViewById(R.id.layout_signuser);
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                KeyUtil.hideSoftKeyboard(SignUserActivity.this);
            }
        });

        final EditText textFullName = (EditText)findViewById(R.id.text_signuser_fullname);
        final EditText textName = (EditText)findViewById(R.id.text_signuser_name);
        final EditText textPassword = (EditText)findViewById(R.id.text_signuser_password);

        Button nextButton = (Button)findViewById(R.id.button_signuser_next);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fullName = textFullName.getText().toString().trim();
                String userName = textName.getText().toString().trim().toLowerCase();
                String password = textPassword.getText().toString();

                if (userName.equals("")) {
                    L.alert(SignUserActivity.this, "No Username!\nPlease insert username to signup.");
                } else if (password.equals("")) {
                    L.alert(SignUserActivity.this, "No Password!\nPlease insert password to signup");
                } else {
                    addUser(fullName, userName, password);

                }

            }
        });

        Button loginButton = (Button)findViewById(R.id.button_signuser_login);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUserActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        Button termsButton = (Button)findViewById(R.id.button_signuser_terms);
        termsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTerms();
            }
        });

        Button privacyButton = (Button)findViewById(R.id.button_signuser_privacy);
        privacyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPrivacy();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    /***********************************************************************************************
     ***********************                Internal Methods                ************************
     **********************************************************************************************/

    private void showTerms() {
        Intent intent = new Intent(SignUserActivity.this, TermsActivity.class);
        startActivity(intent);
    }

    private void showPrivacy() {
        Intent intent = new Intent(SignUserActivity.this, PrivacyActivity.class);
        startActivity(intent);
    }

    private void gotoNextView() {
        SharedData.getInstance().isSignUp = true;
        Intent intent = new Intent(SignUserActivity.this, LocationActivity.class);
        startActivity(intent);
        finish();
    }

    /***********************************************************************************************
     *********************                      Web Api Functions               ********************
     **********************************************************************************************/

    /**
     * api method: addUser
     * parameter: user_name, email, name, password
     * return: json(status, user_id)
     */
    private void addUser(final String fullName, final String userName, final String password) {
        loadingDialog = L.progressDialog(this, "please wait...");
        final SharedData sharedData = SharedData.getInstance();

        RequestParams params = new RequestParams();
        params.put(Constants.ACTION, "addUser");
        params.put(Constants.USER_NAME, userName);
        params.put(Constants.EMAIL, email);
        params.put(Constants.NAME, fullName);
        params.put(Constants.PASSWORD, password);

        sharedData.httpClient.post(SignUserActivity.this, Constants.WEBSERVICE_URL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] response) {
                loadingDialog.dismiss();
                try {
                    String strResponse = new String(response, "UTF-8");
                    JSONArray jsonArray = new JSONArray(strResponse);
                    String status = jsonArray.getJSONObject(0).getString(Constants.STATUS);
                    if (status.equals(Constants.STATUS_SUCCESS)) {
                        JSONObject object = jsonArray.getJSONObject(1);
                        int userId = object.getInt(Constants.ID);

                        editor.putInt(Constants.USER_ID, userId);
                        editor.putString(Constants.USER_NAME, userName);
                        if (fullName.equals(""))
                            editor.putString(Constants.NAME, userName);
                        else
                            editor.putString(Constants.NAME, fullName);
                        editor.putString(Constants.EMAIL, email);
                        editor.putString(Constants.PASSWORD, password);
                        editor.commit();

                        gotoNextView();
                    } else {
                        L.alert(SignUserActivity.this, "This username is already existing. \nPlease insert other username.");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    L.alert(SignUserActivity.this, Constants.WEB_FAILED);
                }
            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] response, Throwable throwable) {
                loadingDialog.dismiss();
                L.alert(SignUserActivity.this, Constants.WEB_FAILED);
            }
        });
    }
}
