package org.unimelb.itime.vendor.dayview;

import org.unimelb.itime.vendor.agendaview.AgendaViewBody;
import org.unimelb.itime.vendor.agendaview.MonthAgendaView;
import org.unimelb.itime.vendor.weekview.WeekView;

/**
 * Created by yuhaoliu on 27/03/2017.
 */

public class Config {
    //For common
    private int lineHeight;
    private EventController.OnEventListener OnBodyOuterListener;
    private TimeSlotController.OnTimeSlotListener onTimeSlotOuterListener;
    //For month day view
    private MonthDayView.OnFlexibleBodyScroll onFlexibleBodyScroll;
    private MonthDayView.OnHeaderListener onMonthDayViewHeaderListener;
    //For week view
    private WeekView.OnHeaderListener onWeekViewHeaderListener;
    private WeekView.OnFlexScroll onFlexScroll;
    //For agenda month day view
    private MonthAgendaView.OnHeaderListener onMonthAgendaViewHeaderListener;
    private AgendaViewBody.OnEventClickListener onEventClickListener;

    public Config() {
    }

    public int getLineHeight() {
        return lineHeight;
    }

    public void setLineHeight(int lineHeight) {
        this.lineHeight = lineHeight;
    }

    public MonthDayView.OnFlexibleBodyScroll getOnFlexibleBodyScroll() {
        return onFlexibleBodyScroll;
    }

    public void setOnFlexibleBodyScroll(MonthDayView.OnFlexibleBodyScroll onFlexibleBodyScroll) {
        this.onFlexibleBodyScroll = onFlexibleBodyScroll;
    }

    public EventController.OnEventListener getOnBodyOuterListener() {
        return OnBodyOuterListener;
    }

    public void setOnBodyOuterListener(EventController.OnEventListener onBodyOuterListener) {
        OnBodyOuterListener = onBodyOuterListener;
    }

    public TimeSlotController.OnTimeSlotListener getOnTimeSlotOuterListener() {
        return onTimeSlotOuterListener;
    }

    public void setOnTimeSlotOuterListener(TimeSlotController.OnTimeSlotListener onTimeSlotOuterListener) {
        this.onTimeSlotOuterListener = onTimeSlotOuterListener;
    }

    public WeekView.OnHeaderListener getOnWeekViewHeaderListener() {
        return onWeekViewHeaderListener;
    }

    public void setOnWeekViewHeaderListener(WeekView.OnHeaderListener onWeekViewHeaderListener) {
        this.onWeekViewHeaderListener = onWeekViewHeaderListener;
    }

    public WeekView.OnFlexScroll getOnFlexScroll() {
        return onFlexScroll;
    }

    public void setOnFlexScroll(WeekView.OnFlexScroll onFlexScroll) {
        this.onFlexScroll = onFlexScroll;
    }

    public MonthAgendaView.OnHeaderListener getOnMonthAgendaViewHeaderListener() {
        return onMonthAgendaViewHeaderListener;
    }

    public void setOnMonthAgendaViewHeaderListener(MonthAgendaView.OnHeaderListener onMonthAgendaViewHeaderListener) {
        this.onMonthAgendaViewHeaderListener = onMonthAgendaViewHeaderListener;
    }

    public AgendaViewBody.OnEventClickListener getOnEventClickListener() {
        return onEventClickListener;
    }

    public void setOnEventClickListener(AgendaViewBody.OnEventClickListener onEventClickListener) {
        this.onEventClickListener = onEventClickListener;
    }

    public MonthDayView.OnHeaderListener getOnMonthDayViewHeaderListener() {
        return onMonthDayViewHeaderListener;
    }

    public void setOnMonthDayViewHeaderListener(MonthDayView.OnHeaderListener onMonthDayViewHeaderListener) {
        this.onMonthDayViewHeaderListener = onMonthDayViewHeaderListener;
    }
}

