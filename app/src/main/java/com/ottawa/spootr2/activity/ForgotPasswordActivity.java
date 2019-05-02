package com.ottawa.spootr2.activity;

import android.app.Activity;
import android.app.ProgressDialog;
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
public class ForgotPasswordActivity extends Activity {
    private ProgressDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_forgotpassword);

        LinearLayout layout = (LinearLayout)findViewById(R.id.layout_forgorpassword);
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                KeyUtil.hideSoftKeyboard(ForgotPasswordActivity.this);
            }
        });

        final EditText textEmail = (EditText)findViewById(R.id.text_forgot_email);
        Button sendButton = (Button)findViewById(R.id.button_forgotpassword_send);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = textEmail.getText().toString().trim();
                if (email.equals("")) {
                    L.alert(ForgotPasswordActivity.this, "No Email!\nPlease inesrt email to reset password.");
                } else if (isValid(email)) {
                    forgotPassword(email);
                } else {
                    L.alert(ForgotPasswordActivity.this, "Invalid Email!\nPlease insert valid email again.");
                }
            }
        });
        Button backButton = (Button)findViewById(R.id.button_forgotpassword_back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
     *************                    Web api functions                         ********************
     **********************************************************************************************/

    /**
     * api method: forgotPassword
     * @param email
     * return: json(status)
     */
    private void forgotPassword(final String email) {
        loadingDialog = L.progressDialog(this, "please wait...");
        final SharedData sharedData = SharedData.getInstance();

        RequestParams params = new RequestParams();
        params.put(Constants.ACTION, "forgotPassword");
        params.put(Constants.EMAIL, email);

        sharedData.httpClient.post(ForgotPasswordActivity.this, Constants.WEBSERVICE_URL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] response) {
                loadingDialog.dismiss();
                try {
                    String strResponse = new String(response, "UTF-8");
                    JSONArray jsonArray = new JSONArray(strResponse);
                    String status = jsonArray.getJSONObject(0).getString(Constants.STATUS);
                    if (status.equals(Constants.STATUS_SUCCESS)) {
                        L.alert(ForgotPasswordActivity.this, "Your request was sent successfully. \nPlease check your email to reset password.");
                    } else {
                        L.alert(ForgotPasswordActivity.this, "This email address isn't registered on spootr. \nPlease insert correct email again.");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    L.alert(ForgotPasswordActivity.this, Constants.WEB_FAILED);
                }
            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] response, Throwable throwable) {
                loadingDialog.dismiss();
                L.alert(ForgotPasswordActivity.this, Constants.WEB_FAILED);
            }
        });
    }
}
