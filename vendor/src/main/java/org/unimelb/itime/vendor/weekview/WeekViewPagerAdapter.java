package org.unimelb.itime.vendor.weekview;

import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import org.unimelb.itime.vendor.dayview.FlexibleLenViewBody;
import org.unimelb.itime.vendor.util.MyCalendar;
import org.unimelb.itime.vendor.listener.ITimeEventPackageInterface;
import org.unimelb.itime.vendor.wrapper.WrapperTimeSlot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by yuhaoliu on 22/08/2016.
 */
class WeekViewPagerAdapter extends PagerAdapter {
    public long duration = 3600 * 1000;
    private int startPst = 0;

    private ArrayList<WeekViewUnit> views;
    private List<WrapperTimeSlot> slotsInfo;
    private ITimeEventPackageInterface eventPackage;

    private MyCalendar startCal;
    private int displayLength = 0 ;

    WeekViewPagerAdapter(int startPst, ArrayList<WeekViewUnit> views, int displayLength){
        this.views = views;
        this.startPst =startPst;
        this.displayLength = displayLength;

        Calendar calendar = Calendar.getInstance();
//        int weekOfDay = calendar.get(Calendar.DAY_OF_WEEK);
//        calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) - weekOfDay + 1);
        this.startCal = new MyCalendar(calendar);
    }

    void setSlotsInfo(ArrayList<WrapperTimeSlot> slotsInfo) {
        this.slotsInfo = slotsInfo;
    }

    void enableTimeSlot(){
        for (WeekViewUnit weekView : views) {
            //0 header, 1 divider, 2 means body
            FlexibleLenViewBody bodyView = weekView.getBody();
            bodyView.enableTimeSlot();
        }
    }

    void removeAllOptListener(){
        for (WeekViewUnit weekView : views) {
            FlexibleLenViewBody bodyView = weekView.getBody();
            bodyView.removeOptListener();
        }
    }

    void setDayEventMap(ITimeEventPackageInterface eventPackage) {
        this.eventPackage = eventPackage;
    }

    void updateTimeSlotsDuration(long duration, boolean animate){
        this.duration = duration;
        for (WeekViewUnit weekView : views) {
            FlexibleLenViewBody bodyView = weekView.getBody();
            bodyView.updateTimeSlotsDuration(duration, animate);
        }
    }

    void reloadEvents(){
        for (WeekViewUnit weekView : views) {
            FlexibleLenViewBody bodyView = weekView.getBody();
            if (this.eventPackage != null){
                bodyView.resetViews();
                bodyView.setEventList(this.eventPackage);
            }
        }
    }

    void reloadTimeSlots(boolean animate){
        for (WeekViewUnit weekView : views
                ) {
            FlexibleLenViewBody bodyView = weekView.getBody();
            bodyView.clearTimeSlots();
            if (this.slotsInfo != null){
                for (int j = 0; j < this.slotsInfo.size(); j++) {
                    WrapperTimeSlot struct = this.slotsInfo.get(j);
                    if (struct.isRecommended() && !struct.isSelected()){
                        bodyView.addRcdSlot(struct);
                    }else {
                        bodyView.addSlot(struct,animate);
                    }
                }
            }else {
                Log.i("debug", "slotsInfo: " + ((this.slotsInfo != null) ? "size 0":"null"));
            }
            bodyView.timeSlotAnimationChecker();
        }

//        updateTimeSlotsDuration(this.duration,false);
    }

    FlexibleLenViewBody getViewBodyByPosition(int position){
        WeekViewUnit viewAtPosition = views.get(position % views.size());
        FlexibleLenViewBody nowBody = viewAtPosition.getBody();
        return nowBody;
    }

    @Override
    public int getCount() {
        return startPst*2+1;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        WeekViewUnit view = views.get(position%views.size());
        ViewGroup parent = (ViewGroup) view.getParent();
        if (parent != null){
            parent.removeView(view);
        }

        int dateOffset =(position - startPst) * displayLength;
        this.updateHeader(view, dateOffset);
        this.updateBody(view, dateOffset);

        container.addView(view);

        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    private void updateHeader(ViewGroup parent, int offset){
        MyCalendar cal = new MyCalendar(this.startCal);
        cal.setOffsetByDate(offset);
        ((WeekViewUnit)parent).getHeader().setMyCalendar(cal);
    }

    private void updateBody(ViewGroup parent, int offset){
        int count = parent.getChildCount();
        for (int i = 0; i < count; i++) {
            if (parent.getChildAt(i) instanceof FlexibleLenViewBody){
                MyCalendar cal = new MyCalendar(this.startCal);
                cal.setOffsetByDate(offset);

                final FlexibleLenViewBody nowBody = ((FlexibleLenViewBody)parent.getChildAt(i));
                nowBody.setCalendar(cal);
                nowBody.resetViews();
                nowBody.setEventList(this.eventPackage);
                nowBody.resetAnimationViews();
                nowBody.clearTimeSlots();
                if (this.slotsInfo != null && this.slotsInfo.size() != 0){
                    for (int j = 0; j < this.slotsInfo.size(); j++) {
                        WrapperTimeSlot struct = this.slotsInfo.get(j);
                        if (struct.isRecommended() && !struct.isSelected()){
                            nowBody.addRcdSlot(struct);
                        }else {
                            nowBody.addSlot(struct,false);
                        }
                    }

//                    updateTimeSlotsDuration(duration,false);
                }else {
                    Log.i("debug", "slotsInfo: " + ((this.slotsInfo != null) ? "size 0":"null"));
                }
                nowBody.timeSlotAnimationChecker();
            }
        }
    }

    public int getDisplayLength() {
        return displayLength;
    }

    public void setDisplayLength(int displayLength) {
        this.displayLength = displayLength;
    }
}
