package com.ottawa.spootr2.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.ottawa.spootr2.R;
import com.ottawa.spootr2.common.Constants;
import com.ottawa.spootr2.common.SharedData;
import com.ottawa.spootr2.listAdapter.EmojiAdapter;
import com.ottawa.spootr2.model.Emoji;

import net.louislam.android.L;

import org.apache.http.Header;
import org.json.JSONArray;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by king on 17/02/16.
 */
public class SignEmojiActivity extends Activity {

    private GridView gridView;
    private ArrayList<Emoji> itemList;
    private ProgressDialog loadingDialog;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private int selectedIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initData();
        preferences = getSharedPreferences(Constants.PREFS_NAME, 0);
        editor = getSharedPreferences(Constants.PREFS_NAME, 0).edit();

        setContentView(R.layout.activity_signemoji);

        gridView = (GridView)findViewById(R.id.gridView_emoji);
        final EmojiAdapter adapter = new EmojiAdapter(this, itemList);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedIndex = position;
                adapter.setSelectedIndex(position);
            }
        });

        Button nextButton = (Button)findViewById(R.id.button_signemoji_next);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextButtonPressed();
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

    private void initData() {
        itemList = new ArrayList<Emoji>();
        for (int i = 1; i <= 20; i ++) {
            String name = String.format("%d-e.png", i);

            try {
                InputStream is = getAssets().open(name);
                Drawable d = Drawable.createFromStream(is, null);
                Emoji item = new Emoji();
                item.setPictureName(name);
                item.setDrawable(d);
                
                itemList.add(item);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private void nextButtonPressed() {
        if (selectedIndex < 0) {
            L.alert(SignEmojiActivity.this, "Please select your emoji to sign up.");
        } else {
            Emoji item = itemList.get(selectedIndex);
            addEmoji(item.getPictureName());
        }
    }

    private void gotoNextView() {
        Intent intent = new Intent(SignEmojiActivity.this, MainActivity.class);
        startActivity(intent);

        finish();
    }


    /***********************************************************************************************
     * **************                   Web Api Functions                           ****************
     **********************************************************************************************/

    /**
     * api method: addEmoji
     * param: picture_name, user_id
     * return: json(status)
     */

    private void addEmoji(final String pictureName) {
        loadingDialog = L.progressDialog(this, "please wait...");
        final SharedData sharedData = SharedData.getInstance();

        int userId = preferences.getInt(Constants.USER_ID, 0);

        RequestParams params = new RequestParams();
        params.put(Constants.ACTION, "addEmoji");
        params.put(Constants.USER_ID, userId);
        params.put(Constants.PICTURE_NAME, pictureName);

        sharedData.httpClient.post(SignEmojiActivity.this, Constants.WEBSERVICE_URL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] response) {
                loadingDialog.dismiss();
                try {
                    String strResponse = new String(response, "UTF-8");
                    JSONArray jsonArray = new JSONArray(strResponse);
                    String status = jsonArray.getJSONObject(0).getString(Constants.STATUS);
                    if (status.equals(Constants.STATUS_SUCCESS)) {

                        editor.putString(Constants.PICTURE_NAME, pictureName);
                        editor.commit();

                        gotoNextView();

                    } else {
                        L.alert(SignEmojiActivity.this, "This username is already existing. \nPlease insert other username.");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    L.alert(SignEmojiActivity.this, Constants.WEB_FAILED);
                }
            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] response, Throwable throwable) {
                loadingDialog.dismiss();
                L.alert(SignEmojiActivity.this, Constants.WEB_FAILED);
            }
        });
    }
}
