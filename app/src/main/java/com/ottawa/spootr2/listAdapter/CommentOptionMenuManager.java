package com.ottawa.spootr2.listAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.ottawa.spootr2.R;

/**
 * Created by king on 18/03/16.
 */
public class CommentOptionMenuManager implements View.OnClickListener{

    private CommentOptionMenuListener listener;
    private final View contentView;

    public CommentOptionMenuManager(Context context, int resourceId) {
        contentView = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(resourceId, null);

        Button reportButton = (Button)contentView.findViewById(R.id.btn_comment_report);
        Button notshowButton = (Button)contentView.findViewById(R.id.btn_comment_notshow);

        reportButton.setOnClickListener(this);
        notshowButton.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        if (this.listener != null) {
            switch (view.getId()) {
                case R.id.btn_comment_report:
                    listener.onReportMenu();
                    break;
                case R.id.btn_comment_notshow:
                    listener.onNotshowMenu();
                    break;
            }
        }

    }

    public View getView() {
        return contentView;
    }

    public void setListener(CommentOptionMenuListener listener) {
        this.listener = listener;
    }

    public CommentOptionMenuListener getListener() {
        return listener;
    }

    interface CommentOptionMenuListener {
        void onReportMenu();
        void onNotshowMenu();
    }

}
