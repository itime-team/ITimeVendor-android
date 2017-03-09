package org.unimelb.itime.vendor.dayview;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.ClipData;
import android.content.Context;
import android.util.Log;
import android.util.Pair;
import android.view.DragEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import org.unimelb.itime.vendor.helper.CalendarEventOverlapHelper;
import org.unimelb.itime.vendor.helper.DensityUtil;
import org.unimelb.itime.vendor.helper.MyCalendar;
import org.unimelb.itime.vendor.listener.ITimeEventInterface;
import org.unimelb.itime.vendor.listener.ITimeEventPackageInterface;
import org.unimelb.itime.vendor.unitviews.DraggableEventView;
import org.unimelb.itime.vendor.wrapper.WrapperEvent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yuhaoliu on 9/01/2017.
 */

public class EventController {
    private static final String TAG = "EventController";

    private FlexibleLenViewBody container;
    private Context context;
    private OnEventListener onEventListener;

    private Map<ITimeEventInterface, Integer> regularEventViewMap = new HashMap<>();
    private Map<ITimeEventInterface, DraggableEventView> uidDragViewMap = new HashMap<>();
    private ArrayList<DraggableEventView> allDayDgEventViews = new ArrayList<>();

    private CalendarEventOverlapHelper xHelper = new CalendarEventOverlapHelper();

    private Class<?> eventClassName;

    EventController(FlexibleLenViewBody container) {
        this.container = container;
        this.context = container.getContext();
    }

    void setEventList(ITimeEventPackageInterface eventPackage) {
        this.clearAllEvents();
        List<ITimeEventInterface> allDayEventList = eventPackage.getAllDayEvents();
        Map<Long, List<ITimeEventInterface>> regularDayEventMap = eventPackage.getRegularEventDayMap();
        Map<Long, List<ITimeEventInterface>> repeatedDayEventMap = eventPackage.getRepeatedEventDayMap();

        MyCalendar tempCal = new MyCalendar(container.myCalendar);
        for (int i = 0; i < container.displayLen; i++) {
            long startTime = tempCal.getBeginOfDayMilliseconds();

            if (allDayEventList != null){
                for (ITimeEventInterface allDayEvent: allDayEventList
                     ) {
                    if (this.isWithin(allDayEvent, i)) {
                        this.addAllDayEvent(allDayEvent, i);
                    }
                }
            }

            if (regularDayEventMap != null && regularDayEventMap.containsKey(startTime)){
                List<ITimeEventInterface> currentDayEvents = regularDayEventMap.get(startTime);
                for (ITimeEventInterface event : currentDayEvents) {
                        this.addRegularEvent(event);
                }
            }

            if (repeatedDayEventMap != null && repeatedDayEventMap.containsKey(startTime)){
                List<ITimeEventInterface> currentDayEvents = repeatedDayEventMap.get(startTime);
                for (ITimeEventInterface event : currentDayEvents) {
                    this.addRegularEvent(event);
                }
            }

            tempCal.setOffsetByDate(1);
        }

        for (DayInnerBodyEventLayout eventLayout:container.eventLayouts
                ) {
            calculateEventLayout(eventLayout);
        }
    }

//    private void addEvent(ITimeEventInterface event) {
//        boolean isTodayAllDayEvent = isWithin(event) && isAllDayEvent(event);
//
//        if (isTodayAllDayEvent) {
//            addAllDayEvent(event);
//        } else {
//            addRegularEvent(event);
//        }
//    }

    private void addAllDayEvent(ITimeEventInterface event, int index) {
        if (container.topAllDayLayout.getVisibility() != View.VISIBLE){
            container.topAllDayLayout.setVisibility(View.VISIBLE);
            ((FrameLayout.LayoutParams)container.getScrollView().getLayoutParams()).setMargins(0,container.topAllDayHeight,0,0);
        }
        int offset = index;
        Log.i(TAG, "offset: " + offset);
        if (offset > -1 && offset < container.displayLen) {
            DraggableEventView new_dgEvent = this.createDayDraggableEventView(event, true);
            DayInnerHeaderEventLayout allDayEventLayout = container.allDayEventLayouts.get(offset);
            allDayEventLayout.addView(new_dgEvent);
            allDayEventLayout.getDgEvents().add(new_dgEvent);
            allDayEventLayout.getEvents().add(event);
        }else {
            Log.i(TAG, "event in header offset error: " + offset);
        }
    }

    private void addRegularEvent(WrapperEvent event) {
        int offset = container.getEventContainerIndex(event.getStartTime(),event.getEndTime(),fromDayBegin);
        if (offset < container.displayLen && offset > -1){
            final DayInnerBodyEventLayout eventLayout = container.eventLayouts.get(offset);
            final DraggableEventView newDragEventView = this.createDayDraggableEventView(event, false);
            final DraggableEventView.LayoutParams params = (DraggableEventView.LayoutParams) newDragEventView.getLayoutParams();

            newDragEventView.setId(View.generateViewId());
            this.regularEventViewMap.put(event, newDragEventView.getId());

            eventLayout.addView(newDragEventView, params);
            eventLayout.getEvents().add(event);
            eventLayout.getDgEvents().add(newDragEventView);
        }else {
            Log.i(TAG, "event in body offset error: " + offset);
        }
    }

    private boolean isWithin(ITimeEventInterface event, int index){
        long startTime = event.getStartTime();
        long endTime = event.getEndTime();

        MyCalendar calS = new MyCalendar(container.getCalendar());
        calS.setOffsetByDate(index);

        MyCalendar calE = new MyCalendar(container.getCalendar());
        calE.setOffsetByDate(index);
        calE.setHour(23);
        calE.setMinute(59);

        long todayStartTime =  calS.getBeginOfDayMilliseconds();
        long todayEndTime =  calE.getCalendar().getTimeInMillis();

        return
                todayEndTime >= startTime && todayStartTime <= endTime;
    }

    void clearAllEvents() {

        if (container.topAllDayEventLayouts != null) {
            for (DayInnerHeaderEventLayout allDayEventLayout:container.allDayEventLayouts
                    ) {
                allDayEventLayout.resetView();
            }
        }

        if (container.eventLayout != null) {
            for (DayInnerBodyEventLayout eventLayout:container.eventLayouts
                    ) {
                eventLayout.resetView();
            }
        }

        this.regularEventViewMap.clear();
        this.allDayDgEventViews.clear();
        this.uidDragViewMap.clear();
    }

    private DraggableEventView createDayDraggableEventView(ITimeEventInterface event, boolean isAllDayEvent) {
        DraggableEventView event_view = new DraggableEventView(context, event, isAllDayEvent);
        event_view.setType(DraggableEventView.TYPE_NORMAL);
        int padding = DensityUtil.dip2px(context,1);
        event_view.setPadding(0,padding,0,padding);
        if (!container.isTimeSlotEnable){
            event_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (onEventListener != null) {
                        onEventListener.onEventClick((DraggableEventView) view);
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
            int eventHeight =(int) (duration * container.heightPerMillisd);

            DraggableEventView.LayoutParams params = new DraggableEventView.LayoutParams(eventHeight, eventHeight);
            if (!container.isTimeSlotEnable){
                event_view.setOnLongClickListener(new EventLongClickListener());
            }
            event_view.setTag(event);
            event_view.setLayoutParams(params);
        }

        //add it to map
        uidDragViewMap.put(event, event_view);

        return event_view;
    }

    private DraggableEventView createTempDayDraggableEventView(float tapX, float tapY) {
        ITimeEventInterface event = this.initializeEvent();
        if (event == null) {
            throw new RuntimeException("need Class name in 'setEventClassName()'");
        }
        DraggableEventView event_view = new DraggableEventView(context, event, false);
        event_view.setType(DraggableEventView.TYPE_TEMP);
        int padding = DensityUtil.dip2px(context,1);
        event_view.setPadding(0,padding,0,0);

        int eventHeight = 1 * container.lineHeight;//one hour
        DraggableEventView.LayoutParams params = new DraggableEventView.LayoutParams(200, eventHeight);
        event_view.setY(tapY - eventHeight / 2);
        event_view.setOnLongClickListener(new EventLongClickListener());
        event_view.setLayoutParams(params);

        return event_view;
    }


    /**
     * calculate the position of event
     * it needs to be called when setting event or event position changed
     */
    private void calculateEventLayout(DayInnerBodyEventLayout eventLayout) {
        List<ArrayList<Pair<Pair<Integer, Integer>, ITimeEventInterface>>> overlapGroups
                = xHelper.computeOverlapXForEvents(eventLayout.getEvents());
        for (ArrayList<Pair<Pair<Integer, Integer>, ITimeEventInterface>> overlapGroup : overlapGroups
                ) {
            for (int i = 0; i < overlapGroup.size(); i++) {

                int startY = getEventY(overlapGroup.get(i).second);
                int widthFactor = overlapGroup.get(i).first.first;
                int startX = overlapGroup.get(i).first.second;
                int topMargin = startY;
                DraggableEventView eventView = (DraggableEventView) eventLayout.findViewById(regularEventViewMap.get(overlapGroup.get(i).second));
                eventView.setPosParam(new DraggableEventView.PosParam(startY, startX, widthFactor, topMargin));
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(eventView.getEvent().getStartTime());
            }
        }
    }

    private int getEventY(ITimeEventInterface event) {
        int type = container.getRegularEventType(event.getStartTime(),event.getEndTime());
        int offset = 0;

        Date date;

        switch (type){
            case FlexibleLenViewBody.REGULAR:
                date = new Date(event.getStartTime());
                break;
            case FlexibleLenViewBody.DAY_CROSS_BEGIN:
                date = new Date(event.getStartTime());
                break;
            case FlexibleLenViewBody.DAY_CROSS_END:
                // use end time as start point then minus self height
                long duration = event.getEndTime() - event.getStartTime();
                int eventHeight =(int) (duration * container.heightPerMillisd);
                offset = - (eventHeight);
                date = new Date(event.getEndTime());
                break;
            default:
                date = new Date(event.getStartTime());
                break;
        }

        String hourWithMinutes = container.sdf.format(date);
        String[] components = hourWithMinutes.split(":");
        float trickTime = Integer.valueOf(components[0]) + Integer.valueOf(components[1]) / (float) 100;
        int getStartY = container.nearestTimeSlotValue(trickTime) + offset;

        return getStartY;
    }

    /****************************************************************************************/

    private class EventLongClickListener implements View.OnLongClickListener {
        @Override
        public boolean onLongClick(final View view) {

            if (container.tempDragView != null || onEventListener !=null && onEventListener.isDraggable((DraggableEventView) view)){
                ClipData data = ClipData.newPlainText("", "");
                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(
                        view);
                if (container.tempDragView != null) {
                    view.setVisibility(View.INVISIBLE);
                } else {
                    view.setVisibility(View.VISIBLE);
                }
                YoYo.with(Techniques.Bounce).
                        pivot(YoYo.CENTER_PIVOT, YoYo.CENTER_PIVOT)
                        .interpolate(new AccelerateDecelerateInterpolator())
                        .duration(1000)
                        .playOn(view);

                ValueAnimator alpha = ValueAnimator.ofObject(new ArgbEvaluator(), DraggableEventView.OPACITY_INT, 255);
                alpha.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                    @Override
                    public void onAnimationUpdate(ValueAnimator animator) {
                        view.getBackground().setAlpha((int) animator.getAnimatedValue());
                    }

                });
                alpha.setDuration(500);
                alpha.start();
                view.startDrag(data, shadowBuilder, view, 0);
            }
            return false;
        }
    }

    class EventDragListener implements View.OnDragListener {
        int index = 0;
        int currentEventNewHour = -1;
        int currentEventNewMinutes = -1;

        EventDragListener(int index) {
            this.index = index;
        }

        @Override
        public boolean onDrag(View v, DragEvent event) {
            DraggableEventView dgView = (DraggableEventView) event.getLocalState();
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    break;
                case DragEvent.ACTION_DRAG_LOCATION:
                    int rawX = (int) (container.layoutWidthPerDay * index + event.getX());
                    container.scrollViewAutoScroll(event);

                    if (onEventListener != null) {
                        onEventListener.onEventDragging(dgView, rawX, (int) event.getY());
                    } else {
                        Log.i(TAG, "onDrag: null onEventDragListener");
                    }
                    container.msgWindowFollow(rawX, (int) event.getY(), index, (View) event.getLocalState());
                    break;
                case DragEvent.ACTION_DRAG_ENTERED:
                    container.msgWindow.setVisibility(View.VISIBLE);
                    if (dgView.getType() == DraggableEventView.TYPE_TEMP){
                        container.tempDragView = dgView;
                    }else{
                        container.tempDragView= null;
                    }
                    break;
                case DragEvent.ACTION_DRAG_EXITED:
                    container.msgWindow.setVisibility(View.INVISIBLE);
                    container.tempDragView = null;
                    break;
                case DragEvent.ACTION_DROP:
                    //handler ended things in here, because ended some time is not triggered
                    dgView.getBackground().setAlpha(DraggableEventView.OPACITY_INT);
                    View finalView = (View) event.getLocalState();
                    finalView.getBackground().setAlpha(DraggableEventView.OPACITY_INT);
                    finalView.setVisibility(View.VISIBLE);
                    container.msgWindow.setVisibility(View.INVISIBLE);

                    float actionStopX = event.getX();
                    float actionStopY = event.getY();
                    // Dropped, reassign View to ViewGroup
                    int newX = (int) actionStopX - dgView.getWidth() / 2;
                    int newY = (int) actionStopY - dgView.getHeight() / 2;
                    int[] reComputeResult = container.reComputePositionToSet(newX, newY, dgView, v);

                    //update the event time
                    String new_time = container.positionToTimeTreeMap.get(reComputeResult[1]);
                    //important! update event time after drag
                    String[] time_parts = new_time.split(":");
                    currentEventNewHour = Integer.valueOf(time_parts[0]);
                    currentEventNewMinutes = Integer.valueOf(time_parts[1]);

                    dgView.getNewCalendar().setHour(currentEventNewHour);
                    dgView.getNewCalendar().setMinute(currentEventNewMinutes);
                    //set dropped container index
                    dgView.setIndexInView(index);

                    if (container.tempDragView == null && onEventListener != null) {
                        onEventListener.onEventDragDrop(dgView);
                    } else {
                        Log.i(TAG, "onDrop Not Called");
                    }

                    if (dgView.getType() == DraggableEventView.TYPE_TEMP) {
                        ViewGroup parent = (ViewGroup) dgView.getParent();
                        if(parent != null){
                            parent.removeView(dgView);
                        }
                        //important! update event time after drag via listener
                        if (onEventListener != null) {
                            onEventListener.onEventCreate(dgView);
                        }
                        //finally reset tempDragView to NULL.
                        container.tempDragView = null;
                    }
                    Log.i(TAG, "onDrag: drop " + index);
                    break;
                case DragEvent.ACTION_DRAG_ENDED:
                    if (dgView != null){
                        dgView.getBackground().setAlpha(DraggableEventView.OPACITY_INT);
                    }
                    break;
            }

            return true;
        }
    }

    class CreateEventListener implements View.OnLongClickListener {

        @Override
        public boolean onLongClick(View v) {
//            if (container.tempDragView == null) {
                DayInnerBodyEventLayout container = (DayInnerBodyEventLayout) v;
                EventController.this.container.tempDragView = createTempDayDraggableEventView(EventController.this.container.nowTapX, EventController.this.container.nowTapY);
                EventController.this.container.tempDragView.setAlpha(0);
                container.addView(EventController.this.container.tempDragView);
//
                EventController.this.container.tempDragView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        EventController.this.container.tempDragView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        ObjectAnimator scaleX = ObjectAnimator.ofFloat(EventController.this.container.tempDragView, "scaleX", 0f,1f);
                        ObjectAnimator scaleY = ObjectAnimator.ofFloat(EventController.this.container.tempDragView, "scaleY", 0f,1f);
                        ObjectAnimator alpha = ObjectAnimator.ofFloat(EventController.this.container.tempDragView, "alpha", 0f,1f);
                        alpha.setDuration(180);
                        scaleX.setDuration(120);
                        scaleY.setDuration(120);

                        AnimatorSet scaleDown = new AnimatorSet();
                        scaleDown.play(alpha).with(scaleY).with(scaleX);
                        scaleX.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                View p= (View) EventController.this.container.tempDragView.getParent();
                                if (p != null){
                                    p.invalidate();
                                }
                            }
                        });
                        scaleDown.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                if (EventController.this.container.tempDragView != null
                                        && EventController.this.container.tempDragView.getParent() != null){
                                    EventController.this.container.tempDragView.performLongClick();
                                }
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {

                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {

                            }
                        });

                        scaleDown.start();
                    }
                });
//            }

            return true;
        }
    }

    <E extends ITimeEventInterface> void setEventClassName(Class<E> className) {
        eventClassName = className;
    }

    private ITimeEventInterface initializeEvent() {
        try {
            ITimeEventInterface t = (ITimeEventInterface) eventClassName.newInstance();
            return t;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * DayDraggableEventView contains data source and all information about new status
     */
    public interface OnEventListener {
        //If current event view is draggable
        boolean isDraggable(DraggableEventView eventView);
        //while creating event view
        void onEventCreate(DraggableEventView eventView);
        //while clicking event
        void onEventClick(DraggableEventView eventView);
        //When start dragging
        void onEventDragStart(DraggableEventView eventView);
        //On dragging
        void onEventDragging(DraggableEventView eventView, int x, int y);
        //When dragging ended
        void onEventDragDrop(DraggableEventView eventView);
    }

    void setOnEventListener(OnEventListener onEventListener) {
        this.onEventListener = onEventListener;
    }

    void showSingleEventAnim(ITimeEventInterface event){
        final DraggableEventView eventView = this.uidDragViewMap.get(event);
        if (eventView!=null){
            eventView.showAlphaAnim();
        }
    }
}
