package com.github.sundeepk.compactcalendarview;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.AttrRes;
import android.support.annotation.IntegerRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by yuhaoliu on 3/05/2017.
 */

public class ITimeInnerCalendar extends RelativeLayout {

    private RelativeLayout calTitleBar;
    private CompactCalendarView calendarView;
    private TextView titleTv;
    private Context context;

    private CompactCalendarView.CompactCalendarViewListener outListener;

    public ITimeInnerCalendar(@NonNull Context context) {
        super(context);
        this.calendarView = new CompactCalendarView(context);
        this.context = context;
        this.init();
    }

    public ITimeInnerCalendar(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.calendarView = new CompactCalendarView(context, attrs, 0);
        this.context = context;
        this.init();
    }

    public ITimeInnerCalendar(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        this.calendarView = new CompactCalendarView(context, attrs, defStyleAttr);
        this.init();
    }

    private void init(){
        initCalendarTitleBar();
        initCalendar();
        initDivider();
    }

    public void setSlotNumMap(HashMap<String, Integer> slotNumMap) {
        this.calendarView.setSlotNumMap(slotNumMap);
    }

    private void initCalendarTitleBar(){
        this.calTitleBar = new RelativeLayout(getContext());
        this.calTitleBar.setBackgroundColor(Color.WHITE);
        int barPad = DensityUtil.dip2px(context,10);
        this.calTitleBar.setPadding(0,barPad,0,barPad);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        this.calTitleBar.setLayoutParams(params);
        this.calTitleBar.setId(View.generateViewId());

        titleTv = new TextView(getContext());
        titleTv.setId(View.generateViewId());
        titleTv.setGravity(Gravity.CENTER);
        RelativeLayout.LayoutParams titleTvParams = new RelativeLayout.LayoutParams(DensityUtil.dip2px(context,150), ViewGroup.LayoutParams.WRAP_CONTENT);
        titleTvParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        calTitleBar.addView(titleTv,titleTvParams);
        updateTitle(new Date());

        int indicatorSize = DensityUtil.dip2px(context,20);

        ImageView leftIcon = new ImageView(getContext());
        leftIcon.setImageDrawable(getResources().getDrawable(R.drawable.indicator_more_left));
        RelativeLayout.LayoutParams leftIconParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, indicatorSize);
        leftIconParams.addRule(RelativeLayout.CENTER_VERTICAL);
        leftIconParams.addRule(RelativeLayout.LEFT_OF,titleTv.getId());
        calTitleBar.addView(leftIcon,leftIconParams);
        leftIcon.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                calendarView.showPreviousMonth();
            }
        });

        ImageView rightIcon = new ImageView(getContext());
        rightIcon.setImageDrawable(getResources().getDrawable(R.drawable.indicator_more_right));
        RelativeLayout.LayoutParams rightIconParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, indicatorSize);
        rightIconParams.addRule(RelativeLayout.CENTER_VERTICAL);
        rightIconParams.addRule(RelativeLayout.RIGHT_OF,titleTv.getId());
        rightIcon.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                calendarView.showNextMonth();
            }
        });

        calTitleBar.addView(rightIcon,rightIconParams);
        this.addView(calTitleBar);
    }

    private void initCalendar(){
//        this.calendarView = (CompactCalendarView) LayoutInflater.from(context).inflate(R.layout.itime_timeslot_inner_calendar, null);
        RelativeLayout.LayoutParams calParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        calendarView.setId(generateViewId());
        calParams.addRule(BELOW, calTitleBar.getId());
        calendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {
                if (outListener != null){
                    outListener.onDayClick(dateClicked);
                }
            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                updateTitle(firstDayOfNewMonth);
                if (outListener != null){
                    outListener.onMonthScroll(firstDayOfNewMonth);
                }
            }
        });
        this.addView(calendarView,calParams);
    }

    private void initDivider(){
        ImageView dividerImgV;
        //divider
        dividerImgV = new ImageView(context);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(ALIGN_PARENT_BOTTOM);
        params.bottomMargin = 50;
        dividerImgV.setLayoutParams(params);
        dividerImgV.setImageDrawable(getResources().getDrawable(R.drawable.divider_with_shadow));
        this.addView(dividerImgV);
    }

    private void updateTitle(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        String monthName = cal.getDisplayName(Calendar.MONTH,Calendar.LONG, Locale.getDefault()).toUpperCase();
        String yearName = String.valueOf(cal.get(Calendar.YEAR));
        String dateStr = monthName + " " + yearName;
        titleTv.setText(dateStr);
    }

    public void setBodyListener(CompactCalendarView.CompactCalendarViewListener listener){
        this.outListener = listener;
    }

    public void refreshSlotNum(){
        calendarView.invalidate();
    }
}
