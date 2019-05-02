package com.ottawa.spootr2.activity;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.TextView;

import com.ottawa.spootr2.R;
import com.ottawa.spootr2.common.Constants;

import net.louislam.android.L;

/**
 * Created by King on 5/10/2016.
 */
public class MyWebViewActivity extends AppCompatActivity {

    private int type;
    private ProgressDialog loadingDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        Bundle bundle = getIntent().getExtras();
        type = bundle.getInt("type");

        WebView webView = (WebView)findViewById(R.id.webView);
        TextView textTitle = (TextView)findViewById(R.id.text_webview_title);
        ImageButton backButton = (ImageButton)findViewById(R.id.button_webview_back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        webView.setWebViewClient(new MyWebViewClient());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setDomStorageEnabled(true);

        if (Build.VERSION.SDK_INT >= 19) {
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        String strURL = "";
        if (type == Constants.FACEBOOK_TYPE) {
            textTitle.setText("Facebook");
            strURL = "https://www.facebook.com/Spootr-261005910717019";
        } else if (type == Constants.TWITTER_TYPE) {
            textTitle.setText("Twitter");
            strURL = "https://twitter.com/spootrhq";
        } else {
            textTitle.setText("Google Play Store");
            strURL = "https://play.google.com/store/apps/details?id=com.ottawa.spootr2";
        }

        webView.loadUrl(strURL);
    }

    private class MyWebViewClient extends WebViewClient {

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            loadingDialog = L.progressDialog(MyWebViewActivity.this, Constants.REQUEST_WAITING);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            loadingDialog.dismiss();
        }

    }
}
