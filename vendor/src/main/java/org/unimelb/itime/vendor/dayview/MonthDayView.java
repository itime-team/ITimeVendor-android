package org.unimelb.itime.vendor.dayview;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.databinding.BindingMethod;
import android.databinding.BindingMethods;
import android.databinding.repacked.kotlin.Deprecated;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;

import org.unimelb.itime.vendor.R;
import org.unimelb.itime.vendor.unitviews.DraggableEventView;
import org.unimelb.itime.vendor.util.MyCalendar;
import org.unimelb.itime.vendor.listener.ITimeEventInterface;
import org.unimelb.itime.vendor.listener.ITimeEventPackageInterface;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by yuhaoliu on 10/08/16.
 */

@BindingMethods(
        {
                @BindingMethod(type = MonthDayView.class, attribute = "monthView:BackToday", method = "backToToday"),
                @BindingMethod(type = MonthDayView.class, attribute = "monthView:onHeaderListener", method = "setOnHeaderListener"),
                @BindingMethod(type = MonthDayView.class, attribute = "monthView:onBodyListener", method = "setOnBodyOuterListener"),
                @BindingMethod(type = MonthDayView.class, attribute = "monthView:onFlexibleBodyScroll", method = "setOnFlexibleBodyScroll"),
        }
)
public class MonthDayView extends LinearLayout {
    private final DisplayMetrics dm = getResources().getDisplayMetrics();
    private int upperBoundsOffset;
    private int headerCollapsedHeight;
    private int headerExpandedHeight;
    private int bodyCurrentPosition;
    private int bodyPagerCurrentState = 0;

    private MyCalendar monthDayViewCalendar = new MyCalendar(Calendar.getInstance());

    private ITimeEventPackageInterface eventPackage;
    private ArrayList<FlexibleLenViewBody> bodyViewList = new ArrayList<>();

    private LinearLayout container;
    private RecyclerView headerRecyclerView;
    private FlexibleLenBodyViewPager bodyPager;


    private FlexibleLenBodyViewPagerAdapter bodyPagerAdapter;
    private DayViewHeaderRecyclerAdapter headerRecyclerAdapter;
    private LinearLayoutManager headerLinearLayoutManager;

    private OnFlexibleBodyScroll onFlexibleBodyScroll;
    private ViewTreeObserver.OnScrollChangedListener onBodyScrollChangedListener;

    private Context context;

    public MonthDayView(Context context) {
        super(context);
        initView();
    }

    public MonthDayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public MonthDayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    @Override
    protected void onDetachedFromWindow() {
        if (this.onBodyScrollChangedListener != null){
            this.getViewTreeObserver().removeOnScrollChangedListener(onBodyScrollChangedListener);
        }
        super.onDetachedFromWindow();
    }

    /***************************************************************************
     * Public methods Part, providing the methods of controlling for MonthDayView
     ***************************************************************************/

    /**
     * Set the event data package
     * @param eventPackage
     */
    public void setDayEventMap(ITimeEventPackageInterface eventPackage){
        this.eventPackage = eventPackage;
        this.bodyPagerAdapter.setEventPackage(eventPackage);
        this.reloadEvents();
    }

    /**
     * Refreshing views, calling if there are any changes of data source
     */
    public void reloadEvents(){
        bodyPagerAdapter.reloadEvents();
        headerRecyclerAdapter.notifyDataSetChanged();
    }

    /**
     * Removing all operation listeners for event, including drag and click
     */
    public void removeAllOptListener(){
        if (bodyPagerAdapter != null){
            bodyPagerAdapter.removeAllOptListener();
        }
    }

    /**
     * scroll to certain date, will reserve scroll position in Scroll View.
     * @param calendar
     */
    public void scrollTo(final Calendar calendar){
        if (this.getHeight() == 0){
            ViewTreeObserver vto = this.getViewTreeObserver();
            final ViewGroup self = this;
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    self.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    headerRecyclerView.stopScroll();
                    headerScrollToDate(calendar,false);
                }
            });
        }else{
            headerRecyclerView.stopScroll();
            headerScrollToDate(calendar,false);
        }
    }

    /**
     * scroll to certain datetime, will scroll to exactly corresponded time of input.
     * @param time
     */
    public void scrollToWithOffset(final long time){
        final Calendar temp = Calendar.getInstance();
        temp.setTimeInMillis(time);

        if (headerRecyclerView.getHeight() == 0) {
            ViewTreeObserver vto = headerRecyclerView.getViewTreeObserver();
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    headerRecyclerView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    headerRecyclerView.stopScroll();
                    headerScrollToDate(temp,true);
                }
            });
        }else{
            headerRecyclerView.stopScroll();
            headerScrollToDate(temp,true);
        }
    }

    /**
     * hide the head part of MonthDayView
     */
    public void hideHeader(){
        if (this.headerRecyclerView != null){
            this.headerRecyclerView.setVisibility(View.GONE);
        }
    }

    /**
     * scroll to date of today with reserving of y position in scroll view.
     */
    public void backToToday(){
        if (headerRecyclerView.getHeight() != headerCollapsedHeight){
            collapseHeader(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    headerScrollToDate(Calendar.getInstance(),true);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
        }else{
            headerScrollToDate(Calendar.getInstance(),true);
        }
    }

    /**
     * All events in the input will show animation
     * @param events
     */
    @Deprecated(message = "Now animation of event is controlled by the attribute isHighlight in WrapperEvent")
    public void showEventAnim(List<ITimeEventInterface> events){
        for (FlexibleLenViewBody body: bodyViewList
                ) {
            body.showEventAnim(events);
        }
    }

    @Deprecated(message = "Now animation of event is controlled by the attribute isHighlight in WrapperEvent")
    public void showEventAnim(ITimeEventInterface... events){
        for (FlexibleLenViewBody body: bodyViewList
             ) {
            body.showEventAnim(events);
        }
    }

    /**
     * If want to entitle vendor to create instance of event, set the event class to vendor.
     * @param className
     * @param <E>
     */
    public <E extends ITimeEventInterface> void setEventClassName(Class<E> className){

        for (FlexibleLenViewBody view: bodyViewList){
            view.setEventClassName(className);
        }

    }

    /***************************************************************************
    * Listener Part, Including onHeaderListener, OnBodyOuterListener
    ***************************************************************************/

    private OnHeaderListener onHeaderListener;

    private class OnHeaderScrollListener extends RecyclerView.OnScrollListener {
        @Override
        public void onScrollStateChanged(RecyclerView v, int newState) {
            super.onScrollStateChanged(headerRecyclerView, newState);

            if (newState == 1){
                if (v.getHeight() == headerCollapsedHeight){
                    expandHeader();
                }
            }
            //for now header date

            if (onHeaderListener != null){
                int index = headerLinearLayoutManager.findFirstCompletelyVisibleItemPosition();
                DayViewHeader fstVisibleHeader = (DayViewHeader) headerLinearLayoutManager.findViewByPosition(index);
                monthDayViewCalendar = fstVisibleHeader.getCalendar();
                onHeaderListener.onMonthChanged(monthDayViewCalendar);
            }
        }
    }

    public void setOnHeaderListener(OnHeaderListener onHeaderListener){
        this.onHeaderListener = onHeaderListener;
    }

    private EventController.OnEventListener OnBodyOuterListener;

    public void setOnBodyOuterListener(EventController.OnEventListener onBodyOuterListener){
        this.OnBodyOuterListener = onBodyOuterListener;
    }

    public interface OnHeaderListener{
        void onMonthChanged(MyCalendar calendar);
    }

    private class OnEventInnerListener implements EventController.OnEventListener {
        int parentWidth = dm.widthPixels;

        @Override
        public boolean isDraggable(DraggableEventView eventView) {
            if (OnBodyOuterListener!=null){
                return OnBodyOuterListener.isDraggable(eventView);
            }else{
                return false;
            }

        }

        @Override
        public void onEventCreate(DraggableEventView eventView) {
            MyCalendar currentCal = (bodyPagerAdapter.getViewByPosition(bodyCurrentPosition)).getCalendar();
            eventView.getNewCalendar().setDay(currentCal.getDay());
            eventView.getNewCalendar().setMonth(currentCal.getMonth());
            eventView.getNewCalendar().setYear(currentCal.getYear());
            if (OnBodyOuterListener != null){OnBodyOuterListener.onEventCreate(eventView);}
        }

        @Override
        public void onEventClick(DraggableEventView eventView) {
            if (OnBodyOuterListener != null){OnBodyOuterListener.onEventClick(eventView);}

        }

        @Override
        public void onEventDragStart(DraggableEventView eventView) {
            if (OnBodyOuterListener != null){OnBodyOuterListener.onEventDragStart(eventView);}

        }

        @Override
        public void onEventDragging(DraggableEventView eventView, int x, int y) {
            boolean isSwiping = bodyPagerCurrentState == 0;
            if (isSwiping){
                this.bodyAutoSwipe(eventView, x, y);
            }
            if (OnBodyOuterListener != null){OnBodyOuterListener.onEventDragging(eventView, x, y);}
        }

        @Override
        public void onEventDragDrop(DraggableEventView eventView) {
            MyCalendar currentCal = new MyCalendar((bodyPagerAdapter.getViewByPosition(bodyCurrentPosition)).getCalendar());
            currentCal.setOffsetByDate(eventView.getIndexInView());
            eventView.getNewCalendar().setDay(currentCal.getDay());
            eventView.getNewCalendar().setMonth(currentCal.getMonth());
            eventView.getNewCalendar().setYear(currentCal.getYear());
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(eventView.getStartTimeM());
            if (OnBodyOuterListener != null){OnBodyOuterListener.onEventDragDrop(eventView);}
        }

        private void bodyAutoSwipe(DraggableEventView eventView, int x, int y){
            int offset = x > (parentWidth * 0.7) ? 1 : (x <= parentWidth * 0.05 ? -1 : 0);
            if (offset != 0){
                int scrollTo = bodyCurrentPosition + offset;
                bodyPager.setCurrentItem(scrollTo,true);
                bodyPagerAdapter.currentDayPos = scrollTo;
                MyCalendar bodyMyCalendar = (bodyPagerAdapter.getViewByPosition(scrollTo)).getCalendar();
                final Calendar body_fst_cal = bodyMyCalendar.getCalendar();
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        headerScrollToDate(body_fst_cal,false);
                    }
                },10);
            }
        }
    }

    public interface OnFlexibleBodyScroll {
        void onScroll(long currentTime);
    }

    public void setOnFlexibleBodyScroll(OnFlexibleBodyScroll onFlexibleBodyScroll) {
        this.onFlexibleBodyScroll = onFlexibleBodyScroll;
    }

    /***************************************************************************
     * Inner private methods block, including function of setting up MonthDayView
     ***************************************************************************/

    private void initView(){
        this.context = getContext();
        this.container = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.itime_month_day_view, null);
        this.addView(container);

        this.headerRecyclerView = (RecyclerView) container.findViewById(R.id.headerRowList);
        //记得改回来 pager
        this.bodyPager = (FlexibleLenBodyViewPager) container.findViewById(R.id.recyclerScrollView);
        this.bodyPager.setScrollDurationFactor(3);
        this.upperBoundsOffset = 100000;

        this.initBody();
        this.setUpHeader();
        this.setUpBody();
    }

    private void setUpHeader(){
        headerRecyclerAdapter = new DayViewHeaderRecyclerAdapter(context, upperBoundsOffset);
        headerRecyclerAdapter.setOnHeaderListener(new DayViewHeaderRecyclerAdapter.OnHeaderListener() {
            @Override
            public void onClick(MyCalendar myCalendar) {
                if (onHeaderListener != null){
                    onHeaderListener.onMonthChanged(myCalendar);
                }
            }
        });
        headerRecyclerAdapter.setOnCheckIfHasEvent(new DayViewHeader.OnCheckIfHasEvent() {
            @Override
            public boolean todayHasEvent(long startOfDay) {
                boolean hasRegular = eventPackage.getRegularEventDayMap().containsKey(startOfDay) && (eventPackage.getRegularEventDayMap().get(startOfDay).size() != 0);
                boolean hasRepeated = eventPackage.getRepeatedEventDayMap().containsKey(startOfDay) && (eventPackage.getRepeatedEventDayMap().get(startOfDay).size() != 0);
                return hasRegular || hasRepeated;
            }
        });
        headerRecyclerView.setHasFixedSize(true);
        headerRecyclerView.setAdapter(headerRecyclerAdapter);
        headerLinearLayoutManager = new LinearLayoutManager(context);
        headerRecyclerView.setLayoutManager(headerLinearLayoutManager);
        headerRecyclerView.addItemDecoration(new DayViewHeaderRecyclerDivider(context));

        headerCollapsedHeight = (dm.widthPixels / 7 - 20) * 2;
        headerExpandedHeight = (dm.widthPixels / 7 - 20) * 4;

        ViewGroup.LayoutParams recycler_layoutParams = headerRecyclerView.getLayoutParams();
        recycler_layoutParams.height = headerCollapsedHeight;
        headerRecyclerView.setLayoutParams(recycler_layoutParams);
        headerRecyclerView.setOnScrollListener(new OnHeaderScrollListener());
        move(upperBoundsOffset);
    }

    private void setUpBody(){
        bodyPagerAdapter = new FlexibleLenBodyViewPagerAdapter(bodyViewList, upperBoundsOffset);

        bodyPagerAdapter.notifyDataSetChanged();
        headerRecyclerAdapter.setBodyPager(bodyPager);
        bodyPager.setAdapter(bodyPagerAdapter);
        bodyPager.setOffscreenPageLimit(1);

        bodyCurrentPosition = upperBoundsOffset;
        bodyPager.setCurrentItem(upperBoundsOffset);
        bodyPagerAdapter.currentDayPos = upperBoundsOffset;
        bodyPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            private boolean slideByUser = false;
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                try{
                    //slideByUser
                    if (slideByUser) {
                        MyCalendar bodyMyCalendar = (bodyPagerAdapter.getViewByPosition(position)).getCalendar();
                        Calendar body_fst_cal = bodyMyCalendar.getCalendar();
                        //update header
                        bodyPagerAdapter.currentDayPos = position;
                        headerScrollToDate(body_fst_cal,false);
                    }
                }finally {
                    bodyCurrentPosition = position;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                bodyPagerCurrentState = state;
                if (state == 1){
                    //because 1->2->selected->0
                    slideByUser = true;
                }else if (state == 2){
                }else {
                    //after executed selected, reset to false;
                    slideByUser = false;
                    //notify header info by interface
                    if (onHeaderListener != null){
                        monthDayViewCalendar = bodyPagerAdapter.getViewByPosition(bodyCurrentPosition).getCalendar();
                        onHeaderListener.onMonthChanged(monthDayViewCalendar);
                    }
                }
            }
        });
    }

    private void move(int n){
        if (n<0 || n>=headerRecyclerAdapter.getItemCount() ){
            return;
        }
        headerRecyclerView.stopScroll();
        headerRecyclerView.scrollToPosition(n);
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
            FlexibleLenViewBody bodyView = new FlexibleLenViewBody(context,1);
            bodyView.setCalendar(new MyCalendar(calendar));
            bodyViewList.add(bodyView);
            bodyView.setOnBodyTouchListener(new FlexibleLenViewBody.OnBodyTouchListener() {
                @Override
                public boolean bodyOnTouchListener(float tapX, float tapY) {
                    if (headerRecyclerView.getHeight() != headerCollapsedHeight){
                        collapseHeader(null);
                        return true;
                    }else{
                        return false;
                    }
                }
            });
            bodyView.setOnBodyListener(new OnEventInnerListener());
        }
        this.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                //if detached remove this listener
                if(onBodyScrollChangedListener == null){
                    onBodyScrollChangedListener = this;
                }

                FlexibleLenViewBody currentShow = bodyPagerAdapter.getViewByPosition(bodyPager.getCurrentItem());
                //scroll listener
                Calendar nowTime = currentShow.getCurrentTime();
                if (nowTime != null && onFlexibleBodyScroll != null){
                    onFlexibleBodyScroll.onScroll(nowTime.getTimeInMillis());
                }

                final int scrollY = currentShow.getScrollView().getScrollY(); // For ScrollView

                for (final FlexibleLenViewBody body:bodyViewList
                        ) {
                    if (body.getScrollView().getHeight() == 0){
                        body.getScrollView().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                            @Override
                            public void onGlobalLayout() {
                                body.getScrollView().getViewTreeObserver().removeOnGlobalLayoutListener(this);
                                body.getScrollView().setScrollY(scrollY);
                            }
                        });
                    }else {
                        body.getScrollView().setScrollY(scrollY);
                    }
                }
            }
        });
    }

    private void headerScrollToDate(final Calendar body_fst_cal, final boolean toTime){
        DayViewHeader headerView =
                (DayViewHeader) headerLinearLayoutManager.findViewByPosition(headerRecyclerAdapter.rowPst);

        if (headerView != null){
            MyCalendar tempH = new MyCalendar(headerView.getCalendar());
            tempH.setHour(0);
            MyCalendar tempB = new MyCalendar(body_fst_cal);
            tempB.setHour(0);

            tempH.setOffsetByDate(headerRecyclerAdapter.indexInRow);

            int date_offset = Math.round((float)(tempB.getCalendar().getTimeInMillis() - tempH.getCalendar().getTimeInMillis()) / (float)(1000*60*60*24));
            int row_diff = date_offset/7;
            int day_diff = ((headerRecyclerAdapter.indexInRow+1) + date_offset%7);

            if (date_offset > 0){
                row_diff = row_diff + (day_diff > 7 ? 1:0);
                day_diff = day_diff > 7 ? day_diff%7 : day_diff;
            }else if(date_offset < 0){
                row_diff = row_diff + (day_diff <= 0 ? -1:0);
                day_diff = day_diff <= 0 ? (7 + day_diff):day_diff;
            }

            if ((row_diff != 0 || day_diff != 0)){
                if (row_diff != 0){
                    int newRowPst = row_diff + headerRecyclerAdapter.rowPst;
                    headerRecyclerView.stopScroll();
                    headerRecyclerView.scrollToPosition(newRowPst);
                    headerRecyclerAdapter.rowPst = newRowPst;
                }
                if (day_diff != 0){
                    final int new_index = day_diff - 1;
                    postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            DayViewHeader need_set_index_header =((DayViewHeader) headerLinearLayoutManager.findViewByPosition(headerRecyclerAdapter.rowPst));
                            if (need_set_index_header != null){
                                need_set_index_header.performNthDayClick(new_index);
                                if (toTime){
                                    //scroll to offset
                                    FlexibleLenViewBody currentBody = bodyPagerAdapter.getViewByPosition(bodyPager.getCurrentItem());
                                    currentBody.scrollToTime(body_fst_cal.getTimeInMillis());
                                }
                            }else {
                                this.run();
                            }
                        }
                    },10);
                    headerRecyclerAdapter.indexInRow = new_index;
                }
            }
        }else {
            headerRecyclerView.stopScroll();
            headerLinearLayoutManager.scrollToPosition(headerRecyclerAdapter.rowPst);
            headerRecyclerView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    headerScrollToDate(body_fst_cal,toTime);
                }
            },10);
        }
    }

    private void collapseHeader(Animator.AnimatorListener callback){
        headerRecyclerView.stopScroll();
        headerLinearLayoutManager.scrollToPositionWithOffset(headerRecyclerAdapter.getCurrentSelectPst(), 0);

        final View view = headerRecyclerView;
        ValueAnimator va = ValueAnimator.ofInt(headerExpandedHeight, headerCollapsedHeight);
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                Integer value = (Integer) animation.getAnimatedValue();
                view.getLayoutParams().height = value.intValue();
                view.requestLayout();
            }
        });

        if(callback != null){
            va.addListener(callback);
        }

        va.setDuration(200);
        va.start();
    }

    private void expandHeader(){
        final View view = headerRecyclerView;
        ValueAnimator va = ValueAnimator.ofInt(headerCollapsedHeight, headerExpandedHeight);
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                Integer value = (Integer) animation.getAnimatedValue();
                view.getLayoutParams().height = value.intValue();
                view.requestLayout();
            }
        });
        va.setDuration(200);
        va.start();
    }

    private boolean isWithin(ITimeEventInterface event, long dayOfBegin, int index){
        long startTime = event.getStartTime();
        long endTime = event.getEndTime();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(dayOfBegin);

        MyCalendar calS = new MyCalendar(calendar);
        calS.setOffsetByDate(index);

        MyCalendar calE = new MyCalendar(calendar);
        calE.setOffsetByDate(index);
        calE.setHour(23);
        calE.setMinute(59);

        long todayStartTime =  calS.getBeginOfDayMilliseconds();
        long todayEndTime =  calE.getCalendar().getTimeInMillis();

        return
                todayEndTime >= startTime && todayStartTime <= endTime;
    }
}


