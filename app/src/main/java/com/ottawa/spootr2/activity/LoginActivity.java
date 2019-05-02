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

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import net.louislam.android.L;

/**
 * Created by king on 21/01/16.
 */
public class LoginActivity extends Activity {

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private ProgressDialog loadingDialog;
    private EditText textUserName;
    private EditText textPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        LinearLayout layout = (LinearLayout)findViewById(R.id.layout_login);
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                KeyUtil.hideSoftKeyboard(LoginActivity.this);
            }
        });

        textUserName = (EditText)findViewById(R.id.text_login_username);
        textPassword = (EditText)findViewById(R.id.text_login_password);

        Button loginButton = (Button)findViewById(R.id.button_login);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });
        Button signUpButton = (Button)findViewById(R.id.button_login_signup);
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignEmailActivity.class);
                startActivity(intent);
            }
        });
        Button forgotButton = (Button)findViewById(R.id.button_forgotpassword);
        forgotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            }
        });

        preferences = getSharedPreferences(Constants.PREFS_NAME, 0);
        editor = getSharedPreferences(Constants.PREFS_NAME, 0).edit();
        int nUserId = preferences.getInt(Constants.USER_ID, 0);
        String userName = preferences.getString(Constants.USER_NAME, "");
        String password = preferences.getString(Constants.PASSWORD, "");
        textUserName.setText(userName);
        textPassword.setText(password);
        if (nUserId > 0) {
            showLocationActivity();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    /***********************************************************************************************
     ***********************                Internal Methods                ************************
     **********************************************************************************************/

    private void login() {
        String userName = textUserName.getText().toString().trim().toLowerCase();
        String password = textPassword.getText().toString();
        if (userName.equals("")) {
            L.alert(this, "No Username!\nPlease insert username to login.");
        } else if (password.equals("")) {
            L.alert(this, "No Password!\nPlease insert password to login");
        } else {
            sign(userName, password);
        }
    }

    private void showLocationActivity() {
        Intent intent = new Intent(LoginActivity.this, LocationActivity.class);
        startActivity(intent);
        finish();
    }

    /***********************************************************************************************
     *****************************              Web Api Functions           ************************
     **********************************************************************************************/

    /**
     * api method: sign
     * parameter: user_name, password
     * return: json(status, user_id)
     */
    private void sign(final String name, final String password) {
        loadingDialog = L.progressDialog(this, "please wait...");
        final SharedData sharedData = SharedData.getInstance();

        RequestParams params = new RequestParams();
        params.put(Constants.ACTION, "sign");
        params.put(Constants.USER_NAME, name);
        params.put(Constants.PASSWORD, password);

        sharedData.httpClient.post(LoginActivity.this, Constants.WEBSERVICE_URL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] response) {
                loadingDialog.dismiss();
                try {
                    String strResponse = new String(response, "UTF-8");
                    JSONArray jsonArray = new JSONArray(strResponse);
                    String status = jsonArray.getJSONObject(0).getString(Constants.STATUS);
                    if (status.equals(Constants.STATUS_SUCCESS)) {
                        JSONObject object = jsonArray.getJSONObject(1);
                        int nUserId = object.getInt(Constants.ID);
                        String fullName = object.getString(Constants.NAME);
                        String email = object.getString(Constants.EMAIL);
                        String pictureName = object.getString(Constants.PICTURE_NAME);
                        if (fullName.equals(""))
                            fullName = name;

                        editor.putInt(Constants.USER_ID, nUserId);
                        editor.putString(Constants.USER_NAME, name);
                        editor.putString(Constants.PASSWORD, password);
                        editor.putString(Constants.NAME, fullName);
                        editor.putString(Constants.EMAIL, email);
                        editor.putString(Constants.PICTURE_NAME, pictureName);
                        editor.commit();

                        showLocationActivity();
                    } else if (status.equals("300")) {
                        L.alert(LoginActivity.this, "Invalid Password! \nPlease try again.");
                    } else if (status.equals("400")) {
                        L.alert(LoginActivity.this, "Invalid Username! \nPlease try again.");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] response, Throwable throwable) {
                loadingDialog.dismiss();
                L.alert(LoginActivity.this, Constants.WEB_FAILED);
            }
        });
    }

}
