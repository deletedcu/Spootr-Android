package com.ottawa.spootr2.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageButton;

import com.ottawa.spootr2.R;

/**
 * Created by king on 24/01/16.
 */
public class PrivacyActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_menu_privacy);

        initComponent();
    }

    /***********************************************************************************************
     ***********************                Internal Methods                ************************
     **********************************************************************************************/

    private void initComponent() {
        WebView webView = (WebView)findViewById(R.id.webView_privacy);
        ImageButton backButton = (ImageButton)findViewById(R.id.button_privacy_back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        webView.loadUrl("file:///android_asset/privacy.html");
    }

}
