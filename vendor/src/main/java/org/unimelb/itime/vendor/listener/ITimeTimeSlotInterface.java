package org.unimelb.itime.vendor.listener;

/**
 * Created by yuhaoliu on 10/09/2016.
 */
public interface ITimeTimeSlotInterface {
    void setStartTime(long startTime);
    long getStartTime();
    void setEndTime(long endTime);
    long getEndTime();

    void setStatus(String status);
    String getStatus();

    int getAcceptedNum();
    void setAcceptedNum(int num);

    int getTotalNum();
    void setTotalNum(int num);

    String getTimeslotUid();

    boolean isRecommended();
}
