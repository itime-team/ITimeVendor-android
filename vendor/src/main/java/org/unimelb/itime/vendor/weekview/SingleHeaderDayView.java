package org.unimelb.itime.vendor.weekview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.unimelb.itime.vendor.util.DensityUtil;


/**
 * Created by yuhaoliu on 23/09/16.
 */
public class SingleHeaderDayView extends LinearLayout {
    TextView dayOfWeekTv;
    TextView nthDayTv;
    LinearLayout container;

    //sp
    private int dayOfWeekTvSize = 11;
    private int nthDayTvSize = 18;

    public SingleHeaderDayView(Context context) {
        super(context);
        init();
    }

    public SingleHeaderDayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init(){
        this.setOrientation(VERTICAL);

        container = new LinearLayout(getContext());
        container.setOrientation(VERTICAL);
        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        containerParams.gravity = Gravity.CENTER;

        this.addView(container,containerParams);

        LinearLayout.LayoutParams dayParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dayParams.gravity = Gravity.CENTER;
        dayOfWeekTv = new TextView(getContext());
        dayOfWeekTv.setTextSize(dayOfWeekTvSize);
        dayOfWeekTv.setGravity(Gravity.CENTER);
        container.addView(dayOfWeekTv,dayParams);

        LinearLayout.LayoutParams nthParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        nthParams.gravity = Gravity.CENTER;
        nthDayTv = new TextView(getContext());
        nthDayTv.setGravity(Gravity.CENTER);
        nthDayTv.setTextSize(nthDayTvSize);
        container.addView(nthDayTv,nthParams);
    }

    public void updateText(String dayOfWeek, String nthDayTv,int color){
        this.dayOfWeekTv.setTextColor(color);
        this.dayOfWeekTv.setText(dayOfWeek);

        this.nthDayTv.setTextColor(color);
        this.nthDayTv.setText(nthDayTv);
    }

    public LinearLayout getContainer(){
        return this.container;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int containerMax = Math.max(container.getMeasuredWidth(),container.getMeasuredHeight());
        container.getLayoutParams().width = containerMax;
        container.getLayoutParams().height = containerMax;
    }
}
