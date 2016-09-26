package org.unimelb.itime.vendor.timeslot;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import org.unimelb.itime.vendor.R;

/**
 * Created by Paul on 26/08/2016.
 */
public class TimeSlotView extends RelativeLayout {
    private int duration;

    private long startTime = 0;
    private long endTime = 0;

    private ImageView icon;

    private boolean isSelect = false;

    public TimeSlotView(Context context) {
        super(context);
        init();
    }

    public void init(){
        initBackground();
        initIcon();
    }

    public void initBackground(){
        this.setBackgroundResource(R.drawable.time_block_background);
    }

    public void initIcon(){
        icon = new ImageView(getContext());
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(ALIGN_PARENT_TOP);
        params.addRule(ALIGN_PARENT_RIGHT);

        if (!isSelect){
            icon.setImageResource(R.drawable.icon_event_timeslot_unselected);
        }else{
            icon.setImageResource(R.drawable.icon_event_attendee_selected);
        }

        this.addView(icon,params);
    }

    public void setTimes(long startTime, long endTime){
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public void setStatus(boolean isSelect){
        this.isSelect = isSelect;
        updateIcon();
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public long getDuration() {
        return endTime - startTime;
    }

    public boolean isSelect() {
        return isSelect;
    }

    private void updateIcon(){
        if (isSelect){
            icon.setImageDrawable(getResources().getDrawable(R.drawable.icon_event_attendee_selected));
        } else {
            icon.setImageDrawable(getResources().getDrawable(R.drawable.icon_event_timeslot_unselected));
        }
    }

}
