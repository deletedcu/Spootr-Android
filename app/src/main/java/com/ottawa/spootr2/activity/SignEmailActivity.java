package com.ottawa.spootr2.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
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

/**
 * Created by king on 17/02/16.
 */
public class SignEmailActivity extends Activity {
    private ProgressDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_signemail);

        LinearLayout layout = (LinearLayout)findViewById(R.id.layout_signemail);
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                KeyUtil.hideSoftKeyboard(SignEmailActivity.this);
            }
        });
        final EditText textEmail = (EditText)findViewById(R.id.text_signemail);
        Button nextButton = (Button)findViewById(R.id.button_signemail_next);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = textEmail.getText().toString().trim().toLowerCase();
                if (email.equals("")) {
                    L.alert(SignEmailActivity.this, "No Email!\nPlease insert email to signup");
                } else if (isValid(email)) {
                    isValidEmail(email);
                } else {
                    L.alert(SignEmailActivity.this, "Invalid Email!\nPlease insert again.");
                }
            }
        });
        Button loginButton = (Button)findViewById(R.id.button_signemail_login);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignEmailActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
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

    private boolean isValid(String email) {
        if (email.equals("")) {
            return false;
        } else {
            return Patterns.EMAIL_ADDRESS.matcher(email).matches();
        }
    }

    /***********************************************************************************************
     *********************                      Web Api Functions               ********************
     **********************************************************************************************/

    /**
     * api method: isValidEmail
     * parameter: email
     * return: json(status)
     */
    private void isValidEmail(final String email) {
        loadingDialog = L.progressDialog(this, "please wait...");
        final SharedData sharedData = SharedData.getInstance();

        RequestParams params = new RequestParams();
        params.put(Constants.ACTION, "isValidEmail");
        params.put(Constants.EMAIL, email);

        sharedData.httpClient.post(SignEmailActivity.this, Constants.WEBSERVICE_URL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] response) {
                loadingDialog.dismiss();
                try {
                    String strResponse = new String(response, "UTF-8");
                    JSONArray jsonArray = new JSONArray(strResponse);
                    String status = jsonArray.getJSONObject(0).getString(Constants.STATUS);
                    if (status.equals(Constants.STATUS_SUCCESS)) {
                        Intent intent = new Intent(SignEmailActivity.this, SignUserActivity.class);
                        intent.putExtra(Constants.EMAIL, email);
                        startActivity(intent);
                        finish();
                    } else {
                        L.alert(SignEmailActivity.this, "This email address is already existing. \nPlease insert other email.");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    L.alert(SignEmailActivity.this, Constants.WEB_FAILED);
                }
            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] response, Throwable throwable) {
                loadingDialog.dismiss();
                L.alert(SignEmailActivity.this, Constants.WEB_FAILED);
            }
        });
    }
}
