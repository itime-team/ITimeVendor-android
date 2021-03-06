package org.unimelb.itime.vendor.dayview;

import android.animation.ValueAnimator;
import android.content.Context;
import android.databinding.BindingMethod;
import android.databinding.BindingMethods;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import org.unimelb.itime.vendor.R;
import org.unimelb.itime.vendor.eventview.DayDraggableEventView;
import org.unimelb.itime.vendor.helper.MyCalendar;
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
                @BindingMethod(type = MonthDayView.class, attribute = "app:MonthDayViewBackToday", method = "backToToday"),
        }
)
public class MonthDayView extends LinearLayout {
    private final String TAG = "MyAPP";

    private Context context;
    final DisplayMetrics dm = getResources().getDisplayMetrics();

    private int upperBoundsOffset = 1;
    private int init_height;
    private int scroll_height;
    private int bodyCurrentPosition;

    private MyCalendar monthDayViewCalendar = new MyCalendar(Calendar.getInstance());

    ArrayList<FlexibleLenViewBody> bodyViewList;

    private LinearLayout parent;
    private LinearLayoutManager headerLinearLayoutManager;

    private FlexibleLenBodyViewPagerAdapter bodyPagerAdapter;
    private FlexibleLenBodyViewPager bodyPager;
    private RecyclerView headerRecyclerView;
    private DayViewHeaderRecyclerAdapter headerRecyclerAdapter;

    private ITimeEventPackageInterface eventPackage;

    private int bodyPagerCurrentState = 0;

    public MonthDayView(Context context) {
        super(context);
        initView();
    }

    public void hideHeader(){
        if (this.headerRecyclerView != null){
            this.headerRecyclerView.setVisibility(View.GONE);
        }
    }

    public void removeAllOptListener(){
        if (bodyPagerAdapter != null){
            bodyPagerAdapter.removeAllOptListener();
        }
    }

    public MonthDayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public MonthDayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public void reloadEvents(){
        bodyPagerAdapter.reloadEvents();
        headerRecyclerAdapter.notifyDataSetChanged();
    }

    private void initView(){
        this.context = getContext();

        parent = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.itime_month_day_view, null);
        this.addView(parent);

        headerRecyclerView = (RecyclerView) parent.findViewById(R.id.headerRowList);
        bodyPager = (FlexibleLenBodyViewPager) parent.findViewById(R.id.pager);
        bodyPager.setScrollDurationFactor(3);
        upperBoundsOffset = 100000;

        bodyViewList = new ArrayList<>();
        this.initBody();
        this.setUpHeader();
        this.setUpBody();
    }


    /*--------------------*/

    public void setDayEventMap(ITimeEventPackageInterface eventPackage){
        this.eventPackage = eventPackage;
        this.bodyPagerAdapter.setEventPackage(eventPackage);
        this.reloadEvents();
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

        init_height = (dm.widthPixels / 7) * 2;
        scroll_height = (dm.widthPixels / 7) * 4;

        ViewGroup.LayoutParams recycler_layoutParams = headerRecyclerView.getLayoutParams();
        recycler_layoutParams.height = init_height;
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
                        headerScrollToDate(body_fst_cal);
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

    public void scrollTo(final Calendar calendar){
        ViewTreeObserver vto = this.getViewTreeObserver();
        final ViewGroup self = this;
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                self.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                headerRecyclerView.stopScroll();
                headerScrollToDate(calendar);
            }
        });
    }

    public void scrollToWithOffset(final long time){
        final Calendar temp = Calendar.getInstance();
        temp.setTimeInMillis(time);

        if (this.getHeight() == 0) {
            ViewTreeObserver vto = this.getViewTreeObserver();
            final ViewGroup self = this;
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    self.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    headerRecyclerView.stopScroll();
                    headerScrollToDate(temp);
                    FlexibleLenViewBody currentBody = bodyPagerAdapter.getViewByPosition(bodyPager.getCurrentItem());
                    currentBody.scrollToTime(time);
                }
            });
        }else{
            headerRecyclerView.stopScroll();
            headerScrollToDate(temp);
            FlexibleLenViewBody currentBody = bodyPagerAdapter.getViewByPosition(bodyPager.getCurrentItem());
            currentBody.scrollToTime(time);
        }
    }

    public void showEventAnim(List<ITimeEventInterface> events){
        for (FlexibleLenViewBody body: bodyViewList
                ) {
            body.showEventAnim(events);
        }
    }

    public void showEventAnim(ITimeEventInterface... events){
        for (FlexibleLenViewBody body: bodyViewList
             ) {
            body.showEventAnim(events);
        }
    }

    public void backToToday(){
        this.headerRecyclerView.stopScroll();
        this.headerRecyclerView.scrollToPosition(upperBoundsOffset);
        this.headerScrollToDate(Calendar.getInstance());
    }

    public void headerScrollToDate(Calendar body_fst_cal){

        DayViewHeader headerView =
                (DayViewHeader) headerLinearLayoutManager.findViewByPosition(headerRecyclerAdapter.rowPst);
        if (headerView != null){
            MyCalendar tempH = new MyCalendar(headerView.getCalendar());
            tempH.setHour(0);
            MyCalendar tempB = new MyCalendar(body_fst_cal);
            tempB.setHour(0);
            tempH.setOffsetByDate(headerRecyclerAdapter.indexInRow);
//            float bugger = (float)(tempB.getCalendar().getTimeInMillis() - tempH.getCalendar().getTimeInMillis()) / (float)(1000*60*60*24);
//            int date_offset =  (int)((float)(tempB.getCalendar().getTimeInMillis() - tempH.getCalendar().getTimeInMillis()) / (float)(1000*60*60*24));
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
                            }
                        }
                    },100);
                    headerRecyclerAdapter.indexInRow = new_index;
                }
            }
        }else {
            headerRecyclerView.stopScroll();
            headerRecyclerView.scrollToPosition(headerRecyclerAdapter.rowPst);
        }
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
            FlexibleLenViewBody bodyView = (FlexibleLenViewBody) LayoutInflater.from(this.context).inflate(R.layout.itime_day_view_body_view, null);
            bodyView.setCalendar(new MyCalendar(calendar));

            final ScrollView scroller = bodyView.getScrollView();

            scroller.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
                @Override
                public void onScrollChanged() {
                    FlexibleLenViewBody currentShow = bodyPagerAdapter.getViewByPosition(bodyPager.getCurrentItem());
                    if (currentShow.getScrollView() == scroller){
                        int scrollY = scroller.getScrollY(); // For ScrollView
                        int scrollX = scroller.getScrollX(); // For ScrollView

                        for (FlexibleLenViewBody body:bodyViewList
                             ) {
                            if (body != currentShow){
                                body.getScrollView().scrollTo(scrollX,scrollY);
                            }
                        }
                    }
                }
            });

//            scroller.setOnScrollChangeListener(new OnScrollChangeListener() {
//                @Override
//                public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
//                    int x = scroller.getScrollX();
//                    int y = scroller.getScrollY();
//
//                    for (FlexibleLenViewBody bodyView:bodyViewList
//                         ) {
//                        ScrollView scrollerInner = bodyView.getScrollView();
//                        if(scrollerInner != scroller){
//                            scrollerInner.scrollTo(x,y);
//                        }
//                    }
//                }
//            });

            bodyViewList.add(bodyView);
            bodyView.setOnBodyTouchListener(new FlexibleLenViewBody.OnBodyTouchListener() {
                @Override
                public boolean bodyOnTouchListener(float tapX, float tapY) {
                    if (headerRecyclerView.getHeight() != init_height){
                        headerRecyclerView.stopScroll();
                        headerLinearLayoutManager.scrollToPositionWithOffset(headerRecyclerAdapter.getCurrentSelectPst(), 0);

                        final View view = headerRecyclerView;
                        ValueAnimator va = ValueAnimator.ofInt(scroll_height, init_height);
                        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            public void onAnimationUpdate(ValueAnimator animation) {
                                Integer value = (Integer) animation.getAnimatedValue();
                                view.getLayoutParams().height = value.intValue();
                                view.requestLayout();
                            }
                        });
                        va.setDuration(200);
                        va.start();

                        return true;
                    }else{
                        return false;
                    }
                }
            });
            bodyView.setOnBodyListener(new OnBodyInnerListener());
        }

    }

    class OnHeaderScrollListener extends RecyclerView.OnScrollListener {
        @Override
        public void onScrollStateChanged(RecyclerView v, int newState) {
            super.onScrollStateChanged(headerRecyclerView, newState);

            if (newState == 1){
                if (v.getHeight() == init_height){
                    final View view = v;
                    ValueAnimator va = ValueAnimator.ofInt(init_height, scroll_height);
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
        public boolean isDraggable(DayDraggableEventView eventView) {
            if (OnBodyOuterListener!=null){
                return OnBodyOuterListener.isDraggable(eventView);
            }else{
                return false;
            }

        }

        @Override
        public void onEventCreate(DayDraggableEventView eventView) {
            MyCalendar currentCal = (bodyPagerAdapter.getViewByPosition(bodyCurrentPosition)).getCalendar();
            eventView.getNewCalendar().setDay(currentCal.getDay());
            eventView.getNewCalendar().setMonth(currentCal.getMonth());
            eventView.getNewCalendar().setYear(currentCal.getYear());
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
            MyCalendar currentCal = new MyCalendar((bodyPagerAdapter.getViewByPosition(bodyCurrentPosition)).getCalendar());
            currentCal.setOffsetByDate(eventView.getIndexInView());
            eventView.getNewCalendar().setDay(currentCal.getDay());
            eventView.getNewCalendar().setMonth(currentCal.getMonth());
            eventView.getNewCalendar().setYear(currentCal.getYear());
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(eventView.getStartTimeM());
            if (OnBodyOuterListener != null){OnBodyOuterListener.onEventDragDrop(eventView);}
        }

        private void bodyAutoSwipe(DayDraggableEventView eventView, int x, int y){
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
                        headerScrollToDate(body_fst_cal);
                    }
                },10);
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


