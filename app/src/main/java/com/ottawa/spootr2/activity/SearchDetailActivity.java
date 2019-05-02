package com.ottawa.spootr2.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupWindow;
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
import com.ottawa.spootr2.library.pullloadmore.PullLoadMoreRecyclerView;
import com.ottawa.spootr2.listAdapter.PostListAdapter;
import com.ottawa.spootr2.listAdapter.ReportOptionMenuManager;
import com.ottawa.spootr2.model.Circle;
import com.ottawa.spootr2.model.Post;

import net.louislam.android.L;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by King on 03/05/2016.
 */
public class SearchDetailActivity extends AppCompatActivity implements OnMapReadyCallback {

    private PullLoadMoreRecyclerView listView;
    private SharedPreferences preferences;
    private ArrayList<Post> itemList;
    private SimpleDateFormat isoFormat;
    private PostListAdapter adapter;
    private boolean isPullToRefresh;
    private ProgressDialog loadingDialog;
    private int maxId;
    private int offset;
    private int oldLikeType;
    private GoogleMap googleMap;
    private Circle selectedCircle;
    private int searchType;
    private String tagName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchdetail);

        preferences = getSharedPreferences(Constants.PREFS_NAME, 0);
        isoFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            searchType = bundle.getInt("searchType");
            selectedCircle = (Circle)bundle.getSerializable("circle");
            tagName = bundle.getString("tag");
        }

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map_searchdetail);
        mapFragment.getMapAsync(this);

        googleMap = mapFragment.getMap();

        TextView textTitle = (TextView)findViewById(R.id.text_searchdetail);
        if (searchType == Constants.CIRCLE_SEARCH) {
            textTitle.setText(selectedCircle.getStrName());
        } else {
            textTitle.setText(tagName);
        }

        ImageButton backButton = (ImageButton)findViewById(R.id.button_searchdetail_back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        initComponent();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        updateMap();
    }

    /**********************************************************************************************
     * Internal Method
     **********************************************************************************************/

    private void updateMap() {
        Float latitude = preferences.getFloat(Constants.LATITUDE, 0);
        Float longitude = preferences.getFloat(Constants.LONGITUDE, 0);

        LatLng latLng = new LatLng(latitude, longitude);

        googleMap.setMyLocationEnabled(true);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        googleMap.addMarker(new MarkerOptions().position(latLng));
    }

    private void initComponent() {
        listView = (PullLoadMoreRecyclerView) findViewById(R.id.listview_searchdetail);
        listView.setLinearLayout();
        listView.setFooterViewText("loading");

        itemList = new ArrayList<Post>();
        adapter = new PostListAdapter(this, itemList);

        listView.setAdapter(adapter);
        listView.setOnPullLoadMoreListener(new PullLoadMoreListener());

        adapter.setOnPostListener(new PostListAdapter.PostListAdapterListener() {

            @Override
            public void onLikeButtonClick(int position, int nLikeType) {
                likeButtonPressed(position, nLikeType);
            }

            @Override
            public void onCommentButtonClick(int position) {
                commentButtonPressed(position);
            }

            @Override
            public void onReportMenuClick(int position) {
                Post post = itemList.get(position);
                popupPostReport(post);
            }

            @Override
            public void onNotshowMenuClick(final int position) {
                final Post post = itemList.get(position);
                AlertDialog.Builder builder = new AlertDialog.Builder(SearchDetailActivity.this);
                builder.setTitle("Are you sure?")
                        .setMessage("Don't you want to see this post?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                notShowPost(post, position);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                return;
                            }
                        }).show();
            }

            @Override
            public void onEditMenuClick(int position) {
                Post post = itemList.get(position);
                Intent intent = new Intent(SearchDetailActivity.this, PostActivity.class);
                intent.putExtra("post", post);
                startActivityForResult(intent, Constants.REQUEST_CODE_EDITPOST);
            }

            @Override
            public void onDeleteMenuClick(final int position) {
                final Post post = itemList.get(position);
                AlertDialog.Builder builder = new AlertDialog.Builder(SearchDetailActivity.this);
                builder.setTitle("Are you sure?")
                        .setMessage("Do you want to delete this post?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                deletePost(post, position);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                return;
                            }
                        }).show();
            }
        });
        loadingDialog = L.progressDialog(this, Constants.REQUEST_WAITING);

        maxId = 0;
        isPullToRefresh = true;

        if (searchType == Constants.CIRCLE_SEARCH) {
            getPostsByCircle();
        } else {
            getPostsByTrending();
        }
    }

    private void likeButtonPressed(int position, int nLikeType) {
        Post item = itemList.get(position);
        oldLikeType = item.getnLikeType();

        if (oldLikeType == nLikeType) {
            item.setnLikeType(0);

            int oldLikeCount = (int)item.getLikeArray().get(oldLikeType - 1);
            oldLikeCount --;
            item.getLikeArray().set(oldLikeType - 1, oldLikeCount);
            item.setnLikeCount(item.getnLikeCount() - 1);

            adapter.updateItem(position, item);

            dislikePost(item, position);

        } else {
            item.setnLikeType(nLikeType);
            int newLikeCount = (int)item.getLikeArray().get(nLikeType - 1);
            newLikeCount ++;
            item.getLikeArray().set(nLikeType - 1, newLikeCount);

            if (oldLikeType > 0) {
                int oldLikeCount = (int)item.getLikeArray().get(oldLikeType - 1);
                oldLikeCount --;
                item.getLikeArray().set(oldLikeType - 1, oldLikeCount);
            } else {
                item.setnLikeCount(item.getnLikeCount() + 1);
            }

            adapter.updateItem(position, item);

            likePost(item, position);
        }

    }

    private void commentButtonPressed(int position) {
        Post post = itemList.get(position);
        Intent intent = new Intent(SearchDetailActivity.this, CommentActivity.class);
        intent.putExtra("post", post);
        startActivityForResult(intent, Constants.REQUEST_CODE_COMMENT);
    }

    private void popupPostReport(final Post post) {
        ReportOptionMenuManager manager = new ReportOptionMenuManager(this, R.layout.popup_report_menu);
        final PopupWindow popupWindow = new PopupWindow(manager.getContentView(), ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT);

        manager.setOnReportMenuListener(new ReportOptionMenuManager.ReportMenuListener() {
            @Override
            public void onContentMenu() {
                reportPost(post.getnId(), "Offensive content");
                popupWindow.dismiss();
            }

            @Override
            public void onTargetMenu() {
                reportPost(post.getnId(), "This post targets someone");
                popupWindow.dismiss();
            }

            @Override
            public void onSpamMenu() {
                reportPost(post.getnId(), "Spam");
                popupWindow.dismiss();
            }
        });

        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        ViewGroup viewGroup = (ViewGroup) ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0);
        popupWindow.showAtLocation(viewGroup, Gravity.CENTER, 0, 0);

    }

    /***********************************************************************************************
     *********************                  Web Api Functions                   ********************
     **********************************************************************************************/

    /**
     * api method: getPostsByCircle
     * parameter: user_id, latitude, longitude, distance, circle_id, circle_name, max_id, offset, pageSize
     * return: json(status, array of post)
     */
    private void getPostsByCircle() {
        final int nUserId = preferences.getInt(Constants.USER_ID, 0);

        SharedData sharedData = SharedData.getInstance();

        if (isPullToRefresh)
            offset = 0;
        else
            offset = itemList.size();

        RequestParams params = new RequestParams();
        params.put(Constants.ACTION, "getPostsByCircle");
        params.put(Constants.USER_ID, nUserId);
        params.put(Constants.MAX_ID, maxId);
        params.put(Constants.OFFSET, offset);
        params.put(Constants.POST_PAGESIZE, Constants.PAGE_SIZE);
        params.put("is_new", 1);
        params.put(Constants.CIRCLE_ID, selectedCircle.getnId());
        params.put(Constants.DISTANCE, Constants.SEARCH_DISTANCE);

        sharedData.httpClient.post(SearchDetailActivity.this, Constants.WEBSERVICE_URL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] response) {
                try {
                    String strResponse = new String(response, "UTF-8");
                    JSONArray jsonArray = new JSONArray(strResponse);
                    String status = jsonArray.getJSONObject(0).getString(Constants.STATUS);
                    if (isPullToRefresh)
                        itemList.clear();

                    if (status.equals(Constants.STATUS_SUCCESS)) {

                        JSONArray results = jsonArray.getJSONArray(1);
                        for (int k = 0; k < results.length(); k++) {
                            JSONObject item = results.getJSONObject(k);
                            int nId = item.getInt(Constants.ID);
                            if (nId > maxId)
                                maxId = nId;
                            Date date = isoFormat.parse(item.getString(Constants.POST_TIME));
                            int userId = item.getInt(Constants.USER_ID);
                            String strContent = item.getString(Constants.CONTENT);
                            int nLikeCount = item.getInt(Constants.LIKE_COUNT);
                            int nCommentCount = item.getInt(Constants.COMMENT_COUNT);
                            int nLikeType = item.getInt(Constants.LIKE_TYPE);
                            boolean isCommented = item.getInt(Constants.IS_COMMENTED) > 0 ? true : false;
                            boolean isMine = userId == nUserId ? true : false;
                            String strImageName = item.getString(Constants.POST_IMAGE);

                            ArrayList likeArray = new ArrayList();
                            likeArray.add(item.getInt(Constants.LIKE1));
                            likeArray.add(item.getInt(Constants.LIKE2));
                            likeArray.add(item.getInt(Constants.LIKE3));
                            likeArray.add(item.getInt(Constants.LIKE4));
                            likeArray.add(item.getInt(Constants.LIKE5));

                            Post post = new Post(nId, userId, strContent, date, nLikeCount,
                                    nCommentCount, nLikeType, isCommented, isMine, strImageName,
                                    likeArray, "", false);

                            itemList.add(post);
                        }
                    }

                    adapter.updateResults(itemList);

                } catch (Exception e) {
                    e.printStackTrace();
                }

                isPullToRefresh = false;
                listView.setPullLoadMoreCompleted();
                loadingDialog.dismiss();
            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] response, Throwable throwable) {
                isPullToRefresh = false;
                listView.setPullLoadMoreCompleted();
                loadingDialog.dismiss();
            }
        });
    }

    private void getPostsByTrending() {
        final int nUserId = preferences.getInt(Constants.USER_ID, 0);

        SharedData sharedData = SharedData.getInstance();

        if (isPullToRefresh)
            offset = 0;
        else
            offset = itemList.size();

        RequestParams params = new RequestParams();
        params.put(Constants.ACTION, "getPostsByTrending");
        params.put("tag", tagName);
        params.put(Constants.USER_ID, nUserId);
        params.put(Constants.MAX_ID, maxId);
        params.put(Constants.OFFSET, offset);
        params.put(Constants.POST_PAGESIZE, Constants.PAGE_SIZE);

        sharedData.httpClient.post(SearchDetailActivity.this, Constants.WEBSERVICE_URL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] response) {
                try {
                    String strResponse = new String(response, "UTF-8");
                    JSONArray jsonArray = new JSONArray(strResponse);
                    String status = jsonArray.getJSONObject(0).getString(Constants.STATUS);
                    if (isPullToRefresh)
                        itemList.clear();

                    if (status.equals(Constants.STATUS_SUCCESS)) {

                        JSONArray results = jsonArray.getJSONArray(1);
                        for (int k = 0; k < results.length(); k++) {
                            JSONObject item = results.getJSONObject(k);
                            int nId = item.getInt(Constants.ID);
                            if (nId > maxId)
                                maxId = nId;
                            Date date = isoFormat.parse(item.getString(Constants.POST_TIME));
                            int userId = item.getInt(Constants.USER_ID);
                            String strContent = item.getString(Constants.CONTENT);
                            int nLikeCount = item.getInt(Constants.LIKE_COUNT);
                            int nCommentCount = item.getInt(Constants.COMMENT_COUNT);
                            int nLikeType = item.getInt(Constants.LIKE_TYPE);
                            boolean isCommented = item.getInt(Constants.IS_COMMENTED) > 0 ? true : false;
                            boolean isMine = userId == nUserId ? true : false;
                            String strImageName = item.getString(Constants.POST_IMAGE);

                            ArrayList likeArray = new ArrayList();
                            likeArray.add(item.getInt(Constants.LIKE1));
                            likeArray.add(item.getInt(Constants.LIKE2));
                            likeArray.add(item.getInt(Constants.LIKE3));
                            likeArray.add(item.getInt(Constants.LIKE4));
                            likeArray.add(item.getInt(Constants.LIKE5));

                            Post post = new Post(nId, userId, strContent, date, nLikeCount,
                                    nCommentCount, nLikeType, isCommented, isMine, strImageName,
                                    likeArray, "", true);

                            itemList.add(post);
                        }
                    }

                    adapter.updateResults(itemList);

                } catch (Exception e) {
                    e.printStackTrace();
                }

                isPullToRefresh = false;
                listView.setPullLoadMoreCompleted();
                loadingDialog.dismiss();
            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] response, Throwable throwable) {
                isPullToRefresh = false;
                listView.setPullLoadMoreCompleted();
                loadingDialog.dismiss();
            }
        });
    }

    private void likePost(final Post post, final int position) {
        SharedData sharedData = SharedData.getInstance();
        RequestParams params = new RequestParams();
        int nUserId = preferences.getInt(Constants.USER_ID, 0);
        params.put(Constants.ACTION, "likePost");
        params.put(Constants.USER_ID, nUserId);
        params.put(Constants.POST_ID, post.getnId());
        params.put(Constants.LIKE_TYPE, post.getnLikeType());

        sharedData.httpClient.post(SearchDetailActivity.this, Constants.WEBSERVICE_URL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] response) {
                try {
                    String strResponse = new String(response, "UTF-8");
                    JSONArray jsonArray = new JSONArray(strResponse);
                    String status = jsonArray.getJSONObject(0).getString(Constants.STATUS);
                    if (status.equals(Constants.STATUS_SUCCESS)) {

                    } else {
                        Post item = post;
                        int newLikeCount = (int)post.getLikeArray().get(post.getnLikeType() - 1);
                        newLikeCount --;

                        item.getLikeArray().set(post.getnLikeType() - 1, newLikeCount);

                        item.setnLikeType(oldLikeType);
                        if (oldLikeType > 0) {
                            int oldLikeCount = (int)post.getLikeArray().get(oldLikeType - 1);
                            oldLikeCount ++;
                            item.getLikeArray().set(oldLikeType - 1, oldLikeCount);
                        } else {
                            item.setnLikeCount(post.getnLikeCount() - 1);
                        }

                        adapter.updateItem(position, item);

                        Toast.makeText(SearchDetailActivity.this, "Reaction was failed. Please try again", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();

                    Post item = post;
                    int newLikeCount = (int)post.getLikeArray().get(post.getnLikeType() - 1);
                    newLikeCount --;

                    item.getLikeArray().set(post.getnLikeType() - 1, newLikeCount);

                    item.setnLikeType(oldLikeType);
                    if (oldLikeType > 0) {
                        int oldLikeCount = (int)post.getLikeArray().get(oldLikeType - 1);
                        oldLikeCount ++;
                        item.getLikeArray().set(oldLikeType - 1, oldLikeCount);
                    } else {
                        item.setnLikeCount(post.getnLikeCount() - 1);
                    }
                    adapter.updateItem(position, item);
                    Toast.makeText(SearchDetailActivity.this, "Reaction was failed. Please try again", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] response, Throwable throwable) {
                Post item = post;
                int newLikeCount = (int)post.getLikeArray().get(post.getnLikeType() - 1);
                newLikeCount --;

                item.getLikeArray().set(post.getnLikeType() - 1, newLikeCount);

                item.setnLikeType(oldLikeType);
                if (oldLikeType > 0) {
                    int oldLikeCount = (int)post.getLikeArray().get(oldLikeType - 1);
                    oldLikeCount ++;
                    item.getLikeArray().set(oldLikeType - 1, oldLikeCount);
                } else {
                    item.setnLikeCount(post.getnLikeCount() - 1);
                }

                adapter.updateItem(position, item);
                Toast.makeText(SearchDetailActivity.this, "Reaction was failed. Please try again", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void dislikePost(final Post post, final int position) {
        SharedData sharedData = SharedData.getInstance();
        RequestParams params = new RequestParams();
        int nUserId = preferences.getInt(Constants.USER_ID, 0);
        params.put(Constants.ACTION, "dislikePost");
        params.put(Constants.USER_ID, nUserId);
        params.put(Constants.POST_ID, post.getnId());

        sharedData.httpClient.post(SearchDetailActivity.this, Constants.WEBSERVICE_URL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] response) {
                try {
                    String strResponse = new String(response, "UTF-8");
                    JSONArray jsonArray = new JSONArray(strResponse);
                    String status = jsonArray.getJSONObject(0).getString(Constants.STATUS);
                    if (status.equals(Constants.STATUS_SUCCESS)) {

                    } else {
                        Post item = post;
                        item.setnLikeType(oldLikeType);

                        int oldLikeCount = (int)post.getLikeArray().get(oldLikeType - 1);
                        oldLikeCount ++;
                        item.getLikeArray().set(oldLikeType - 1, oldLikeCount);
                        item.setnLikeCount(post.getnLikeCount() + 1);

                        adapter.updateItem(position, item);
                    }
                } catch (Exception e) {
                    e.printStackTrace();

                    Post item = post;
                    item.setnLikeType(oldLikeType);

                    int oldLikeCount = (int)post.getLikeArray().get(oldLikeType - 1);
                    oldLikeCount ++;
                    item.getLikeArray().set(oldLikeType - 1, oldLikeCount);
                    item.setnLikeCount(post.getnLikeCount() + 1);

                    adapter.updateItem(position, item);
                }

            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] response, Throwable throwable) {
                Post item = post;
                item.setnLikeType(oldLikeType);

                int oldLikeCount = (int)post.getLikeArray().get(oldLikeType - 1);
                oldLikeCount ++;
                item.getLikeArray().set(oldLikeType - 1, oldLikeCount);
                item.setnLikeCount(post.getnLikeCount() + 1);

                adapter.updateItem(position, item);
            }
        });
    }

    private void notShowPost(final Post post, final int position) {
        SharedData sharedData = SharedData.getInstance();
        RequestParams params = new RequestParams();
        int nUserId = preferences.getInt(Constants.USER_ID, 0);
        params.put(Constants.ACTION, "notShowPost");
        params.put(Constants.USER_ID, nUserId);
        params.put(Constants.POST_ID, post.getnId());

        sharedData.httpClient.post(SearchDetailActivity.this, Constants.WEBSERVICE_URL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] response) {
                try {
                    String strResponse = new String(response, "UTF-8");
                    JSONArray jsonArray = new JSONArray(strResponse);
                    String status = jsonArray.getJSONObject(0).getString(Constants.STATUS);
                    if (status.equals(Constants.STATUS_SUCCESS)) {
                        adapter.removeItem(position);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] response, Throwable throwable) {

            }
        });
    }

    private void deletePost(final Post post, final int position) {
        SharedData sharedData = SharedData.getInstance();
        RequestParams params = new RequestParams();
        params.put(Constants.ACTION, "deletePost");
        params.put(Constants.POST_ID, post.getnId());

        sharedData.httpClient.post(SearchDetailActivity.this, Constants.WEBSERVICE_URL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] response) {
                try {
                    String strResponse = new String(response, "UTF-8");
                    JSONArray jsonArray = new JSONArray(strResponse);
                    String status = jsonArray.getJSONObject(0).getString(Constants.STATUS);
                    if (status.equals(Constants.STATUS_SUCCESS)) {
                        adapter.removeItem(position);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] response, Throwable throwable) {

            }
        });
    }

    private void reportPost(int postId, String content) {
        loadingDialog = L.progressDialog(SearchDetailActivity.this, Constants.REQUEST_WAITING);
        SharedData sharedData = SharedData.getInstance();
        SharedPreferences preferences = getSharedPreferences(Constants.PREFS_NAME, 0);
        int nUserId = preferences.getInt(Constants.USER_ID, 0);
        RequestParams params = new RequestParams();
        params.put(Constants.ACTION, "reportPost");
        params.put(Constants.USER_ID, nUserId);
        params.put(Constants.POST_ID, postId);
        params.put(Constants.CONTENT, content);

        sharedData.httpClient.post(this, Constants.WEBSERVICE_URL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] response) {
                try {
                    String strResponse = new String(response, "UTF-8");
                    JSONArray jsonArray = new JSONArray(strResponse);
                    String status = jsonArray.getJSONObject(0).getString(Constants.STATUS);
                    if (status.equals(Constants.STATUS_SUCCESS)) {

                    } else {
                        L.alert(SearchDetailActivity.this, "Report post was failed");
                    }
                } catch (Exception e) {
                    L.alert(SearchDetailActivity.this, Constants.WEB_FAILED);
                } finally {
                    loadingDialog.dismiss();
                }
            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                loadingDialog.dismiss();
                L.alert(SearchDetailActivity.this, Constants.WEB_FAILED);
            }
        });
    }

    class PullLoadMoreListener implements PullLoadMoreRecyclerView.PullLoadMoreListener {
        @Override
        public void onRefresh() {
            maxId = 0;
            isPullToRefresh = true;
            if (searchType == Constants.CIRCLE_SEARCH) {
                getPostsByCircle();
            } else {
                getPostsByTrending();
            }
        }

        @Override
        public void onLoadMore() {
            if (searchType == Constants.CIRCLE_SEARCH) {
                getPostsByCircle();
            } else {
                getPostsByTrending();
            }
        }
    }

}
