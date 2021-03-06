package org.unimelb.itime.vendor.weekview;

import android.content.Context;
import android.databinding.BindingMethod;
import android.databinding.BindingMethods;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import org.unimelb.itime.vendor.dayview.FlexibleLenBodyViewPager;
import org.unimelb.itime.vendor.dayview.FlexibleLenViewBody;
import org.unimelb.itime.vendor.eventview.DayDraggableEventView;
import org.unimelb.itime.vendor.helper.DensityUtil;
import org.unimelb.itime.vendor.helper.MyCalendar;
import org.unimelb.itime.vendor.listener.ITimeEventInterface;
import org.unimelb.itime.vendor.listener.ITimeEventPackageInterface;
import org.unimelb.itime.vendor.listener.ITimeTimeSlotInterface;
import org.unimelb.itime.vendor.timeslot.TimeSlotView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.support.v4.view.ViewPager.SCROLL_STATE_IDLE;

/**
 * Created by yuhaoliu on 10/08/16.
 */

@BindingMethods(
        {
                @BindingMethod(type = WeekView.class, attribute = "app:onWeekViewDayChange", method="setOnHeaderListener"),
                @BindingMethod(type = WeekView.class, attribute = "app:onTimeSlotOuterListener", method = "setOnTimeSlotOuterListener"),
                @BindingMethod(type = WeekView.class, attribute = "app:addTimeSlot" ,method = "addTimeSlot"),
                @BindingMethod(type = WeekView.class, attribute = "app:weekViewBackToToday" ,method = "backToToday")
        }
)
public class WeekView extends LinearLayout {
    private final DisplayMetrics dm = getResources().getDisplayMetrics();
    private MyCalendar monthDayViewCalendar = new MyCalendar(Calendar.getInstance());

    private Context context;

    private int upperBoundsOffset = 1;
    private int bodyCurrentPosition;
    private int bodyPagerCurrentState = 0;

    private ArrayList<WeekViewHeader> headerViewList;
    private ArrayList<FlexibleLenViewBody> bodyViewList;
    private ArrayList<LinearLayout> weekViewList;
    private ArrayList<ITimeTimeSlotInterface> slotsInfo = new ArrayList<>();

    private FlexibleLenBodyViewPager weekViewPager;
    private WeekViewPagerAdapter adapter;
    private ITimeEventPackageInterface eventPackage;

    private FlexibleLenViewBody.OnBodyListener OnBodyOuterListener;
    private FlexibleLenViewBody.OnTimeSlotListener onTimeSlotOuterListener;
    private OnHeaderListener onHeaderListener;

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
        int padding = DensityUtil.dip2px(context,5);
        //must be consistent with width of left bar in body part.
        int leftBarPadding = DensityUtil.dip2px(context,40);
        for (int i = 0; i < size; i++) {
            WeekViewHeader headerView = new WeekViewHeader(context);
            headerView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            headerView.setPadding(leftBarPadding,padding,0,padding);
            headerViewList.add(headerView);
        }
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
            FlexibleLenViewBody bodyView = new FlexibleLenViewBody(context,7);
            bodyView.setLayoutParams(new LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            bodyView.setCalendar(new MyCalendar(calendar));
            bodyView.setOnBodyListener(new OnBodyInnerListener());
            bodyView.setOnTimeSlotListener(new OnTimeSlotInnerListener());

            final ScrollView scroller = bodyView.getScrollView();

            scroller.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
                @Override
                public void onScrollChanged() {
                    FlexibleLenViewBody currentShow = adapter.getViewBodyByPosition(weekViewPager.getCurrentItem());
                    if (bodyPagerCurrentState == SCROLL_STATE_IDLE){
                        currentShow.timeSlotAnimationChecker();
                    }

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
            bodyViewList.add(bodyView);
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

    private void setUpWeekView(){
        weekViewPager = new FlexibleLenBodyViewPager(context);
        weekViewPager.setScrollDurationFactor(3);
        upperBoundsOffset = 500;
        bodyCurrentPosition = upperBoundsOffset;
        adapter = new WeekViewPagerAdapter(upperBoundsOffset,weekViewList);
        if (this.eventPackage != null){
            adapter.setDayEventMap(this.eventPackage);
        }
        adapter.setSlotsInfo(this.slotsInfo);
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
                monthDayViewCalendar = adapter.getViewBodyByPosition(position).getCalendar();
                if (onHeaderListener != null){
                    onHeaderListener.onMonthChanged(monthDayViewCalendar);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                bodyPagerCurrentState = state;
                if (state == SCROLL_STATE_IDLE){
                    //when slide down
                    FlexibleLenViewBody currentShow = adapter.getViewBodyByPosition(weekViewPager.getCurrentItem());
                    currentShow.timeSlotAnimationChecker();
                }
            }
        });
    }

    private int getRowDiff(Calendar body_fst_cal){

        MyCalendar tempH = new MyCalendar(Calendar.getInstance());
        MyCalendar tempB = new MyCalendar(body_fst_cal);

        int date_offset =  Math.round((float)(tempB.getCalendar().getTimeInMillis() - tempH.getCalendar().getTimeInMillis()) / (float)(1000*60*60*24));

        int row_diff = date_offset/7;
        int day_diff = (tempH.getDayOfWeek() + date_offset%7);

        if (date_offset > 0){
            row_diff = row_diff + (day_diff > 7 ? 1:0);
            day_diff = day_diff > 7 ? day_diff%7 : day_diff;
        }else if(date_offset < 0){
            row_diff = row_diff + (day_diff <= 0 ? -1:0);
            day_diff = day_diff <= 0 ? (7 + day_diff):day_diff;
        }

        return row_diff;
    }

    public void reloadEvents(){
        adapter.reloadEvents();
    }

    public void reloadTimeSlots(boolean animate){
//        adapter.reloadTimeSlots(animate);
    }

    public void backToToday(){
        weekViewPager.setCurrentItem(upperBoundsOffset,false);
    }

    public void scrollTo(final Calendar toDate){
        ViewTreeObserver vto = this.getViewTreeObserver();
        final ViewGroup self = this;
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                self.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                weekViewPager.setCurrentItem(upperBoundsOffset + getRowDiff(toDate),false);
            }
        });
    }

    public void scrollToWithOffset(final long time){
        final Calendar temp = Calendar.getInstance();
        temp.setTimeInMillis(time);

        if (this.getHeight() == 0){
            ViewTreeObserver vto = this.getViewTreeObserver();
            final ViewGroup self = this;
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                self.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                weekViewPager.setCurrentItem(upperBoundsOffset + getRowDiff(temp),false);
                FlexibleLenViewBody currentBody = adapter.getViewBodyByPosition(weekViewPager.getCurrentItem());
                currentBody.scrollToTime(time);

                }
            });

        }else {
            weekViewPager.setCurrentItem(upperBoundsOffset + getRowDiff(temp),false);
            FlexibleLenViewBody currentBody = adapter.getViewBodyByPosition(weekViewPager.getCurrentItem());
            currentBody.scrollToTime(time);
        }

    }

    public void setDayEventMap(ITimeEventPackageInterface eventPackage){
        this.eventPackage = eventPackage;
        if (adapter != null){
            adapter.setDayEventMap(this.eventPackage);
        }else {
            Log.i("Debug", "adapter: null" );
        }
        this.reloadEvents();
    }

    public void showEventAnim(final List<ITimeEventInterface> events){
        for (final FlexibleLenViewBody body: bodyViewList
                ) {
            if (body.isShown()){
                body.showEventAnim(events);
            }else{
                ViewTreeObserver vto = this.getViewTreeObserver();
                vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        WeekView.this.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        body.showEventAnim(events);
                    }
                });
            }
        }
    }

    public void showEventAnim(final ITimeEventInterface... events){
        for (final FlexibleLenViewBody body: bodyViewList
                ) {
            if (body.isShown()){
                body.showEventAnim(events);
            }else{
                ViewTreeObserver vto = this.getViewTreeObserver();
                vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        WeekView.this.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        body.showEventAnim(events);
                    }
                });
            }
        }
    }

    public <T extends ITimeTimeSlotInterface>void showTimeslotAnim(final T ... timeslots){
        for (final FlexibleLenViewBody body: bodyViewList
                ) {
            if (body.isShown()){
                body.showTimeslotAnim(timeslots);
            }else{
                ViewTreeObserver vto = this.getViewTreeObserver();
                vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        WeekView.this.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        body.showTimeslotAnim(timeslots);
                    }
                });
            }
        }
    }

    public void showTimeslotAnim(final List<? extends ITimeTimeSlotInterface> timeslots){
        for (final FlexibleLenViewBody body: bodyViewList
                ) {
            if (body.isShown()){
                body.showTimeslotAnim(timeslots);
            }else{
                ViewTreeObserver vto = this.getViewTreeObserver();
                vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        WeekView.this.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        body.showTimeslotAnim(timeslots);
                    }
                });
            }
        }
    }


    public void setOnHeaderListener(OnHeaderListener onHeaderListener){
        this.onHeaderListener = onHeaderListener;
    }

    public void setOnBodyOuterListener(FlexibleLenViewBody.OnBodyListener onBodyOuterListener){
        this.OnBodyOuterListener = onBodyOuterListener;
    }

    public void setOnTimeSlotOuterListener(FlexibleLenViewBody.OnTimeSlotListener onTimeSlotOuterListener){
        this.onTimeSlotOuterListener = onTimeSlotOuterListener;
    }

    public class OnTimeSlotInnerListener implements FlexibleLenViewBody.OnTimeSlotListener{
        @Override
        public void onTimeSlotCreate(TimeSlotView timeSlotView) {
            MyCalendar currentCal = new MyCalendar((adapter.getViewBodyByPosition(bodyCurrentPosition)).getCalendar());
            currentCal.setOffsetByDate(timeSlotView.getIndexInView());
            timeSlotView.getNewCalendar().setDay(currentCal.getDay());
            timeSlotView.getNewCalendar().setMonth(currentCal.getMonth());
            timeSlotView.getNewCalendar().setYear(currentCal.getYear());

//            TimeSlotStruct newStruct = new TimeSlotStruct();
//            newStruct.startTime = timeSlotView.getNewStartTime();
//            newStruct.endTime = timeSlotView.getNewStartTime() + timeSlotView.getDuration();
//            newStruct.status = false;

//            timeSlotView.setTag(newStruct);
            if (onTimeSlotOuterListener != null){
                onTimeSlotOuterListener.onTimeSlotCreate(timeSlotView);
//                ){
//                    addTimeSlot(newStruct);
//                    reloadTimeSlots(false);
//                }
            }
        }

        @Override
        public void onTimeSlotClick(TimeSlotView timeSlotView) {
            if (onTimeSlotOuterListener != null){
                onTimeSlotOuterListener.onTimeSlotClick(timeSlotView);
            }
        }

        @Override
        public void onTimeSlotDragStart(TimeSlotView timeSlotView) {
            if (onTimeSlotOuterListener != null){
                onTimeSlotOuterListener.onTimeSlotDragStart(timeSlotView);
            }
        }

        @Override
        public void onTimeSlotDragging(TimeSlotView timeSlotView, int x, int y) {
            if (onTimeSlotOuterListener != null){
                onTimeSlotOuterListener.onTimeSlotDragging(timeSlotView, x, y);
            }
        }

        @Override
        public void onTimeSlotDragDrop(TimeSlotView timeSlotView, long start, long end) {
            MyCalendar currentCal = new MyCalendar((adapter.getViewBodyByPosition(bodyCurrentPosition)).getCalendar());
            currentCal.setOffsetByDate(timeSlotView.getIndexInView());
            timeSlotView.getNewCalendar().setDay(currentCal.getDay());
            timeSlotView.getNewCalendar().setMonth(currentCal.getMonth());
            timeSlotView.getNewCalendar().setYear(currentCal.getYear());

            if (onTimeSlotOuterListener != null){
                onTimeSlotOuterListener.onTimeSlotDragDrop(timeSlotView, timeSlotView.getNewStartTime(), timeSlotView.getNewEndTime());
            }
        }
    }

    private class OnBodyInnerListener implements FlexibleLenViewBody.OnBodyListener{
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
            MyCalendar currentCal = (adapter.getViewBodyByPosition(bodyCurrentPosition)).getCalendar();
            MyCalendar eventNewCal = new MyCalendar(currentCal);
            eventNewCal.setOffsetByDate(eventView.getIndexInView());
            eventView.getNewCalendar().setDay(eventNewCal.getDay());
            eventView.getNewCalendar().setMonth(eventNewCal.getMonth());
            eventView.getNewCalendar().setYear(eventNewCal.getYear());

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
            MyCalendar currentCal = (adapter.getViewBodyByPosition(bodyCurrentPosition)).getCalendar();
            MyCalendar eventNewCal = new MyCalendar(currentCal);

            eventNewCal.setOffsetByDate(eventView.getIndexInView());
            eventView.getNewCalendar().setDay(eventNewCal.getDay());
            eventView.getNewCalendar().setMonth(eventNewCal.getMonth());
            eventView.getNewCalendar().setYear(eventNewCal.getYear());

            if (OnBodyOuterListener != null){OnBodyOuterListener.onEventDragDrop(eventView);}
        }

//        @Override
//        public ViewTreeObserver.OnScrollChangedListener setScrollChangeListener() {
//            return null;
//        }

        private void bodyAutoSwipe(DayDraggableEventView eventView, int x, int y){
            int offset = x > (parentWidth * 0.85) ? 1 : (x <= parentWidth * 0.05 ? -1 : 0);
            if (offset != 0){
                int scrollTo = bodyCurrentPosition + offset;
                weekViewPager.setCurrentItem(scrollTo,true);
            }
        }
    }

    /**
     * For creating instance,
     * @param className
     * @param <E>
     */
    public <E extends ITimeEventInterface> void setEventClassName(Class<E> className){

        for (FlexibleLenViewBody view: bodyViewList){
            view.setEventClassName(className);
        }
    }

    public void enableTimeSlot(){
        if (adapter != null){
            adapter.enableTimeSlot();
        }
    }

    public void addTimeSlot(ITimeTimeSlotInterface slotInfo){
        slotsInfo.add(slotInfo);
        if (adapter != null){
            adapter.notifyDataSetChanged();
        }
    }

    public void removeAllOptListener(){
        if (adapter != null){
            adapter.removeAllOptListener();
        }
    }

    public void resetTimeSlots(){
//        slotsInfo.clear();
        reloadTimeSlots(false);
    }

    public void updateTimeSlotsDuration(long duration, boolean animate){
        if (adapter != null){
            adapter.updateTimeSlotsDuration(duration,animate);
        }
    }

//    public static class TimeSlotStruct{
//        public long startTime = 0;
//        public long endTime = 0;
//        public boolean status = false;
//        public Object object = null;
//    }

    /**
     * Interface for header changing
     */
    public interface OnHeaderListener{
        void onMonthChanged(MyCalendar calendar);
    }

}


