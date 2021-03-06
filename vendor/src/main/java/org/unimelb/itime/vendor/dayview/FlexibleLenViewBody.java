package org.unimelb.itime.vendor.dayview;

import android.content.ClipData;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.util.TypedValue;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.unimelb.itime.vendor.R;
import org.unimelb.itime.vendor.eventview.DayDraggableEventView;
import org.unimelb.itime.vendor.helper.VendorAnimation;
import org.unimelb.itime.vendor.helper.CalendarEventOverlapHelper;
import org.unimelb.itime.vendor.helper.DensityUtil;
import org.unimelb.itime.vendor.helper.MyCalendar;
import org.unimelb.itime.vendor.listener.ITimeEventInterface;
import org.unimelb.itime.vendor.listener.ITimeEventPackageInterface;
import org.unimelb.itime.vendor.listener.ITimeTimeSlotInterface;
import org.unimelb.itime.vendor.timeslot.TimeSlotView;
import org.unimelb.itime.vendor.weekview.WeekView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by yuhaoliu on 3/08/16.
 */
public class FlexibleLenViewBody extends RelativeLayout {
    public final String TAG = "MyAPP";
    private final long allDayMilliseconds = 24 * 60 * 60 * 1000;

    private boolean isTimeSlotEnable = false;
    private boolean isRemoveOptListener = false;

    private ScrollContainerView scrollContainerView;
    private RelativeLayout bodyContainerLayout;
    private RelativeLayout animationLayout;

    private LinearLayout topAllDayLayout;
    private LinearLayout topAllDayEventLayouts;

    private RelativeLayout leftSideRLayout;
    private RelativeLayout rightContentLayout;

    private RelativeLayout dividerBgRLayout;
    private LinearLayout eventLayout;

    public MyCalendar myCalendar;
    private Context context;

    private ArrayList<DayDraggableEventView> allDayDgEventViews = new ArrayList<>();

    private ArrayList<DayInnerHeaderEventLayout> allDayEventLayouts = new ArrayList<>();
    private ArrayList<DayInnerBodyEventLayout> eventLayouts = new ArrayList<>();

    private TreeMap<Integer, String> positionToTimeTreeMap = new TreeMap<>();
    private TreeMap<Float, Integer> timeToPositionTreeMap = new TreeMap<>();
    private Map<ITimeEventInterface, Integer> regularEventViewMap = new HashMap<>();
    private Map<String, DayDraggableEventView> uidDragViewMap = new HashMap<>();

    private CalendarEventOverlapHelper xHelper = new CalendarEventOverlapHelper();

    private TextView msgWindow;
    private TextView nowTime;
    private ImageView nowTimeLine;
    //tag: false-> moving, true, done
    private View tempDragView = null;

    private int leftSideWidth = 40;
    private int lineHeight = 50;
    private int timeTextSize = 20;
    private int overlapGapHeight;
    private int layoutWidthPerDay;
    private int layoutHeightPerDay;

    private int displayLen = 7;

    private float nowTapX = 0;
    private float nowTapY = 0;
    private float fstEventPosY = 0;

    private OnBodyTouchListener onBodyTouchListener;
    private OnBodyListener onBodyListener;

    private float heightPerMillisd = 0;

    private ViewTreeObserver.OnScrollChangedListener onScrollChangeListener;

    private ImageView leftArrow;
    private ImageView rightArrow;
    private ImageView topArrow;
    private ImageView bottomArrow;

    public FlexibleLenViewBody(Context context, int displayLen) {
        super(context);
        this.context = context;
        this.displayLen = displayLen;
        initLayoutParams();
        init();
    }

    public FlexibleLenViewBody(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initLayoutParams();
        loadAttributes(attrs, context);
        init();
    }

    public FlexibleLenViewBody(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initLayoutParams();
        loadAttributes(attrs, context);
        init();
    }

    private void init(){
        initViews();
        initBackgroundView();
        initAnimations();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        layoutWidthPerDay = MeasureSpec.getSize(eventLayout.getMeasuredWidth()/displayLen);
        int cCount = getChildCount();
        for (int i = 0; i < cCount; i++) {
            measureChildren(widthMeasureSpec,heightMeasureSpec);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
    }

    private void initLayoutParams(){
        this.overlapGapHeight = DensityUtil.dip2px(context, 1);
        this.lineHeight = DensityUtil.dip2px(context, lineHeight);
        this.heightPerMillisd = (float)lineHeight/(3600*1000);
        this.leftSideWidth = DensityUtil.dip2px(context,leftSideWidth);
    }

    private void initViews() {
        this.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        scrollContainerView = new ScrollContainerView(context);
        scrollContainerView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        this.addView(scrollContainerView);

        bodyContainerLayout = new RelativeLayout(context);
        bodyContainerLayout.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        scrollContainerView.addView(bodyContainerLayout);

        topAllDayLayout = new LinearLayout(getContext());
        topAllDayLayout.setBackgroundColor(Color.parseColor("#EBEBEB"));
        topAllDayLayout.setId(View.generateViewId());
        RelativeLayout.LayoutParams topAllDayLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        topAllDayLayout.setLayoutParams(topAllDayLayoutParams);

        ImageView dividerTop = getDivider();
        ((RelativeLayout.LayoutParams) dividerTop.getLayoutParams()).addRule(ALIGN_PARENT_TOP);
        this.addView(dividerTop);

        ImageView dividerBottom = getDivider();
        ((RelativeLayout.LayoutParams) dividerBottom.getLayoutParams()).addRule(ALIGN_BOTTOM, topAllDayLayout.getId());
        bodyContainerLayout.addView(dividerBottom);

        TextView allDayTitleTv = new TextView(context);
        LinearLayout.LayoutParams allDayTitleTvParams = new LinearLayout.LayoutParams(leftSideWidth, ViewGroup.LayoutParams.MATCH_PARENT);
        int allDayTitleTvPadding = DensityUtil.dip2px(context, 3);
        allDayTitleTv.setPadding(allDayTitleTvPadding, allDayTitleTvPadding, allDayTitleTvPadding, allDayTitleTvPadding);
        allDayTitleTv.setTextSize(10);
        allDayTitleTv.setText("All Day");
        allDayTitleTv.setTextColor(context.getResources().getColor(R.color.text_enable));
        allDayTitleTv.setGravity(Gravity.CENTER_VERTICAL);
        allDayTitleTv.setLayoutParams(allDayTitleTvParams);
        allDayTitleTv.measure(0,0);
        leftSideWidth = allDayTitleTv.getMeasuredWidth();
        topAllDayLayout.addView(allDayTitleTv);

        topAllDayEventLayouts = new LinearLayout(getContext());
        int topAllDayEventLayoutsPadding = DensityUtil.dip2px(context, 3);
        topAllDayEventLayouts.setPadding(0,topAllDayEventLayoutsPadding,0,topAllDayEventLayoutsPadding);

        topAllDayEventLayouts.setId(View.generateViewId());
        LinearLayout.LayoutParams topAllDayEventLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DensityUtil.dip2px(context, 40));
        topAllDayEventLayouts.setLayoutParams(topAllDayEventLayoutParams);
        this.initInnerHeaderEventLayouts(topAllDayEventLayouts);
        topAllDayLayout.addView(topAllDayEventLayouts);

        leftSideRLayout = new RelativeLayout(getContext());
        leftSideRLayout.setId(View.generateViewId());
        RelativeLayout.LayoutParams leftSideRLayoutParams = new RelativeLayout.LayoutParams(leftSideWidth, ViewGroup.LayoutParams.MATCH_PARENT);
        leftSideRLayoutParams.addRule(RelativeLayout.BELOW, topAllDayLayout.getId());
        leftSideRLayout.setGravity(Gravity.CENTER);
        leftSideRLayout.setPadding(0, 0, 0, 0);
        leftSideRLayout.setLayoutParams(leftSideRLayoutParams);

        rightContentLayout = new RelativeLayout(getContext());
        rightContentLayout.setId(View.generateViewId());
        RelativeLayout.LayoutParams rightContentLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        rightContentLayoutParams.addRule(RelativeLayout.BELOW, topAllDayLayout.getId());
        rightContentLayoutParams.addRule(RelativeLayout.RIGHT_OF, leftSideRLayout.getId());
        rightContentLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        rightContentLayoutParams.addRule(RelativeLayout.ALIGN_TOP, leftSideRLayout.getId());
        rightContentLayoutParams.addRule(RelativeLayout.ALIGN_BOTTOM, leftSideRLayout.getId());
        rightContentLayout.setLayoutParams(rightContentLayoutParams);

        dividerBgRLayout = new RelativeLayout(getContext());
//        dividerBgRLayout.setBackgroundColor(Color.parseColor("#80000000"));
        dividerBgRLayout.setId(View.generateViewId());
        RelativeLayout.LayoutParams dividerBgRLayoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dividerBgRLayout.setLayoutParams(dividerBgRLayoutParams);
        rightContentLayout.addView(dividerBgRLayout);

        eventLayout = new LinearLayout(getContext());
        eventLayout.setId(View.generateViewId());
        eventLayout.setOrientation(LinearLayout.HORIZONTAL);
        RelativeLayout.LayoutParams eventLayoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        eventLayoutParams.addRule(ALIGN_LEFT, rightContentLayout.getId());
//        eventLayoutParams.addRule(ALIGN_RIGHT, rightContentLayout.getId());
        eventLayoutParams.addRule(ALIGN_TOP, rightContentLayout.getId());
//        eventLayoutParams.addRule(ALIGN_BOTTOM, rightContentLayout.getId());
        this.initInnerBodyEventLayouts(eventLayout);
        eventLayout.setLayoutParams(eventLayoutParams);

        rightContentLayout.addView(eventLayout);
//        rightContentLayout.addView(animationLayout);

        bodyContainerLayout.addView(topAllDayLayout);
        bodyContainerLayout.addView(leftSideRLayout);
        bodyContainerLayout.addView(rightContentLayout);

        animationLayout = new RelativeLayout(context);
        RelativeLayout.LayoutParams animationLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        animationLayout.setLayoutParams(animationLayoutParams);
        this.addView(animationLayout);

//        dividerBgRLayout.bringToFront();
        dividerTop.bringToFront();
        dividerBottom.bringToFront();
    }

    private void initAnimations(){
        initTimeSlotArrow();
    }

    public void resetAnimationViews(){
        leftArrow.setVisibility(INVISIBLE);
        rightArrow.setVisibility(INVISIBLE);
        topArrow.setVisibility(INVISIBLE);
        bottomArrow.setVisibility(INVISIBLE);
    }

    private void initTimeSlotArrow(){
        int width = DensityUtil.dip2px(context,30);

        leftArrow = new ImageView(context);
        leftArrow.setImageDrawable(getResources().getDrawable(R.drawable.icon_timeslot_arrow));
        RelativeLayout.LayoutParams leftParams = new RelativeLayout.LayoutParams(width, width);
        leftParams.addRule(ALIGN_PARENT_LEFT);
        leftParams.addRule(CENTER_VERTICAL);
        leftParams.leftMargin = leftSideWidth;
        leftArrow.setScaleType(ImageView.ScaleType.FIT_CENTER);
        leftArrow.setLayoutParams(leftParams);
        leftArrow.setPivotX(width/2);
        leftArrow.setPivotY(width/2);
        leftArrow.setRotation(180);
        leftArrow.setVisibility(INVISIBLE);

        rightArrow = new ImageView(context);
        rightArrow.setImageDrawable(getResources().getDrawable(R.drawable.icon_timeslot_arrow));
        RelativeLayout.LayoutParams rightParams = new RelativeLayout.LayoutParams(width, width);
        rightParams.addRule(ALIGN_PARENT_RIGHT);
        rightParams.addRule(CENTER_VERTICAL);
        rightArrow.setScaleType(ImageView.ScaleType.FIT_CENTER);
        rightArrow.setLayoutParams(rightParams);
        rightArrow.setVisibility(INVISIBLE);

        topArrow = new ImageView(context);
        topArrow.setImageDrawable(getResources().getDrawable(R.drawable.icon_timeslot_arrow));
        RelativeLayout.LayoutParams topParams = new RelativeLayout.LayoutParams(width, width);
        topParams.addRule(ALIGN_PARENT_TOP);
        topParams.addRule(CENTER_HORIZONTAL);
        topArrow.setLayoutParams(topParams);
        topArrow.setPivotX(width/2);
        topArrow.setPivotY(width/2);
        topArrow.setRotation(-90);
        topArrow.setVisibility(INVISIBLE);

        bottomArrow = new ImageView(context);
        bottomArrow.setImageDrawable(getResources().getDrawable(R.drawable.icon_timeslot_arrow));
        RelativeLayout.LayoutParams bottomParams = new RelativeLayout.LayoutParams(width, width);
        bottomParams.addRule(ALIGN_PARENT_BOTTOM);
        bottomParams.addRule(CENTER_HORIZONTAL);
        bottomArrow.setLayoutParams(bottomParams);
        bottomArrow.setPivotX(width/2);
        bottomArrow.setPivotY(width/2);
        bottomArrow.setRotation(90);
        bottomArrow.setVisibility(INVISIBLE);

        if (this.animationLayout != null){
            animationLayout.addView(leftArrow);
            animationLayout.addView(rightArrow);
            animationLayout.addView(topArrow);
            animationLayout.addView(bottomArrow);
        }
    }

    private void initInnerHeaderEventLayouts(LinearLayout parent){
        for (int i = 0; i < displayLen; i++) {
            DayInnerHeaderEventLayout allDayEventLayout = new DayInnerHeaderEventLayout(context);
//            allDayEventLayout.setBackgroundColor(getResources().getColor(R.color.allday_container_bg));
            int allDayEventLayoutPadding = DensityUtil.dip2px(context, 1);
            allDayEventLayout.setPadding(allDayEventLayoutPadding,0,allDayEventLayoutPadding,0);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT,1f);
            parent.addView(allDayEventLayout,params);

            allDayEventLayouts.add(allDayEventLayout);
        }
    }

    private void initInnerBodyEventLayouts(LinearLayout parent){
        for (int i = 0; i < displayLen; i++) {
            DayInnerBodyEventLayout eventLayout = new DayInnerBodyEventLayout(context);
            eventLayout.setBackgroundColor(getResources().getColor(displayLen == 1 ? R.color.color_75white : (i%2 == 0 ? R.color.color_f2f2f5 : R.color.color_75white)));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT,1f);
            int eventLayoutPadding = DensityUtil.dip2px(context, 1);
            eventLayout.setPadding(eventLayoutPadding,0,eventLayoutPadding,0);
            parent.addView(eventLayout,params);
            if (!isTimeSlotEnable){
                eventLayout.setOnDragListener(new EventDragListener(i));
                eventLayout.setOnLongClickListener(new CreateEventListener());
            }else {
                eventLayout.setOnDragListener(new TimeSlotDragListener(i));
                eventLayout.setOnLongClickListener(new CreateTimeSlotListener());
            }

            eventLayouts.add(eventLayout);
        }

    }

    public void timeSlotAnimationChecker(){
        Rect scrollBounds = new Rect();
        scrollContainerView.getHitRect(scrollBounds);

        for (int i = 0; i < slotViews.size(); i++) {
            TimeSlotView slotview = slotViews.get(i);

            if (slotview.getWindowToken() != null){
                if (slotview.getLocalVisibleRect(scrollBounds)) {
                    //showing
                    Log.i(TAG, "timeSlotAnimationChecker: ");
                } else {
                    //hiding
                    if (scrollBounds.bottom <= 0){
                        if (topArrow.getVisibility() != VISIBLE)
                            topArrow.startAnimation(VendorAnimation.getInstance().getFadeInAnim());
                    }else{
                        if (bottomArrow.getVisibility() != VISIBLE){
                            bottomArrow.startAnimation(VendorAnimation.getInstance().getFadeInAnim());
                        }

                    }
                }
            }
        }
    }

    private ImageView getDivider() {
        ImageView dividerImgV;
        //divider
        dividerImgV = new ImageView(context);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dividerImgV.setLayoutParams(params);
        dividerImgV.setImageDrawable(getResources().getDrawable(org.unimelb.itime.vendor.R.drawable.itime_header_divider_line));

        return dividerImgV;
    }

    public int getDisplayLen() {
        return displayLen;
    }

    private void loadAttributes(AttributeSet attrs, Context context) {
        if (attrs != null && context != null) {
            TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.dayStyle, 0, 0);
            try {
                displayLen = typedArray.getInteger(R.styleable.dayStyle_display_length,displayLen);
                timeTextSize = typedArray.getDimensionPixelSize(R.styleable.dayStyle_timeTextSize,
                        (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, timeTextSize, context.getResources().getDisplayMetrics()));

            } finally {
                typedArray.recycle();
            }
        }
    }

    public void initBackgroundView() {
        initTimeSlot();
        initMsgWindow();
        initTimeText(getHours());
        initDividerLine(getHours());
    }

    public ScrollView getScrollView(){
        return this.scrollContainerView;
    }

    private void initTimeSlot() {
        double startPoint = timeTextSize * 0.5;
        double timeSlotHeight = lineHeight / 4;
        String[] hours = getHours();
        for (int slot = 0; slot < hours.length; slot++) {
            //add full clock
            positionToTimeTreeMap.put((int) startPoint + lineHeight * slot, hours[slot] + ":00");
            String hourPart = hours[slot].substring(0, 2); // XX
            timeToPositionTreeMap.put((float) Integer.valueOf(hourPart), (int) startPoint + lineHeight * slot);
            for (int miniSlot = 0; miniSlot < 3; miniSlot++) {
                String minutes = String.valueOf((miniSlot + 1) * 15);
                String time = hourPart + ":" + minutes;
                int positionY = (int) (startPoint + lineHeight * slot + timeSlotHeight * (miniSlot + 1));
                positionToTimeTreeMap.put(positionY, time);
                timeToPositionTreeMap.put(Integer.valueOf(hourPart) + (float) Integer.valueOf(minutes) / 100, positionY);
            }
        }
    }

    private void initMsgWindow() {
        msgWindow = new TextView(context);

        msgWindow.setTextColor(context.getResources().getColor(R.color.text_enable));
        msgWindow.setText("SUN 00:00");
        msgWindow.setTextSize(20);
        msgWindow.setGravity(Gravity.CENTER);
        msgWindow.setVisibility(View.INVISIBLE);
        msgWindow.measure(0, 0);
        int height = msgWindow.getMeasuredHeight(); //get height
        int width = msgWindow.getMeasuredWidth();
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width+10, height);
        params.setMargins(0, 0, 0, 0);
        msgWindow.setLayoutParams(params);
        dividerBgRLayout.addView(msgWindow);
    }

    private void initTimeText(String[] HOURS) {
        for (int time = 0; time < HOURS.length; time++) {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            TextView timeView = new TextView(context);
            params.setMargins(0, lineHeight * time, 0, 0);
            timeView.setLayoutParams(params);
            timeView.setTextColor(context.getResources().getColor(R.color.text_enable));
            timeView.setText(HOURS[time]);
            timeView.setTextSize(12);
            timeView.setGravity(Gravity.CENTER);
            timeTextSize = (int) timeView.getTextSize() + timeView.getPaddingTop();
            leftSideRLayout.addView(timeView);
        }
    }

    private void initDividerLine(String[] HOURS) {
        for (int numOfDottedLine = 0; numOfDottedLine < HOURS.length; numOfDottedLine++) {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            ImageView dividerImageView = new ImageView(context);
            dividerImageView.setImageResource(R.drawable.itime_day_view_dotted);
            dividerImageView.setY(this.nearestTimeSlotValue(numOfDottedLine));
            dividerImageView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            dividerImageView.setLayoutParams(params);
            dividerImageView.setPadding(0, 0, 0, 0);
            dividerBgRLayout.addView(dividerImageView);
        }
    }

    public void setCalendar(MyCalendar myCalendar) {
        this.myCalendar = myCalendar;
    }

    public MyCalendar getCalendar() {
        return myCalendar;
    }

    private String[] getHours() {
        String[] HOURS = new String[]{
                "00", "01", "02", "03", "04", "05", "06", "07",
                "08", "09", "10", "11", "12", "13", "14", "15",
                "16", "17", "18", "19", "20", "21", "22", "23",
                "24"
        };

        return HOURS;
    }

    /**
     * reset all the layouts and views in body
     */
    public void resetViews() {
        clearAllEvents();

        bodyContainerLayout.removeView(nowTime);
        bodyContainerLayout.removeView(nowTimeLine);

        MyCalendar tempCal = new MyCalendar(this.myCalendar);
        for (int dayOffset = 0; dayOffset < this.getDisplayLen(); dayOffset++) {
            if (tempCal.isToday()) {
                addNowTimeLine();
                break;
            }
            tempCal.setOffsetByDate(1);
        }

    }

    /**
     * just clear the events in the layout
     */
    public void clearAllEvents() {

        if (this.topAllDayEventLayouts != null) {
            for (DayInnerHeaderEventLayout allDayEventLayout:allDayEventLayouts
                 ) {
                allDayEventLayout.resetView();
            }
        }

        if (this.eventLayout != null) {
            for (DayInnerBodyEventLayout eventLayout:eventLayouts
                 ) {
                eventLayout.resetView();
            }
        }

        this.regularEventViewMap.clear();
        this.allDayDgEventViews.clear();
        this.uidDragViewMap.clear();
    }

    public void addNowTimeLine() {
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        Date currentLocalTime = cal.getTime();
        DateFormat date = new SimpleDateFormat("HH:mm");
        String localTime = date.format(currentLocalTime);

        nowTime = new TextView(context);
        nowTime.setId(View.generateViewId());
        nowTimeLine = new ImageView(context);
        nowTimeLine.setId(View.generateViewId());

        int lineMarin_top = getNowTimeLinePst() + (int) context.getResources().getDimension(R.dimen.all_day_height);
        nowTime.setText(localTime);
        nowTime.setTextSize(10);
        RelativeLayout.LayoutParams paramsText = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paramsText.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        paramsText.addRule(RelativeLayout.ALIGN_BOTTOM, nowTimeLine.getId());
        int textPadding = DensityUtil.dip2px(context, 5);
        nowTime.setPadding(textPadding / 2, 0, textPadding / 2, 0);
        nowTime.setLayoutParams(paramsText);
        nowTime.setTextColor(context.getResources().getColor(R.color.text_today_color));
        nowTime.setBackgroundColor(context.getResources().getColor(R.color.whites));
        bodyContainerLayout.addView(nowTime);

        RelativeLayout.LayoutParams nowTimeLineParams =
                new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT
                        , ViewGroup.LayoutParams.WRAP_CONTENT);
        nowTimeLine.setImageResource(R.drawable.itime_now_time_full_line);
        nowTimeLineParams.topMargin = lineMarin_top;
        nowTimeLineParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        nowTimeLineParams.addRule(RelativeLayout.RIGHT_OF, nowTime.getId());
        nowTimeLine.setLayoutParams(nowTimeLineParams);
        bodyContainerLayout.addView(nowTimeLine);
    }

    private int getNowTimeLinePst() {
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        Date currentLocalTime = cal.getTime();
        DateFormat date = new SimpleDateFormat("HH:mm");
        String localTime = date.format(currentLocalTime);
        String[] converted = localTime.split(":");
        int hour = Integer.valueOf(converted[0]);
        int minutes = Integer.valueOf(converted[1]);
        int nearestPst = nearestTimeSlotValue(hour + (float) minutes / 100); //
        int correctPst = (minutes % 15) * ((lineHeight / 4) / 15);
        return nearestPst + correctPst;
    }

    /**
     * add one event, only called in this class
     *
     * @param event
     */
    private void addEvent(ITimeEventInterface event) {
        boolean isAllDayEvent = isAllDayEvent(event);

        if (isAllDayEvent) {
            addAllDayEvent(event);
        } else {
            addRegularEvent(event);
        }
//        this.msgWindow.bringToFront();
    }

    private void addAllDayEvent(ITimeEventInterface event) {
        int offset = getEventContainerIndex(event.getStartTime());
        if (offset < displayLen) {
            DayDraggableEventView new_dgEvent = this.createDayDraggableEventView(event, true);
            DayInnerHeaderEventLayout allDayEventLayout = this.allDayEventLayouts.get(offset);
            allDayEventLayout.addView(new_dgEvent);
            allDayEventLayout.getDgEvents().add(new_dgEvent);
            allDayEventLayout.getEvents().add(event);
        }else {
            Log.i(TAG, "event in header offset error: " + offset);
        }
    }

    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

    private void addRegularEvent(ITimeEventInterface event) {
        int offset = getEventContainerIndex(event.getStartTime());
        if (offset < displayLen){
            DayInnerBodyEventLayout eventLayout = this.eventLayouts.get(offset);
            DayDraggableEventView newDragEventView = this.createDayDraggableEventView(event, false);
            DayDraggableEventView.LayoutParams params = (DayDraggableEventView.LayoutParams) newDragEventView.getLayoutParams();

            newDragEventView.setId(View.generateViewId());
            this.regularEventViewMap.put(event, newDragEventView.getId());

            eventLayout.addView(newDragEventView, params);
            eventLayout.getEvents().add(event);
            eventLayout.getDgEvents().add(newDragEventView);
        }else {
            Log.i(TAG, "event in body offset error: " + offset);
        }

    }

    private int getEventContainerIndex(long startTime){
        long today = this.myCalendar.getBeginOfDayMilliseconds();
        long dayLong = (24 * 60 * 60 * 1000);

        return (int)(Math.floor((float)(startTime - today)/ dayLong));
//        return (int)((startTime - today)/ dayLong);
    }

    private boolean isAllDayEvent(ITimeEventInterface event) {
        long duration = event.getEndTime() - event.getStartTime();
        boolean isAllDay = duration >= allDayMilliseconds;

        return isAllDay;
    }

    private int getEventY(ITimeEventInterface event) {
        String hourWithMinutes = sdf.format(new Date(event.getStartTime()));

        String[] components = hourWithMinutes.split(":");
        float trickTime = Integer.valueOf(components[0]) + Integer.valueOf(components[1]) / (float) 100;
        int getStartY = nearestTimeSlotValue(trickTime);

        return getStartY;
    }

    /**
     * set publc for other
     *
     * @param eventPackage
     */
    public void setEventList(ITimeEventPackageInterface eventPackage) {
        this.clearAllEvents();
        Map<Long, List<ITimeEventInterface>> dayEventMap = eventPackage.getRegularEventDayMap();
        Map<Long, List<ITimeEventInterface>> repeatedDayEventMap = eventPackage.getRepeatedEventDayMap();

        MyCalendar tempCal = new MyCalendar(this.myCalendar);
        for (int i = 0; i < displayLen; i++) {
            long startTime = tempCal.getBeginOfDayMilliseconds();
            if (dayEventMap != null && dayEventMap.containsKey(startTime)){
                List<ITimeEventInterface> currentDayEvents = dayEventMap.get(startTime);
                for (ITimeEventInterface event : currentDayEvents) {
                    this.addEvent(event);
                }
            }else {
//                Log.i(TAG, "dayEventMap null: " + tempCal.getDay());
            }

            if (repeatedDayEventMap != null && repeatedDayEventMap.containsKey(startTime)){
                List<ITimeEventInterface> currentDayEvents = repeatedDayEventMap.get(startTime);
                for (ITimeEventInterface event : currentDayEvents) {
                    this.addEvent(event);
                }
            }

            tempCal.setOffsetByDate(1);
        }

        for (DayInnerBodyEventLayout eventLayout:eventLayouts
             ) {
            calculateEventLayout(eventLayout);
        }
    }

    public void showEventAnim(ITimeEventInterface... events){
        for (ITimeEventInterface event:events
                ) {
            showSingleEventAnim(event);
        }
    }

    public void showEventAnim(List<ITimeEventInterface> events){
        for (ITimeEventInterface event:events
             ) {
            showSingleEventAnim(event);
        }
    }

    public <T extends ITimeTimeSlotInterface> void showTimeslotAnim(T ... timeslots){
        for (ITimeTimeSlotInterface timeslot:timeslots) {
            showSingleTimeslotAnim(timeslot);
        }
    }

    public void showTimeslotAnim(List<? extends ITimeTimeSlotInterface> timeslots){
        for (ITimeTimeSlotInterface timeslot:timeslots) {
            showSingleTimeslotAnim(timeslot);
        }
    }

    private void showSingleEventAnim(ITimeEventInterface event){
        final DayDraggableEventView eventView = this.uidDragViewMap.get(event.getEventUid());
        if (eventView!=null){
            eventView.showAlphaAnim();
        }
    }

    private void showSingleTimeslotAnim(ITimeTimeSlotInterface timeslot){
        final TimeSlotView timeslotView = findTimeslotView(slotViews, timeslot);
        if (timeslotView!=null){
            timeslotView.showAlphaAnim();
        }
    }

    private TimeSlotView findTimeslotView(ArrayList<TimeSlotView> timeSlotViews, ITimeTimeSlotInterface timeslot){
        for (TimeSlotView timeslotView:timeSlotViews
             ) {
            ITimeTimeSlotInterface slot = timeslotView.getTimeslot();
            if (slot != null && slot.getTimeslotUid().equals(timeslot.getTimeslotUid())){
                return timeslotView;
            }
        }
        return null;
    }

    /**
     * calculate the position of event
     * it needs to be called when setting event or event position changed
     */
    private void calculateEventLayout(DayInnerBodyEventLayout eventLayout) {
        List<ArrayList<Pair<Pair<Integer, Integer>, ITimeEventInterface>>> overlapGroups
                = xHelper.computeOverlapXForEvents(eventLayout.getEvents());
        int previousGroupExtraY = 0;
        for (ArrayList<Pair<Pair<Integer, Integer>, ITimeEventInterface>> overlapGroup : overlapGroups
                ) {
            for (int i = 0; i < overlapGroup.size(); i++) {

                int startY = getEventY(overlapGroup.get(i).second);
                int widthFactor = overlapGroup.get(i).first.first;
                int startX = overlapGroup.get(i).first.second;
//                int topMargin = startY + overlapGapHeight * i + previousGroupExtraY;
                int topMargin = startY + previousGroupExtraY;
                DayDraggableEventView eventView = (DayDraggableEventView) eventLayout.findViewById(regularEventViewMap.get(overlapGroup.get(i).second));
                eventView.setPosParam(new DayDraggableEventView.PosParam(startY, startX, widthFactor, topMargin));
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(eventView.getEvent().getStartTime());
            }
            previousGroupExtraY += overlapGapHeight * overlapGroup.size();
        }
    }

    private DayDraggableEventView createDayDraggableEventView(ITimeEventInterface event, boolean isAllDayEvent) {
        DayDraggableEventView event_view = new DayDraggableEventView(context, event, isAllDayEvent);
        event_view.setType(DayDraggableEventView.TYPE_NORMAL);
        if (!isTimeSlotEnable){
            event_view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (onBodyListener != null) {
                        onBodyListener.onEventClick((DayDraggableEventView) view);
                    }
                }
            });
        }

        if (isAllDayEvent) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT,1f);
            event_view.setTag(event);
            event_view.setLayoutParams(params);
        } else {
            long duration = event.getEndTime() - event.getStartTime();
            int eventHeight =(int) (duration * heightPerMillisd);

            DayDraggableEventView.LayoutParams params = new DayDraggableEventView.LayoutParams(eventHeight, eventHeight);
            if (!isTimeSlotEnable){
                event_view.setOnLongClickListener(new EventLongClickListener());
            }
            event_view.setTag(event);
            event_view.setLayoutParams(params);
        }

        //add it to map
        uidDragViewMap.put(event.getEventUid(), event_view);

        return event_view;
    }

    public DayDraggableEventView createTempDayDraggableEventView(float tapX, float tapY) {
        ITimeEventInterface event = this.initializeEvent();
        if (event == null) {
            throw new RuntimeException("need Class name in 'setEventClassName()'");
        }
        DayDraggableEventView event_view = new DayDraggableEventView(context, event, false);
        event_view.setType(DayDraggableEventView.TYPE_TEMP);

        int eventHeight = 1 * lineHeight;//one hour
        DayDraggableEventView.LayoutParams params = new DayDraggableEventView.LayoutParams(200, eventHeight);
        event_view.setX(tapY - eventHeight / 2);
        event_view.setOnLongClickListener(new EventLongClickListener());
        event_view.setLayoutParams(params);

        return event_view;
    }

    /****************************************************************************************/

    private class EventLongClickListener implements View.OnLongClickListener {
        @Override
        public boolean onLongClick(View view) {

            if (tempDragView != null || onBodyListener!=null && onBodyListener.isDraggable((DayDraggableEventView) view)){
                ClipData data = ClipData.newPlainText("", "");
                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(
                        view);
                view.startDrag(data, shadowBuilder, view, 0);
                if (tempDragView != null) {
                    view.setVisibility(View.INVISIBLE);
                } else {
                    view.setVisibility(View.VISIBLE);
                }
                view.getBackground().setAlpha(255);
            }
            return false;
        }
    }

    private class EventDragListener implements View.OnDragListener {
        int index = 0;
        int currentEventNewHour = -1;
        int currentEventNewMinutes = -1;

        public EventDragListener(int index) {
            this.index = index;
        }

        @Override
        public boolean onDrag(View v, DragEvent event) {
            DayDraggableEventView dgView = (DayDraggableEventView) event.getLocalState();
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    break;
                case DragEvent.ACTION_DRAG_LOCATION:
                    int rawX = (int) (layoutWidthPerDay * index + event.getX());
                    scrollViewAutoScroll(event);

                    if (onBodyListener != null) {
                        onBodyListener.onEventDragging(dgView, rawX, (int) event.getY());
                    } else {
                        Log.i(TAG, "onDrag: null onEventDragListener");
                    }
                    msgWindowFollow(rawX, (int) event.getY(), index, (View) event.getLocalState());
                    break;
                case DragEvent.ACTION_DRAG_ENTERED:
                    msgWindow.setVisibility(View.VISIBLE);
                    if (dgView.getType() == DayDraggableEventView.TYPE_TEMP){
                        tempDragView = dgView;
                    }else{
                        tempDragView= null;
                    }
                    break;
                case DragEvent.ACTION_DRAG_EXITED:
                    msgWindow.setVisibility(View.INVISIBLE);
                    tempDragView = null;
                    break;
                case DragEvent.ACTION_DROP:
                    //handler ended things in here, because ended some time is not triggered
                    dgView.getBackground().setAlpha(128);
                    View finalView = (View) event.getLocalState();
                    finalView.getBackground().setAlpha(128);
                    finalView.setVisibility(View.VISIBLE);
                    msgWindow.setVisibility(View.INVISIBLE);

                    float actionStopX = event.getX();
                    float actionStopY = event.getY();
                    // Dropped, reassign View to ViewGroup
                    int newX = (int) actionStopX - dgView.getWidth() / 2;
                    int newY = (int) actionStopY - dgView.getHeight() / 2;
                    int[] reComputeResult = reComputePositionToSet(newX, newY, dgView, v);

                    //update the event time
                    String new_time = positionToTimeTreeMap.get(reComputeResult[1]);
                    //important! update event time after drag
                    String[] time_parts = new_time.split(":");
                    currentEventNewHour = Integer.valueOf(time_parts[0]);
                    currentEventNewMinutes = Integer.valueOf(time_parts[1]);

                    dgView.getNewCalendar().setHour(currentEventNewHour);
                    dgView.getNewCalendar().setMinute(currentEventNewMinutes);
                    //set dropped container index
                    dgView.setIndexInView(index);

                    if (tempDragView == null && onBodyListener != null) {
                        onBodyListener.onEventDragDrop(dgView);
                    } else {
                        Log.i(TAG, "onDrop Not Called");
                    }

                    if (dgView.getType() == DayDraggableEventView.TYPE_TEMP) {
                        ViewGroup parent = (ViewGroup) dgView.getParent();
                        if(parent != null){
                            parent.removeView(dgView);
                        }
                        //important! update event time after drag via listener
                        if (onBodyListener != null) {
                            onBodyListener.onEventCreate(dgView);
                        }
                        //finally reset tempDragView to NULL.
                        tempDragView = null;
                    }
                    Log.i(TAG, "onDrag: drop " + index);
                    break;
                case DragEvent.ACTION_DRAG_ENDED:
                    break;
                default:
                    break;
            }

            return true;
        }
    }

    private class CreateEventListener implements View.OnLongClickListener {

        @Override
        public boolean onLongClick(View v) {
            if (tempDragView == null) {
                DayInnerBodyEventLayout container = (DayInnerBodyEventLayout) v;
                tempDragView = createTempDayDraggableEventView(nowTapX, nowTapY);
                container.addView(tempDragView);

                tempDragView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        tempDragView.performLongClick();
                    }
                }, 100);
            }

            return true;
        }
    }

    /****************************************************************************************/

    private int[] reComputePositionToSet(int actualX, int actualY, View draggableObj, View container) {
        int containerWidth = container.getWidth();
        int containerHeight = container.getHeight();

        int objWidth = draggableObj.getWidth();
        int objHeight = draggableObj.getHeight();

        int finalX = (int) (timeTextSize * 1.5);
        int finalY = actualY;

        if (actualY < 0) {
            finalY = 0;
        } else if (actualY + objHeight > containerHeight) {
            finalY = containerHeight - objHeight;
        }
        int findNearestPosition = nearestTimeSlotKey(finalY);
        if (findNearestPosition != -1) {
            finalY = findNearestPosition;
        } else {
            Log.i(TAG, "reComputePositionToSet: " + "ERROR NO SUCH POSITION");
        }

        return new int[]{finalX, finalY};
    }

    private int nearestTimeSlotKey(int tapY) {
        int key = tapY;
        Map.Entry<Integer, String> low = positionToTimeTreeMap.floorEntry(key);
        Map.Entry<Integer, String> high = positionToTimeTreeMap.ceilingEntry(key);
        if (low != null && high != null) {
            return Math.abs(key - low.getKey()) < Math.abs(key - high.getKey())
                    ? low.getKey()
                    : high.getKey();
        } else if (low != null || high != null) {
            return low != null ? low.getKey() : high.getKey();
        }

        return -1;
    }

    private int nearestTimeSlotValue(float time) {
        float key = time;
        Map.Entry<Float, Integer> low = timeToPositionTreeMap.floorEntry(key);
        Map.Entry<Float, Integer> high = timeToPositionTreeMap.ceilingEntry(key);
        if (low != null && high != null) {
            return Math.abs(key - low.getKey()) < Math.abs(key - high.getKey())
                    ? low.getValue()
                    : high.getValue();
        } else if (low != null || high != null) {
            return low != null ? low.getValue() : high.getValue();
        }

        return -1;
    }

    private void scrollViewAutoScroll(DragEvent event) {
        Rect scrollBounds = new Rect();
        scrollContainerView.getDrawingRect(scrollBounds);
        float heightOfView = ((View) event.getLocalState()).getHeight();
        float allDayLayoutHeight = this.topAllDayLayout.getHeight();
        float needPositionY_top = event.getY() - heightOfView / 2;
        float needPositionY_bottom = event.getY() + heightOfView / 2;

        if ((scrollBounds.top - allDayLayoutHeight) > needPositionY_top) {
            int offsetY = scrollContainerView.getScrollY() - DensityUtil.dip2px(context, 10);
            scrollContainerView.smoothScrollTo(scrollContainerView.getScrollX(), offsetY);
        } else if ((scrollBounds.bottom - allDayLayoutHeight) < needPositionY_bottom) {
            int offsetY = scrollContainerView.getScrollY() + DensityUtil.dip2px(context, 10);
            scrollContainerView.smoothScrollTo(scrollContainerView.getScrollX(), offsetY);
        }
    }

    public void scrollToTime(long time) {
        String hourWithMinutes = sdf.format(new Date(time));

        String[] components = hourWithMinutes.split(":");
        float trickTime = Integer.valueOf(components[0]) + Integer.valueOf(components[1]) / (float) 100;
        final int getStartY = nearestTimeSlotValue(trickTime);

        scrollContainerView.scrollTo(scrollContainerView.getScrollX(), (int)(getStartY + rightContentLayout.getY() - DensityUtil.dip2px(context,10)));
    }

    private void msgWindowFollow(int tapX, int tapY, int index, View followView) {
        float toX;
        float toY;

        toY = tapY - followView.getHeight() / 2 - msgWindow.getHeight();
        if (tapX + msgWindow.getWidth() / 2 > dividerBgRLayout.getWidth()) {
            toX = dividerBgRLayout.getWidth() - msgWindow.getWidth();
        } else if (tapX - msgWindow.getWidth() / 2 < 0) {
            toX = 0;
        } else {
            toX = tapX - msgWindow.getWidth() / 2;
        }
        int nearestProperPosition = nearestTimeSlotKey(tapY - followView.getHeight() / 2);
        if (nearestProperPosition != -1) {
            if (this.displayLen == 1){
                msgWindow.setText(positionToTimeTreeMap.get(nearestProperPosition));
            }else{
                MyCalendar myCal = new MyCalendar(this.myCalendar);
                myCal.setOffsetByDate(index);
                String dayInfo = (myCal.getCalendar().getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault()));
                msgWindow.setText(dayInfo + " " + positionToTimeTreeMap.get(nearestProperPosition));
            }
        } else {
            Log.i(TAG, "msgWindowFollow: " + "Error, text not found in Map");
        }

        msgWindow.setTranslationX(toX);
        msgWindow.setTranslationY(toY);
    }

    private long[] changeDateFromString(ITimeEventInterface event, int hour, int minute) {
        long startTime = event.getStartTime();
        long endTime = event.getEndTime();
        long duration = endTime - startTime;

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(startTime);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.MILLISECOND, 0);

        long new_start = calendar.getTimeInMillis();
        calendar.setTimeInMillis(new_start + duration);
        long new_end = calendar.getTimeInMillis();
        long[] param = {new_start, new_end};

        return param;
    }


    /***************************
     * Interface
     ******************************************/

    public interface OnBodyTouchListener {
        boolean bodyOnTouchListener(float tapX, float tapY);
    }

    public void setOnBodyTouchListener(OnBodyTouchListener onBodyTouchListener) {
        this.onBodyTouchListener = onBodyTouchListener;
    }

    /**
     * DayDraggableEventView contains data source and all information about new status
     */
    public interface OnBodyListener {
        //If current event view is draggable
        boolean isDraggable(DayDraggableEventView eventView);
        //while creating event view
        void onEventCreate(DayDraggableEventView eventView);
        //while clicking event
        void onEventClick(DayDraggableEventView eventView);
        //When start dragging
        void onEventDragStart(DayDraggableEventView eventView);
        //On dragging
        void onEventDragging(DayDraggableEventView eventView, int x, int y);
        //When dragging ended
        void onEventDragDrop(DayDraggableEventView eventView);
    }

    public void setOnBodyListener(OnBodyListener onBodyListener) {
        this.onBodyListener = onBodyListener;
//        this.onScrollChangeListener = onBodyListener.setScrollChangeListener();
//        if (this.onScrollChangeListener != null){
//            this.scrollContainerView.getViewTreeObserver().addOnScrollChangedListener(this.onScrollChangeListener);
//        }
    }

    Class<?> eventClassName;

    public <E extends ITimeEventInterface> void setEventClassName(Class<E> className) {
        eventClassName = className;
    }

    /**
     * @return
     */
    private ITimeEventInterface initializeEvent() {
        try {
            ITimeEventInterface t = (ITimeEventInterface) eventClassName.newInstance();
            return t;
        } catch (Exception e) {
            return null;
        }
    }

    public void setCurrentTempView(DayDraggableEventView tempDragView){
        this.tempDragView = tempDragView;
    }

    /************************** For time slot view *************************************************/
    private ArrayList<TimeSlotView> slotViews = new ArrayList<>();

    public void clearTimeSlots(){
        for (TimeSlotView timeSlotView:slotViews
             ) {
            ViewGroup parent = (ViewGroup)timeSlotView.getParent();
            if (parent != null){
                parent.removeView(timeSlotView);
            }
        }
    }

    public void addSlot(ITimeTimeSlotInterface timeslot, boolean animate){
        int offset = this.getEventContainerIndex(timeslot.getStartTime());

        if (rightArrow!= null && offset >= displayLen){
            rightArrow.setVisibility(VISIBLE);
        }else if (rightArrow!= null && offset <= -1){
            leftArrow.setVisibility(VISIBLE);
        }

        if (offset < displayLen && offset > -1){
            TimeSlotView timeSlotView = createTimeSlotView(timeslot);
            eventLayouts.get(offset).addView(timeSlotView,timeSlotView.getLayoutParams());
            timeSlotView.bringToFront();
            timeSlotView.setVisibility(VISIBLE);
            resizeTimeSlot(timeSlotView,animate);
            slotViews.add(timeSlotView);
            timeSlotView.requestLayout();
        }
    }

    private TimeSlotView createTimeSlotView(ITimeTimeSlotInterface timeslot){
        TimeSlotView timeSlotView = new TimeSlotView(context, timeslot);
        if (timeslot != null){
            timeSlotView.setType(TimeSlotView.TYPE_NORMAL);
            timeSlotView.setTimes(timeslot.getStartTime(), timeslot.getEndTime());
            timeSlotView.setStatus(timeslot.getDisplayStatus());
            DayInnerBodyEventLayout.LayoutParams params = new DayInnerBodyEventLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, layoutWidthPerDay);
            timeSlotView.setLayoutParams(params);
//            timeSlotView.setTag(struct);
        }else {
            long duration = this.slotViews.size() == 0 ? 3600 * 1000 : this.slotViews.get(0).getDuration();
            timeSlotView.setDuration(duration);
            int tempViewHeight = (int)(duration/((float)(3600*1000)) * lineHeight);
            timeSlotView.setType(TimeSlotView.TYPE_TEMP);
            DayInnerBodyEventLayout.LayoutParams params = new DayInnerBodyEventLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, tempViewHeight);
            timeSlotView.setLayoutParams(params);
        }

        timeSlotView.setOnLongClickListener(new TimeSlotLongClickListener());
        timeSlotView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                TimeSlotView timeSlotView = (TimeSlotView) v;

                if (onTimeSlotListener != null){
                    onTimeSlotListener.onTimeSlotClick(timeSlotView);
                }

            }
        });

        return timeSlotView;
    }

    private void resizeTimeSlot(TimeSlotView timeSlotView, boolean animate){
        final DayInnerBodyEventLayout.LayoutParams params = (DayInnerBodyEventLayout.LayoutParams) timeSlotView.getLayoutParams();
        long duration = timeSlotView.getDuration();
        final int slotHeight = (int) (((float) duration / (3600 * 1000)) * lineHeight);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        String hourWithMinutes = sdf.format(new Date(timeSlotView.getNewStartTime()));
        String[] components = hourWithMinutes.split(":");
        float trickTime = Integer.valueOf(components[0]) + (float) Integer.valueOf(components[1]) / 100;
        final int topMargin = nearestTimeSlotValue(trickTime);
        timeSlotView.setY(topMargin);

        if (animate){
            ResizeAnimation resizeAnimation = new ResizeAnimation(
                    timeSlotView,
                    slotHeight,
                    ResizeAnimation.Type.HEIGHT,
                    600
            );

            timeSlotView.startAnimation(resizeAnimation);
        }else {
            params.height = slotHeight;
        }
    }

    public void updateTimeSlotsDuration(long duration, boolean animate){
        for (TimeSlotView tsV : this.slotViews
             ) {
            int offset = this.getEventContainerIndex(tsV.getNewStartTime());
            long startTime = tsV.getNewStartTime();

            tsV.setTimes(startTime, startTime + duration);

            if (offset < displayLen && offset > -1){
                resizeTimeSlot(tsV,animate);
            }
        }
    }

    /************************** Time Slot Listener *************************************************/
    private class TimeSlotLongClickListener implements View.OnLongClickListener {
        @Override
        public boolean onLongClick(View view) {
            ClipData data = ClipData.newPlainText("", "");
            View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(
                    view);
            view.startDrag(data, shadowBuilder, view, 0);
            view.setVisibility(View.VISIBLE);
            if (tempDragView != null) {
                view.setVisibility(View.INVISIBLE);
            } else {
                view.setVisibility(View.VISIBLE);
            }
            view.getBackground().setAlpha(255);
            return false;
        }
    }

    private class TimeSlotDragListener implements View.OnDragListener {
        int index = 0;
        int currentEventNewHour = -1;
        int currentEventNewMinutes = -1;

        public TimeSlotDragListener(int index) {
            this.index = index;
        }

        @Override
        public boolean onDrag(View v, DragEvent event) {
            TimeSlotView tsView = (TimeSlotView) event.getLocalState();

            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    break;
                case DragEvent.ACTION_DRAG_LOCATION:
                    int rawX = (int) (layoutWidthPerDay * index + event.getX());

                    scrollViewAutoScroll(event);

                    if (onTimeSlotListener != null) {
                        onTimeSlotListener.onTimeSlotDragging(tsView, rawX, (int) event.getY());
                    } else {
                        Log.i(TAG, "onDrag: null onEventDragListener");
                    }
                    msgWindowFollow(rawX, (int) event.getY(), index, (View) event.getLocalState());
                    break;
                case DragEvent.ACTION_DRAG_ENTERED:
                    msgWindow.setVisibility(View.VISIBLE);
                    if (tsView.getType() == DayDraggableEventView.TYPE_TEMP){
                        tempDragView = tsView;
                    }else{
                        tempDragView= null;
                    }

                    break;
                case DragEvent.ACTION_DRAG_EXITED:
                    msgWindow.setVisibility(View.INVISIBLE);
                    tempDragView = null;
                    break;
                case DragEvent.ACTION_DROP:
                    //handler ended things in here, because ended some time is not triggered
                    View finalView = (View) event.getLocalState();
                    finalView.setVisibility(View.VISIBLE);
                    msgWindow.setVisibility(View.INVISIBLE);

                    float actionStopX = event.getX();
                    float actionStopY = event.getY();
                    // Dropped, reassign View to ViewGroup
                    int newX = (int) actionStopX - tsView.getWidth() / 2;
                    int newY = (int) actionStopY - tsView.getHeight() / 2;
                    int[] reComputeResult = reComputePositionToSet(newX, newY, tsView, v);

                    //update the event time
                    String new_time = positionToTimeTreeMap.get(reComputeResult[1]);
                    //important! update event time after drag
                    String[] time_parts = new_time.split(":");
                    currentEventNewHour = Integer.valueOf(time_parts[0]);
                    currentEventNewMinutes = Integer.valueOf(time_parts[1]);
//
                    tsView.getCalendar().setHour(currentEventNewHour);
                    tsView.getCalendar().setMinute(currentEventNewMinutes);
//                    //set dropped container index
                    tsView.setIndexInView(index);

                    if (tempDragView == null && onTimeSlotListener != null) {
                        onTimeSlotListener.onTimeSlotDragDrop(tsView, 0, 0);
                    } else {
                        Log.i(TAG, "onDrop Not Called");
                    }

                    if (tsView.getType() == DayDraggableEventView.TYPE_TEMP) {
                        ViewGroup parent = (ViewGroup) tsView.getParent();
                        if(parent != null){
                            parent.removeView(tsView);
                        }
                        //important! update event time after drag via listener
                        if (onTimeSlotListener != null) {
                            onTimeSlotListener.onTimeSlotCreate(tsView);
                        }
                        //finally reset tempDragView to NULL.
                        tempDragView = null;
                    }

                    break;
                case DragEvent.ACTION_DRAG_ENDED:
                    break;
                default:
                    break;
            }

            return true;
        }
    }

    private class CreateTimeSlotListener implements View.OnLongClickListener {

        @Override
        public boolean onLongClick(View v) {
//            if (tempDragView == null) {
                DayInnerBodyEventLayout container = (DayInnerBodyEventLayout) v;
                tempDragView = createTimeSlotView(null);
                tempDragView.setY(nowTapY);
                container.addView(tempDragView);

                tempDragView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        tempDragView.performLongClick();
                    }
                }, 100);
//            }else{
//                Log.i(TAG, "onLongClitempDragView  not null ");
//            }

            return true;
        }
    }

    public void enableTimeSlot(){
        this.isTimeSlotEnable = true;
        for (int i = 0; i < displayLen; i++) {
            //remove previous listeners
            eventLayouts.get(i).setOnDragListener(new TimeSlotDragListener(i));
            eventLayouts.get(i).setOnLongClickListener(new CreateTimeSlotListener());

            for (int j = 0; j < eventLayouts.get(i).getChildCount(); j++) {
                if (eventLayouts.get(i).getChildAt(j) instanceof DayDraggableEventView){
                    eventLayouts.get(i).getChildAt(j).setOnLongClickListener(null);

                }
            }
        }
    }

    private OnTimeSlotListener onTimeSlotListener;
    public interface OnTimeSlotListener {
        void onTimeSlotCreate(TimeSlotView timeSlotView);

        void onTimeSlotClick(TimeSlotView timeSlotView);

        void onTimeSlotDragStart(TimeSlotView timeSlotView);

        void onTimeSlotDragging(TimeSlotView timeSlotView, int x, int y);

        void onTimeSlotDragDrop(TimeSlotView timeSlotView, long startTime, long endTime);
    }

    public void setOnTimeSlotListener(OnTimeSlotListener onTimeSlotListener) {
        this.onTimeSlotListener = onTimeSlotListener;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (onBodyTouchListener != null){
            return onBodyTouchListener.bodyOnTouchListener(ev.getX(),ev.getY());
        }
        return false;
    }

    public void removeOptListener(){
        isRemoveOptListener = true;
        for (int i = 0; i < displayLen; i++) {
            //remove previous listeners
            eventLayouts.get(i).setOnDragListener(null);
            eventLayouts.get(i).setOnLongClickListener(null);

            for (int j = 0; j < eventLayouts.get(i).getChildCount(); j++) {
                if (eventLayouts.get(i).getChildAt(j) instanceof DayDraggableEventView
                        ||
                        eventLayouts.get(i).getChildAt(j) instanceof TimeSlotView
                        ){
                    eventLayouts.get(i).getChildAt(j).setOnLongClickListener(null);
                }
            }
        }
    }

}
