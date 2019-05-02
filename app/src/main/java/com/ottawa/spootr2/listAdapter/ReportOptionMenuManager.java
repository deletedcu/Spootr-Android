package com.ottawa.spootr2.listAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.ottawa.spootr2.R;

/**
 * Created by King on 5/11/2016.
 */
public class ReportOptionMenuManager implements View.OnClickListener {

    private ReportMenuListener mListener;
    private View contentView;

    public ReportOptionMenuManager (Context context, int resourceId) {
        contentView = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(resourceId, null);

        Button contentButton = (Button)contentView.findViewById(R.id.btn_popupreport_content);
        Button targetButton = (Button)contentView.findViewById(R.id.btn_popupreport_target);
        Button spamButton = (Button)contentView.findViewById(R.id.btn_popupreport_spam);

        contentButton.setOnClickListener(this);
        targetButton.setOnClickListener(this);
        spamButton.setOnClickListener(this);

    }

    public View getContentView() {
        return contentView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_popupreport_content:
                if (mListener != null) {
                    mListener.onContentMenu();
                }
                break;
            case R.id.btn_popupreport_target:
                if (mListener != null) {
                    mListener.onTargetMenu();
                }
                break;
            case R.id.btn_popupreport_spam:
                if (mListener != null) {
                    mListener.onSpamMenu();
                }
                break;
            default:
                break;
        }
    }

    public void setOnReportMenuListener(ReportMenuListener listener) {
        mListener = listener;
    }

    public interface ReportMenuListener {
        void onContentMenu();
        void onTargetMenu();
        void onSpamMenu();
    }
}
