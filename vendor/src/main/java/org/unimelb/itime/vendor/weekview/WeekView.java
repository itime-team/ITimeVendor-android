package org.unimelb.itime.vendor.weekview;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.unimelb.itime.vendor.dayview.FlexibleLenBodyViewPager;
import org.unimelb.itime.vendor.dayview.FlexibleLenViewBody;
import org.unimelb.itime.vendor.eventview.DayDraggableEventView;
import org.unimelb.itime.vendor.helper.DensityUtil;
import org.unimelb.itime.vendor.helper.MyCalendar;
import org.unimelb.itime.vendor.listener.ITimeEventInterface;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by yuhaoliu on 10/08/16.
 */

public class WeekView extends LinearLayout {
    private final String TAG = "MyAPP";

    private Context context;
    final DisplayMetrics dm = getResources().getDisplayMetrics();

    private int upperBoundsOffset = 1;
    private int bodyCurrentPosition;

    private MyCalendar monthDayViewCalendar = new MyCalendar(Calendar.getInstance());

    ArrayList<WeekViewHeader> headerViewList;
    ArrayList<FlexibleLenViewBody> bodyViewList;
    ArrayList<LinearLayout> weekViewList;


    private FlexibleLenBodyViewPager weekViewPager;
    private WeekViewPagerAdapter adapter;

    private Map<Long, List<ITimeEventInterface>> dayEventMap;

    private int bodyPagerCurrentState = 0;

    public WeekView(Context context) {
        super(context);
        initView();
    }

    public WeekView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public WeekView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public void reloadEvents(){

    }

    private void initView(){
        this.context = getContext();

        bodyViewList = new ArrayList<>();
        this.initBody();
        headerViewList = new ArrayList<>();
        this.initHeader();
        weekViewList = new ArrayList<>();
        this.initWeekViews();

        this.setUpWeekView();
    }

    private void initHeader(){
        int size = 4;
        int padding = DensityUtil.dip2px(context,20);
        for (int i = 0; i < size; i++) {
            WeekViewHeader headerView = new WeekViewHeader(context);

            headerView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            headerView.setPadding(padding,padding,0,padding);
            headerViewList.add(headerView);

        }
    }

    private void initWeekViews(){
        int size = 4;
        for (int i = 0; i < size; i++) {
            LinearLayout weekView = new LinearLayout(context);
            weekView.setOrientation(VERTICAL);
            weekView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            weekView.addView(this.headerViewList.get(i));
            weekView.addView(this.bodyViewList.get(i));
            this.weekViewList.add(weekView);
        }
    }

    /*--------------------*/

    public void setDayEventMap(Map<Long, List<ITimeEventInterface>> dayEventMap){
        this.dayEventMap = dayEventMap;
        if (adapter != null){
            adapter.setDayEventMap(this.dayEventMap);
        }else {
            Log.i(TAG, "adapter: null" );
        }
    }

    private void setUpWeekView(){
        weekViewPager = new FlexibleLenBodyViewPager(context);
        weekViewPager.setScrollDurationFactor(3);
        upperBoundsOffset = 500;
        bodyCurrentPosition = upperBoundsOffset;
        adapter = new WeekViewPagerAdapter(upperBoundsOffset,weekViewList);
        if (this.dayEventMap != null){
            adapter.setDayEventMap(this.dayEventMap);
        }
        weekViewPager.setAdapter(adapter);
        weekViewPager.setCurrentItem(upperBoundsOffset);
        this.addView(weekViewPager,new LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        weekViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                bodyCurrentPosition = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }


    private void initBody(){
        int size = 4;
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTime(new Date());
        int day_of_week = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        calendar.set(Calendar.DAY_OF_MONTH,calendar.get(Calendar.DAY_OF_MONTH) - day_of_week);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        for (int i = 0; i < size; i++) {
            FlexibleLenViewBody bodyView = new FlexibleLenViewBody(context,3);
            bodyView.setLayoutParams(new LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            bodyView.setCalendar(new MyCalendar(calendar));
            bodyView.setOnBodyListener(new OnBodyInnerListener());

            bodyViewList.add(bodyView);
        }

    }


    private OnHeaderListener onHeaderListener;

    public void setOnHeaderListener(OnHeaderListener onHeaderListener){
        this.onHeaderListener = onHeaderListener;
    }

    FlexibleLenViewBody.OnBodyListener OnBodyOuterListener;

    public void setOnBodyOuterListener(FlexibleLenViewBody.OnBodyListener onBodyOuterListener){
        this.OnBodyOuterListener = onBodyOuterListener;
    }

    public interface OnHeaderListener{
        void onMonthChanged(MyCalendar calendar);
    }

    public class OnBodyInnerListener implements FlexibleLenViewBody.OnBodyListener{
        int parentWidth = dm.widthPixels;

        @Override
        public void onEventCreate(DayDraggableEventView eventView) {
//            MyCalendar currentCal = (adapter.getViewByPosition(bodyCurrentPosition)).getCalendar();
//            eventView.getNewCalendar().setDay(currentCal.getDay());
//            eventView.getNewCalendar().setMonth(currentCal.getMonth());
//            eventView.getNewCalendar().setYear(currentCal.getYear());
            if (OnBodyOuterListener != null){OnBodyOuterListener.onEventCreate(eventView);}
        }

        @Override
        public void onEventClick(DayDraggableEventView eventView) {
            if (OnBodyOuterListener != null){OnBodyOuterListener.onEventClick(eventView);}

        }

        @Override
        public void onEventDragStart(DayDraggableEventView eventView) {
            if (OnBodyOuterListener != null){OnBodyOuterListener.onEventDragStart(eventView);}

        }

        @Override
        public void onEventDragging(DayDraggableEventView eventView, int x, int y) {
            boolean isSwiping = bodyPagerCurrentState == 0;
            if (isSwiping){
                this.bodyAutoSwipe(eventView, x, y);
            }
            if (OnBodyOuterListener != null){OnBodyOuterListener.onEventDragging(eventView, x, y);}
        }

        @Override
        public void onEventDragDrop(DayDraggableEventView eventView) {
//            MyCalendar currentCal = (adapter.getViewByPosition(bodyCurrentPosition)).getCalendar();
//            currentCal.setOffsetByDate(eventView.getIndexInView());
//            eventView.getNewCalendar().setDay(currentCal.getDay());
//            eventView.getNewCalendar().setMonth(currentCal.getMonth());
//            eventView.getNewCalendar().setYear(currentCal.getYear());
//            Calendar cal = Calendar.getInstance();
//            cal.setTimeInMillis(eventView.getStartTimeM());
            if (OnBodyOuterListener != null){OnBodyOuterListener.onEventDragDrop(eventView);}
        }

        private void bodyAutoSwipe(DayDraggableEventView eventView, int x, int y){
            Log.i(TAG, "bodyAutoSwipe: " + x);
            Log.i(TAG, "parentWidth: " + parentWidth);
            int offset = x > (parentWidth * 0.85) ? 1 : (x <= parentWidth * 0.05 ? -1 : 0);
            if (offset != 0){
                int scrollTo = bodyCurrentPosition + offset;
                weekViewPager.setCurrentItem(scrollTo,true);
            }
        }
    }

    /**
     *
     * @param className
     * @param <E>
     */
    public <E extends ITimeEventInterface> void setEventClassName(Class<E> className){

        for (FlexibleLenViewBody view: bodyViewList){
            view.setEventClassName(className);
        }

    }

}


