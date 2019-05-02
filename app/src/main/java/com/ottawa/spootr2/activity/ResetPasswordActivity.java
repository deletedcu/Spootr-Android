package com.ottawa.spootr2.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
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
public class ResetPasswordActivity extends Activity {

    private String email;
    private SharedPreferences.Editor editor;
    private ProgressDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        Uri data = intent.getData();
        email = data.getQueryParameter("email");

        editor = getSharedPreferences(Constants.PREFS_NAME, 0).edit();

        setContentView(R.layout.activity_resetpassword);

        LinearLayout layout = (LinearLayout)findViewById(R.id.layout_resetpasswrod);
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                KeyUtil.hideSoftKeyboard(ResetPasswordActivity.this);
            }
        });
        final EditText textPassword = (EditText)findViewById(R.id.text_reset_password);
        final EditText textConfirmPassword = (EditText)findViewById(R.id.text_reset_confirmpassword);
        Button resetButton = (Button)findViewById(R.id.button_resetpassword_reset);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = textPassword.getText().toString();
                String confirmPassword = textConfirmPassword.getText().toString();
                if (password.equals("")) {
                    L.alert(ResetPasswordActivity.this, "No Password!\nPlease insert new password.");
                } else if (!confirmPassword.equals(password)) {
                    L.alert(ResetPasswordActivity.this, "No Match!\nConfirm password is not matched. Please try again.");
                } else {
                    resetPassword(password);
                }
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

    private void gotoNextView() {
        Intent intent = new Intent(ResetPasswordActivity.this, LocationActivity.class);
        startActivity(intent);
        finish();
    }

    /***********************************************************************************************
     *********************                      Web Api Functions               ********************
     **********************************************************************************************/

    /**
     * api method: resetPassword
     * parameter: email, password
     * return: user info
     */
    private void resetPassword(final String password) {
        loadingDialog = L.progressDialog(this, "please wait...");
        final SharedData sharedData = SharedData.getInstance();

        RequestParams params = new RequestParams();
        params.put(Constants.ACTION, "resetPassword");
        params.put(Constants.EMAIL, email);
        params.put(Constants.PASSWORD, password);

        sharedData.httpClient.post(ResetPasswordActivity.this, Constants.WEBSERVICE_URL, params, new AsyncHttpResponseHandler() {
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
                        String fullName = object.getString(Constants.NAME);
                        String name = object.getString(Constants.USER_NAME);
                        String email = object.getString(Constants.EMAIL);
                        String pictureName = object.getString(Constants.PICTURE_NAME);
                        if (fullName.equals(""))
                            fullName = name;

                        editor.putInt(Constants.USER_ID, userId);
                        editor.putString(Constants.USER_NAME, name);
                        editor.putString(Constants.NAME, fullName);
                        editor.putString(Constants.EMAIL, email);
                        editor.putString(Constants.PASSWORD, password);
                        editor.putString(Constants.PICTURE_NAME, pictureName);
                        editor.commit();

                        gotoNextView();

                    } else {
                        L.alert(ResetPasswordActivity.this, Constants.WEB_FAILED);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    L.alert(ResetPasswordActivity.this, Constants.WEB_FAILED);
                }
            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] response, Throwable throwable) {
                loadingDialog.dismiss();
                L.alert(ResetPasswordActivity.this, Constants.WEB_FAILED);
            }
        });
    }
}
