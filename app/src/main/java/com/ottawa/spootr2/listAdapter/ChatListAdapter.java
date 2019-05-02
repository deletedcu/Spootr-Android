package com.ottawa.spootr2.listAdapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ottawa.spootr2.R;
import com.ottawa.spootr2.common.TimeAgo;
import com.ottawa.spootr2.model.Chat;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by King on 5/10/2016.
 */
public class ChatListAdapter extends ArrayAdapter<Chat> {

    private Context context = null;
    private int nResourceId;
    private ArrayList<Chat> itemLists;
    private LayoutInflater inflater;
    private TimeAgo timeAgo;

    private class ViewHolder {
        ImageView userImage;
        ImageView dotImage;
        TextView textName;
        TextView textMessage;
        TextView textTime;
    }

    public ChatListAdapter(Context context, int nResourceId, ArrayList<Chat> objects) {
        super(context, nResourceId, objects);
        this.context = context;
        this.nResourceId = nResourceId;
        this.itemLists = objects;
        timeAgo = new TimeAgo();

        inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View containerView, ViewGroup viewGroup) {
        ViewHolder holder = null;
        holder = new ViewHolder();
        Chat item = itemLists.get(position);

        containerView = inflater.inflate(R.layout.listitem_chat, null);
        holder.userImage = (ImageView)containerView.findViewById(R.id.image_chatlist_user);
        holder.dotImage = (ImageView)containerView.findViewById(R.id.image_chatlist_dot);
        holder.textTime = (TextView)containerView.findViewById(R.id.text_chatlist_time);
        holder.textName = (TextView)containerView.findViewById(R.id.text_chatlist_name);
        holder.textMessage = (TextView)containerView.findViewById(R.id.text_chatlist_message);

        if (item.isNew()) {
            holder.dotImage.setVisibility(View.VISIBLE);
        } else {
            holder.dotImage.setVisibility(View.GONE);
        }

        try {
            InputStream is = context.getAssets().open(item.getStrPictureName());
            Drawable drawable = Drawable.createFromStream(is, null);
            holder.userImage.setImageDrawable(drawable);
        } catch (IOException e) {
            e.printStackTrace();
        }

        holder.textTime.setText(timeAgo.timeAgo(item.getChatTime()));
        holder.textName.setText(item.getStrUserName());

        if (item.getStrMessage().equals("")) {
            holder.textMessage.setText("New contact");
        } else if (item.isFromme()) {
            holder.textMessage.setText(item.getStrMessage());
        } else {
            holder.textMessage.setText(String.format("↩ %s", item.getStrMessage()));
        }

        return containerView;
    }

    public void updateResults(ArrayList<Chat> results) {
        itemLists = results;
        // Triggers the list update
        notifyDataSetInvalidated();
    }
}
