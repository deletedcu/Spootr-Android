package com.ottawa.spootr2.fragment;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.ottawa.spootr2.R;
import com.ottawa.spootr2.activity.MessageActivity;
import com.ottawa.spootr2.common.Constants;
import com.ottawa.spootr2.common.SharedData;
import com.ottawa.spootr2.listAdapter.ChatListAdapter;
import com.ottawa.spootr2.model.Chat;
import com.ottawa.spootr2.model.Circle;
import com.ottawa.spootr2.model.Emoji;

import net.louislam.android.L;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by King on 02/05/2016.
 */
public class ChatFragment extends Fragment {

    private SharedPreferences preferences;
    private ListView listView;
    private TextView textEmpty;
    private ArrayList<Chat> itemList;
    private ChatListAdapter adapter;
    private ProgressDialog loadingDialog;
    private SimpleDateFormat isoFormat;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        preferences = getActivity().getSharedPreferences(Constants.PREFS_NAME, 0);
        isoFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        initailize(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(newMessage, new IntentFilter("message"));

        itemList.clear();
        getChats();
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(newMessage);
    }

    private BroadcastReceiver newMessage = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            itemList.clear();
            getChats();
        }
    };
    /**********************************************************************************************
     * Internal methods
     *********************************************************************************************/

    private void initailize(View view) {
        textEmpty = (TextView)view.findViewById(R.id.text_fragment_chat_empty);
        textEmpty.setVisibility(View.GONE);

        listView = (ListView)view.findViewById(R.id.listview_fragment_chat);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Chat chat = itemList.get(position);

                int messageCount = SharedData.getInstance().messageCount;
                messageCount = messageCount - chat.getnMessageCount();
                SharedData.getInstance().messageCount = messageCount;
                Intent broadcastIntent = new Intent();
                broadcastIntent.setAction("message");
                getActivity().sendBroadcast(broadcastIntent);

                Intent intent = new Intent(getActivity(), MessageActivity.class);
                intent.putExtra("chat", chat);
                startActivity(intent);
            }
        });

        itemList = new ArrayList<Chat>();
        adapter = new ChatListAdapter(getActivity(), 0, itemList);
        listView.setAdapter(adapter);

    }

    private void getChats() {
        loadingDialog = L.progressDialog(getActivity(), Constants.REQUEST_WAITING);
        int nUserId = preferences.getInt(Constants.USER_ID, 0);
        final SharedData sharedData = SharedData.getInstance();
        RequestParams params = new RequestParams();
        params.put(Constants.ACTION, "getChats");
        params.put(Constants.USER_ID, nUserId);

        sharedData.httpClient.post(getActivity(), Constants.WEBSERVICE_URL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int k, Header[] headers, byte[] response) {
                try {
                    String strResponse = new String(response, "UTF-8");
                    JSONArray jsonArray = new JSONArray(strResponse);
                    String status = jsonArray.getJSONObject(0).getString(Constants.STATUS);

                    if (status.equals(Constants.STATUS_SUCCESS)) {
                        int count = 0;
                        JSONArray result = jsonArray.getJSONArray(1);
                        if (result.length() > 0) {
                            for (int i = 0; i < result.length(); i ++) {
                                JSONObject item = result.getJSONObject(i);

                                int roomId = item.getInt(Constants.CHATROOM_ID);
                                int userId = item.getInt(Constants.ID);
                                String userName = item.getString(Constants.USER_NAME);
                                String name = item.getString(Constants.NAME);
                                if(name.equals("")) {
                                    name = userName;
                                }
                                String pictureName = item.getString(Constants.PICTURE_NAME);
                                String message = item.getString(Constants.MESSAGE);
                                boolean fromme = item.getInt(Constants.FROMME) == 1 ? true : false;
                                int messageCount = item.getInt(Constants.MESSAGE_COUNT);
                                Date date = isoFormat.parse(item.getString(Constants.CHAT_TIME));

                                boolean isNew = false;
                                if (messageCount > 0) {
                                    isNew = true;
                                }
                                count += messageCount;

                                Chat chat = new Chat();
                                chat.setnRoomId(roomId);
                                chat.setnUserId(userId);
                                chat.setStrUserName(name);
                                chat.setStrPictureName(pictureName);
                                chat.setStrMessage(message);
                                chat.setFromme(fromme);
                                chat.setnMessageCount(messageCount);
                                chat.setChatTime(date);
                                chat.setNew(isNew);

                                itemList.add(chat);

                            }
                        }
                        sharedData.messageCount = count;
                    }

                    adapter.updateResults(itemList);
                    if (itemList.size() == 0) {
                        textEmpty.setVisibility(View.VISIBLE);
                    } else {
                        textEmpty.setVisibility(View.GONE);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), "getGlobalCircles was failed", Toast.LENGTH_SHORT);
                } finally {
                    loadingDialog.dismiss();
                }
            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                loadingDialog.dismiss();
                Toast.makeText(getActivity(), Constants.WEB_FAILED, Toast.LENGTH_SHORT);
            }
        });
    }
}
