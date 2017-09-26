package org.unimelb.itime.vendor.weekview;

import android.content.Context;
import android.content.res.TypedArray;
import android.databinding.BindingMethod;
import android.databinding.BindingMethods;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import org.unimelb.itime.vendor.R;
import org.unimelb.itime.vendor.dayview.EventController;
import org.unimelb.itime.vendor.dayview.FlexibleLenBodyViewPager;
import org.unimelb.itime.vendor.dayview.FlexibleLenViewBody;
import org.unimelb.itime.vendor.dayview.TimeSlotController;
import org.unimelb.itime.vendor.unitviews.DraggableEventView;
import org.unimelb.itime.vendor.unitviews.RecommendedSlotView;
import org.unimelb.itime.vendor.unitviews.TimeSlotInnerCalendarView;
import org.unimelb.itime.vendor.util.DensityUtil;
import org.unimelb.itime.vendor.util.MyCalendar;
import org.unimelb.itime.vendor.listener.ITimeEventInterface;
import org.unimelb.itime.vendor.listener.ITimeEventPackageInterface;
import org.unimelb.itime.vendor.listener.ITimeTimeSlotInterface;
import org.unimelb.itime.vendor.unitviews.DraggableTimeSlotView;
import org.unimelb.itime.vendor.wrapper.WrapperTimeSlot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static android.support.v4.view.ViewPager.SCROLL_STATE_IDLE;

/**
 * Created by yuhaoliu on 10/08/16.
 */

@BindingMethods(
        {
                @BindingMethod(type = WeekView.class, attribute = "weekView:onWeekViewDayChange", method="setOnHeaderListener"),
                @BindingMethod(type = WeekView.class, attribute = "weekView:onTimeSlotOuterListener", method = "setOnTimeSlotOuterListener"),
                @BindingMethod(type = WeekView.class, attribute = "weekView:addTimeSlot" ,method = "addTimeSlot"),
                @BindingMethod(type = WeekView.class, attribute = "weekView:weekViewBackToToday" ,method = "backToToday")
        }
)
public class WeekView extends FrameLayout {

    private int displayLength = 3;

    public static final int TIMESLOT_KEEP_SHOW = 1;
    public static final int TIMESLOT_KEEP_HIDE = -1;
    public static final int TIMESLOT_AUTO = 0;

    private final DisplayMetrics dm = getResources().getDisplayMetrics();

    private int upperBoundsOffset = 1;
    private int bodyCurrentPosition;
    private int bodyPagerCurrentState = 0;

    private ArrayList<WeekViewHeader> headerViewList;
    private ArrayList<FlexibleLenViewBody> bodyViewList;
    private ArrayList<WeekViewUnit> weekViewList;
    private ArrayList<WrapperTimeSlot> slotsInfo = new ArrayList<>();


    private FrameLayout staticLayer;
    private TimeSlotInnerCalendarView innerCalView;
    private FlexibleLenBodyViewPager weekViewPager;
    private WeekViewPagerAdapter adapter;
    private ITimeEventPackageInterface eventPackage;

    private EventController.OnEventListener OnBodyOuterListener;
    private TimeSlotController.OnTimeSlotListener onTimeSlotOuterListener;
    private OnHeaderListener onHeaderListener;
    private OnFlexScroll onFlexScroll;
    private ViewTreeObserver.OnScrollChangedListener onScrollChangedListener;

    private MyCalendar monthDayViewCalendar = new MyCalendar(Calendar.getInstance());

    private SimpleDateFormat slotFmt = new SimpleDateFormat("yyyyMMdd");
    private HashMap<String, Integer> numSlotMap = new HashMap<>();

    private Context context;

    private int headerHeight;

    public WeekView(Context context, int displayLength) {
        super(context);
        this.displayLength = displayLength;
        initView();
    }

    public WeekView(Context context, AttributeSet attrs) {
        super(context, attrs);
        loadAttributes(attrs, context);
        initView();
    }

    public WeekView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        loadAttributes(attrs, context);
        initView();
    }

    private void loadAttributes(AttributeSet attrs, Context context) {
        if (attrs != null && context != null) {
            TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.dayStyle, 0, 0);
            try {
                displayLength = typedArray.getInteger(R.styleable.dayStyle_display_length, displayLength);
            } finally {
                typedArray.recycle();
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        if (this.onScrollChangedListener != null){
            this.getViewTreeObserver().removeOnScrollChangedListener(onScrollChangedListener);
        }
        super.onDetachedFromWindow();
    }

    /***************************************************************************
     * Public methods Part, providing the methods of controlling for WeekView
     ***************************************************************************/

    public void setDayEventMap(ITimeEventPackageInterface eventPackage){
        this.eventPackage = eventPackage;
        if (adapter != null){
            adapter.setDayEventMap(this.eventPackage);
        }else {
            Log.i("Debug", "adapter: null" );
        }
        this.reloadEvents();
    }

    public void reloadEvents(){
        adapter.reloadEvents();
    }

    public void reloadTimeSlots(boolean animate){
        adapter.reloadTimeSlots(animate);
        this.updateNumTimeslotMap();
    }

    public void deleteTimeslot(ITimeTimeSlotInterface timeslot){
        for (WrapperTimeSlot wrapper:slotsInfo
             ) {
            ITimeTimeSlotInterface slot = wrapper.getTimeSlot();
            if (slot != null && slot.getTimeslotUid().equals(timeslot.getTimeslotUid())){
                slotsInfo.remove(wrapper);
                this.reloadTimeSlots(false);
                this.updateNumTimeslotMap();
                break;
            }
        }
    }

    public void backToToday(){
        weekViewPager.setCurrentItem(upperBoundsOffset,false);
    }

    public void scrollTo(final Calendar toDate){
        if (this.getHeight() == 0){
            ViewTreeObserver vto = this.getViewTreeObserver();
            final ViewGroup self = this;
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    self.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    weekViewPager.setCurrentItem(upperBoundsOffset + getRowDiff(toDate),false);
                    innerCalView.setMonthTitle(monthDayViewCalendar.getCalendar());
                    innerCalView.setCurrentDate(monthDayViewCalendar.getCalendar().getTime());
                }
            });
        }else{
            weekViewPager.setCurrentItem(upperBoundsOffset + getRowDiff(toDate),false);
            innerCalView.setMonthTitle(monthDayViewCalendar.getCalendar());
            innerCalView.setCurrentDate(monthDayViewCalendar.getCalendar().getTime());
        }
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

    public <T extends ITimeTimeSlotInterface> void showTimeslotAnim(final T ... timeslots){

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

    public void enableTimeSlot(){
        if (adapter != null){
            adapter.enableTimeSlot();
        }

        //staticLayer become visible
        staticLayer.setVisibility(VISIBLE);
    }

    public void addTimeSlot(ITimeTimeSlotInterface slotInfo){
        WrapperTimeSlot wrapper = new WrapperTimeSlot(slotInfo);
        addSlotToList(wrapper);
        if (adapter != null){
            adapter.notifyDataSetChanged();
        }
    }

    public void addTimeSlot(WrapperTimeSlot wrapperTimeSlot){
        addSlotToList(wrapperTimeSlot);
        if (adapter != null){
            adapter.notifyDataSetChanged();
        }
    }

    public void addTimeSlot(ITimeTimeSlotInterface slotInfo, boolean isSelected){
        WrapperTimeSlot wrapper = new WrapperTimeSlot(slotInfo);
        wrapper.setSelected(isSelected);
        addSlotToList(wrapper);
        if (adapter != null){
            adapter.notifyDataSetChanged();
        }
    }

    private void addSlotToList(WrapperTimeSlot wrapperSlot){
        slotsInfo.add(wrapperSlot);
        updateNumTimeslotMap();
    }

    private void updateNumTimeslotMap(){
        numSlotMap.clear();
        for (WrapperTimeSlot wrapper:slotsInfo
             ) {
            if (wrapper.getTimeSlot() != null && wrapper.isSelected()){
                String strDate = slotFmt.format(new Date(wrapper.getTimeSlot().getStartTime()));
                if (this.numSlotMap.containsKey(strDate)){
                    numSlotMap.put(strDate, numSlotMap.get(strDate) + 1);
                }else {
                    numSlotMap.put(strDate,1);
                }
            }
        }
        innerCalView.refreshSlotNum();
    }

    public void resetTimeSlots(){
        slotsInfo.clear();
        numSlotMap.clear();
        reloadTimeSlots(false);
    }

    public void updateTimeSlotsDuration(long duration, boolean animate){
        if (adapter != null){
            adapter.updateTimeSlotsDuration(duration,animate);
        }
    }

    public void setArrowVisibility(int l, int t, int r, int b){
        for (FlexibleLenViewBody body:bodyViewList
             ) {
            body.leftArrowVisibility = l;
            body.topArrowVisibility = t;
            body.rightArrowVisibility = r;
            body.bottomArrowVisibility = b;
        }
    }

    public void removeAllOptListener(){
        if (adapter != null){
            adapter.removeAllOptListener();
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

//    public void setOnRcdTimeSlot(OnRcdTimeSlot onRcdTimeSlot) {
//        this.onRcdTimeSlot = onRcdTimeSlot;
//        for (FlexibleLenViewBody body:bodyViewList
//             ) {
//            body.setOnRcdTimeSlot(onRcdTimeSlot);
//        }
//    }

    /***************************************************************************
     * Listener Part, Including onHeaderListener, OnBodyOuterListener
     ***************************************************************************/

    public void setOnTimeSlotInnerCalendar(TimeSlotInnerCalendarView.OnTimeSlotInnerCalendar onTimeSlotInnerCalendar) {
        this.innerCalView.setOnTimeSlotInnerCalendar(onTimeSlotInnerCalendar);
    }

    public void setOnHeaderListener(OnHeaderListener onHeaderListener){
        this.onHeaderListener = onHeaderListener;
    }

    public void setOnBodyOuterListener(EventController.OnEventListener onBodyOuterListener){
        this.OnBodyOuterListener = onBodyOuterListener;
    }

    public void setOnTimeSlotOuterListener(TimeSlotController.OnTimeSlotListener onTimeSlotOuterListener){
        this.onTimeSlotOuterListener = onTimeSlotOuterListener;
    }

    public void setOnFlexScroll(OnFlexScroll onFlexScroll) {
        this.onFlexScroll = onFlexScroll;
    }

    private class OnTimeSlotInnerListener implements TimeSlotController.OnTimeSlotListener{
        @Override
        public void onTimeSlotCreate(DraggableTimeSlotView draggableTimeSlotView) {
            MyCalendar currentCal = new MyCalendar((adapter.getViewBodyByPosition(bodyCurrentPosition)).getCalendar());
            currentCal.setOffsetByDate(draggableTimeSlotView.getIndexInView());
            draggableTimeSlotView.getNewCalendar().setDay(currentCal.getDay());
            draggableTimeSlotView.getNewCalendar().setMonth(currentCal.getMonth());
            draggableTimeSlotView.getNewCalendar().setYear(currentCal.getYear());

            if (onTimeSlotOuterListener != null){
                onTimeSlotOuterListener.onTimeSlotCreate(draggableTimeSlotView);
            }
        }

        @Override
        public void onTimeSlotClick(DraggableTimeSlotView draggableTimeSlotView) {
            if (onTimeSlotOuterListener != null){
                onTimeSlotOuterListener.onTimeSlotClick(draggableTimeSlotView);
            }
        }

        @Override
        public void onRcdTimeSlotClick(RecommendedSlotView v) {
            if (onTimeSlotOuterListener != null){
                onTimeSlotOuterListener.onRcdTimeSlotClick(v);
            }
        }

        @Override
        public void onTimeSlotDragStart(DraggableTimeSlotView draggableTimeSlotView) {
            if (onTimeSlotOuterListener != null){
                onTimeSlotOuterListener.onTimeSlotDragStart(draggableTimeSlotView);
            }
        }

        @Override
        public void onTimeSlotDragging(DraggableTimeSlotView draggableTimeSlotView, int x, int y) {
            if (onTimeSlotOuterListener != null){
                onTimeSlotOuterListener.onTimeSlotDragging(draggableTimeSlotView, x, y);
            }
        }

        @Override
        public void onTimeSlotDragDrop(DraggableTimeSlotView draggableTimeSlotView, long start, long end) {
            MyCalendar currentCal = new MyCalendar((adapter.getViewBodyByPosition(bodyCurrentPosition)).getCalendar());
            currentCal.setOffsetByDate(draggableTimeSlotView.getIndexInView());
            draggableTimeSlotView.getNewCalendar().setDay(currentCal.getDay());
            draggableTimeSlotView.getNewCalendar().setMonth(currentCal.getMonth());
            draggableTimeSlotView.getNewCalendar().setYear(currentCal.getYear());

            if (onTimeSlotOuterListener != null){
                onTimeSlotOuterListener.onTimeSlotDragDrop(draggableTimeSlotView, draggableTimeSlotView.getNewStartTime(), draggableTimeSlotView.getNewEndTime());
            }
        }

        @Override
        public void onTimeSlotEdit(DraggableTimeSlotView draggableTimeSlotView) {
            if (onTimeSlotOuterListener != null){
                onTimeSlotOuterListener.onTimeSlotEdit(draggableTimeSlotView);
            }
        }

        @Override
        public void onTimeSlotDelete(DraggableTimeSlotView draggableTimeSlotView) {
            if (onTimeSlotOuterListener != null){
                onTimeSlotOuterListener.onTimeSlotDelete(draggableTimeSlotView);
            }
        }
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
            MyCalendar currentCal = (adapter.getViewBodyByPosition(bodyCurrentPosition)).getCalendar();
            MyCalendar eventNewCal = new MyCalendar(currentCal);
            eventNewCal.setOffsetByDate(eventView.getIndexInView());
            eventView.getNewCalendar().setDay(eventNewCal.getDay());
            eventView.getNewCalendar().setMonth(eventNewCal.getMonth());
            eventView.getNewCalendar().setYear(eventNewCal.getYear());

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

        private void bodyAutoSwipe(DraggableEventView eventView, int x, int y){
            int offset = x > (parentWidth * 0.85) ? 1 : (x <= parentWidth * 0.05 ? -1 : 0);
            if (offset != 0){
                int scrollTo = bodyCurrentPosition + offset;
                weekViewPager.setCurrentItem(scrollTo,true);
            }
        }
    }

    public interface OnHeaderListener{
        void onMonthChanged(MyCalendar calendar);
    }

    public interface OnFlexScroll{
        void onScroll(long currentTime);
    }

//    public interface OnRcdTimeSlot{
//        void onClick(RecommendedSlotView v);
//    }

    /***************************************************************************
     * Inner private methods block, including function of setting up MonthDayView
     ***************************************************************************/

    private void initView(){
        this.context = getContext();

        bodyViewList = new ArrayList<>();
        this.initBody();
        headerViewList = new ArrayList<>();
        this.initHeader();
        weekViewList = new ArrayList<>();
        this.initWeekViews();

        this.setUpWeekView();
        this.setUpStaticLayer();
        //set to initial position
        weekViewPager.setCurrentItem(upperBoundsOffset);
    }

    private void initHeader(){
        int size = 4;
        int padding = DensityUtil.dip2px(context,0);
        headerHeight = DensityUtil.dip2px(getContext(),50);

        //must be consistent with width of left bar in body part.
        for (int i = 0; i < size; i++) {
            WeekViewHeader headerView = new WeekViewHeader(context, displayLength);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, headerHeight);
            params.gravity = Gravity.CENTER_VERTICAL;
            headerView.setLayoutParams(params);
            headerView.setPadding(0,padding,0,padding);
            headerViewList.add(headerView);
        }
    }

    private void initBody(){
        int size = 4;
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTime(new Date());
//        int day_of_week = calendar.get(Calendar.DAY_OF_WEEK) - 1;
//        calendar.set(Calendar.DAY_OF_MONTH,calendar.get(Calendar.DAY_OF_MONTH) - day_of_week);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        for (int i = 0; i < size; i++) {
            FlexibleLenViewBody bodyView = new FlexibleLenViewBody(context, displayLength);
            bodyView.setLayoutParams(new LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            bodyView.setCalendar(new MyCalendar(calendar));
            bodyView.setOnBodyListener(new OnEventInnerListener());
            bodyView.setOnTimeSlotListener(new OnTimeSlotInnerListener());
            bodyViewList.add(bodyView);
        }

        ViewTreeObserver vto = this.getViewTreeObserver();
        vto.addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                //if detached remove this listener
                if(onScrollChangedListener == null){
                    onScrollChangedListener = this;
                }

                FlexibleLenViewBody currentShow = adapter.getViewBodyByPosition(weekViewPager.getCurrentItem());

                //scroll listener
                Calendar nowTime = currentShow.getCurrentTime();
                ScrollView scroller = currentShow.getScrollView();
                if (nowTime != null && onFlexScroll != null){
                    onFlexScroll.onScroll(nowTime.getTimeInMillis());
                }

                //for scrolling end
                if (bodyPagerCurrentState == SCROLL_STATE_IDLE){
                    currentShow.timeSlotAnimationChecker();
                }

                if (currentShow.getScrollView() == scroller){
                    final int scrollY = scroller.getScrollY(); // For ScrollView

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
            }
        });
    }

    private void initWeekViews(){
        int size = 4;
        for (int i = 0; i < size; i++) {
            WeekViewUnit weekView = new WeekViewUnit(context);
            weekView.setHeader(this.headerViewList.get(i));
            weekView.setDivider(this.getDivider());
            weekView.setBody(this.bodyViewList.get(i));
            this.weekViewList.add(weekView);
        }
    }

    private void setUpWeekView(){
        //set up pager
        weekViewPager = new FlexibleLenBodyViewPager(context);
        weekViewPager.setScrollDurationFactor(3);
        upperBoundsOffset = 500;
        bodyCurrentPosition = upperBoundsOffset;
        adapter = new WeekViewPagerAdapter(upperBoundsOffset, weekViewList, displayLength);
        if (this.eventPackage != null){
            adapter.setDayEventMap(this.eventPackage);
        }
        adapter.setSlotsInfo(this.slotsInfo);
        weekViewPager.setAdapter(adapter);
        this.addView(weekViewPager,new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        //init pager listener
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
                    //reset timeslot visibility to hidden
                    currentShow.resetTimeSlotViews();
                    currentShow.timeSlotAnimationChecker();
                }
            }
        });
    }

    private void setUpStaticLayer(){
        //set up static layer
        staticLayer = new FrameLayout(context);
        FrameLayout.LayoutParams stcPageParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        innerCalView = new TimeSlotInnerCalendarView(context);
        innerCalView.setHeaderHeight(headerHeight);
        innerCalView.setSlotNumMap(numSlotMap);

        FrameLayout.LayoutParams innerCalViewParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        this.staticLayer.addView(innerCalView,innerCalViewParams);

        staticLayer.setVisibility(GONE);
        this.addView(staticLayer, stcPageParams);
    }

    private int getRowDiff(Calendar body_fst_cal){
        MyCalendar tempNow = new MyCalendar(Calendar.getInstance());
        MyCalendar tempCompared = new MyCalendar(body_fst_cal);
        tempNow.setToSameBeginOfDay(tempCompared);

        long tT = tempNow.getCalendar().getTimeInMillis();
        long cT = tempCompared.getCalendar().getTimeInMillis();

        int rowDiff = (int)Math.floor(((float)(cT - tT) / (1000*60*60*24))/displayLength);

        return rowDiff;
    }

    private ImageView getDivider() {
        ImageView dividerImgV;
        //divider
        dividerImgV = new ImageView(context);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dividerImgV.setLayoutParams(params);
        dividerImgV.setImageDrawable(getResources().getDrawable(org.unimelb.itime.vendor.R.drawable.itime_header_divider_line));

        return dividerImgV;
    }

    private WrapperTimeSlot getTimeSlotWrapper(ITimeTimeSlotInterface slotInterface){
        for (WrapperTimeSlot wrapper:slotsInfo
                ) {
            if (wrapper.getTimeSlot() != null && wrapper.getTimeSlot().getTimeslotUid().equals(slotInterface.getTimeslotUid())){
                return  wrapper;
            }
        }

        return null;
    }

}


