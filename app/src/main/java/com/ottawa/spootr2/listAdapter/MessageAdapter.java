package com.ottawa.spootr2.listAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ottawa.spootr2.R;
import com.ottawa.spootr2.model.Message;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by King on 5/11/2016.
 */
public class MessageAdapter extends ArrayAdapter<Message> {

    private ArrayList<Message> itemList;
    private LayoutInflater inflater;
    private SimpleDateFormat dateFormat1;
    private SimpleDateFormat dateFormat2;

    private class ViewHolder {
        ImageView imageView;
        TextView textMessage;
        TextView textDate;
        ProgressBar progressBar;
        TextView textFailed;
        int type;
    }

    public MessageAdapter(Context context, int nResourceId, ArrayList<Message> list) {
        super(context, nResourceId, list);

        itemList = list;
        inflater = LayoutInflater.from(context);
        dateFormat1 = new SimpleDateFormat("H:mm:ss");
        dateFormat2 = new SimpleDateFormat("M/d H:mm:ss");
    }

    @Override
    public View getView(int position, View containerView, ViewGroup viewGroup) {
        ViewHolder holder = null;
        Message item = itemList.get(position);

//        if (containerView == null) {
            holder = new ViewHolder();
//        } else {
//            holder = (ViewHolder) containerView.getTag();
//        }
        if (item.isFromme()) {

            containerView = inflater.inflate(R.layout.listitem_message_send, null);
            holder.textMessage = (TextView)containerView.findViewById(R.id.text_message_send_content);
            holder.textDate = (TextView)containerView.findViewById(R.id.text_message_send_date);
            holder.textFailed = (TextView)containerView.findViewById(R.id.text_message_failed);
            holder.progressBar = (ProgressBar)containerView.findViewById(R.id.circle_progress);
            holder.type = 1;

        } else {

            containerView = inflater.inflate(R.layout.listitem_message_receive, null);
            holder.textMessage = (TextView)containerView.findViewById(R.id.text_message_receive_content);
            holder.textDate = (TextView)containerView.findViewById(R.id.text_message_receive_date);
            holder.imageView = (ImageView)containerView.findViewById(R.id.image_message_receive_user);
            holder.type = 0;

        }

        holder.textMessage.setText(item.getStrMessage());
        holder.textDate.setText(getDate(item.getMessageDate()));
        if (item.isFromme()) {
            if (item.getStatus() == 1) {
                holder.progressBar.setVisibility(View.VISIBLE);
                holder.textFailed.setVisibility(View.GONE);
            } else if (item.getStatus() == 2) {
                holder.progressBar.setVisibility(View.GONE);
                holder.textFailed.setVisibility(View.VISIBLE);
            } else {
                holder.progressBar.setVisibility(View.GONE);
                holder.textFailed.setVisibility(View.GONE);
            }
        } else {
            holder.imageView.setImageDrawable(item.getEmoji().getDrawable());
        }

        return containerView;
    }

    public void updateResult(ArrayList<Message> list) {
        itemList = list;
        notifyDataSetInvalidated();
    }

    private String getDate(Date date) {
        String strDate = "";

        Date now = new Date();
        if (date.getYear() == now.getYear() && date.getMonth() == now.getMonth() && date.getDay() == now.getDay()) {
            strDate = dateFormat1.format(date);
        } else {
            strDate = dateFormat2.format(date);
        }

        return strDate;
    }

}
