package com.ottawa.spootr2.activity;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.ottawa.spootr2.R;
import com.ottawa.spootr2.common.Constants;
import com.ottawa.spootr2.common.SharedData;
import com.ottawa.spootr2.listAdapter.MessageAdapter;
import com.ottawa.spootr2.model.Chat;
import com.ottawa.spootr2.model.Emoji;
import com.ottawa.spootr2.model.Message;

import net.louislam.android.L;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by King on 5/10/2016.
 */
public class MessageActivity extends AppCompatActivity implements View.OnClickListener {

    private ListView listView;
    private ArrayList<Message> itemList;
    private MessageAdapter adapter;
    private SharedPreferences preferences;
    private ProgressDialog loadingDialog;
    private RelativeLayout menuLayout;
    private EditText editText;
    private SimpleDateFormat isoFormat;
    private Chat chat;
    private Emoji userEmoji;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        registerReceiver(messageReceiver, new IntentFilter("message"));

        preferences = getSharedPreferences(Constants.PREFS_NAME, 0);
        Bundle bundle = getIntent().getExtras();
        chat = (Chat)bundle.getSerializable("chat");
        isoFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        userEmoji = new Emoji();
        try {
            InputStream is = getAssets().open(chat.getStrPictureName());
            Drawable drawable = Drawable.createFromStream(is, null);
            userEmoji.setPictureName(chat.getStrPictureName());
            userEmoji.setDrawable(drawable);
        } catch (IOException e) {
            e.printStackTrace();
        }

        menuLayout = (RelativeLayout)findViewById(R.id.layout_message_menu);
        menuLayout.setVisibility(View.GONE);
        ImageButton closeButton = (ImageButton)findViewById(R.id.button_message_close);
        closeButton.setOnClickListener(this);
        ImageButton menuButton = (ImageButton)findViewById(R.id.button_message_menu);
        menuButton.setOnClickListener(this);
        ImageButton blockButton = (ImageButton)findViewById(R.id.button_message_block);
        blockButton.setOnClickListener(this);
        ImageButton deleteButton = (ImageButton)findViewById(R.id.button_message_delete);
        deleteButton.setOnClickListener(this);
        ImageButton sendButton = (ImageButton)findViewById(R.id.button_message_send);
        sendButton.setOnClickListener(this);

        editText = (EditText)findViewById(R.id.text_message_edit);
        listView = (ListView)findViewById(R.id.listview_message);

        itemList = new ArrayList<Message>();
        adapter = new MessageAdapter(this, 0, itemList);
        listView.setAdapter(adapter);

        getMessages();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(messageReceiver);
    }

    private BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            int senderId = bundle.getInt("senderId");
            if (senderId == chat.getnUserId()) {
                String content = bundle.getString("content");
                Message message = new Message();
                message.setStrMessage(content);
                message.setMessageDate(new Date());
                message.setEmoji(userEmoji);

                receiveMessage(message);
            }

        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_message_close:
                finish();
                break;
            case R.id.button_message_menu:
                if (menuLayout.isShown()) {
                    menuLayout.setVisibility(View.GONE);
                } else {
                    menuLayout.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.button_message_block:
                AlertDialog.Builder builder = new AlertDialog.Builder(MessageActivity.this);
                builder.setTitle("Are you sure?")
                        .setMessage("Do you want block this user?")
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                blockUser();
                            }
                        })
                        .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                return;
                            }
                        }).show();

                break;
            case R.id.button_message_delete:
                AlertDialog.Builder builder1 = new AlertDialog.Builder(MessageActivity.this);
                builder1.setTitle("Are you sure?")
                        .setMessage("Do you want delete chat with this user?")
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteChat();
                            }
                        })
                        .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                return;
                            }
                        }).show();
                break;
            case R.id.button_message_send:
                sendNewMessage();
                break;
            default:
                break;
        }
    }

    private void sendNewMessage() {
        String content = editText.getText().toString().trim();
        if (!content.equals("")) {
            Message message = new Message();
            message.setStrMessage(content);
            message.setMessageDate(new Date());
            message.setFromme(true);
            message.setStatus(1);

            editText.setText("");
            itemList.add(message);
            adapter.updateResult(itemList);

            messageSend(message);
        }
    }

    private void receiveMessage(Message message) {
        itemList.add(message);
        adapter.updateResult(itemList);
        readMessage();
    }

    // Web api

    private void getMessages() {
        loadingDialog = L.progressDialog(this, Constants.REQUEST_WAITING);
        int nUserId = preferences.getInt(Constants.USER_ID, 0);
        SharedData sharedData = SharedData.getInstance();
        RequestParams params = new RequestParams();
        params.put(Constants.ACTION, "getMessages");
        params.put(Constants.SENDER_ID, nUserId);
        params.put(Constants.CHATROOM_ID, chat.getnRoomId());

        sharedData.httpClient.post(this, Constants.WEBSERVICE_URL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int k, Header[] headers, byte[] response) {
                try {
                    String strResponse = new String(response, "UTF-8");
                    JSONArray jsonArray = new JSONArray(strResponse);
                    String status = jsonArray.getJSONObject(0).getString(Constants.STATUS);
                    if (status.equals(Constants.STATUS_SUCCESS)) {
                        JSONArray result = jsonArray.getJSONArray(1);
                        if (result.length() > 0) {
                            for (int i = 0; i < result.length(); i ++) {
                                JSONObject data = result.getJSONObject(i);

                                String message = data.getString(Constants.MESSAGE);
                                boolean fromme = data.getInt(Constants.FROMME) == 1 ? true : false;
                                Date date = isoFormat.parse(data.getString(Constants.MESSAGE_TIME));
                                Emoji emoji = new Emoji();
                                if (!fromme) {
                                    emoji = userEmoji;
                                }

                                Message item = new Message();
                                item.setStrMessage(message);
                                item.setFromme(fromme);
                                item.setMessageDate(date);
                                item.setEmoji(emoji);

                                itemList.add(item);

                            }
                        }
                        adapter.updateResult(itemList);

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(MessageActivity.this, "getMessages was failed", Toast.LENGTH_SHORT);
                } finally {
                    loadingDialog.dismiss();
                }
            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                loadingDialog.dismiss();
                Toast.makeText(MessageActivity.this, Constants.WEB_FAILED, Toast.LENGTH_SHORT);
            }
        });
    }

    private void messageSend(final Message message) {
        int nUserId = preferences.getInt(Constants.USER_ID, 0);
        String curUserName = preferences.getString(Constants.NAME, "");
        SharedData sharedData = SharedData.getInstance();
        RequestParams params = new RequestParams();
        params.put(Constants.ACTION, "sendMessage");
        params.put(Constants.SENDER_ID, nUserId);
        params.put(Constants.CHATROOM_ID, chat.getnRoomId());
        params.put(Constants.NAME, curUserName);
        params.put(Constants.CONVERSATION_ID, chat.getnUserId());
        params.put(Constants.MESSAGE, message.getStrMessage());

        sharedData.httpClient.post(this, Constants.WEBSERVICE_URL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int k, Header[] headers, byte[] response) {
                try {
                    String strResponse = new String(response, "UTF-8");
                    JSONArray jsonArray = new JSONArray(strResponse);
                    String status = jsonArray.getJSONObject(0).getString(Constants.STATUS);
                    if (status.equals(Constants.STATUS_SUCCESS)) {
                        message.setStatus(0);
                        itemList.set(itemList.size() - 1, message);
                        adapter.updateResult(itemList);
                        return;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(MessageActivity.this, "Message was failed", Toast.LENGTH_SHORT);
                }

                message.setStatus(2);
                itemList.set(itemList.size() - 1, message);
                adapter.updateResult(itemList);
            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                Toast.makeText(MessageActivity.this, Constants.WEB_FAILED, Toast.LENGTH_SHORT);
                message.setStatus(2);
                itemList.set(itemList.size() - 1, message);
                adapter.updateResult(itemList);
            }
        });
    }

    private void deleteChat() {
        loadingDialog = L.progressDialog(this, Constants.REQUEST_WAITING);
        SharedData sharedData = SharedData.getInstance();
        RequestParams params = new RequestParams();
        params.put(Constants.ACTION, "deleteChat");
        params.put(Constants.CHATROOM_ID, chat.getnRoomId());

        sharedData.httpClient.post(this, Constants.WEBSERVICE_URL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int k, Header[] headers, byte[] response) {
                try {
                    String strResponse = new String(response, "UTF-8");
                    JSONArray jsonArray = new JSONArray(strResponse);
                    String status = jsonArray.getJSONObject(0).getString(Constants.STATUS);
                    if (status.equals(Constants.STATUS_SUCCESS)) {
                        finish();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(MessageActivity.this, "Message was failed", Toast.LENGTH_SHORT);
                } finally {
                    loadingDialog.dismiss();
                }

            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                Toast.makeText(MessageActivity.this, Constants.WEB_FAILED, Toast.LENGTH_SHORT);
                loadingDialog.dismiss();
            }
        });
    }

    private void blockUser() {
        loadingDialog = L.progressDialog(this, Constants.REQUEST_WAITING);
        int nUserId = preferences.getInt(Constants.USER_ID, 0);
        SharedData sharedData = SharedData.getInstance();
        RequestParams params = new RequestParams();
        params.put(Constants.ACTION, "blockUser");
        params.put(Constants.CHATROOM_ID, chat.getnRoomId());
        params.put(Constants.USER_ID, nUserId);

        sharedData.httpClient.post(this, Constants.WEBSERVICE_URL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int k, Header[] headers, byte[] response) {
                try {
                    String strResponse = new String(response, "UTF-8");
                    JSONArray jsonArray = new JSONArray(strResponse);
                    String status = jsonArray.getJSONObject(0).getString(Constants.STATUS);
                    if (status.equals(Constants.STATUS_SUCCESS)) {
                        finish();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(MessageActivity.this, "Message was failed", Toast.LENGTH_SHORT);
                } finally {
                    loadingDialog.dismiss();
                }

            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                Toast.makeText(MessageActivity.this, Constants.WEB_FAILED, Toast.LENGTH_SHORT);
                loadingDialog.dismiss();
            }
        });
    }

    private void readMessage() {
        int nUserId = preferences.getInt(Constants.USER_ID, 0);
        SharedData sharedData = SharedData.getInstance();
        RequestParams params = new RequestParams();
        params.put(Constants.ACTION, "readMessage");
        params.put(Constants.CHATROOM_ID, chat.getnRoomId());
        params.put(Constants.SENDER_ID, chat.getnUserId());
        params.put(Constants.CONVERSATION_ID, nUserId);

        sharedData.httpClient.post(this, Constants.WEBSERVICE_URL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int k, Header[] headers, byte[] response) {
                try {
                    String strResponse = new String(response, "UTF-8");
                    JSONArray jsonArray = new JSONArray(strResponse);
                    String status = jsonArray.getJSONObject(0).getString(Constants.STATUS);
                    if (status.equals(Constants.STATUS_SUCCESS)) {

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {

            }
        });
    }

}
