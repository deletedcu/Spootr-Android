package com.ottawa.spootr2.listAdapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.ottawa.spootr2.R;
import com.ottawa.spootr2.common.Constants;
import com.ottawa.spootr2.common.SharedData;
import com.ottawa.spootr2.common.TimeAgo;
import com.ottawa.spootr2.model.Post;
import com.ottawa.spootr2.view.PointerPopupWindow;

import java.util.ArrayList;

/**
 * Created by king on 24/01/16.
 */
public class PostListAdapter extends RecyclerView.Adapter<PostListAdapter.ViewHolder> {

    private ArrayList<Post> dataList;
    private Context mContext;
    private TimeAgo timeAgo;
    private PostListAdapterListener mListener;
    private int width;

    public ArrayList<Post> getDataList() {
        return dataList;
    }

    public PostListAdapter(Context context, ArrayList<Post> dataList) {
        this.dataList = dataList;
        this.mContext = context;
        timeAgo = new TimeAgo();
        SharedData sharedData = SharedData.getInstance();
        width = sharedData.image_width - 16;

    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public RelativeLayout layout;
        public TextView textTime;
        public TextView textPost;
        public TextView textLikeCount;
        public TextView textCommentCount;
        public TextView textExtraInfo;
        public ImageView imageViewLike;
        public ImageView imageViewComment;
        public ImageButton buttonMenu;
        public Button buttonLike;
        public ImageView imageView;

        public ViewHolder(View view) {
            super(view);
            layout = (RelativeLayout)view.findViewById(R.id.layout_postitem);
            textTime = (TextView)view.findViewById(R.id.text_post_time);
            textPost = (TextView)view.findViewById(R.id.text_post);
            textLikeCount = (TextView)view.findViewById(R.id.text_likecount);
            textCommentCount = (TextView)view.findViewById(R.id.text_commentcount);
            textExtraInfo = (TextView)view.findViewById(R.id.text_post_circle);
            buttonMenu = (ImageButton)view.findViewById(R.id.button_post_menu);
            buttonLike = (Button)view.findViewById(R.id.button_like);
            imageViewLike = (ImageView)view.findViewById(R.id.imageview_like);
            imageViewComment = (ImageView)view.findViewById(R.id.imageview_comment);
            imageView = (ImageView)view.findViewById(R.id.imageView_post);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem_post, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position)     {
        final Post item = dataList.get(position);

        holder.textTime.setText(timeAgo.timeAgo(item.getPostDate()));
        holder.textPost.setText(item.getStrContent());
        holder.textLikeCount.setText(String.valueOf(item.getnLikeCount()));
        holder.textCommentCount.setText(String.valueOf(item.getnCommentCount()));
        holder.textExtraInfo.setText(item.getExtraInfo());

        if (item.getStrImageName().equals("")) {
            holder.imageView.getLayoutParams().width = width;
            holder.imageView.getLayoutParams().height = 0;
        } else {
            holder.imageView.getLayoutParams().width = width;
            holder.imageView.getLayoutParams().height = width;
            holder.imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            String strURL = String.format("%s%s", Constants.DOWNLOAD_URL, item.getStrImageName());
            UrlImageViewHelper.setUrlDrawable(holder.imageView, strURL, R.drawable.placeholder);

        }
        holder.imageView.requestLayout();

        if (item.getnLikeType() > 0) {
            holder.imageViewLike.setBackgroundResource(R.drawable.like_red);
        } else {
            holder.imageViewLike.setBackgroundResource(R.drawable.like_grey);
        }
        if (item.isCommented()) {
            holder.imageViewComment.setBackgroundResource(R.drawable.comment_blue);
        } else {
            holder.imageViewComment.setBackgroundResource(R.drawable.comment_grey);
        }

        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onCommentButtonClick(position);
                }
            }
        });

        holder.buttonMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupMenu(view, position, item.isMine());
            }
        });

        holder.buttonLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLikeMenu(view, position);

            }
        });

    }

    private void showPopupMenu(View view, final int position, boolean isMine) {

        int width = 0;
        if (isMine)
            width = mContext.getResources().getDimensionPixelSize(R.dimen.popup_menu_width2);
        else
            width = mContext.getResources().getDimensionPixelSize(R.dimen.popup_menu_width1);
        final PointerPopupWindow popupWindow = new PointerPopupWindow(mContext, width);
        PostItemOptionMenuManager manager = new PostItemOptionMenuManager(mContext, R.layout.popup_postitem_menu, isMine);

        manager.setListener(new PostItemOptionMenuManager.PostItemOptionMenuListener() {
            @Override
            public void onReportMenu() {
                if (mListener != null) {
                    mListener.onReportMenuClick(position);
                    popupWindow.dismiss();
                }
            }

            @Override
            public void onNotshowMenu() {
                if (mListener != null) {
                    mListener.onNotshowMenuClick(position);
                    popupWindow.dismiss();
                }
            }

            @Override
            public void onEditMenu() {
                if (mListener != null) {
                    mListener.onEditMenuClick(position);
                    popupWindow.dismiss();
                }
            }

            @Override
            public void onDeleteMenu() {
                if (mListener != null) {
                    mListener.onDeleteMenuClick(position);
                    popupWindow.dismiss();
                }
            }
        });
        // set some pupup window properties
        popupWindow.setFocusable(true);
        // set your group view as a popup window content
        popupWindow.setContentView(manager.getView());
        popupWindow.setPointerImageRes(R.drawable.ic_popup_pointer_down);
        // This will allow you to close window by clickin not in its area
        popupWindow.setOutsideTouchable(true);
        // Show the window at desired place. The first argument is a control, wich will be used to place window... defining dx and dy will shift the popup window
        popupWindow.setAlignMode(PointerPopupWindow.AlignMode.AUTO_OFFSET);
        popupWindow.showAsPointer(view);
    }

    private void showLikeMenu(View view, final int position) {
        Post post = dataList.get(position);

        final PointerPopupWindow popupWindow = new PointerPopupWindow(mContext, mContext.getResources().getDimensionPixelSize(R.dimen.popup_like_width), true);
        PostLikeManager manager = new PostLikeManager(mContext, R.layout.popup_postitem_like, post.getLikeArray(), post.getnLikeType());

        manager.setListener(new PostLikeManager.PostLikeListener() {
            @Override
            public void onLikeClick(int nLikeType) {
                if (mListener != null) {
                    mListener.onLikeButtonClick(position, nLikeType);
                    popupWindow.dismiss();
                }
            }
        });
        // set some pupup window properties
        popupWindow.setFocusable(true);

        // set your group view as a popup window content
        popupWindow.setContentView(manager.getView());
        popupWindow.setPointerImageRes(R.drawable.ic_popup_pointer_up);

        // This will allow you to close window by clickin not in its area
        popupWindow.setOutsideTouchable(true);
        // Show the window at desired place. The first argument is a control, wich will be used to place window... defining dx and dy will shift the popup window
        popupWindow.setAlignMode(PointerPopupWindow.AlignMode.AUTO_OFFSET);
        int yOff = mContext.getResources().getDimensionPixelOffset(R.dimen.popup_like_yoff);
        popupWindow.showAsPointer(view, yOff);
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public void updateResults(ArrayList<Post> results) {
        dataList = results;
        // Triggers the list update
        notifyDataSetChanged();
    }

    public void addItem(int position, Post post) {
        dataList.add(position, post);
        notifyItemInserted(position);
    }

    public void removeItem(int position) {
        dataList.remove(position);
        notifyItemRemoved(position);
    }

    public void updateItem(int position, Post post) {
        dataList.set(position, post);
        notifyItemChanged(position);
    }

    public void setOnPostListener(PostListAdapterListener listener) {
        mListener = listener;
    }

    public interface PostListAdapterListener {
        void onLikeButtonClick(int position, int nLikeType);
        void onCommentButtonClick(int position);
        void onReportMenuClick(int position);
        void onNotshowMenuClick(int position);
        void onEditMenuClick(int position);
        void onDeleteMenuClick(int position);
    }
}
