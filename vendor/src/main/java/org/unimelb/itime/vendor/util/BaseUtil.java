package org.unimelb.itime.vendor.util;

import org.unimelb.itime.vendor.listener.ITimeEventInterface;

import java.util.Calendar;

/**
 * Created by yuhaoliu on 14/03/2017.
 */

public class BaseUtil {

    public static long getAllDayLong(long withInDayTime){
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(withInDayTime);
        MyCalendar myCal = new MyCalendar(cal);
        return myCal.getEndOfDayMilliseconds() - myCal.getBeginOfDayMilliseconds();
    }

    public static boolean isAllDayEvent(ITimeEventInterface event) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(event.getStartTime());
        int hour = cal.get(Calendar.HOUR);
        int minutes = cal.get(Calendar.MINUTE);
        long duration = event.getEndTime() - event.getStartTime();
        boolean isAllDay = hour == 0
                && minutes == 0
                && duration >= (getAllDayLong(event.getStartTime()) * 0.9);

        return isAllDay;
    }
}
