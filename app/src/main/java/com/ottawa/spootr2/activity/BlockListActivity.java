package com.ottawa.spootr2.activity;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
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
import com.ottawa.spootr2.common.Constants;
import com.ottawa.spootr2.common.SharedData;
import com.ottawa.spootr2.listAdapter.BlockListAdapter;
import com.ottawa.spootr2.model.Chat;
import com.ottawa.spootr2.model.Emoji;

import net.louislam.android.L;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by King on 5/10/2016.
 */
public class BlockListActivity extends AppCompatActivity implements OnMapReadyCallback {

    private ListView listView;
    private ArrayList<Chat> itemList;
    private TextView textEmpty;
    private BlockListAdapter adapter;
    private GoogleMap googleMap;
    private SharedPreferences preferences;
    private ProgressDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blocklist);

        preferences = getSharedPreferences(Constants.PREFS_NAME, 0);

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map_blocklist);
        mapFragment.getMapAsync(this);

        googleMap = mapFragment.getMap();

        initailize();
    }

    public void onMapReady(GoogleMap googleMap) {
        updateMap();
    }

    private void updateMap() {
        Float latitude = preferences.getFloat(Constants.LATITUDE, 0);
        Float longitude = preferences.getFloat(Constants.LONGITUDE, 0);

        LatLng latLng = new LatLng(latitude, longitude);

        googleMap.setMyLocationEnabled(true);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        googleMap.addMarker(new MarkerOptions().position(latLng));
    }

    private void initailize() {
        textEmpty = (TextView)findViewById(R.id.text_blocklist_empty);
        ImageButton backButton = (ImageButton)findViewById(R.id.button_blocklist_back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        listView = (ListView)findViewById(R.id.listview_blocklist);

        itemList = new ArrayList<Chat>();
        adapter = new BlockListAdapter(this, 0, itemList);
        adapter.setOnBlockListener(new BlockListAdapter.BlockListener() {
            @Override
            public void unBlock(int index) {
                Chat item = itemList.get(index);
                unblockUser(item);
            }
        });

        listView.setAdapter(adapter);

        getBlockList();

    }

    private void getBlockList() {
        loadingDialog = L.progressDialog(this, Constants.REQUEST_WAITING);
        int nUserId = preferences.getInt(Constants.USER_ID, 0);
        SharedData sharedData = SharedData.getInstance();
        RequestParams params = new RequestParams();
        params.put(Constants.ACTION, "getBlockList");
        params.put(Constants.USER_ID, nUserId);

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
                                JSONObject item = result.getJSONObject(i);

                                int roomId = item.getInt(Constants.CHATROOM_ID);
                                int userId = item.getInt(Constants.ID);
                                String userName = item.getString(Constants.USER_NAME);
                                String name = item.getString(Constants.NAME);
                                if(name.equals("")) {
                                    name = userName;
                                }
                                String pictureName = item.getString(Constants.PICTURE_NAME);

                                Chat chat = new Chat();
                                chat.setnRoomId(roomId);
                                chat.setnUserId(userId);
                                chat.setStrUserName(name);
                                chat.setStrPictureName(pictureName);

                                itemList.add(chat);

                            }
                        }
                    }

                    adapter.updateResults(itemList);
                    if (itemList.size() == 0) {
                        textEmpty.setVisibility(View.VISIBLE);
                    } else {
                        textEmpty.setVisibility(View.GONE);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(BlockListActivity.this, "getBlockList was failed", Toast.LENGTH_SHORT);
                } finally {
                    loadingDialog.dismiss();
                }
            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                loadingDialog.dismiss();
                Toast.makeText(BlockListActivity.this, Constants.WEB_FAILED, Toast.LENGTH_SHORT);
            }
        });
    }

    private void unblockUser(final Chat item) {
        loadingDialog = L.progressDialog(this, Constants.REQUEST_WAITING);
        int nUserId = preferences.getInt(Constants.USER_ID, 0);
        SharedData sharedData = SharedData.getInstance();
        RequestParams params = new RequestParams();
        params.put(Constants.ACTION, "unblockUser");
        params.put(Constants.USER_ID, nUserId);
        params.put(Constants.CHATROOM_ID, item.getnRoomId());

        sharedData.httpClient.post(this, Constants.WEBSERVICE_URL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int k, Header[] headers, byte[] response) {
                try {
                    String strResponse = new String(response, "UTF-8");
                    JSONArray jsonArray = new JSONArray(strResponse);
                    String status = jsonArray.getJSONObject(0).getString(Constants.STATUS);

                    if (status.equals(Constants.STATUS_SUCCESS)) {
                        itemList.remove(item);
                        adapter.updateResults(itemList);

                        if (itemList.size() == 0) {
                            textEmpty.setVisibility(View.VISIBLE);
                        } else {
                            textEmpty.setVisibility(View.GONE);
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(BlockListActivity.this, "unblockUser was failed", Toast.LENGTH_SHORT);
                } finally {
                    loadingDialog.dismiss();
                }
            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                loadingDialog.dismiss();
                Toast.makeText(BlockListActivity.this, Constants.WEB_FAILED, Toast.LENGTH_SHORT);
            }
        });
    }
}
