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
public class PostItemOptionMenuManager implements View.OnClickListener{

    private PostItemOptionMenuListener listener;
    private final View contentView;

    public PostItemOptionMenuManager(Context context, int resourceId, boolean isMine) {
        contentView = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(resourceId, null);

        LinearLayout layout1 = (LinearLayout)contentView.findViewById(R.id.layout_postmenu_1);
        LinearLayout layout2 = (LinearLayout)contentView.findViewById(R.id.layout_postmenu_2);

        Button reportButton = (Button)contentView.findViewById(R.id.btn_postmenu_report);
        Button notshowButton = (Button)contentView.findViewById(R.id.btn_postmenu_notshow);
        Button editButton = (Button)contentView.findViewById(R.id.btn_postmenu_edit);
        Button deleteButton = (Button)contentView.findViewById(R.id.btn_postmenu_delete);
        reportButton.setOnClickListener(this);
        notshowButton.setOnClickListener(this);
        editButton.setOnClickListener(this);
        deleteButton.setOnClickListener(this);

        if (isMine) {
            layout1.setVisibility(View.GONE);
            layout2.setVisibility(View.VISIBLE);
        } else {
            layout1.setVisibility(View.VISIBLE);
            layout2.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View view) {
        if (this.listener != null) {
            switch (view.getId()) {
                case R.id.btn_postmenu_report:
                    listener.onReportMenu();
                    break;
                case R.id.btn_postmenu_notshow:
                    listener.onNotshowMenu();
                    break;
                case R.id.btn_postmenu_edit:
                    listener.onEditMenu();
                    break;
                case R.id.btn_postmenu_delete:
                    listener.onDeleteMenu();
                    break;
            }
        }

    }

    public View getView() {
        return contentView;
    }

    public void setListener(PostItemOptionMenuListener listener) {
        this.listener = listener;
    }

    public PostItemOptionMenuListener getListener() {
        return listener;
    }

    interface PostItemOptionMenuListener {
        void onReportMenu();
        void onNotshowMenu();
        void onEditMenu();
        void onDeleteMenu();
    }

}
