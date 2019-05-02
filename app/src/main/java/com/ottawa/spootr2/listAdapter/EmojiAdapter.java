package com.ottawa.spootr2.listAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.ottawa.spootr2.R;
import com.ottawa.spootr2.model.Emoji;

import java.util.ArrayList;

/**
 * Created by king on 17/02/16.
 */
public class EmojiAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<Emoji> itemList;
    private LayoutInflater inflater;
    private int selectedIndex;

    public EmojiAdapter(Context context, ArrayList<Emoji> objs) {
        mContext = context;
        itemList = objs;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return itemList.size();
    }

    public Object getItem(int position) {
        return itemList.get(position);
    }

    public long getItemId(int position) {
        return 0;
    }

    public View getView(int position, View containerView, ViewGroup parent) {
        View view;

        if (containerView == null) {
            view = inflater.inflate(R.layout.listitem_emoji, null);
        } else {
            view = containerView;
        }

        if (selectedIndex == position) {
            view.setBackgroundResource(R.drawable.emoji_item_selected);
        } else {
            view.setBackgroundResource(R.drawable.emoji_item_normal);
        }

        ImageView imageView = (ImageView)view.findViewById(R.id.imageview_emoji);
        Emoji item = itemList.get(position);
        imageView.setImageDrawable(item.getDrawable());

        return view;
    }

    public void setSelectedIndex(int index) {
        selectedIndex = index;
        notifyDataSetChanged();
    }

}
