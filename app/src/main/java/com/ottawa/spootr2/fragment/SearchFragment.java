package com.ottawa.spootr2.fragment;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.ottawa.spootr2.R;
import com.ottawa.spootr2.activity.SearchActivity;
import com.ottawa.spootr2.activity.SearchDetailActivity;
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
 * Created by King on 02/05/2016.
 */
public class SearchFragment extends Fragment {

    private SharedPreferences preferences;
    private ListView listView;
    private CircleListAdapter adapter;
    private ArrayList<Circle> itemList;
    private ProgressDialog loadingDialog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        preferences = getActivity().getSharedPreferences(Constants.PREFS_NAME, 0);

        initialize(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getGlobalCircles();
    }

    /**********************************************************************************************
     * Internal methods
     *********************************************************************************************/

    private void initialize(View view) {
        ImageButton searchButton = (ImageButton)view.findViewById(R.id.button_fragment_search);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SearchActivity.class);
                startActivity(intent);
            }
        });

        listView = (ListView)view.findViewById(R.id.listview_fragment_search);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Circle item = itemList.get(position);

                if (!item.isHeader()) {
                    if (item.isTrending()) {
                        Intent intent = new Intent(getActivity(), SearchDetailActivity.class);
                        intent.putExtra("searchType", Constants.TRENDING_SEARCH);
                        intent.putExtra("tag", item.getStrName());
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(getActivity(), SearchDetailActivity.class);
                        intent.putExtra("searchType", Constants.CIRCLE_SEARCH);
                        intent.putExtra("circle", item);
                        startActivity(intent);
                    }
                }
            }
        });

        itemList = new ArrayList<Circle>();
        adapter = new CircleListAdapter(getActivity(), 0, itemList);
        listView.setAdapter(adapter);

    }


    /**********************************************************************************************
     * Web api
     **********************************************************************************************/

    private void getGlobalCircles() {
        itemList.clear();
        loadingDialog = L.progressDialog(getActivity(), Constants.REQUEST_WAITING);
        SharedData sharedData = SharedData.getInstance();
        RequestParams params = new RequestParams();
        params.put(Constants.ACTION, "getGlobalCircles");

        sharedData.httpClient.post(getActivity(), Constants.WEBSERVICE_URL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int k, Header[] headers, byte[] response) {
                try {
                    String strResponse = new String(response, "UTF-8");
                    JSONArray jsonArray = new JSONArray(strResponse);
                    ArrayList<Circle> globalCircles = new ArrayList<Circle>();
                    ArrayList<Circle> trendingCircles = new ArrayList<Circle>();

                    JSONArray globals = jsonArray.getJSONArray(0);
                    if (globals.length() > 0) {
                        for (int i = 0; i < globals.length(); i ++) {
                            JSONObject item = globals.getJSONObject(i);

                            int circleId = item.getInt(Constants.ID);
                            String name = item.getString(Constants.NAME);

                            Circle circle = new Circle(circleId, name, false);
                            globalCircles.add(circle);
                        }
                    }

                    JSONArray trendings = jsonArray.getJSONArray(1);
                    if (trendings.length() > 0) {
                        for (int i = 0; i < trendings.length(); i ++) {
                            JSONObject item = trendings.getJSONObject(i);
                            String name = item.getString("tag");

                            Circle circle = new Circle(0, name, true);
                            trendingCircles.add(circle);
                        }
                    }

                    Circle header1 = new Circle("Global Circles", true);
                    Circle header2 = new Circle("Trending Topics", true);

                    itemList.add(header1);
                    if (globalCircles.size() > 0)
                        itemList.addAll(globalCircles);
                    itemList.add(header2);
                    if (trendingCircles.size() > 0)
                        itemList.addAll(trendingCircles);

                    adapter.updateResults(itemList);

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), "getGlobalCircles was failed", Toast.LENGTH_SHORT);
                } finally {
                    loadingDialog.dismiss();
                }
            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                Toast.makeText(getActivity(), Constants.WEB_FAILED, Toast.LENGTH_SHORT);
            }
        });
    }

}
