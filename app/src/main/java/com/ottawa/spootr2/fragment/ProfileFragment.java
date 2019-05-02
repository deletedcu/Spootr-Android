package com.ottawa.spootr2.fragment;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.ottawa.spootr2.R;
import com.ottawa.spootr2.activity.CommentActivity;
import com.ottawa.spootr2.activity.NotificationActivity;
import com.ottawa.spootr2.activity.PostActivity;
import com.ottawa.spootr2.activity.SettingActivity;
import com.ottawa.spootr2.common.Constants;
import com.ottawa.spootr2.common.SharedData;
import com.ottawa.spootr2.library.pullloadmore.PullLoadMoreRecyclerView;
import com.ottawa.spootr2.listAdapter.PostListAdapter;
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
 * Created by King on 02/05/2016.
 */
public class ProfileFragment extends Fragment {

    private SharedPreferences preferences;
    private PullLoadMoreRecyclerView listView;
    private TextView textPostCount;
    private TextView textReactCount;
    private TextView textCommentCount;
    private ImageButton notificationButton;
    private ArrayList<Post> itemList;
    private SimpleDateFormat isoFormat;
    private PostListAdapter adapter;
    private boolean isPullToRefresh;
    private ProgressDialog loadingDialog;
    private int offset;
    private int oldLikeType;
    private int nPostCount;
    private int nReactCount;
    private int nCommentCount;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        preferences = getActivity().getSharedPreferences(Constants.PREFS_NAME, 0);
        isoFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        initComponent(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(notificationReceiver, new IntentFilter("notification"));

        int count = SharedData.getInstance().notificationCount;
        if (count > 0) {
            notificationButton.setImageResource(R.drawable.alarm_notification);
        } else {
            notificationButton.setImageResource(R.drawable.alarm);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(notificationReceiver);
    }

    private BroadcastReceiver notificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int count = SharedData.getInstance().notificationCount;
            if (count > 0) {
                notificationButton.setImageResource(R.drawable.alarm_notification);
            } else {
                notificationButton.setImageResource(R.drawable.alarm);
            }
            getMyPostInfo();
            getMyPosts();
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == getActivity().RESULT_OK) {
            if (requestCode == Constants.REQUEST_CODE_EDITPOST || requestCode == Constants.REQUEST_CODE_COMMENT || requestCode == Constants.REQUEST_CODE_EDITPOST) {
                Bundle bundle = data.getExtras();
                Post post = (Post) bundle.getSerializable("post");
                for (int i = 0; i < itemList.size(); i++) {
                    Post item = itemList.get(i);
                    if (item.getnId() == post.getnId()) {
                        itemList.set(i, post);
                        adapter.updateItem(i, post);
                        break;
                    }
                }
            }
        }
    }

    /**********************************************************************************************
     * Internal methods
     *********************************************************************************************/

    private void initComponent(View view) {

        ImageView imageView = (ImageView)view.findViewById(R.id.image_profile_user);
        TextView textUserName = (TextView)view.findViewById(R.id.text_profile_username);
        String pictureName = preferences.getString(Constants.PICTURE_NAME, "empty.png");
        try {
            InputStream is = getActivity().getAssets().open(pictureName);
            Drawable d = Drawable.createFromStream(is, null);
            imageView.setImageDrawable(d);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String name = preferences.getString(Constants.NAME, "");
        textUserName.setText(name);

        textPostCount = (TextView)view.findViewById(R.id.text_profile_postcount);
        textReactCount = (TextView)view.findViewById(R.id.text_profile_reactcount);
        textCommentCount = (TextView)view.findViewById(R.id.text_profile_commentcount);

        notificationButton = (ImageButton)view.findViewById(R.id.btn_profile_alarm);
        ImageButton settingButton = (ImageButton)view.findViewById(R.id.btn_profile_setting);
        notificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), NotificationActivity.class);
                startActivity(intent);
            }
        });

        settingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SettingActivity.class);
                startActivity(intent);
            }
        });

        listView = (PullLoadMoreRecyclerView) view.findViewById(R.id.listview_profile);
        listView.setLinearLayout();
        listView.setFooterViewText("loading...");

        itemList = new ArrayList<Post>();
        adapter = new PostListAdapter(getActivity(), itemList);

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

            }

            @Override
            public void onNotshowMenuClick(final int position) {

            }

            @Override
            public void onEditMenuClick(int position) {
                Post post = itemList.get(position);
                Intent intent = new Intent(getActivity(), PostActivity.class);
                intent.putExtra("post", post);
                startActivityForResult(intent, Constants.REQUEST_CODE_EDITPOST);
            }

            @Override
            public void onDeleteMenuClick(final int position) {
                final Post post = itemList.get(position);
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
        loadingDialog = L.progressDialog(getActivity(), Constants.REQUEST_WAITING);

        isPullToRefresh = true;
        getMyPostInfo();
        getMyPosts();
    }

    private void setPostInfo() {

        textPostCount.setText(String.valueOf(nPostCount));
        textReactCount.setText(String.valueOf(nReactCount));
        textCommentCount.setText(String.valueOf(nCommentCount));

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
        Intent intent = new Intent(getActivity(), CommentActivity.class);
        intent.putExtra("post", post);
        startActivityForResult(intent, Constants.REQUEST_CODE_COMMENT);
    }

    /***********************************************************************************************
     *********************                  Web Api Functions                   ********************
     **********************************************************************************************/

    /**
     * api method: getMyPosts
     * parameter: user_id, circle_ids, max_id, offset, pageSize, is_new
     * return: json(status, array of post)
     */
    private void getMyPosts() {
        final int nUserId = preferences.getInt(Constants.USER_ID, 0);
        double latitude = preferences.getFloat(Constants.LATITUDE, 0);
        double longitude = preferences.getFloat(Constants.LONGITUDE, 0);

        SharedData sharedData = SharedData.getInstance();

        if (isPullToRefresh)
            offset = 0;
        else
            offset = itemList.size();

        RequestParams params = new RequestParams();
        params.put(Constants.ACTION, "getMyPosts");
        params.put(Constants.USER_ID, nUserId);
        params.put(Constants.LATITUDE, latitude);
        params.put(Constants.LONGITUDE, longitude);
        params.put(Constants.OFFSET, offset);
        params.put(Constants.POST_PAGESIZE, Constants.PAGE_SIZE);

        sharedData.httpClient.post(getActivity(), Constants.WEBSERVICE_URL, params, new AsyncHttpResponseHandler() {
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
                            Date date = isoFormat.parse(item.getString(Constants.POST_TIME));
                            int userId = item.getInt(Constants.USER_ID);
                            String strContent = item.getString(Constants.CONTENT);
                            int nLikeCount = item.getInt(Constants.LIKE_COUNT);
                            int nCommentCount = item.getInt(Constants.COMMENT_COUNT);
                            int nLikeType = item.getInt(Constants.LIKE_TYPE);
                            boolean isCommented = item.getInt(Constants.IS_COMMENTED) > 0 ? true : false;
                            String strImageName = item.getString(Constants.POST_IMAGE);
                            double distance = item.getDouble(Constants.DISTANCE);
                            String strExtraInfo = "";
                            if (distance < 0.1) {
                                strExtraInfo = "Near by";
                            } else {
                                strExtraInfo = String.format("%1$.1f km away", distance);
                            }

                            ArrayList likeArray = new ArrayList();
                            likeArray.add(item.getInt(Constants.LIKE1));
                            likeArray.add(item.getInt(Constants.LIKE2));
                            likeArray.add(item.getInt(Constants.LIKE3));
                            likeArray.add(item.getInt(Constants.LIKE4));
                            likeArray.add(item.getInt(Constants.LIKE5));

                            Post post = new Post(nId, userId, strContent, date, nLikeCount,
                                    nCommentCount, nLikeType, isCommented, true, strImageName,
                                    likeArray, strExtraInfo, false);

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

        sharedData.httpClient.post(getActivity(), Constants.WEBSERVICE_URL, params, new AsyncHttpResponseHandler() {
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

                        Toast.makeText(getActivity(), "Reaction was failed. Please try again", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(getActivity(), "Reaction was failed. Please try again", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getActivity(), "Reaction was failed. Please try again", Toast.LENGTH_SHORT).show();
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

        sharedData.httpClient.post(getActivity(), Constants.WEBSERVICE_URL, params, new AsyncHttpResponseHandler() {
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

    private void deletePost(final Post post, final int position) {
        SharedData sharedData = SharedData.getInstance();
        RequestParams params = new RequestParams();
        params.put(Constants.ACTION, "deletePost");
        params.put(Constants.POST_ID, post.getnId());

        sharedData.httpClient.post(getActivity(), Constants.WEBSERVICE_URL, params, new AsyncHttpResponseHandler() {
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

    private void getMyPostInfo() {
        final int nUserId = preferences.getInt(Constants.USER_ID, 0);
        SharedData sharedData = SharedData.getInstance();

        RequestParams params = new RequestParams();
        params.put(Constants.ACTION, "getMyPostInfo");
        params.put(Constants.USER_ID, nUserId);

        sharedData.httpClient.post(getActivity(), Constants.WEBSERVICE_URL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] response) {
                try {
                    String strResponse = new String(response, "UTF-8");
                    JSONArray jsonArray = new JSONArray(strResponse);
                    String status = jsonArray.getJSONObject(0).getString(Constants.STATUS);

                    if (status.equals(Constants.STATUS_SUCCESS)) {
                        JSONObject result = jsonArray.getJSONObject(1);
                        nPostCount = result.getInt("postCount");
                        nReactCount = result.getInt("reactCount");
                        nCommentCount = result.getInt("commentCount");

                        setPostInfo();
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
    class PullLoadMoreListener implements PullLoadMoreRecyclerView.PullLoadMoreListener {
        @Override
        public void onRefresh() {
            isPullToRefresh = true;
            getMyPosts();
        }

        @Override
        public void onLoadMore() {
            getMyPosts();
        }
    }
}
