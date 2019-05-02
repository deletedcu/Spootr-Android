package com.ottawa.spootr2.listAdapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.ottawa.spootr2.R;
import com.ottawa.spootr2.model.Chat;
import com.ottawa.spootr2.model.Emoji;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by King on 5/10/2016.
 */
public class BlockListAdapter extends ArrayAdapter<Chat> {

    private Context context = null;
    private int nResourceId;
    private ArrayList<Chat> itemLists;
    private LayoutInflater inflater;
    private BlockListener mListener;

    public BlockListAdapter(Context context, int nResourceId, ArrayList<Chat> objects) {
        super(context, nResourceId, objects);
        this.context = context;
        this.nResourceId = nResourceId;
        this.itemLists = objects;

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(final int position, View containerView, ViewGroup viewGroup) {
        View view;
        final Chat item = itemLists.get(position);

        view = inflater.inflate(R.layout.listitem_block, null);
        ImageView userImage = (ImageView)view.findViewById(R.id.image_blocklist_user);
        TextView textName = (TextView)view.findViewById(R.id.text_blocklist_name);
        Button unBlockButton = (Button)view.findViewById(R.id.button_blocklist_unblock);

        try {
            InputStream is = context.getAssets().open(item.getStrPictureName());
            Drawable drawable = Drawable.createFromStream(is, null);
            userImage.setImageDrawable(drawable);
        } catch (IOException e) {
            e.printStackTrace();
        }

        textName.setText(item.getStrUserName());

        unBlockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.unBlock(position);
                }
            }
        });

        return view;
    }

    public void updateResults(ArrayList<Chat> results) {
        itemLists = results;
        // Triggers the list update
        notifyDataSetInvalidated();
    }

    public void setOnBlockListener(BlockListener listener) {
        mListener = listener;
    }

    public interface BlockListener {
        void unBlock(int index);
    }
}
