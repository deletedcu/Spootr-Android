package com.ottawa.spootr2.listAdapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ottawa.spootr2.R;
import com.ottawa.spootr2.model.Circle;

import java.util.ArrayList;

/**
 * Created by king on 21/01/16.
 */
public class CircleListAdapter extends ArrayAdapter<Circle> {

    private Context context = null;
    private int nResourceId;
    private ArrayList<Circle> itemLists;
    private LayoutInflater inflater;

    public CircleListAdapter(Context context, int nResourceId, ArrayList<Circle> objects) {
        super(context, nResourceId, objects);
        this.context = context;
        this.nResourceId = nResourceId;
        this.itemLists = objects;

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View containerView, ViewGroup viewGroup) {
        View view;

        final Circle circle = itemLists.get(position);

        if (circle.isHeader()) {
            view = inflater.inflate(R.layout.listitem_title, null);
            TextView textView = (TextView)view.findViewById(R.id.text_itemtitle);
            textView.setText(circle.getStrName());
        } else {

            view = inflater.inflate(R.layout.listitem_circle, null);
            TextView textView = (TextView)view.findViewById(R.id.text_circle);
            textView.setTextColor(Color.BLACK);

            textView.setText(circle.getStrName());

        }

        return view;
    }

    public void updateResults(ArrayList<Circle> results) {
        itemLists = results;
        // Triggers the list update
        notifyDataSetInvalidated();
    }
}
