package org.unimelb.itime.test.david;

import android.util.Log;

import com.google.gson.Gson;

import org.unimelb.itime.test.RuleFactory.RuleFactory;
import org.unimelb.itime.test.RuleFactory.RuleModel;
import org.unimelb.itime.test.bean.Event;
import org.unimelb.itime.vendor.listener.ITimeContactInterface;
import org.unimelb.itime.vendor.listener.ITimeEventInterface;
import org.unimelb.itime.vendor.listener.ITimeEventPackageInterface;
import org.unimelb.itime.vendor.listener.ITimeTimeSlotInterface;

import java.io.EOFException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Created by yuhaoliu on 29/08/16.
 */
public class EventManager {
    private final String TAG = "MyAPP";
    private static EventManager ourInstance = new EventManager();

    private List<ITimeEventInterface> allDayEventList = new ArrayList<>();

    private Map<Long, List<ITimeEventInterface>> regularEventMap = new HashMap<>();

    private ArrayList<Event> orgRepeatedEventList = new ArrayList<>();
    private Map<Long, List<ITimeEventInterface>> repeatedEventMap = new HashMap<>();

    //<UUID, List of tracer> : For tracking event on Day of repeated event map
    private Map<String,ArrayList<EventTracer>> uidTracerMap = new HashMap();

    private EventsPackage eventsPackage = new EventsPackage();

    private final int defaultRepeatedRange = 500;

    private Calendar nowRepeatedEndAt = Calendar.getInstance();
    private Calendar nowRepeatedStartAt = Calendar.getInstance();

    private Calendar calendar = Calendar.getInstance();

    final long allDayMilliseconds = 24 * 60 * 60 * 1000;

    public static EventManager getInstance() {
        return ourInstance;
    }

    private EventManager() {
        nowRepeatedStartAt.add(Calendar.DATE, -defaultRepeatedRange);
        nowRepeatedEndAt.add(Calendar.DATE, defaultRepeatedRange);

        eventsPackage.setRepeatedEventMap(repeatedEventMap);
        eventsPackage.setRegularEventMap(regularEventMap);
        eventsPackage.setAllDayEventList(allDayEventList);
    }

    public ITimeEventPackageInterface getEventsMap(){
        return this.eventsPackage;
    }

    public void addEvent(Event event){
//        if (isAllDayEvent(event)){
//            allDayEventList.add(event);
//            return;
//        }
        //if not repeated
        if (event.getRecurrence().length == 0){
            Long startTime = event.getStartTime();
            Long dayBeginMilliseconds = getDayBeginMilliseconds(startTime);

            if (regularEventMap.containsKey(dayBeginMilliseconds)){
                regularEventMap.get(dayBeginMilliseconds).add(event);
            }else {
                regularEventMap.put(dayBeginMilliseconds,new ArrayList<ITimeEventInterface>());
                regularEventMap.get(dayBeginMilliseconds).add(event);
            }
        }else{
            orgRepeatedEventList.add(event);
            this.addRepeatedEvent(event,nowRepeatedStartAt.getTimeInMillis(),nowRepeatedEndAt.getTimeInMillis());
        }
    }

    private boolean isAllDayEvent(ITimeEventInterface event) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(event.getStartTime());
        int hour = cal.get(Calendar.HOUR);
        int minutes = cal.get(Calendar.MINUTE);
        long duration = event.getEndTime() - event.getStartTime();
        boolean isAllDay = hour == 0
                        && minutes == 0
                        && duration >= (allDayMilliseconds * 0.9);

        return isAllDay;
    }

    private void addRepeatedEvent(Event event, long rangeStart, long rangeEnd){
        RuleModel rule = RuleFactory.getInstance().getRuleModel(event);
        event.setRule(rule);

        ArrayList<Long> repeatedEventsTimes = rule.getOccurenceDates(rangeStart,rangeEnd);

        for (Long time: repeatedEventsTimes
                ) {
            Event dup_event = null;
            try {
                dup_event = (Event) event.clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
            if (dup_event == null){
                throw new RuntimeException("Clone error");
            }
            long duration = dup_event.getDurationMilliseconds();
            dup_event.setStartTime(time);
            dup_event.setEndTime(time + duration);

            Long startTime = dup_event.getStartTime();
            Long dayBeginMilliseconds = getDayBeginMilliseconds(startTime);

            EventTracer tracer = new EventTracer(this.repeatedEventMap, dup_event, dayBeginMilliseconds);

            //add event to uuid - tracer map for tracking back to delete on day map.
            if (uidTracerMap.containsKey(event.getEventUid())){
                uidTracerMap.get(event.getEventUid()).add(tracer);
            }else {
                uidTracerMap.put(event.getEventUid(), new ArrayList<EventTracer>());
                uidTracerMap.get(event.getEventUid()).add(tracer);
            }

            //add event to repeated map
            if (repeatedEventMap.containsKey(dayBeginMilliseconds)){
                repeatedEventMap.get(dayBeginMilliseconds).add(dup_event);
            }else {
                repeatedEventMap.put(dayBeginMilliseconds,new ArrayList<ITimeEventInterface>());
                repeatedEventMap.get(dayBeginMilliseconds).add(dup_event);
            }
        }
    }

    public void loadRepeatedEvent(long rangeStart, long rangeEnd){
        this.nowRepeatedStartAt.setTimeInMillis(rangeStart);
        this.nowRepeatedEndAt.setTimeInMillis(rangeEnd);
        for (Event event:orgRepeatedEventList
             ) {
            this.addRepeatedEvent(event, rangeStart, rangeEnd);
        }
    }

    public void removeRepeatedEvent(Event event){
        List<EventTracer> tracers = uidTracerMap.get(event.getEventUid());
        for (EventTracer tracer:tracers
             ) {
            tracer.removeSelfFromRepeatedEventMap();
        }
        uidTracerMap.remove(event.getEventUid());
    }

    public void updateRepeatedEvent(Event event){
        removeRepeatedEvent(event);
        addRepeatedEvent(event,nowRepeatedStartAt.getTimeInMillis(),nowRepeatedEndAt.getTimeInMillis());
    }

    private long getDayBeginMilliseconds(long startTime){
        calendar.setTimeInMillis(startTime);

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND,0);

        return calendar.getTimeInMillis();
    }

    public void updateEvent(Event oldEvent, long newStartTime, long newEndTime){
        // problem here
        long oldBeginTime = this.getDayBeginMilliseconds(oldEvent.getStartTime());
        if (this.regularEventMap.containsKey(oldBeginTime)){
            Log.i(TAG, "oldEvent: " + oldEvent);
            for (ITimeEventInterface event :this.regularEventMap.get(oldBeginTime)
                 ) {
                Log.i(TAG, "event: " + event);
            }
            this.regularEventMap.get(oldBeginTime).remove(oldEvent);
            oldEvent.setStartTime(newStartTime);
            oldEvent.setEndTime(newEndTime);
            this.addEvent(oldEvent);
        }
    }

    private class EventsPackage implements ITimeEventPackageInterface{

        private List<ITimeEventInterface> allDayEventList;
        private Map<Long, List<ITimeEventInterface>> regularEventMap;
        private Map<Long, List<ITimeEventInterface>> repeatedEventMap;

        void setAllDayEventList(List<ITimeEventInterface> allDayEventList) {
            this.allDayEventList = allDayEventList;
        }

        void setRegularEventMap(Map<Long, List<ITimeEventInterface>> regularEventMap) {
            this.regularEventMap = regularEventMap;
        }

        void setRepeatedEventMap(Map<Long, List<ITimeEventInterface>> repeatedEventMap) {
            this.repeatedEventMap = repeatedEventMap;
        }

        public void clearPackage(){
            this.regularEventMap.clear();
            //** here need to handle the linked effect.
//            this.repeatedMap.clear();
        }

        @Override
        public Map<Long, List<ITimeEventInterface>> getRegularEventDayMap() {
            return regularEventMap;
        }

        @Override
        public Map<Long,List<ITimeEventInterface>> getRepeatedEventDayMap() {
            return repeatedEventMap;
        }

        @Override
        public List<ITimeEventInterface> getAllDayEvents() {
            return allDayEventList;
        }
    }

    private class EventTracer{
        private Map<Long, List<ITimeEventInterface>> repeatedEventMap;
        private ITimeEventInterface event;
        private long belongToDayOfBegin;

        EventTracer(Map<Long, List<ITimeEventInterface>> repeatedEventMap
                , ITimeEventInterface event,long belongToDayOfBegin){
            this.repeatedEventMap = repeatedEventMap;
            this.event = event;
            this.belongToDayOfBegin = belongToDayOfBegin;
        }

        void removeSelfFromRepeatedEventMap(){
            repeatedEventMap.get(belongToDayOfBegin).remove(event);
        }
    }
}
