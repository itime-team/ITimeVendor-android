package org.unimelb.itime.vendor.dayview;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.unimelb.itime.vendor.helper.DensityUtil;

/**
 * Created by yuhaoliu on 18/02/2017.
 */

public class DayViewHeaderCell extends RelativeLayout {
    private TextView dateView;
    private TextView titleView;
    private ImageView dotView;

    public DayViewHeaderCell(Context context) {
        super(context);
    }

    public DayViewHeaderCell(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TextView getDateView() {
        return dateView;
    }

    public TextView getTitleView() {
        return titleView;
    }

    public ImageView getDotView() {
        return dotView;
    }

    public void setDateView(TextView dateView, TextView titleView, ImageView dotView) {
        this.removeAllViews();

        this.dateView = dateView;
        RelativeLayout.LayoutParams dateParams = new RelativeLayout.LayoutParams(DensityUtil.dip2px(getContext(),50), DensityUtil.dip2px(getContext(),50));
        dateParams.addRule(CENTER_IN_PARENT);
        this.dateView.setId(View.generateViewId());
        this.dateView.setLayoutParams(dateParams);

        this.titleView = titleView;
        RelativeLayout.LayoutParams titleParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        titleParams.addRule(CENTER_HORIZONTAL);
        this.titleView.setLayoutParams(titleParams);

        this.dotView = dotView;
        RelativeLayout.LayoutParams dotParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dotParams.addRule(CENTER_HORIZONTAL);
        dotParams.addRule(ALIGN_PARENT_BOTTOM);
        dotParams.bottomMargin = DensityUtil.dip2px(getContext(),2);
        this.dotView.setLayoutParams(dotParams);

        this.addView(dateView);
        this.addView(titleView);
        this.addView(dotView);
    }
}
