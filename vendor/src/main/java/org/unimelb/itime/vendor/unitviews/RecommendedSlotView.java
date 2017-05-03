package org.unimelb.itime.vendor.unitviews;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.unimelb.itime.vendor.R;
import org.unimelb.itime.vendor.util.DensityUtil;
import org.unimelb.itime.vendor.wrapper.WrapperTimeSlot;

import java.util.Calendar;

/**
 * Created by yuhaoliu on 1/05/2017.
 */

public class RecommendedSlotView extends RelativeLayout {
    private TextView label;
    private TextView title;
    private ImageView icon;
    private WrapperTimeSlot wrapper;

    public RecommendedSlotView(@NonNull Context context, WrapperTimeSlot wrapper) {
        super(context);
        this.wrapper = wrapper;
        init();
    }

    public RecommendedSlotView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RecommendedSlotView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        this.setBackgroundResource(R.drawable.icon_timeslot_rcd);
        this.getBackground().setAlpha(217);
        label = new TextView(getContext());
        label.setText("Recommended");
        label.setTextColor(getResources().getColor(R.color.timeslot_rcd_label));
        label.setGravity(Gravity.CENTER);
        label.setTextSize(9);
        label.setId(generateViewId());
        RelativeLayout.LayoutParams labelPrams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        labelPrams.addRule(ALIGN_PARENT_TOP);
        labelPrams.addRule(CENTER_HORIZONTAL);
        this.addView(label,labelPrams);

        title = new TextView(getContext());
        title.setText(getTimeText());
        title.setGravity(Gravity.CENTER);
        title.setTextColor(getResources().getColor(R.color.timeslot_rcd_title));
        title.setTextSize(12);
        title.setId(View.generateViewId());
        RelativeLayout.LayoutParams titlePrams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        titlePrams.addRule(BELOW,label.getId());
        titlePrams.addRule(CENTER_HORIZONTAL);
        this.addView(title, titlePrams);


        FrameLayout frameLayout = new FrameLayout(getContext());
        RelativeLayout.LayoutParams frameLayoutPrams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        frameLayoutPrams.addRule(BELOW,title.getId());
        this.addView(frameLayout,frameLayoutPrams);

        icon = new ImageView(getContext());
        icon.setImageDrawable(getResources().getDrawable(R.drawable.icon_timeslot_plus));
        int size = getPlusIconSize(wrapper.getTimeSlot().getEndTime() - wrapper.getTimeSlot().getStartTime());
        FrameLayout.LayoutParams iconPrams = new FrameLayout.LayoutParams(size, size);
        iconPrams.gravity = Gravity.CENTER;
        frameLayout.addView(icon, iconPrams);
    }

    private int getPlusIconSize(long duration){
        long oneHour = 3600 * 1000;
        long halfHour = 1800 * 1000;
        if (duration > oneHour){
            return DensityUtil.dip2px(getContext(),50);
        }

        if (duration > halfHour){
            return DensityUtil.dip2px(getContext(),20);
        }

        return 0;
    }

    private String getTimeText(){
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(wrapper.getTimeSlot().getStartTime());
        String starTime = String.format("%02d:%02d", cal.get(Calendar.HOUR_OF_DAY),cal.get(Calendar.MINUTE));
        cal.setTimeInMillis(wrapper.getTimeSlot().getEndTime());
        String endTime = String.format("%02d:%02d", cal.get(Calendar.HOUR_OF_DAY),cal.get(Calendar.MINUTE));
        return starTime + "-" + endTime;
    }

    public WrapperTimeSlot getWrapper() {
        return wrapper;
    }

    public void setWrapper(WrapperTimeSlot wrapper) {
        this.wrapper = wrapper;
    }
}
