package org.unimelb.itime.vendor.wrapper;

import org.unimelb.itime.vendor.listener.ITimeTimeSlotInterface;

/**
 * Created by yuhaoliu on 4/01/2017.
 */

public class WrapperTimeSlot {
    private ITimeTimeSlotInterface timeSlot = null;
    private boolean isSelected = false;
    private boolean isAnimated = true;
    private boolean isRead = false;

    public WrapperTimeSlot(ITimeTimeSlotInterface timeSlot) {
        this.timeSlot = timeSlot;
    }

    public WrapperTimeSlot copyWrapperTimeslot(WrapperTimeSlot wrapperTimeSlot){
        WrapperTimeSlot wrapper = new WrapperTimeSlot(wrapperTimeSlot.getTimeSlot());
        wrapper.setSelected(wrapperTimeSlot.isSelected());
        wrapper.setRead(wrapperTimeSlot.isRead());
        wrapper.setAnimated(wrapperTimeSlot.isAnimated());
        return wrapper;
    }

    public boolean isAnimated() {
        return isAnimated;
    }

    public void setAnimated(boolean animated) {
        isAnimated = animated;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public ITimeTimeSlotInterface getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(ITimeTimeSlotInterface timeSlot) {
        this.timeSlot = timeSlot;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }
}
