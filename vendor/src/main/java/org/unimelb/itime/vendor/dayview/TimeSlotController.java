package org.unimelb.itime.vendor.dayview;

import android.content.ClipData;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.daasuu.bl.ArrowDirection;
import com.daasuu.bl.BubbleLayout;

import org.unimelb.itime.vendor.listener.ITimeTimeSlotInterface;
import org.unimelb.itime.vendor.unitviews.DraggableEventView;
import org.unimelb.itime.vendor.unitviews.DraggableTimeSlotView;
import org.unimelb.itime.vendor.unitviews.RecommendedSlotView;
import org.unimelb.itime.vendor.util.DensityUtil;
import org.unimelb.itime.vendor.weekview.WeekView;
import org.unimelb.itime.vendor.wrapper.WrapperTimeSlot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

/**
 * Created by yuhaoliu on 9/01/2017.
 */

public class TimeSlotController {
    private static final String TAG = "Tim1eSlotManager";

    private FlexibleLenViewBody container;
    private OnTimeSlotListener onTimeSlotListener;

    private ArrayList<DraggableTimeSlotView> slotViews = new ArrayList<>();
    private ArrayList<RecommendedSlotView> rcdSlotViews = new ArrayList<>();
    private WeekView.OnRcdTimeSlot onRcdTimeSlot;


    TimeSlotController(FlexibleLenViewBody container) {
        this.container = container;
    }

    /**
     * TimeSlotView contains data source(ITimeTimeSlotInterface)
     * and all information about new status
     */
    public interface OnTimeSlotListener {
        //While creating time block
        void onTimeSlotCreate(DraggableTimeSlotView draggableTimeSlotView);
        //While clicking existed time block
        void onTimeSlotClick(DraggableTimeSlotView draggableTimeSlotView);
        //When start dragging
        void onTimeSlotDragStart(DraggableTimeSlotView draggableTimeSlotView);

        /**
         * On dragging
         * @param draggableTimeSlotView : The view on dragging
         * @param x : current X position of View
         * @param y : current Y position of View
         */
        void onTimeSlotDragging(DraggableTimeSlotView draggableTimeSlotView, int x, int y);

        /**
         * When dragging ended
         * @param draggableTimeSlotView : The view on drop
         * @param startTime : dropped X position of View
         * @param endTime : dropped Y position of View
         */
        void onTimeSlotDragDrop(DraggableTimeSlotView draggableTimeSlotView, long startTime, long endTime);

        void onTimeSlotEdit(DraggableTimeSlotView draggableTimeSlotView);
        void onTimeSlotDelete(DraggableTimeSlotView draggableTimeSlotView);
    }

    void setOnTimeSlotListener(OnTimeSlotListener onTimeSlotListener) {
        this.onTimeSlotListener = onTimeSlotListener;
    }

    public void setOnRcdTimeSlot(final WeekView.OnRcdTimeSlot onRcdTimeSlot){
        this.onRcdTimeSlot = onRcdTimeSlot;
    }

    void onTimeSlotEdit(DraggableTimeSlotView slotView){
        if (this.onTimeSlotListener != null){
            this.onTimeSlotListener.onTimeSlotEdit(slotView);
        }
    }

    void onTimeSlotDelete(DraggableTimeSlotView slotView){
        if (this.onTimeSlotListener != null){
            this.onTimeSlotListener.onTimeSlotDelete(slotView);
        }
    }

    private class OnRcdClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            if (onRcdTimeSlot != null){
                onRcdTimeSlot.onClick((RecommendedSlotView) v);
            }
        }
    }

    class CreateTimeSlotListener implements View.OnLongClickListener {

        @Override
        public boolean onLongClick(View v) {
            DayInnerBodyEventLayout container = (DayInnerBodyEventLayout) v;
            TimeSlotController.this.container.tempDragView = createTimeSlotView(new WrapperTimeSlot(null));
            DayInnerBodyEventLayout.LayoutParams params = (DayInnerBodyEventLayout.LayoutParams)TimeSlotController.this.container.tempDragView.getLayoutParams();
            params.top = (int) TimeSlotController.this.container.nowTapY;
            container.addView(TimeSlotController.this.container.tempDragView);

            TimeSlotController.this.container.tempDragView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    TimeSlotController.this.container.tempDragView.performLongClick();
                }
            }, 100);

            return true;
        }
    }

    class TimeSlotDragListener implements View.OnDragListener {
        int index = 0;
        int currentEventNewHour = -1;
        int currentEventNewMinutes = -1;

        TimeSlotDragListener(int index) {
            this.index = index;
        }

        @Override
        public boolean onDrag(View v, DragEvent event) {
            DraggableTimeSlotView tsView = (DraggableTimeSlotView) event.getLocalState();

            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    break;
                case DragEvent.ACTION_DRAG_LOCATION:
                    int rawX = (int) (container.layoutWidthPerDay * index + event.getX());

                    container.scrollViewAutoScroll(event);

                    if (onTimeSlotListener != null) {
                        onTimeSlotListener.onTimeSlotDragging(tsView, rawX, (int) event.getY());
                    } else {
                        Log.i(TAG, "onDrag: null onEventDragListener");
                    }
                    container.msgWindowFollow(rawX, (int) event.getY(), index, (View) event.getLocalState());
                    break;
                case DragEvent.ACTION_DRAG_ENTERED:
                    container.msgWindow.setVisibility(VISIBLE);
                    if (tsView.getType() == DraggableEventView.TYPE_TEMP){
                        container.tempDragView = tsView;
                    }else{
                        container.tempDragView= null;
                    }

                    break;
                case DragEvent.ACTION_DRAG_EXITED:
                    container.msgWindow.setVisibility(INVISIBLE);
                    container.tempDragView = null;
                    break;
                case DragEvent.ACTION_DROP:
                    //handler ended things in here, because ended some time is not triggered
                    View finalView = (View) event.getLocalState();
                    finalView.setVisibility(VISIBLE);
                    container.msgWindow.setVisibility(INVISIBLE);

                    float actionStopX = event.getX();
                    float actionStopY = event.getY();
                    // Dropped, reassign View to ViewGroup
                    int newX = (int) actionStopX - tsView.getWidth() / 2;
                    int newY = (int) actionStopY - tsView.getHeight() / 2;
                    int[] reComputeResult = container.reComputePositionToSet(newX, newY, tsView, v);

                    //update the event time
                    String new_time = container.positionToTimeTreeMap.get(reComputeResult[1]);
                    //important! update event time after drag
                    String[] time_parts = new_time.split(":");
                    currentEventNewHour = Integer.valueOf(time_parts[0]);
                    currentEventNewMinutes = Integer.valueOf(time_parts[1]);
//
                    tsView.getCalendar().setHour(currentEventNewHour);
                    tsView.getCalendar().setMinute(currentEventNewMinutes);
//                    //set dropped container index
                    tsView.setIndexInView(index);

                    if (container.tempDragView == null && onTimeSlotListener != null) {
                        onTimeSlotListener.onTimeSlotDragDrop(tsView, 0, 0);
                    } else {
                        Log.i(TAG, "onDrop Not Called");
                    }

                    if (tsView.getType() == DraggableEventView.TYPE_TEMP) {
                        ViewGroup parent = (ViewGroup) tsView.getParent();
                        if(parent != null){
                            parent.removeView(tsView);
                        }
                        //important! update event time after drag via listener
                        if (onTimeSlotListener != null) {
                            onTimeSlotListener.onTimeSlotCreate(tsView);
                        }
                        //finally reset tempDragView to NULL.
                        container.tempDragView = null;
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

    private class TimeSlotLongClickListener implements View.OnLongClickListener {
        @Override
        public boolean onLongClick(View view) {
            ClipData data = ClipData.newPlainText("", "");
            View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(
                    view);
            view.startDrag(data, shadowBuilder, view, 0);
            view.setVisibility(VISIBLE);
            if (container.tempDragView != null) {
                view.setVisibility(INVISIBLE);
            } else {
                view.setVisibility(VISIBLE);
            }
            view.getBackground().setAlpha(255);
            return false;
        }
    }

    private DraggableTimeSlotView createTimeSlotView(WrapperTimeSlot wrapper){
        DraggableTimeSlotView draggableTimeSlotView = new DraggableTimeSlotView(container.context, wrapper);
        if (wrapper.getTimeSlot() != null){
            ITimeTimeSlotInterface timeslot = wrapper.getTimeSlot();
            draggableTimeSlotView.setType(DraggableTimeSlotView.TYPE_NORMAL);
            draggableTimeSlotView.setTimes(timeslot.getStartTime(), timeslot.getEndTime());
            draggableTimeSlotView.setIsSelected(wrapper.isSelected());
            DayInnerBodyEventLayout.LayoutParams params = new DayInnerBodyEventLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, container.layoutWidthPerDay);
            draggableTimeSlotView.setLayoutParams(params);
        }else {
            long duration = this.slotViews.size() == 0 ? 3600 * 1000 : this.slotViews.get(0).getDuration();
            draggableTimeSlotView.setDuration(duration);
            int tempViewHeight = (int)(duration/((float)(3600*1000)) * container.lineHeight);
            draggableTimeSlotView.setType(DraggableTimeSlotView.TYPE_TEMP);
            DayInnerBodyEventLayout.LayoutParams params = new DayInnerBodyEventLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, tempViewHeight);
            draggableTimeSlotView.setLayoutParams(params);
        }

        if (wrapper.getTimeSlot() != null && wrapper.isAnimated()){
            draggableTimeSlotView.showAlphaAnim();
        }

        draggableTimeSlotView.setOnLongClickListener(new TimeSlotLongClickListener());
        draggableTimeSlotView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DraggableTimeSlotView draggableTimeSlotView = (DraggableTimeSlotView) v;
                showTimeSlotTools(draggableTimeSlotView);

                if (onTimeSlotListener != null){
                    onTimeSlotListener.onTimeSlotClick(draggableTimeSlotView);
                }
            }
        });

        return draggableTimeSlotView;
    }

    private void showTimeSlotTools(DraggableTimeSlotView slotView){
        BubbleLayout bubble = container.bubble;
        Object tag = bubble.getTag();
        if (tag != null && tag == slotView){
            //which means second time to click same slot
            bubble.setVisibility(View.GONE);
            bubble.setTag(null);
            return;
        }else {
            //which means its clicked the different slot
            bubble.setTag(slotView);
        }

        int buttonLoc[] = {0, 0};
        slotView.getLocationOnScreen(buttonLoc);
        float posX = buttonLoc[0];
        float posY = slotView.getY();
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) bubble.getLayoutParams();
        int topMargin = (int)posY - params.height;
        params.topMargin = topMargin>0?topMargin:0;
        params.leftMargin = (int)posX;

        bubble.setVisibility(View.VISIBLE);
        bubble.requestLayout();

    }

    private RecommendedSlotView createRcdTimeSlotView(WrapperTimeSlot wrapper){
        RecommendedSlotView recommendedSlotView = new RecommendedSlotView(container.context, wrapper);
        recommendedSlotView.setOnClickListener(new OnRcdClickListener());
        if (wrapper.getTimeSlot() != null){
            DayInnerBodyEventLayout.LayoutParams params = new DayInnerBodyEventLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, container.layoutWidthPerDay);
            recommendedSlotView.setLayoutParams(params);
        }

//        draggableTimeSlotView.setOnLongClickListener(new TimeSlotLongClickListener());
//        draggableTimeSlotView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                DraggableTimeSlotView draggableTimeSlotView = (DraggableTimeSlotView) v;
//
//                if (onTimeSlotListener != null){
//                    onTimeSlotListener.onTimeSlotClick(draggableTimeSlotView);
//                }
//
//            }
//        });

        return recommendedSlotView;
    }


    void enableTimeSlot(){
        container.isTimeSlotEnable = true;
        for (int i = 0; i < container.displayLen; i++) {
            //remove previous listeners
            container.eventLayouts.get(i).setOnDragListener(new TimeSlotDragListener(i));
            container.eventLayouts.get(i).setOnLongClickListener(new CreateTimeSlotListener());

            for (int j = 0; j < container.eventLayouts.get(i).getChildCount(); j++) {
                if (container.eventLayouts.get(i).getChildAt(j) instanceof DraggableEventView){
                    container.eventLayouts.get(i).getChildAt(j).setOnLongClickListener(null);

                }
            }
        }
    }

    void addSlot(WrapperTimeSlot wrapper, boolean animate){
        int offset = container.getContainerIndex(wrapper.getTimeSlot().getStartTime());

        if (offset < container.displayLen && offset > -1){
            DraggableTimeSlotView draggableTimeSlotView = createTimeSlotView(wrapper);
            container.eventLayouts.get(offset).addView(draggableTimeSlotView, draggableTimeSlotView.getLayoutParams());
            draggableTimeSlotView.bringToFront();
            draggableTimeSlotView.setVisibility(VISIBLE);
            resizeTimeSlot(draggableTimeSlotView,animate);
            slotViews.add(draggableTimeSlotView);
            draggableTimeSlotView.requestLayout();
        }

        if (container.rightArrowVisibility == WeekView.TIMESLOT_AUTO){
            if (container.rightArrow!= null && offset >= container.displayLen){
                container.rightArrow.setVisibility(VISIBLE);
            }
        }

        if (container.leftArrowVisibility == WeekView.TIMESLOT_AUTO){
            if (container.leftArrow!= null && offset <= -1){
                container.leftArrow.setVisibility(VISIBLE);
            }
        }
    }


    void addRecommended(WrapperTimeSlot wrapper){
        int offset = container.getContainerIndex(wrapper.getTimeSlot().getStartTime());

        if (offset < container.displayLen && offset > -1){
            RecommendedSlotView rcdSlotView = createRcdTimeSlotView(wrapper);
            container.eventLayouts.get(offset).addView(rcdSlotView, rcdSlotView.getLayoutParams());
            rcdSlotView.bringToFront();
            rcdSlotView.setVisibility(VISIBLE);
            resizeRcdTimeSlot(rcdSlotView);
            rcdSlotViews.add(rcdSlotView);
            rcdSlotView.requestLayout();
        }
    }

    void updateTimeSlotsDuration(long duration, boolean animate){
        for (DraggableTimeSlotView tsV : this.slotViews
                ) {
            int offset = container.getContainerIndex(tsV.getNewStartTime());
            long startTime = tsV.getNewStartTime();

            tsV.setTimes(startTime, startTime + duration);

            if (offset < container.displayLen && offset > -1){
                resizeTimeSlot(tsV,animate);
            }
        }
    }

    void clearTimeSlots(){
        for (DraggableTimeSlotView draggableTimeSlotView :slotViews
                ) {
            ViewGroup parent = (ViewGroup) draggableTimeSlotView.getParent();
            if (parent != null){
                parent.removeView(draggableTimeSlotView);
            }
        }

        this.slotViews.clear();

        for (RecommendedSlotView rcdView : rcdSlotViews
                ) {
            ViewGroup parent = (ViewGroup) rcdView.getParent();
            if (parent != null){
                parent.removeView(rcdView);
            }
        }

        rcdSlotViews.clear();
    }

    void resetTimeSlotViews(){
        for (DraggableTimeSlotView draggableTimeSlotView :slotViews
                ) {
            draggableTimeSlotView.resetView();
        }
    }

    void showSingleTimeslotAnim(ITimeTimeSlotInterface timeslot){
        final DraggableTimeSlotView timeslotViewDraggable = findTimeslotView(slotViews, timeslot);
        if (timeslotViewDraggable !=null){
            timeslotViewDraggable.showAlphaAnim();
        }
    }

    void showAllSlotAnim(){
        for (int i = 0; i < slotViews.size(); i++) {
            slotViews.get(i).showAlphaAnim();
        }
    }

    void timeSlotAnimationChecker(){
        boolean topShow = false;
        boolean bottomShow = false;

        Rect scrollBounds = new Rect();
        container.scrollContainerView.getHitRect(scrollBounds);

        for (int i = 0; i < slotViews.size(); i++) {
            DraggableTimeSlotView slotview = slotViews.get(i);

            if (!slotview.getLocalVisibleRect(scrollBounds)) {
                //hiding
                if (scrollBounds.bottom <= 0){
                    topShow = true;
                }else{
                    bottomShow = true;
                }

                slotview.onScreen = false;
            } else {
                if (!slotview.onScreen && slotview.getWrapper().isAnimated()){
                    slotview.showAlphaAnim();
                }

                if (!slotview.getWrapper().isRead()){
                    slotview.getWrapper().setRead(true);
                }
                //showing
                slotview.onScreen = true;
            }

            if (topShow && bottomShow){
                break;
            }
        }

        if (container.topArrowVisibility == WeekView.TIMESLOT_AUTO){
            container.topArrow.setVisibility(topShow?VISIBLE:INVISIBLE);
        }

        if (container.bottomArrowVisibility == WeekView.TIMESLOT_AUTO){
            container.bottomArrow.setVisibility(bottomShow?VISIBLE:INVISIBLE);
        }
    }

    private void resizeTimeSlot(DraggableTimeSlotView draggableTimeSlotView, boolean animate){
        final DayInnerBodyEventLayout.LayoutParams params = (DayInnerBodyEventLayout.LayoutParams) draggableTimeSlotView.getLayoutParams();
        long duration = draggableTimeSlotView.getDuration();
        final int slotHeight = getSlotHeight(duration);
        final int topMargin = getSlotTopMargin(draggableTimeSlotView.getNewStartTime());

        ((DayInnerBodyEventLayout.LayoutParams) draggableTimeSlotView.getLayoutParams()).top = topMargin;

        if (animate){
            ResizeAnimation resizeAnimation = new ResizeAnimation(
                    draggableTimeSlotView,
                    slotHeight,
                    ResizeAnimation.Type.HEIGHT,
                    600
            );

            draggableTimeSlotView.startAnimation(resizeAnimation);
        }else {
            params.height = slotHeight;
        }
    }

    private void resizeRcdTimeSlot(RecommendedSlotView rcd){
        long duration = rcd.getWrapper().getTimeSlot().getEndTime() - rcd.getWrapper().getTimeSlot().getStartTime();
        final int slotHeight = getSlotHeight(duration);
        final int topMargin = getSlotTopMargin(rcd.getWrapper().getTimeSlot().getStartTime());

        ((DayInnerBodyEventLayout.LayoutParams) rcd.getLayoutParams()).height = slotHeight;
        ((DayInnerBodyEventLayout.LayoutParams) rcd.getLayoutParams()).top = topMargin;
    }

    private int getSlotHeight(long duration){
        final int slotHeight = (int) (((float) duration / (3600 * 1000)) * container.lineHeight);
        return slotHeight;
    }

    private int getSlotTopMargin(long startTime){
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        String hourWithMinutes = sdf.format(new Date(startTime));
        String[] components = hourWithMinutes.split(":");
        float trickTime = Integer.valueOf(components[0]) + (float) Integer.valueOf(components[1]) / 100;
        final int topMargin = container.nearestTimeSlotValue(trickTime);

        return topMargin;
    }

    private DraggableTimeSlotView findTimeslotView(ArrayList<DraggableTimeSlotView> draggableTimeSlotViews, ITimeTimeSlotInterface timeslot){
        for (DraggableTimeSlotView timeslotViewDraggable : draggableTimeSlotViews
                ) {
            ITimeTimeSlotInterface slot = timeslotViewDraggable.getTimeslot();
            if (slot != null && slot.getTimeslotUid().equals(timeslot.getTimeslotUid())){
                return timeslotViewDraggable;
            }
        }
        return null;
    }

}
