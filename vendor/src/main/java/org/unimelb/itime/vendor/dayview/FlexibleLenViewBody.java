package org.unimelb.itime.vendor.dayview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.unimelb.itime.vendor.R;
import org.unimelb.itime.vendor.helper.DensityUtil;
import org.unimelb.itime.vendor.helper.MyCalendar;
import org.unimelb.itime.vendor.listener.ITimeEventInterface;
import org.unimelb.itime.vendor.listener.ITimeEventPackageInterface;
import org.unimelb.itime.vendor.listener.ITimeTimeSlotInterface;
import org.unimelb.itime.vendor.unitviews.DraggableEventView;
import org.unimelb.itime.vendor.unitviews.DraggableTimeSlotView;
import org.unimelb.itime.vendor.weekview.WeekView;
import org.unimelb.itime.vendor.wrapper.WrapperEvent;
import org.unimelb.itime.vendor.wrapper.WrapperTimeSlot;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by yuhaoliu on 3/08/16.
 */
public class FlexibleLenViewBody extends FrameLayout {
    public final String TAG = "MyAPP";
    //FOR event inner type
    public static final int UNDEFINED = -1;
    public static final int REGULAR = 0;
    public static final int DAY_CROSS_BEGIN = 1;
    public static final int DAY_CROSS_ALL_DAY = 2;
    public static final int DAY_CROSS_END = 3;
    /**
     * Color category
     */
    /*************************** Start of Color Setting **********************************/
    private int color_allday_bg = Color.parseColor("#EBEBEB");
    private int color_allday_title = R.color.black;
    private int color_bg_day_even = R.color.color_f2f2f5;
    private int color_bg_day_odd = R.color.color_75white;
    private int color_msg_window_text = R.color.text_enable;
    private int color_time_text = R.color.text_enable;
    private int color_nowtime = R.color.text_today_color;
    private int color_nowtime_bg = R.color.whites;
    /*************************** End of Color Setting **********************************/

    /*************************** Start of Resources Setting ****************************/
    private int rs_divider_line = R.drawable.itime_day_view_dotted;
    private int rs_nowtime_line = R.drawable.itime_now_time_full_line;
    /*************************** End of Resources Setting ****************************/

    protected final long allDayMilliseconds = 24 * 60 * 60 * 1000;

    protected boolean isTimeSlotEnable = false;
    protected boolean isRemoveOptListener = false;

    protected ScrollContainerView scrollContainerView;
    private FrameLayout bodyContainerLayout;

    private RelativeLayout globalAnimationLayout;
    private RelativeLayout localAnimationLayout;

    protected LinearLayout topAllDayLayout;
    protected LinearLayout topAllDayEventLayouts;

    private FrameLayout timeLayout;
    private FrameLayout dividerBgRLayout;
    protected LinearLayout eventLayout;

    public MyCalendar myCalendar;
    protected Context context;

    private ArrayList<DraggableEventView> allDayDgEventViews = new ArrayList<>();

    protected ArrayList<DayInnerHeaderEventLayout> allDayEventLayouts = new ArrayList<>();
    protected ArrayList<DayInnerBodyEventLayout> eventLayouts = new ArrayList<>();

    protected TreeMap<Integer, String> positionToTimeTreeMap = new TreeMap<>();
    protected TreeMap<Float, Integer> timeToPositionTreeMap = new TreeMap<>();

    protected SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

    protected TextView msgWindow;
    private TextView nowTime;
    private ImageView nowTimeLine;
    //tag: false-> moving, true, done
    protected View tempDragView = null;

    //dp
    private int leftSideWidth = 40;
    //dp
    protected int lineHeight = 45;
    private int timeTextSize = 20;
    protected int topAllDayHeight;

    protected int layoutWidthPerDay;
    protected int layoutHeightPerDay;

    protected int displayLen = 7;

    public Integer leftArrowVisibility = WeekView.TIMESLOT_AUTO;
    public Integer rightArrowVisibility = WeekView.TIMESLOT_AUTO;
    public Integer topArrowVisibility = WeekView.TIMESLOT_AUTO;
    public Integer bottomArrowVisibility = WeekView.TIMESLOT_AUTO;

    protected float nowTapX = 0;
    protected float nowTapY = 0;

    private OnBodyTouchListener onBodyTouchListener;

    protected float heightPerMillisd = 0;

    ImageView leftArrow;
    ImageView rightArrow;
    ImageView topArrow;
    ImageView bottomArrow;

    final Handler uiHandler= new Handler();
    private Thread uiUpdateThread;

    private TimeSlotController timeSlotController;
    private EventController eventController;

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
        timeSlotController = new TimeSlotController(this);
        eventController = new EventController(this);

        initViews();
        initBackgroundView();
        initAnimations();
        initTreeObserver();
    }

    private void initTreeObserver(){
        this.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                FlexibleLenViewBody.this.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                preTasks();
            }
        });
    }

    private void preTasks(){
        this.timeSlotAnimationChecker();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        layoutWidthPerDay = MeasureSpec.getSize(eventLayout.getMeasuredWidth()/displayLen);
    }

    private void initLayoutParams(){
        this.lineHeight = DensityUtil.dip2px(context, lineHeight);
        this.heightPerMillisd = (float)lineHeight/(3600*1000);
        this.leftSideWidth = DensityUtil.dip2px(context,leftSideWidth);
    }

    private void initViews() {
        this.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        int topAllDayEventLayoutsPadding = DensityUtil.dip2px(context, 3);
        this.topAllDayHeight = DensityUtil.dip2px(context, 30);

        scrollContainerView = new ScrollContainerView(context);
        LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//        scrollParams.setMargins(0,0,0,0);
        scrollContainerView.setLayoutParams(scrollParams);
        this.addView(scrollContainerView);

        topAllDayLayout = new LinearLayout(getContext());
        topAllDayLayout.setOrientation(LinearLayout.HORIZONTAL);
        topAllDayLayout.setGravity(Gravity.CENTER);
        topAllDayLayout.setBackgroundColor(color_allday_bg);
        topAllDayLayout.setBackground(getResources().getDrawable(R.drawable.bg_bottom_line));
        topAllDayLayout.setId(View.generateViewId());
        FrameLayout.LayoutParams topAllDayLayoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        topAllDayLayout.setLayoutParams(topAllDayLayoutParams);

        TextView allDayTitleTv = new TextView(context);
        LinearLayout.LayoutParams allDayTitleTvParams = new LinearLayout.LayoutParams(leftSideWidth, ViewGroup.LayoutParams.MATCH_PARENT);
        allDayTitleTv.setTextSize(10);
        allDayTitleTv.setText("All Day");
        allDayTitleTv.setTextColor(getResources().getColor(color_allday_title));
        allDayTitleTv.setGravity(Gravity.CENTER);
        allDayTitleTv.setLayoutParams(allDayTitleTvParams);
        allDayTitleTv.measure(0,0);
        topAllDayLayout.addView(allDayTitleTv);

        topAllDayEventLayouts = new LinearLayout(getContext());
        topAllDayEventLayouts.setPadding(0,topAllDayEventLayoutsPadding - DensityUtil.dip2px(context,1),0,topAllDayEventLayoutsPadding);
        topAllDayEventLayouts.setId(View.generateViewId());
        LinearLayout.LayoutParams topAllDayEventLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, topAllDayHeight);
        topAllDayEventLayouts.setLayoutParams(topAllDayEventLayoutParams);
        this.initInnerHeaderEventLayouts(topAllDayEventLayouts);
        topAllDayLayout.addView(topAllDayEventLayouts);
        topAllDayLayout.setVisibility(View.GONE);
        this.addView(topAllDayLayout);

        globalAnimationLayout = new RelativeLayout(context);
        RelativeLayout.LayoutParams animationLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        globalAnimationLayout.setLayoutParams(animationLayoutParams);
        this.addView(globalAnimationLayout);

        bodyContainerLayout = new FrameLayout(context);
        bodyContainerLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        scrollContainerView.addView(bodyContainerLayout);

        timeLayout = new FrameLayout(getContext());
        timeLayout.setId(View.generateViewId());
        timeLayout.setBackgroundColor(getResources().getColor(color_bg_day_odd));
        FrameLayout.LayoutParams leftSideRLayoutParams = new FrameLayout.LayoutParams(leftSideWidth, ViewGroup.LayoutParams.MATCH_PARENT);
        timeLayout.setLayoutParams(leftSideRLayoutParams);
//        leftSideRLayoutParams.topMargin = topAllDayHeight;
        bodyContainerLayout.addView(timeLayout);

        dividerBgRLayout = new FrameLayout(getContext());
        dividerBgRLayout.setId(View.generateViewId());
        FrameLayout.LayoutParams dividerBgRLayoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//        dividerBgRLayoutParams.topMargin = topAllDayHeight;
        dividerBgRLayoutParams.leftMargin = leftSideWidth;
        dividerBgRLayout.setLayoutParams(dividerBgRLayoutParams);
        bodyContainerLayout.addView(dividerBgRLayout);

        eventLayout = new LinearLayout(getContext());
        eventLayout.setId(View.generateViewId());
        eventLayout.setOrientation(LinearLayout.HORIZONTAL);
        FrameLayout.LayoutParams eventLayoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        this.initInnerBodyEventLayouts(eventLayout);
//        eventLayoutParams.topMargin = topAllDayHeight;
        eventLayoutParams.leftMargin = leftSideWidth;
        eventLayout.setLayoutParams(eventLayoutParams);

        bodyContainerLayout.addView(eventLayout);

        localAnimationLayout = new RelativeLayout(context);
        FrameLayout.LayoutParams localAnimationLayoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//        localAnimationLayoutParams.topMargin = topAllDayHeight;
        localAnimationLayout.setLayoutParams(localAnimationLayoutParams);
        bodyContainerLayout.addView(localAnimationLayout);
    }

    private void initAnimations(){
        initTimeSlotArrow();
    }

    public void resetAnimationViews(){
        //reset arrows
        resetArrow(leftArrow, leftArrowVisibility);
        resetArrow(rightArrow, rightArrowVisibility);
        resetArrow(topArrow, topArrowVisibility);
        resetArrow(bottomArrow, bottomArrowVisibility);
    }

    private void resetArrow(View v, Integer obj){
        switch (obj){
            case WeekView.TIMESLOT_AUTO:
                v.setVisibility(INVISIBLE);
                break;
            case WeekView.TIMESLOT_KEEP_HIDE:
                v.setVisibility(INVISIBLE);
                break;
            case WeekView.TIMESLOT_KEEP_SHOW:
                v.setVisibility(VISIBLE);
                break;
        }
    }

    private void initTimeSlotArrow(){
        int width = DensityUtil.dip2px(context,30);

        leftArrow = new ImageView(context);
        leftArrow.setImageDrawable(getResources().getDrawable(R.drawable.icon_timeslot_arrow));
        RelativeLayout.LayoutParams leftParams = new RelativeLayout.LayoutParams(width, width);
        leftParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        leftParams.addRule(RelativeLayout.CENTER_VERTICAL);
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
        rightParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        rightParams.addRule(RelativeLayout.CENTER_VERTICAL);
        rightArrow.setScaleType(ImageView.ScaleType.FIT_CENTER);
        rightArrow.setLayoutParams(rightParams);
        rightArrow.setVisibility(INVISIBLE);

        topArrow = new ImageView(context);
        topArrow.setImageDrawable(getResources().getDrawable(R.drawable.icon_timeslot_arrow));
        RelativeLayout.LayoutParams topParams = new RelativeLayout.LayoutParams(width, width);
        topParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        topParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        topArrow.setLayoutParams(topParams);
        topArrow.setPivotX(width/2);
        topArrow.setPivotY(width/2);
        topArrow.setRotation(-90);
        topArrow.setVisibility(INVISIBLE);

        bottomArrow = new ImageView(context);
        bottomArrow.setImageDrawable(getResources().getDrawable(R.drawable.icon_timeslot_arrow));
        RelativeLayout.LayoutParams bottomParams = new RelativeLayout.LayoutParams(width, width);
        bottomParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        bottomParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        bottomArrow.setLayoutParams(bottomParams);
        bottomArrow.setPivotX(width/2);
        bottomArrow.setPivotY(width/2);
        bottomArrow.setRotation(90);
        bottomArrow.setVisibility(INVISIBLE);

        if (this.globalAnimationLayout != null){
            globalAnimationLayout.addView(leftArrow);
            globalAnimationLayout.addView(rightArrow);
            globalAnimationLayout.addView(topArrow);
            globalAnimationLayout.addView(bottomArrow);
        }
    }

    private void initInnerHeaderEventLayouts(LinearLayout parent){
        for (int i = 0; i < displayLen; i++) {
            DayInnerHeaderEventLayout allDayEventLayout = new DayInnerHeaderEventLayout(context);
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
            eventLayout.setBackgroundColor(getResources().getColor(displayLen == 1 ? color_bg_day_odd : (i%2 == 0 ? color_bg_day_even : color_bg_day_odd)));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT,1f);

            final int eventLayoutPadding = DensityUtil.dip2px(context, 1);
            eventLayout.setPadding(eventLayoutPadding,0,eventLayoutPadding,0);

            parent.addView(eventLayout,params);
            if (!isTimeSlotEnable){
                eventLayout.setOnTouchListener(new OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        nowTapX = event.getX();
                        nowTapY = event.getY();
                        return false;
                    }
                });
                eventLayout.setOnDragListener(eventController.new EventDragListener(i));
                eventLayout.setOnLongClickListener(eventController.new CreateEventListener());
            }else {
                eventLayout.setOnDragListener(this.timeSlotController.new TimeSlotDragListener(i));
                eventLayout.setOnLongClickListener(this.timeSlotController.new CreateTimeSlotListener());
            }

            eventLayouts.add(eventLayout);
        }

    }

    public void timeSlotAnimationChecker(){
       timeSlotController.timeSlotAnimationChecker();
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
        initTimeText(getHours());
        initDividerLine(getHours());
        initMsgWindow();
    }

    public ScrollView getScrollView(){
        return this.scrollContainerView;
    }

    private void initTimeSlot() {
        double startPoint = DensityUtil.dip2px(context,10);
        double timeSlotHeight = lineHeight / 60;
        String[] hours = getHours();
        for (int slot = 0; slot < hours.length; slot++) {
            //add full clock
            positionToTimeTreeMap.put((int) startPoint + lineHeight * slot, hours[slot] + ":00");
            String hourPart = hours[slot].substring(0, 2); // XX
            timeToPositionTreeMap.put((float) Integer.valueOf(hourPart), (int) startPoint + lineHeight * slot);
            for (int miniSlot = 0; miniSlot < 59; miniSlot++) {
//                String minutes = String.valueOf((miniSlot + 1) * 15);
                String minutes = String.format("%02d", miniSlot + 1);
//                minutes = String.format("%02d", minutes);
//                Log.i(TAG, "minutes: " + minutes);
                String time = hourPart + ":" + minutes;
                int positionY = (int) (startPoint + lineHeight * slot + timeSlotHeight * (miniSlot + 1));
                positionToTimeTreeMap.put(positionY, time);
                timeToPositionTreeMap.put(Integer.valueOf(hourPart) + (float) Integer.valueOf(minutes) / 100, positionY);
            }
        }
    }

    private void initMsgWindow() {
        msgWindow = new TextView(context);

        msgWindow.setTextColor(context.getResources().getColor(color_msg_window_text));
        msgWindow.setText("SUN 00:00");
        msgWindow.setTextSize(17);
        msgWindow.setGravity(Gravity.LEFT);
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
        int height = DensityUtil.dip2px(context,20);
        for (int time = 0; time < HOURS.length; time++) {
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
            TextView timeView = new TextView(context);
            timeView.setLayoutParams(params);
            timeView.setTextColor(context.getResources().getColor(color_time_text));
            timeView.setText(HOURS[time]);
            timeView.setTextSize(11);
            timeView.setGravity(Gravity.CENTER);
            int timeTextY = nearestTimeSlotValue(time);
            params.setMargins(0, timeTextY - height/2, 0, 0);

            timeTextSize = (int) timeView.getTextSize() + timeView.getPaddingTop();
            timeLayout.addView(timeView);
        }
    }

    private void initDividerLine(String[] HOURS) {
        for (int numOfDottedLine = 0; numOfDottedLine < HOURS.length; numOfDottedLine++) {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            ImageView dividerImageView = new ImageView(context);
            dividerImageView.setImageResource(rs_divider_line);
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
        resetAllDayLayout();
        clearAllEvents();
        resetNowTimeViews();
        startUIUpdateThread();
    }

    private void resetAllDayLayout(){
        if (this.topAllDayLayout.getVisibility() != GONE){
            this.topAllDayLayout.setVisibility(GONE);
            ((FrameLayout.LayoutParams)this.scrollContainerView.getLayoutParams()).setMargins(0,0,0,0);
        }
    }

    private void resetNowTimeViews(){
        localAnimationLayout.removeView(nowTime);
        localAnimationLayout.removeView(nowTimeLine);

        MyCalendar tempCal = new MyCalendar(this.myCalendar);
        for (int dayOffset = 0; dayOffset < this.getDisplayLen(); dayOffset++) {
            if (tempCal.isToday()) {
                addNowTimeLine();
                break;
            }
            tempCal.setOffsetByDate(1);
        }
    }

    private void startUIUpdateThread(){
        uiUpdateThread = new Thread(){
            @Override
            public void run() {
                while (true){
                    try {
                        Thread.sleep(5000);
                        uiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                resetNowTimeViews();
                            }
                        });
                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                        Thread.currentThread().interrupt();
                    }
                }
            }
        };
        uiUpdateThread.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.uiUpdateThread != null && !this.uiUpdateThread.isInterrupted()){
            this.uiUpdateThread.interrupt();
        }
    }

    /**
     * just clear the events in the layout
     */
    public void clearAllEvents() {
        eventController.clearAllEvents();
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

        int lineMarin_top = getNowTimeLinePst();
        nowTime.setText(localTime);
        nowTime.setTextSize(10);
        RelativeLayout.LayoutParams paramsText = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paramsText.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        paramsText.addRule(RelativeLayout.ALIGN_BOTTOM, nowTimeLine.getId());
        int textPadding = DensityUtil.dip2px(context, 5);
        nowTime.setPadding(textPadding / 2, 0, textPadding / 2, 0);
        nowTime.setLayoutParams(paramsText);
        nowTime.setTextColor(context.getResources().getColor(color_nowtime));
        nowTime.setBackgroundColor(context.getResources().getColor(color_nowtime_bg));
        localAnimationLayout.addView(nowTime);

        RelativeLayout.LayoutParams nowTimeLineParams =
                new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT
                        , ViewGroup.LayoutParams.WRAP_CONTENT);
        nowTimeLine.setImageResource(rs_nowtime_line);
        nowTimeLineParams.topMargin = lineMarin_top;
        nowTimeLineParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        nowTimeLineParams.addRule(RelativeLayout.RIGHT_OF, nowTime.getId());
        nowTimeLine.setLayoutParams(nowTimeLineParams);
        localAnimationLayout.addView(nowTimeLine);
    }

    private int getNowTimeLinePst() {
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        Date currentLocalTime = cal.getTime();
        DateFormat date = new SimpleDateFormat("HH:mm");
        String localTime = date.format(currentLocalTime);
        String[] converted = localTime.split(":");
        int hour = Integer.valueOf(converted[0]);
        int minutes = Integer.valueOf(converted[1]);
        int nearestPst = nearestTimeSlotValue(hour + (float) minutes / 100);
//        int correctPst = (minutes % 15) * ((lineHeight / 4) / 15);
        return nearestPst;
    }

    protected int getEventContainerIndex(WrapperEvent wrapper){
        long dayLong = (24 * 60 * 60 * 1000);
        long todayBegin = this.myCalendar.getBeginOfDayMilliseconds();
        long startTime = wrapper.getEvent().getStartTime();
        long endTime = wrapper.getEvent().getEndTime();
        long fromDayBegin = wrapper.getFromDayBegin();
        int regularIndex;

        switch (getRegularEventType(wrapper)){
            case REGULAR:
                regularIndex = (int)(Math.floor((float)(startTime - todayBegin)/ dayLong));
                return regularIndex;
            case DAY_CROSS_BEGIN:
                regularIndex = (int)(Math.floor((float)(startTime - todayBegin)/ dayLong));
                return regularIndex;
            case DAY_CROSS_ALL_DAY:
                regularIndex = (int)(Math.floor((float)(fromDayBegin - todayBegin)/ dayLong));
                return regularIndex;
            case DAY_CROSS_END:
                regularIndex = (int)(Math.floor((float)(endTime - todayBegin)/ dayLong));
                return regularIndex;
            default:
                regularIndex = (int)(Math.floor((float)(startTime - todayBegin)/ dayLong));
                return regularIndex;
        }
    }

    protected int getRegularEventType(WrapperEvent wrapper){
        long startTime = wrapper.getEvent().getStartTime();
        long endTime = wrapper.getEvent().getEndTime();
        long fromDayBegin = wrapper.getFromDayBegin();
//        long todayBegin = this.myCalendar.getBeginOfDayMilliseconds();
//        long todayEnd = this.myCalendar.getEndOfDayMilliseconds();
        long todayBegin = fromDayBegin;
        long todayEnd = fromDayBegin + allDayMilliseconds;

        //regular
        if (startTime >= todayBegin && endTime <= todayEnd){
            return REGULAR;
        }

        //Begin part
        if (startTime > todayBegin && endTime > todayEnd){
            return DAY_CROSS_BEGIN;
        }

        //All day part
        if (fromDayBegin == todayBegin && endTime > todayEnd){
            return DAY_CROSS_ALL_DAY;
        }
        //End part
        if (startTime < todayBegin && endTime > todayBegin){
            return DAY_CROSS_END;
        }

        return UNDEFINED;
    }

    protected int getContainerIndex(long startTime){
        long today = this.myCalendar.getBeginOfDayMilliseconds();
        long dayLong = (24 * 60 * 60 * 1000);

        return (int)(Math.floor((float)(startTime - today)/ dayLong));
    }

    /**
     * set publc for other
     *
     * @param eventPackage
     */
    public void setEventList(ITimeEventPackageInterface eventPackage) {
        this.eventController.setEventList(eventPackage);
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
        eventController.showSingleEventAnim(event);
    }

    private void showSingleTimeslotAnim(ITimeTimeSlotInterface timeslot){
        timeSlotController.showSingleTimeslotAnim(timeslot);
    }

    /********************************* For base view use *************************************/

    protected int[] reComputePositionToSet(int actualX, int actualY, View draggableObj, View container) {
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

    protected int nearestTimeSlotValue(float time) {
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

    protected void scrollViewAutoScroll(DragEvent event) {
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
        if (scrollContainerView.getHeight() == 0){
            scrollContainerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    scrollContainerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    scrollContainerView.scrollTo(scrollContainerView.getScrollX(),(getStartY));
                }
            });
        }else{
            scrollContainerView.scrollTo(scrollContainerView.getScrollX(), (getStartY));
        }
    }

    protected void msgWindowFollow(int tapX, int tapY, int index, View followView) {
        float toX;
        float toY;

        toY = tapY - followView.getHeight() / 2 - msgWindow.getHeight();
        if (toY <= 0){
            toY = tapY + followView.getHeight() / 2;
        }
        //for msg window in center
//        if (tapX + msgWindow.getWidth() / 2 > dividerBgRLayout.getWidth()) {
//            toX = dividerBgRLayout.getWidth() - msgWindow.getWidth();
//        } else if (tapX - msgWindow.getWidth() / 2 < 0) {
//            toX = 0;
//        } else {
//            toX = tapX - msgWindow.getWidth() / 2;
//        }
        //for left
        toX = 0;
        if (tapX < msgWindow.getWidth() * 1.5){
            toX = dividerBgRLayout.getWidth() - msgWindow.getWidth();
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

    public Calendar getCurrentTime(){
        if (this.scrollContainerView != null && this.myCalendar != null){
            int nowY = this.scrollContainerView.getScrollY();
            //update the event time
            String new_time = positionToTimeTreeMap.get(nearestTimeSlotKey(nowY));
            if (new_time == null){
                return null;
            }
            //important! update event time after drag
            String[] time_parts = new_time.split(":");
            int hour = Integer.valueOf(time_parts[0]);
            int minute = Integer.valueOf(time_parts[1]);

            MyCalendar cal = new MyCalendar(this.myCalendar);
            cal.setHour(hour);
            cal.setMinute(minute);

//            return cal.getCalendar().getTimeInMillis();
            return cal.getCalendar();
        }

        return null;
    }

    public TimeSlotController getTimeSlotController() {
        return timeSlotController;
    }

    public EventController getEventController() {
        return eventController;
    }

    public void showAllSlotAnim(){
        this.timeSlotController.showAllSlotAnim();
    }

    /********************************* For common Listener use *************************************/

    public interface OnBodyTouchListener {
        boolean bodyOnTouchListener(float tapX, float tapY);
    }

    public void setOnBodyTouchListener(OnBodyTouchListener onBodyTouchListener) {
        this.onBodyTouchListener = onBodyTouchListener;
    }

    public void setOnBodyListener(EventController.OnEventListener onEventListener) {
        this.eventController.setOnEventListener(onEventListener);
    }

    public <E extends ITimeEventInterface> void setEventClassName(Class<E> className) {
        this.eventController.setEventClassName(className);
    }

    public void setCurrentTempView(DraggableEventView tempDragView){
        this.tempDragView = tempDragView;
    }

    /************************** For time slot view *************************************************/

    public void clearTimeSlots(){
        timeSlotController.clearTimeSlots();
    }

    public void resetTimeSlotViews(){
        timeSlotController.resetTimeSlotViews();
    }

    public void addSlot(WrapperTimeSlot wrapper, boolean animate){
        timeSlotController.addSlot(wrapper,animate);
    }

    public void updateTimeSlotsDuration(long duration, boolean animate){
        timeSlotController.updateTimeSlotsDuration(duration, animate);
    }

    public void enableTimeSlot(){
        timeSlotController.enableTimeSlot();
    }

    public void setOnTimeSlotListener(TimeSlotController.OnTimeSlotListener onTimeSlotListener) {
        timeSlotController.setOnTimeSlotListener(onTimeSlotListener);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return onBodyTouchListener != null && onBodyTouchListener.bodyOnTouchListener(ev.getX(), ev.getY());
    }

    public void removeOptListener(){
        isRemoveOptListener = true;
        for (int i = 0; i < displayLen; i++) {
            //remove previous listeners
            eventLayouts.get(i).setOnDragListener(null);
            eventLayouts.get(i).setOnLongClickListener(null);

            for (int j = 0; j < eventLayouts.get(i).getChildCount(); j++) {
                if (eventLayouts.get(i).getChildAt(j) instanceof DraggableEventView
                        ||
                        eventLayouts.get(i).getChildAt(j) instanceof DraggableTimeSlotView
                        ){
                    eventLayouts.get(i).getChildAt(j).setOnLongClickListener(null);
                }
            }
        }
    }
}
