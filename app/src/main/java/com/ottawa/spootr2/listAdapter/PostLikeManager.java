package com.ottawa.spootr2.listAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ottawa.spootr2.R;

import java.util.ArrayList;

/**
 * Created by king on 18/03/16.
 */
public class PostLikeManager implements View.OnClickListener{

    private PostLikeListener listener;
    private final View contentView;

    public PostLikeManager(Context context, int resourceId, ArrayList likeArray, int nLikeType) {
        contentView = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(resourceId, null);

        ImageView imageview1 = (ImageView)contentView.findViewById(R.id.iv_likecheck_1);
        ImageView imageview2 = (ImageView)contentView.findViewById(R.id.iv_likecheck_2);
        ImageView imageview3 = (ImageView)contentView.findViewById(R.id.iv_likecheck_3);
        ImageView imageview4 = (ImageView)contentView.findViewById(R.id.iv_likecheck_4);
        ImageView imageview5 = (ImageView)contentView.findViewById(R.id.iv_likecheck_5);

        if (nLikeType == 1) {
            imageview1.setVisibility(View.VISIBLE);
            imageview2.setVisibility(View.GONE);
            imageview3.setVisibility(View.GONE);
            imageview4.setVisibility(View.GONE);
            imageview5.setVisibility(View.GONE);
        } else if (nLikeType == 2) {
            imageview2.setVisibility(View.VISIBLE);
            imageview1.setVisibility(View.GONE);
            imageview3.setVisibility(View.GONE);
            imageview4.setVisibility(View.GONE);
            imageview5.setVisibility(View.GONE);
        } else if (nLikeType == 3) {
            imageview3.setVisibility(View.VISIBLE);
            imageview2.setVisibility(View.GONE);
            imageview1.setVisibility(View.GONE);
            imageview4.setVisibility(View.GONE);
            imageview5.setVisibility(View.GONE);
        } else if (nLikeType == 4) {
            imageview4.setVisibility(View.VISIBLE);
            imageview2.setVisibility(View.GONE);
            imageview3.setVisibility(View.GONE);
            imageview1.setVisibility(View.GONE);
            imageview5.setVisibility(View.GONE);
        } else if (nLikeType == 5) {
            imageview5.setVisibility(View.VISIBLE);
            imageview2.setVisibility(View.GONE);
            imageview3.setVisibility(View.GONE);
            imageview4.setVisibility(View.GONE);
            imageview1.setVisibility(View.GONE);
        }

        TextView textView1 = (TextView)contentView.findViewById(R.id.tv_like_1);
        TextView textView2 = (TextView)contentView.findViewById(R.id.tv_like_2);
        TextView textView3 = (TextView)contentView.findViewById(R.id.tv_like_3);
        TextView textView4 = (TextView)contentView.findViewById(R.id.tv_like_4);
        TextView textView5 = (TextView)contentView.findViewById(R.id.tv_like_5);

        textView1.setText(likeArray.get(0).toString());
        textView2.setText(likeArray.get(1).toString());
        textView3.setText(likeArray.get(2).toString());
        textView4.setText(likeArray.get(3).toString());
        textView5.setText(likeArray.get(4).toString());

        Button button1 = (Button)contentView.findViewById(R.id.btn_postlike_1);
        Button button2 = (Button)contentView.findViewById(R.id.btn_postlike_2);
        Button button3 = (Button)contentView.findViewById(R.id.btn_postlike_3);
        Button button4 = (Button)contentView.findViewById(R.id.btn_postlike_4);
        Button button5 = (Button)contentView.findViewById(R.id.btn_postlike_5);

        button1.setOnClickListener(this);
        button2.setOnClickListener(this);
        button3.setOnClickListener(this);
        button4.setOnClickListener(this);
        button5.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        if (this.listener != null) {
            switch (view.getId()) {
                case R.id.btn_postlike_1:
                    listener.onLikeClick(1);
                    break;
                case R.id.btn_postlike_2:
                    listener.onLikeClick(2);
                    break;
                case R.id.btn_postlike_3:
                    listener.onLikeClick(3);
                    break;
                case R.id.btn_postlike_4:
                    listener.onLikeClick(4);
                    break;
                case R.id.btn_postlike_5:
                    listener.onLikeClick(5);
                    break;
            }
        }

    }

    public View getView() {
        return contentView;
    }

    public void setListener(PostLikeListener listener) {
        this.listener = listener;
    }

    public PostLikeListener getListener() {
        return listener;
    }

    interface PostLikeListener {
        void onLikeClick(int nLikeType);
    }

}
