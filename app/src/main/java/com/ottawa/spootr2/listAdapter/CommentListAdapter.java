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
import com.ottawa.spootr2.model.Comment;
import com.ottawa.spootr2.model.Post;
import com.ottawa.spootr2.view.PointerPopupWindow;

import java.util.ArrayList;

/**
 * Created by king on 24/01/16.
 */
public class CommentListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<Comment> dataList;
    private Context mContext;
    private TimeAgo timeAgo;
    private CommentListAdapterListener mListener;
    private Post post;
    private int width;

    public ArrayList<Comment> getDataList() {
        return dataList;
    }

    public CommentListAdapter(Context context, ArrayList<Comment> dataList, Post item) {
        this.dataList = dataList;
        this.mContext = context;
        this.post = item;
        timeAgo = new TimeAgo();

        SharedData sharedData = SharedData.getInstance();
        width = sharedData.image_width;
    }

    class ViewHolder0 extends RecyclerView.ViewHolder {
        public RelativeLayout layout;
        public TextView textTime;
        public TextView textPost;
        public TextView textLikeCount;
        public TextView textCommentCount;
        public TextView textCircle;
        public ImageButton buttonMenu;
        public Button buttonLike;
        public ImageView imageViewLike;
        public ImageView imageViewComment;
        public ImageView imageView;

        public ViewHolder0(View view) {
            super(view);
            layout = (RelativeLayout)view.findViewById(R.id.layout_postitem);
            textTime = (TextView)view.findViewById(R.id.text_post_time);
            textPost = (TextView)view.findViewById(R.id.text_post);
            textLikeCount = (TextView)view.findViewById(R.id.text_likecount);
            textCommentCount = (TextView)view.findViewById(R.id.text_commentcount);
            textCircle = (TextView)view.findViewById(R.id.text_post_circle);
            buttonMenu = (ImageButton)view.findViewById(R.id.button_post_menu);
            buttonLike = (Button)view.findViewById(R.id.button_like);
            imageViewLike = (ImageView)view.findViewById(R.id.imageview_like);
            imageViewComment = (ImageView)view.findViewById(R.id.imageview_comment);
            imageView = (ImageView)view.findViewById(R.id.imageView_post);
        }
    }

    class ViewHolder2 extends RecyclerView.ViewHolder {
        public RelativeLayout layout;
        public TextView textCommentTime;
        public TextView textCommentContent;
        public TextView textUserName;
        public ImageView imageView;
        public ImageButton menuButton;

        public ViewHolder2(View view) {
            super(view);

            layout = (RelativeLayout)view.findViewById(R.id.layout_commentItem);
            textCommentContent = (TextView)view.findViewById(R.id.text_comment);
            textCommentTime = (TextView)view.findViewById(R.id.text_comment_time);
            textUserName = (TextView)view.findViewById(R.id.text_comment_name);
            imageView = (ImageView)view.findViewById(R.id.imageView_comment);
            menuButton = (ImageButton)view.findViewById(R.id.btn_comment_menu);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0)
            return 0;
        else
            return 1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 0) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem_post, parent, false);
            return new ViewHolder0(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem_comment, parent, false);
            return new ViewHolder2(v);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position)     {
        if (position == 0) {
            ((ViewHolder0)holder).textTime.setText(timeAgo.timeAgo(post.getPostDate()));
            ((ViewHolder0)holder).textPost.setText(post.getStrContent());
            ((ViewHolder0)holder).textLikeCount.setText(String.valueOf(post.getnLikeCount()));
            ((ViewHolder0)holder).textCommentCount.setText(String.valueOf(post.getnCommentCount()));
            ((ViewHolder0)holder).textCircle.setText(post.getExtraInfo());
            if (post.getStrImageName().equals("")) {
                ((ViewHolder0)holder).imageView.getLayoutParams().width = width;
                ((ViewHolder0)holder).imageView.getLayoutParams().height = 0;
            } else {
                ((ViewHolder0)holder).imageView.getLayoutParams().width = width;
                ((ViewHolder0)holder).imageView.getLayoutParams().height = width;
                String strURL = String.format("%s%s", Constants.DOWNLOAD_URL, post.getStrImageName());
                UrlImageViewHelper.setUrlDrawable(((ViewHolder0)holder).imageView, strURL, R.drawable.spootr);
            }
            ((ViewHolder0)holder).imageView.requestLayout();

            if (post.getnLikeType() > 0) {
                ((ViewHolder0)holder).imageViewLike.setBackgroundResource(R.drawable.like_red);
            } else {
                ((ViewHolder0)holder).imageViewLike.setBackgroundResource(R.drawable.like_grey);
            }
            if (post.isCommented()) {
                ((ViewHolder0)holder).imageViewComment.setBackgroundResource(R.drawable.comment_blue);
            } else {
                ((ViewHolder0)holder).imageViewComment.setBackgroundResource(R.drawable.comment_grey);
            }

            ((ViewHolder0)holder).layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onDismissKeyboard();
                    }
                }
            });

            ((ViewHolder0)holder).buttonMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showPopupMenu(view);
                }
            });

            ((ViewHolder0)holder).buttonLike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showLikeMenu(view);
                }
            });

        } else {
            Comment item = dataList.get(position - 1);
            ((ViewHolder2)holder).textCommentContent.setText(item.getStrContent());
            ((ViewHolder2)holder).textCommentTime.setText(timeAgo.timeAgo(item.getCommentDate()));
            ((ViewHolder2)holder).textUserName.setText(item.getStrUserName());
            ((ViewHolder2)holder).imageView.setImageDrawable(item.getDrawable());
            ((ViewHolder2)holder).menuButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showCommentMenu(v, position);
                }
            });
            ((ViewHolder2)holder).layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onDismissKeyboard();
                    }
                }
            });
        }

    }

    private void showPopupMenu(View view) {

        int width = 0;
        if (post.isMine())
            width = mContext.getResources().getDimensionPixelSize(R.dimen.popup_menu_width2);
        else
            width = mContext.getResources().getDimensionPixelSize(R.dimen.popup_menu_width1);
        final PointerPopupWindow popupWindow = new PointerPopupWindow(mContext, width);
        PostItemOptionMenuManager manager = new PostItemOptionMenuManager(mContext, R.layout.popup_postitem_menu, post.isMine());

        manager.setListener(new PostItemOptionMenuManager.PostItemOptionMenuListener() {
            @Override
            public void onReportMenu() {
                if (mListener != null) {
                    mListener.onReportMenuClick();
                    popupWindow.dismiss();
                }
            }

            @Override
            public void onNotshowMenu() {
                if (mListener != null) {
                    mListener.onNotshowMenuClick();
                    popupWindow.dismiss();
                }
            }

            @Override
            public void onEditMenu() {
                if (mListener != null) {
                    mListener.onEditMenuClick();
                    popupWindow.dismiss();
                }
            }

            @Override
            public void onDeleteMenu() {
                if (mListener != null) {
                    mListener.onDeleteMenuClick();
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

    private void showLikeMenu(View view) {

        final PointerPopupWindow popupWindow = new PointerPopupWindow(mContext, mContext.getResources().getDimensionPixelSize(R.dimen.popup_like_width), true);
        PostLikeManager manager = new PostLikeManager(mContext, R.layout.popup_postitem_like, post.getLikeArray(), post.getnLikeType());
        manager.setListener(new PostLikeManager.PostLikeListener() {
            @Override
            public void onLikeClick(int nLikeType) {
                if (mListener != null) {
                    mListener.onLikeButtonClick(nLikeType);
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

    private void showCommentMenu(View view, final int position) {
        final PointerPopupWindow popupWindow = new PointerPopupWindow(mContext, mContext.getResources().getDimensionPixelSize(R.dimen.popup_menu_width3));

        CommentOptionMenuManager manager = new CommentOptionMenuManager(mContext, R.layout.popup_comment_menu);
        manager.setListener(new CommentOptionMenuManager.CommentOptionMenuListener() {
            @Override
            public void onReportMenu() {
                if (mListener != null) {
                    mListener.onCommentReport(position);
                    popupWindow.dismiss();
                }
            }

            @Override
            public void onNotshowMenu() {
                if (mListener != null) {
                    mListener.onCommentNotshow(position);
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

    @Override
    public int getItemCount() {
        return dataList.size() + 1;
    }

    public void updateResults(Post post, ArrayList<Comment> results) {
        this.post = post;
        dataList = results;
        // Triggers the list update
        notifyDataSetChanged();
    }

    public void updatePost(Post post) {
        this.post = post;
        notifyDataSetChanged();
    }

    public void removeComment(Comment comment) {
        dataList.remove(comment);

        notifyDataSetChanged();
    }

    public void setOnPostListener(CommentListAdapterListener listener) {
        mListener = listener;
    }

    public interface CommentListAdapterListener {
        void onLikeButtonClick(int nLikeType);
        void onReportMenuClick();
        void onNotshowMenuClick();
        void onEditMenuClick();
        void onDeleteMenuClick();
        void onCommentReport(int position);
        void onCommentNotshow(int position);
        void onDismissKeyboard();
    }

}
