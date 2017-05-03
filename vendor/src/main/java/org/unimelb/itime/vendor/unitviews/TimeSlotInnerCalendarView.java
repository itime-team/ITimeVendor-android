package org.unimelb.itime.vendor.unitviews;

import android.animation.Animator;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.ITimeInnerCalendar;

import org.unimelb.itime.vendor.R;
import org.unimelb.itime.vendor.util.DensityUtil;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by yuhaoliu on 2/05/2017.
 */

public class TimeSlotInnerCalendarView extends LinearLayout {
    private Context context;

    private LinearLayout btnBlock;
    private TextView monthTitle;
    private ImageView indicator;
    private RotateAnimation showIndicatorAnim;
    private RotateAnimation hideIndicatorAnim;

//    private RelativeLayout calTitleBar;
//    private CompactCalendarView calendarView;
    private ITimeInnerCalendar calendarView;

    private OnTimeSlotInnerCalendar onTimeSlotInnerCalendar;
    private int headerHeight;

    public TimeSlotInnerCalendarView(Context context) {
        super(context);
        this.context = context;
        intViews();
    }

    public TimeSlotInnerCalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        intViews();
    }

    public TimeSlotInnerCalendarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        intViews();
    }

    public void setHeaderHeight(int headerHeight) {
        this.headerHeight = headerHeight;
        this.btnBlock.getLayoutParams().height = this.headerHeight;
    }

    public void setSlotNumMap(HashMap<String, Integer> slotNumMap) {
        this.calendarView.setSlotNumMap(slotNumMap);
    }

    private void intViews(){
        this.setOrientation(VERTICAL);
        initBtnBlock();
//        initCalendarTitleBar();
//        initCalendar();
        initITimeInnerCalendar();
        initListeners();
        initAnimations();
    }

    private void initBtnBlock(){
        int leftBarWidth = DensityUtil.dip2px(context,70);
        int leftBarHeight = headerHeight;

        btnBlock = new LinearLayout(getContext());
        btnBlock.setBackgroundColor(getResources().getColor(R.color.white));
        btnBlock.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams leftBtnParams = new LinearLayout.LayoutParams(leftBarWidth, leftBarHeight);

        int leftBarWidgetPadding = DensityUtil.dip2px(getContext(),10);
        int monthTitleWidth = DensityUtil.dip2px(getContext(),35);
        monthTitle = new TextView(getContext());
        LinearLayout.LayoutParams monthTitleParams = new LinearLayout.LayoutParams(monthTitleWidth, ViewGroup.LayoutParams.MATCH_PARENT);
        monthTitle.setText("April" + ".");
        monthTitle.setGravity(Gravity.CENTER);
        monthTitleParams.leftMargin = leftBarWidgetPadding;
        monthTitleParams.rightMargin = leftBarWidgetPadding;
        btnBlock.addView(monthTitle,monthTitleParams);

        indicator = new ImageView(getContext());
        int triangleSize = DensityUtil.dip2px(getContext(),10);

        indicator.setImageDrawable(getResources().getDrawable(R.drawable.triangle));
        LinearLayout.LayoutParams indicatorParams = new LinearLayout.LayoutParams(triangleSize, triangleSize);
        indicatorParams.gravity = Gravity.CENTER;
        btnBlock.addView(indicator,indicatorParams);
        this.addView(btnBlock, leftBtnParams);
        addDivider();
    }

    private void initITimeInnerCalendar(){
        this.calendarView = (ITimeInnerCalendar) LayoutInflater.from(context).inflate(R.layout.itime_timeslot_inner_calendar, null);
        LinearLayout.LayoutParams calParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DensityUtil.dip2px(getContext(),260));
        this.addView(calendarView,calParams);
        this.calendarView.setVisibility(GONE);
    }

//    private void initCalendarTitleBar(){
//        this.calTitleBar = new RelativeLayout(getContext());
//        this.calTitleBar.setBackgroundColor(Color.WHITE);
//        int barPad = DensityUtil.dip2px(context,10);
//        this.calTitleBar.setPadding(0,barPad,0,barPad);
//        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        this.calTitleBar.setLayoutParams(params);
//
//        TextView titleTv = new TextView(getContext());
//        titleTv.setText("MARCH 2017");
//        titleTv.setId(generateViewId());
//        RelativeLayout.LayoutParams titleTvParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        titleTvParams.addRule(RelativeLayout.CENTER_IN_PARENT);
//        calTitleBar.addView(titleTv,titleTvParams);
//
//        int indicatorSize = DensityUtil.dip2px(context,20);
//
//        ImageView leftIcon = new ImageView(getContext());
//        leftIcon.setImageDrawable(getResources().getDrawable(R.drawable.indicator_more_left));
//        RelativeLayout.LayoutParams leftIconParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, indicatorSize);
//        leftIconParams.addRule(RelativeLayout.LEFT_OF,titleTv.getId());
//        calTitleBar.addView(leftIcon,leftIconParams);
//
//        ImageView rightIcon = new ImageView(getContext());
//        rightIcon.setImageDrawable(getResources().getDrawable(R.drawable.indicator_more_right));
//        RelativeLayout.LayoutParams rightIconParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, indicatorSize);
//        rightIconParams.addRule(RelativeLayout.RIGHT_OF,titleTv.getId());
//        calTitleBar.addView(rightIcon,rightIconParams);
//        this.addView(calTitleBar);
//        this.calTitleBar.setVisibility(GONE);
//    }

//    private void initCalendar(){
//        this.calendarView = (CompactCalendarView) LayoutInflater.from(context).inflate(R.layout.itime_timeslot_inner_calendar, null);
//        LinearLayout.LayoutParams calParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DensityUtil.dip2px(context,220));
//        this.addView(calendarView,calParams);
//        this.calendarView.setVisibility(GONE);
//        addDivider();
//    }

    private void initAnimations(){
        showIndicatorAnim = new RotateAnimation(0, 180, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        showIndicatorAnim.setDuration(200);
        showIndicatorAnim.setFillAfter(true);
        showIndicatorAnim.setInterpolator(new LinearInterpolator());

        hideIndicatorAnim = new RotateAnimation(180, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        hideIndicatorAnim.setFillAfter(true);
        hideIndicatorAnim.setDuration(200);
        hideIndicatorAnim.setInterpolator(new LinearInterpolator());
    }

    private void initListeners(){
        btnBlock.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (calendarView.getVisibility() != VISIBLE){
                    calendarView.setVisibility(VISIBLE);
//                    calendarView.setVisibility(VISIBLE);
//                    YoYo.with(Techniques.FadeIn)
//                            .duration(200)
//                            .playOn(calTitleBar);
                    YoYo.with(Techniques.FadeInDown)
                            .duration(200)
                            .playOn(calendarView);
                    indicator.startAnimation(showIndicatorAnim);
                }else {
//                    YoYo.with(Techniques.FadeOut)
//                            .duration(200)
//                            .onEnd(new YoYo.AnimatorCallback() {
//                                @Override
//                                public void call(Animator animator) {
//                                    calTitleBar.setVisibility(GONE);
//                                }
//                            })
//                            .playOn(calTitleBar);
                    YoYo.with(Techniques.FadeOutUp)
                            .duration(200)
                            .onEnd(new YoYo.AnimatorCallback() {
                                @Override
                                public void call(Animator animator) {
                                    calendarView.setVisibility(GONE);
                                }
                            })
                            .playOn(calendarView);
                    indicator.startAnimation(hideIndicatorAnim);
                }

                if (onTimeSlotInnerCalendar != null){
                    onTimeSlotInnerCalendar.onCalendarBtnClick(v,!(calendarView.getVisibility()==INVISIBLE));
                }
            }
        });

        this.calendarView.setBodyListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {
                if (onTimeSlotInnerCalendar != null){
                    onTimeSlotInnerCalendar.onDayClick(dateClicked);
                }
            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                if (onTimeSlotInnerCalendar != null){
                    onTimeSlotInnerCalendar.onMonthScroll(firstDayOfNewMonth);
                }
            }
        });
    }

    private void addDivider(){
        ImageView dividerImgV;
        //divider
        dividerImgV = new ImageView(context);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dividerImgV.setLayoutParams(params);
        dividerImgV.setImageDrawable(getResources().getDrawable(org.unimelb.itime.vendor.R.drawable.itime_header_divider_line));
        this.addView(dividerImgV);
    }

    public void setMonth(Calendar calendar){
        String monthName = calendar.getDisplayName(Calendar.MONTH,Calendar.SHORT, Locale.getDefault());
        monthTitle.setText(monthName + ".");
    }

    public OnTimeSlotInnerCalendar getOnTimeSlotInnerCalendar() {
        return onTimeSlotInnerCalendar;
    }

    public void setOnTimeSlotInnerCalendar(OnTimeSlotInnerCalendar onTimeSlotInnerCalendar) {
        this.onTimeSlotInnerCalendar = onTimeSlotInnerCalendar;
    }

    public interface OnTimeSlotInnerCalendar{
        void onCalendarBtnClick(View v, boolean result);
        void onDayClick(Date dateClicked);
        void onMonthScroll(Date firstDayOfNewMonth);
    }
}
