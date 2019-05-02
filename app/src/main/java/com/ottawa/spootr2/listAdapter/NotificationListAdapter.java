package com.ottawa.spootr2.listAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ottawa.spootr2.R;
import com.ottawa.spootr2.common.TimeAgo;
import com.ottawa.spootr2.model.Post;

import java.util.ArrayList;

/**
 * Created by king on 21/01/16.
 */
public class NotificationListAdapter extends ArrayAdapter<Post> {

    private Context context = null;
    private ArrayList<Post> itemLists;
    private LayoutInflater inflater;
    private TimeAgo timeAgo;

    public NotificationListAdapter(Context context, int nResourceId, ArrayList<Post> objects) {
        super(context, nResourceId, objects);
        itemLists = objects;
        this.context = context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        timeAgo = new TimeAgo();

    }

    @Override
    public View getView(int position, View containerView, ViewGroup viewGroup) {
        View view = containerView;

        Post item = itemLists.get(position);
        if (view == null) {
            view = inflater.inflate(R.layout.listitem_notification, null);
        }
        TextView textTime = (TextView)view.findViewById(R.id.text_notification_time);
        TextView textType = (TextView)view.findViewById(R.id.text_notification_type);
        TextView textContent = (TextView)view.findViewById(R.id.text_notification_content);

        textTime.setText(timeAgo.timeAgo(item.getNotificationDate()));
        String strType = "";
        if (item.getNotificationType() == 1) {
            strType = String.format("%d Reacted to: ", item.getNotificationCount());
        } else {
            strType = String.format("Someone commented on: ", item.getNotificationCount());
        }
        textType.setText(strType);
        textContent.setText(item.getStrContent());

        return view;
    }

    public void updateResults(ArrayList<Post> results) {
        itemLists = results;
        // Triggers the list update
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        itemLists.remove(position);

        notifyDataSetChanged();
    }
}
