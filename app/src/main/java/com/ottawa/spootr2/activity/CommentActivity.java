package com.ottawa.spootr2.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;
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
import com.ottawa.spootr2.common.KeyUtil;
import com.ottawa.spootr2.common.SharedData;
import com.ottawa.spootr2.library.pullloadmore.PullLoadMoreRecyclerView;
import com.ottawa.spootr2.listAdapter.CommentListAdapter;
import com.ottawa.spootr2.listAdapter.ReportOptionMenuManager;
import com.ottawa.spootr2.model.Chat;
import com.ottawa.spootr2.model.Comment;
import com.ottawa.spootr2.model.Post;

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
 * Created by king on 24/01/16.
 */
public class CommentActivity extends AppCompatActivity implements OnMapReadyCallback{

    private PullLoadMoreRecyclerView listView;
    private ArrayList<Comment> itemList;
    private Post post;
    private int maxId;
    private CommentListAdapter adapter;
    private ProgressDialog loadingDialog;
    private SimpleDateFormat isoFormat;
    private SharedPreferences preferences;
    private int oldLikeType;
    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceBundle) {
        super.onCreate(savedInstanceBundle);

        Bundle bundle = getIntent().getExtras();
        post = (Post)bundle.getSerializable("post");

        isoFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        preferences = getSharedPreferences(Constants.PREFS_NAME, 0);

        setContentView(R.layout.activity_comment);

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map_comment);
        mapFragment.getMapAsync(this);

        initComponent();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        Float latitude = preferences.getFloat(Constants.LATITUDE, 0);
        Float longitude = preferences.getFloat(Constants.LONGITUDE, 0);

        LatLng latLng = new LatLng(latitude, longitude);

        map.setMyLocationEnabled(true);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

        map.addMarker(new MarkerOptions().position(latLng));

    }
    /***********************************************************************************************
     ***********************                Internal Methods                ************************
     **********************************************************************************************/

    private void initComponent() {
        listView = (PullLoadMoreRecyclerView)findViewById(R.id.listView_comment);
        listView.setLinearLayout();
        listView.setFooterViewText("loading");

        listView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                KeyUtil.hideSoftKeyboard(CommentActivity.this);
            }
        });

        ImageButton backButton = (ImageButton) findViewById(R.id.button_comment_back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                KeyUtil.hideSoftKeyboard(CommentActivity.this);
                Intent intent = new Intent();
                intent.putExtra("post", post);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        ImageButton chatButton = (ImageButton) findViewById(R.id.button_comment_chat);
        chatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getChatroomInfo();
            }
        });

        editText = (EditText)findViewById(R.id.editText_comment);

        ImageButton sendButton = (ImageButton)findViewById(R.id.button_comment_send);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String content = editText.getText().toString().trim();
                if (content.length() > 0) {
                    KeyUtil.hideSoftKeyboard(CommentActivity.this);
                    addComment();
                }
            }
        });

        loadData();
    }

    private void loadData() {
        itemList = new ArrayList<Comment>();
        adapter = new CommentListAdapter(CommentActivity.this, itemList, post);
        listView.setAdapter(adapter);
        adapter.setOnPostListener(new CommentListAdapter.CommentListAdapterListener() {

            @Override
            public void onDismissKeyboard() {
                KeyUtil.hideSoftKeyboard(CommentActivity.this);
            }

            @Override
            public void onLikeButtonClick(int nLikeType) {
                likeButtonPressed(nLikeType);
            }

            @Override
            public void onReportMenuClick() {
                popupPostReport();
            }

            @Override
            public void onNotshowMenuClick() {
                AlertDialog.Builder builder = new AlertDialog.Builder(CommentActivity.this);
                builder.setTitle("Are you sure?")
                        .setMessage("Don't you want to see this post?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                notShowPost(post);
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
            public void onEditMenuClick() {
                Intent intent = new Intent(CommentActivity.this, PostActivity.class);
                intent.putExtra("post", post);
                startActivityForResult(intent, Constants.REQUEST_CODE_EDITPOST);
            }

            @Override
            public void onDeleteMenuClick() {
                AlertDialog.Builder builder = new AlertDialog.Builder(CommentActivity.this);
                builder.setTitle("Are you sure?")
                        .setMessage("Do you want to delete this post?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                deletePost(post);
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
            public void onCommentReport(int position) {
                Comment comment = itemList.get(position - 1);
                popupCommentReport(comment.getnId());
            }

            @Override
            public void onCommentNotshow(int position) {
                final Comment comment = itemList.get(position - 1);
                AlertDialog.Builder builder = new AlertDialog.Builder(CommentActivity.this);
                builder.setTitle("Are you sure?")
                        .setMessage("Don't you want to see this comment?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                notShowComment(comment);
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

        listView.setOnPullLoadMoreListener(new PullLoadMoreListener());
        loadingDialog = L.progressDialog(CommentActivity.this, Constants.REQUEST_WAITING);
        getComments();
    }

    private void popupPostReport() {
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

    private void popupCommentReport(final int commentId) {
        ReportOptionMenuManager manager = new ReportOptionMenuManager(this, R.layout.popup_report_menu);
        final PopupWindow popupWindow = new PopupWindow(manager.getContentView(), ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT);

        manager.setOnReportMenuListener(new ReportOptionMenuManager.ReportMenuListener() {
            @Override
            public void onContentMenu() {
                reportComment(commentId, "Offensive content");
                popupWindow.dismiss();
            }

            @Override
            public void onTargetMenu() {
                reportComment(commentId, "This post targets someone");
                popupWindow.dismiss();
            }

            @Override
            public void onSpamMenu() {
                reportComment(commentId, "Spam");
                popupWindow.dismiss();
            }
        });

        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        ViewGroup viewGroup = (ViewGroup) ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0);
        popupWindow.showAtLocation(viewGroup, Gravity.CENTER, 0, 0);
    }

    private void likeButtonPressed(int nLikeType) {
        oldLikeType = post.getnLikeType();

        if (oldLikeType == nLikeType) {
            post.setnLikeType(0);

            int oldLikeCount = (int)post.getLikeArray().get(oldLikeType - 1);
            oldLikeCount --;
            post.getLikeArray().set(oldLikeType - 1, oldLikeCount);
            post.setnLikeCount(post.getnLikeCount() - 1);

            adapter.updatePost(post);

            dislikePost();

        } else {
            post.setnLikeType(nLikeType);
            int newLikeCount = (int)post.getLikeArray().get(nLikeType - 1);
            newLikeCount ++;
            post.getLikeArray().set(nLikeType - 1, newLikeCount);

            if (oldLikeType > 0) {
                int oldLikeCount = (int)post.getLikeArray().get(oldLikeType - 1);
                oldLikeCount --;
                post.getLikeArray().set(oldLikeType - 1, oldLikeCount);
            } else {
                post.setnLikeCount(post.getnLikeCount() + 1);
            }

            adapter.updatePost(post);

            likePost();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == Constants.REQUEST_CODE_EDITPOST) {
                Bundle bundle = data.getExtras();
                post = (Post)bundle.getSerializable("post");
                adapter.updatePost(post);
            }
        }
    }

    /***********************************************************************************************
     *********************                  Web Api Functions                   ********************
     **********************************************************************************************/

    /**
     * api method: getComments
     * parameter: user_id, post_id, max_id, offset, page_size
     * return: json(status, array of comment)
     */
    private void getComments() {
        SharedData sharedData = SharedData.getInstance();
        int nUserId = preferences.getInt(Constants.USER_ID, 0);
        RequestParams params = new RequestParams();
        params.put(Constants.ACTION, "getComments");
        params.put(Constants.POST_ID, post.getnId());
        params.put(Constants.USER_ID, nUserId);
        params.put(Constants.MAX_ID, maxId);
        params.put(Constants.OFFSET, itemList.size());
        params.put(Constants.POST_PAGESIZE, Constants.PAGE_SIZE);

        sharedData.httpClient.post(CommentActivity.this, Constants.WEBSERVICE_URL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] response) {
                try {
                    String strResponse = new String(response, "UTF-8");
                    JSONArray jsonArray = new JSONArray(strResponse);
                    String status = jsonArray.getJSONObject(0).getString(Constants.STATUS);
                    if (status.equals(Constants.STATUS_SUCCESS)) {
                        JSONArray result = jsonArray.getJSONArray(1);
                        for (int k = 0; k < result.length(); k++) {
                            JSONObject item = result.getJSONObject(k);
                            int nId = item.getInt(Constants.ID);
                            if (nId > maxId)
                                maxId = nId;
                            Date date = isoFormat.parse(item.getString(Constants.COMMENT_TIME));
                            String strContent = item.getString(Constants.CONTENT);
                            String pictureName = item.getString(Constants.PICTURE_NAME);
                            String strUserName = item.getString(Constants.NAME);
                            InputStream is;
                            try {
                                is = getAssets().open(pictureName);

                            } catch (IOException e) {
                                e.printStackTrace();
                                is = getAssets().open("empty.png");
                            }

                            Drawable d = Drawable.createFromStream(is, null);

                            Comment comment = new Comment(nId, strContent, date, pictureName, strUserName, d);

                            itemList.add(comment);

                        }
                        post.setnCommentCount(itemList.size());
                        adapter.updateResults(post, itemList);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                loadingDialog.dismiss();
                listView.setPullLoadMoreCompleted();
            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] response, Throwable throwable) {
                loadingDialog.dismiss();
                listView.setPullLoadMoreCompleted();
            }
        });
    }

    private void getNewComments() {
        SharedData sharedData = SharedData.getInstance();
        int nUserId = preferences.getInt(Constants.USER_ID, 0);
        RequestParams params = new RequestParams();
        params.put(Constants.ACTION, "getNewComments");
        params.put(Constants.POST_ID, post.getnId());
        params.put(Constants.USER_ID, nUserId);
        params.put(Constants.MAX_ID, maxId);

        sharedData.httpClient.post(CommentActivity.this, Constants.WEBSERVICE_URL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] response) {
                try {
                    String strResponse = new String(response, "UTF-8");
                    JSONArray jsonArray = new JSONArray(strResponse);
                    String status = jsonArray.getJSONObject(0).getString(Constants.STATUS);
                    if (status.equals(Constants.STATUS_SUCCESS)) {
                        JSONArray result = jsonArray.getJSONArray(1);
                        for (int k = 0; k < result.length(); k ++) {
                            JSONObject item = result.getJSONObject(k);
                            int nId = item.getInt(Constants.ID);
                            if (nId > maxId)
                                maxId = nId;
                            Date date = isoFormat.parse(item.getString(Constants.COMMENT_TIME));
                            String strContent = item.getString(Constants.CONTENT);
                            String pictureName = item.getString(Constants.PICTURE_NAME);
                            String strUserName = item.getString(Constants.NAME);
                            InputStream is;
                            try {
                                is = getAssets().open(pictureName);

                            } catch (IOException e) {
                                e.printStackTrace();
                                is = getAssets().open("empty.png");
                            }

                            Drawable d = Drawable.createFromStream(is, null);

                            Comment comment = new Comment(nId, strContent, date, pictureName, strUserName, d);

                            itemList.add(comment);
                        }
                        post.setnCommentCount(itemList.size());
                        adapter.updateResults(post, itemList);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                loadingDialog.dismiss();
                listView.setPullLoadMoreCompleted();
            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] response, Throwable throwable) {
                loadingDialog.dismiss();
                listView.setPullLoadMoreCompleted();
            }
        });
    }

    private void likePost() {
        SharedData sharedData = SharedData.getInstance();
        RequestParams params = new RequestParams();
        int nUserId = preferences.getInt(Constants.USER_ID, 0);
        params.put(Constants.ACTION, "likePost");
        params.put(Constants.USER_ID, nUserId);
        params.put(Constants.POST_ID, post.getnId());
        params.put(Constants.LIKE_TYPE, post.getnLikeType());

        sharedData.httpClient.post(CommentActivity.this, Constants.WEBSERVICE_URL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] response) {
                try {
                    String strResponse = new String(response, "UTF-8");
                    JSONArray jsonArray = new JSONArray(strResponse);
                    String status = jsonArray.getJSONObject(0).getString(Constants.STATUS);
                    if (status.equals(Constants.STATUS_SUCCESS)) {

                    } else {
                        int newLikeCount = (int)post.getLikeArray().get(post.getnLikeType() - 1);
                        newLikeCount --;

                        post.getLikeArray().set(post.getnLikeType() - 1, newLikeCount);

                        post.setnLikeType(oldLikeType);
                        if (oldLikeType > 0) {
                            int oldLikeCount = (int)post.getLikeArray().get(oldLikeType - 1);
                            oldLikeCount ++;
                            post.getLikeArray().set(oldLikeType - 1, oldLikeCount);
                        } else {
                            post.setnLikeCount(post.getnLikeCount() - 1);
                        }
                        adapter.updatePost(post);
                        Toast.makeText(CommentActivity.this, "Reaction was failed. Please try again", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    int newLikeCount = (int)post.getLikeArray().get(post.getnLikeType() - 1);
                    newLikeCount --;

                    post.getLikeArray().set(post.getnLikeType() - 1, newLikeCount);
                    post.setnLikeType(oldLikeType);

                    if (oldLikeType > 0) {
                        int oldLikeCount = (int)post.getLikeArray().get(oldLikeType - 1);
                        oldLikeCount ++;
                        post.getLikeArray().set(oldLikeType - 1, oldLikeCount);
                    } else {
                        post.setnLikeCount(post.getnLikeCount() - 1);
                    }

                    adapter.updatePost(post);
                    Toast.makeText(CommentActivity.this, "Reaction was failed. Please try again", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] response, Throwable throwable) {
                int newLikeCount = (int)post.getLikeArray().get(post.getnLikeType() - 1);
                newLikeCount --;

                post.getLikeArray().set(post.getnLikeType() - 1, newLikeCount);
                post.setnLikeType(oldLikeType);

                if (oldLikeType > 0) {
                    int oldLikeCount = (int)post.getLikeArray().get(oldLikeType - 1);
                    oldLikeCount ++;
                    post.getLikeArray().set(oldLikeType - 1, oldLikeCount);
                } else {
                    post.setnLikeCount(post.getnLikeCount() - 1);
                }

                adapter.updatePost(post);
                Toast.makeText(CommentActivity.this, "Reaction was failed. Please try again", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void dislikePost() {
        SharedData sharedData = SharedData.getInstance();
        RequestParams params = new RequestParams();
        int nUserId = preferences.getInt(Constants.USER_ID, 0);
        params.put(Constants.ACTION, "dislikePost");
        params.put(Constants.USER_ID, nUserId);
        params.put(Constants.POST_ID, post.getnId());

        sharedData.httpClient.post(CommentActivity.this, Constants.WEBSERVICE_URL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] response) {
                try {
                    String strResponse = new String(response, "UTF-8");
                    JSONArray jsonArray = new JSONArray(strResponse);
                    String status = jsonArray.getJSONObject(0).getString(Constants.STATUS);
                    if (status.equals(Constants.STATUS_SUCCESS)) {

                    } else {
                        post.setnLikeType(oldLikeType);

                        int oldLikeCount = (int)post.getLikeArray().get(oldLikeType - 1);
                        oldLikeCount ++;
                        post.getLikeArray().set(oldLikeType - 1, oldLikeCount);
                        post.setnLikeCount(post.getnLikeCount() + 1);

                        adapter.updatePost(post);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    post.setnLikeType(oldLikeType);

                    int oldLikeCount = (int)post.getLikeArray().get(oldLikeType - 1);
                    oldLikeCount ++;
                    post.getLikeArray().set(oldLikeType - 1, oldLikeCount);
                    post.setnLikeCount(post.getnLikeCount() + 1);

                    adapter.updatePost(post);
                }

            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] response, Throwable throwable) {
                post.setnLikeType(oldLikeType);

                int oldLikeCount = (int)post.getLikeArray().get(oldLikeType - 1);
                oldLikeCount ++;
                post.getLikeArray().set(oldLikeType - 1, oldLikeCount);
                post.setnLikeCount(post.getnLikeCount() + 1);

                adapter.updatePost(post);
            }
        });
    }

    private void notShowPost(final Post post) {
        SharedData sharedData = SharedData.getInstance();
        RequestParams params = new RequestParams();
        int nUserId = preferences.getInt(Constants.USER_ID, 0);
        params.put(Constants.ACTION, "notShowPost");
        params.put(Constants.USER_ID, nUserId);
        params.put(Constants.POST_ID, post.getnId());

        sharedData.httpClient.post(CommentActivity.this, Constants.WEBSERVICE_URL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] response) {
                try {
                    String strResponse = new String(response, "UTF-8");
                    JSONArray jsonArray = new JSONArray(strResponse);
                    String status = jsonArray.getJSONObject(0).getString(Constants.STATUS);
                    if (status.equals(Constants.STATUS_SUCCESS)) {
                        Intent intent = new Intent();
                        intent.putExtra("post", post);
                        setResult(Constants.RESULT_REMOVE, intent);
                        finish();
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

    private void deletePost(final Post post) {
        SharedData sharedData = SharedData.getInstance();
        RequestParams params = new RequestParams();
        int nUserId = preferences.getInt(Constants.USER_ID, 0);
        params.put(Constants.ACTION, "deletePost");
        params.put(Constants.POST_ID, post.getnId());

        sharedData.httpClient.post(CommentActivity.this, Constants.WEBSERVICE_URL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] response) {
                try {
                    String strResponse = new String(response, "UTF-8");
                    JSONArray jsonArray = new JSONArray(strResponse);
                    String status = jsonArray.getJSONObject(0).getString(Constants.STATUS);
                    if (status.equals(Constants.STATUS_SUCCESS)) {
                        Intent intent = new Intent();
                        intent.putExtra("post", post);
                        setResult(Constants.RESULT_REMOVE, intent);
                        finish();
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

    private void notShowComment(final Comment comment) {
        SharedData sharedData = SharedData.getInstance();
        RequestParams params = new RequestParams();
        int nUserId = preferences.getInt(Constants.USER_ID, 0);
        params.put(Constants.ACTION, "notshowComment");
        params.put(Constants.USER_ID, nUserId);
        params.put(Constants.COMMENT_ID, comment.getnId());

        sharedData.httpClient.post(CommentActivity.this, Constants.WEBSERVICE_URL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] response) {
                try {
                    String strResponse = new String(response, "UTF-8");
                    JSONArray jsonArray = new JSONArray(strResponse);
                    String status = jsonArray.getJSONObject(0).getString(Constants.STATUS);
                    if (status.equals(Constants.STATUS_SUCCESS)) {
                        adapter.removeComment(comment);
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

    private void addComment() {
        loadingDialog = L.progressDialog(CommentActivity.this, Constants.REQUEST_WAITING);
        SharedData sharedData = SharedData.getInstance();
        SharedPreferences preferences = getSharedPreferences(Constants.PREFS_NAME, 0);
        int nUserId = preferences.getInt(Constants.USER_ID, 0);
        String content = editText.getText().toString().trim();
        RequestParams params = new RequestParams();
        params.put(Constants.ACTION, "addComment");
        params.put(Constants.USER_ID, nUserId);
        params.put(Constants.POST_ID, post.getnId());
        params.put(Constants.CONTENT, content);

        sharedData.httpClient.post(CommentActivity.this, Constants.WEBSERVICE_URL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] response) {
                loadingDialog.dismiss();
                try {
                    String strResponse = new String(response, "UTF-8");
                    JSONArray jsonArray = new JSONArray(strResponse);
                    String status = jsonArray.getJSONObject(0).getString(Constants.STATUS);
                    if (status.equals(Constants.STATUS_SUCCESS)) {
                        editText.setText("");
                        post.setCommented(true);
                        getNewComments();
                    } else {
                        L.alert(CommentActivity.this, Constants.WEB_FAILED);
                    }
                } catch (Exception e) {
                    L.alert(CommentActivity.this, Constants.WEB_FAILED);
                }
            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                loadingDialog.dismiss();
                L.alert(CommentActivity.this, Constants.WEB_FAILED);
            }
        });
    }

    private void getChatroomInfo() {
        loadingDialog = L.progressDialog(CommentActivity.this, Constants.REQUEST_WAITING);
        SharedData sharedData = SharedData.getInstance();
        SharedPreferences preferences = getSharedPreferences(Constants.PREFS_NAME, 0);
        int nUserId = preferences.getInt(Constants.USER_ID, 0);
        String content = editText.getText().toString().trim();
        RequestParams params = new RequestParams();
        params.put(Constants.ACTION, "getChatroomInfo");
        params.put(Constants.USER1_ID, nUserId);
        params.put(Constants.USER2_ID, post.getnUserId());
        params.put(Constants.CONTENT, content);

        sharedData.httpClient.post(CommentActivity.this, Constants.WEBSERVICE_URL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] response) {
                try {
                    String strResponse = new String(response, "UTF-8");
                    JSONArray jsonArray = new JSONArray(strResponse);
                    String status = jsonArray.getJSONObject(0).getString(Constants.STATUS);
                    if (status.equals(Constants.STATUS_SUCCESS)) {
                        JSONObject result1 = jsonArray.getJSONObject(1);
                        int chatroomId = result1.getInt(Constants.CHATROOM_ID);

                        JSONObject result2 = jsonArray.getJSONObject(2);
                        int uId = result2.getInt(Constants.ID);
                        String userName = result2.getString(Constants.USER_NAME);
                        String picturName = result2.getString(Constants.PICTURE_NAME);

                        Chat chat = new Chat();
                        chat.setnRoomId(chatroomId);
                        chat.setnUserId(uId);
                        chat.setStrUserName(userName);
                        chat.setStrPictureName(picturName);

                        Intent intent = new Intent(CommentActivity.this, MessageActivity.class);
                        intent.putExtra("chat", chat);
                        startActivity(intent);

                    } else if (status.equals("300")) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(CommentActivity.this);
                        builder.setTitle("Block!")
                                .setMessage("This user was blocked by you.")
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        return;
                                    }
                                }).show();
                    } else if (status.equals("301")) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(CommentActivity.this);
                        builder.setTitle("Block!")
                                .setMessage("You was blocked by this user.")
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        return;
                                    }
                                }).show();
                    }
                } catch (Exception e) {
                    L.alert(CommentActivity.this, Constants.WEB_FAILED);
                } finally {
                    loadingDialog.dismiss();
                }
            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                loadingDialog.dismiss();
                L.alert(CommentActivity.this, Constants.WEB_FAILED);
            }
        });
    }

    private void reportPost(int postId, String content) {
        loadingDialog = L.progressDialog(CommentActivity.this, Constants.REQUEST_WAITING);
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
                        L.alert(CommentActivity.this, "Report post was failed");
                    }
                } catch (Exception e) {
                    L.alert(CommentActivity.this, Constants.WEB_FAILED);
                } finally {
                    loadingDialog.dismiss();
                }
            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                loadingDialog.dismiss();
                L.alert(CommentActivity.this, Constants.WEB_FAILED);
            }
        });
    }

    private void reportComment(int commentId, String content) {
        loadingDialog = L.progressDialog(CommentActivity.this, Constants.REQUEST_WAITING);
        SharedData sharedData = SharedData.getInstance();
        SharedPreferences preferences = getSharedPreferences(Constants.PREFS_NAME, 0);
        int nUserId = preferences.getInt(Constants.USER_ID, 0);
        RequestParams params = new RequestParams();
        params.put(Constants.ACTION, "reportComment");
        params.put(Constants.USER_ID, nUserId);
        params.put(Constants.COMMENT_ID, commentId);
        params.put(Constants.CONTENT, content);

        sharedData.httpClient.post(CommentActivity.this, Constants.WEBSERVICE_URL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] response) {
                try {
                    String strResponse = new String(response, "UTF-8");
                    JSONArray jsonArray = new JSONArray(strResponse);
                    String status = jsonArray.getJSONObject(0).getString(Constants.STATUS);
                    if (status.equals(Constants.STATUS_SUCCESS)) {

                    } else {
                        L.alert(CommentActivity.this, "Report comment was failed");
                    }
                } catch (Exception e) {
                    L.alert(CommentActivity.this, Constants.WEB_FAILED);
                } finally {
                    loadingDialog.dismiss();
                }
            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                loadingDialog.dismiss();
                L.alert(CommentActivity.this, Constants.WEB_FAILED);
            }
        });
    }

    class PullLoadMoreListener implements PullLoadMoreRecyclerView.PullLoadMoreListener {
        @Override
        public void onRefresh() {
            itemList.clear();
            getComments();
        }

        @Override
        public void onLoadMore() {
            getNewComments();
        }
    }
}
