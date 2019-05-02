package com.ottawa.spootr2.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
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
import com.ottawa.spootr2.listAdapter.CircleListAdapter;
import com.ottawa.spootr2.model.Circle;

import net.louislam.android.L;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by King on 03/05/2016.
 */
public class SearchActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener {

    private GoogleMap googleMap;
    private SharedPreferences preferences;
    private EditText textSearch;
    private ListView listView;
    private ArrayList<Circle> itemList;
    private CircleListAdapter adapter;
    private ProgressDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchcircle);

        preferences = getSharedPreferences(Constants.PREFS_NAME, 0);
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map_searchcircle);
        mapFragment.getMapAsync(this);
        googleMap = mapFragment.getMap();

        initialize();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        updateMap();
    }

    /***********************************************************************************************
     * Internal Methods
     */

    private void updateMap() {
        Float latitude = preferences.getFloat(Constants.LATITUDE, 0);
        Float longitude = preferences.getFloat(Constants.LONGITUDE, 0);
        LatLng latLng = new LatLng(latitude, longitude);

        googleMap.setMyLocationEnabled(true);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        googleMap.addMarker(new MarkerOptions().position(latLng));
    }

    private void initialize() {
        ImageButton searchButton = (ImageButton)findViewById(R.id.button_searchcircle_search);
        ImageButton backButton = (ImageButton)findViewById(R.id.button_searchcircle_back);
        searchButton.setOnClickListener(this);
        backButton.setOnClickListener(this);

        textSearch = (EditText)findViewById(R.id.editText_searchcircle);
        listView = (ListView)findViewById(R.id.listview_searchcircle);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Circle item = itemList.get(position);
                Intent intent = new Intent(SearchActivity.this, SearchDetailActivity.class);
                intent.putExtra("searchType", Constants.CIRCLE_SEARCH);
                intent.putExtra("circle", item);
                startActivity(intent);
            }
        });

        itemList = new ArrayList<Circle>();
        adapter = new CircleListAdapter(this, 0, itemList);
        listView.setAdapter(adapter);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_searchcircle_search:
                String search = textSearch.getText().toString().trim().toLowerCase();
                if (!search.equals("")) {
                    getCircles(search);
                }
                break;
            case R.id.button_searchcircle_back:
                finish();
                break;
            default:
                break;
        }
    }

    /**********************************************************************************************
     * Web api
     **********************************************************************************************/

    private void getCircles(String search) {
        loadingDialog = L.progressDialog(this, Constants.REQUEST_WAITING);
        SharedData sharedData = SharedData.getInstance();
        final int nUserId = preferences.getInt(Constants.USER_ID, 0);
        itemList.clear();

        RequestParams params = new RequestParams();
        params.put(Constants.ACTION, "getCircles");
        params.put("search", search);

        sharedData.httpClient.post(SearchActivity.this, Constants.WEBSERVICE_URL, params, new AsyncHttpResponseHandler() {
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
                                int circleId = data.getInt(Constants.ID);
                                String name = data.getString(Constants.NAME);
                                Circle circle = new Circle(circleId, name, false);

                                itemList.add(circle);
                            }
                        }

                    }
                    adapter.updateResults(itemList);

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(SearchActivity.this, "getCircles was failed", Toast.LENGTH_SHORT);
                } finally {
                    loadingDialog.dismiss();
                }
            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                Toast.makeText(SearchActivity.this, Constants.WEB_FAILED, Toast.LENGTH_SHORT);
            }
        });
    }

}
