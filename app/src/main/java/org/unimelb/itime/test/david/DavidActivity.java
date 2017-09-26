package org.unimelb.itime.test.david;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import org.unimelb.itime.test.R;
import org.unimelb.itime.test.bean.Contact;
import org.unimelb.itime.test.bean.Event;
import org.unimelb.itime.test.bean.Invitee;
import org.unimelb.itime.test.bean.TimeSlot;
import org.unimelb.itime.vendor.dayview.MonthDayView;
import org.unimelb.itime.vendor.dayview.TimeSlotController;
import org.unimelb.itime.vendor.unitviews.DraggableTimeSlotView;
import org.unimelb.itime.vendor.unitviews.RecommendedSlotView;
import org.unimelb.itime.vendor.unitviews.TimeSlotInnerCalendarView;
import org.unimelb.itime.vendor.weekview.WeekView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class DavidActivity extends AppCompatActivity {
    private final String TAG= "MyAPP";
    private DBManager dbManager;
    private EventManager eventManager;
    private MonthDayView monthDayView;
    private WeekView weekView;

    private ArrayList<TimeSlot> slots = new ArrayList<>();

    private Event event;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_david);

        eventThing();
    }
    private void eventThing(){
        dbManager = DBManager.getInstance(this);
        eventManager = EventManager.getInstance();
        initData();
        loadData();
//        doInviteesThings();

        doTimeSlotThings();
//        doMonthAgendaViewThings();
//        displayAllInvitee();
//        doMonthDayViewThings();
    }

    private void doTimeSlotThings(){
        initSlots();

        weekView = (WeekView) findViewById(R.id.weekview_david);
        weekView.setDayEventMap(eventManager.getEventsMap());
        weekView.enableTimeSlot();
        weekView.setOnTimeSlotInnerCalendar(new TimeSlotInnerCalendarView.OnTimeSlotInnerCalendar() {
            @Override
            public void onCalendarBtnClick(View v, boolean result) {

            }

            @Override
            public void onDayClick(Date dateClicked) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(dateClicked);
                weekView.scrollTo(cal);
            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
            }
        });
        weekView.setOnTimeSlotOuterListener(new TimeSlotController.OnTimeSlotListener() {
            @Override
            public void onTimeSlotCreate(DraggableTimeSlotView draggableTimeSlotView) {
                
            }

            @Override
            public void onTimeSlotClick(DraggableTimeSlotView draggableTimeSlotView) {
                weekView.reloadTimeSlots(false);
            }

            @Override
            public void onRcdTimeSlotClick(RecommendedSlotView v) {
                v.getWrapper().setSelected(true);
                weekView.reloadTimeSlots(false);
            }

            @Override
            public void onTimeSlotDragStart(DraggableTimeSlotView draggableTimeSlotView) {

            }

            @Override
            public void onTimeSlotDragging(DraggableTimeSlotView draggableTimeSlotView, int x, int y) {

            }

            @Override
            public void onTimeSlotDragDrop(DraggableTimeSlotView draggableTimeSlotView, long startTime, long endTime) {

            }

            @Override
            public void onTimeSlotEdit(DraggableTimeSlotView draggableTimeSlotView) {
                Log.i(TAG, "onTimeSlotEdit: ");
            }

            @Override
            public void onTimeSlotDelete(DraggableTimeSlotView draggableTimeSlotView) {
                Log.i(TAG, "onTimeSlotDelete: ");
            }   
        });
//        weekView.setOnRcdTimeSlot(new WeekView.OnRcdTimeSlot() {
//            @Override
//            public void onClick(RecommendedSlotView v) {
//                v.getWrapper().setSelected(true);
//                weekView.reloadTimeSlots(false);
//            }
//        });

        HashMap<String, Integer> numSlot = new HashMap<>();


        for (TimeSlot slot:slots
             ) {
            weekView.addTimeSlot(slot);
        }
    }

    private void initSlots(){
        Calendar cal = Calendar.getInstance();
        long startTime = cal.getTimeInMillis();
        long duration = 3*3600*1000;
        for (int i = 0; i < 10; i++) {
            TimeSlot slot = new TimeSlot();
            slot.setStartTime(startTime);
            slot.setEndTime(startTime+duration);
            slot.setRecommended(true);
            slots.add(slot);

            startTime += 5 * 3600 * 1000;
        }
    }

//    private void doMonthDayViewThings(){
//        Button back = (Button) findViewById(R.id.back);
//        monthDayView = (MonthDayView) findViewById(R.id.monthDayView);
//        monthDayView.setDayEventMap(eventManager.getEventsMap());
//        monthDayView.setEventClassName(Event.class);
//        monthDayView.setOnHeaderListener(new MonthDayView.OnHeaderListener() {
//            @Override
//            public void onMonthChanged(MyCalendar calendar) {
//                Log.i(TAG, "onMonthChanged: " + calendar.getCalendar().getTime());
//            }
//        });
//        monthDayView.setOnBodyOuterListener(new EventController.OnEventListener() {
//            @Override
//            public boolean isDraggable(DraggableEventView eventView) {
//                return true;
//            }
//
//            @Override
//            public void onEventCreate(DraggableEventView eventView) {
//                Calendar cal = Calendar.getInstance();
//                cal.setTimeInMillis(eventView.getStartTimeM());
//                Log.i(TAG, "onEventCreate: s" + cal.getTime());
//                cal.setTimeInMillis(eventView.getEndTimeM());
//                Log.i(TAG, "onEventCreate: " + cal.getTime());
//
////                monthDayView.scrollToWithOffset(eventView.getStartTimeM());
//                monthDayView.reloadEvents();
//            }
//
//            @Override
//            public void onEventClick(DraggableEventView eventView) {
//                Calendar cal = Calendar.getInstance();
//                cal.setTimeInMillis(eventView.getStartTimeM());
//
//                EventManager.getInstance().updateEvent((Event) eventView.getEvent(),10,10);
//            }
//
//            @Override
//            public void onEventDragStart(DraggableEventView eventView) {
//                eventView.setEvent(new Event());
//            }
//
//            @Override
//            public void onEventDragging(DraggableEventView eventView, int x, int y) {
//
//            }
//
//            @Override
//            public void onEventDragDrop(DraggableEventView eventView) {
//                Calendar cal = Calendar.getInstance();
//                cal.setTimeInMillis(eventView.getStartTimeM());
//                Log.i(TAG, "onEventDragDrop: s" + cal.getTime());
//                cal.setTimeInMillis(eventView.getEndTimeM());
//                Log.i(TAG, "onEventDragDrop: " + cal.getTime());
//
////                monthDayView.scrollToWithOffset(eventView.getStartTimeM());
//            }
//
//        });
//
//        back.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
////                monthDayView.reloadEvents();
//                loadData();
//            }
//        });
//    }

//    private void doMonthAgendaViewThings(){
//        Button back = (Button) findViewById(R.id.back);
//        final MonthAgendaView monthDayView = (MonthAgendaView) findViewById(R.id.monthAgendaView);
//        monthDayView.setDayEventMap(eventManager.getEventsMap());
//        back.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                monthDayView.backToToday();
//            }
//        });
//    }

    private void initData(){
        this.dbManager.clearDB();
        this.initDB();
    }

    private void loadData(){
        List<Event> allEvents = dbManager.getAllEvents();
        EventManager.getInstance().getEventsMap().clearPackage();
        Event testE = null;
        for (Event event: allEvents
             ) {
            this.event = event;
            EventManager.getInstance().addEvent(event);
        }
    }

    private void initDB(){
        Calendar calendar = Calendar.getInstance();
        List<Event> events = new ArrayList<>();
        List<Contact> contacts = initContact();
        int[] type = {0,1,2};
        int[] status = {0,1};
        long interval = 3600 * 1000;
        long startTime = calendar.getTimeInMillis();
        long endTime;
        for (int i = 1; i < 3; i++) {
            endTime = startTime + (3600*1000);

            Event event = new Event();
            event.setEventUid("" + i);
            event.setTitle("adawdwadwadaw" + i);
            event.setDisplayEventType(0);
            event.setDisplayStatus("#63ADF2|slash|icon_normal");
            event.setLocation("adawdwadwadawadawdwadwadawadawdwadwadawadawdwadwadawadawdwadwadawadawdwadwadawadawdwadwadawadawdwadwadawadawdwadwadawadawdwadwadawadawdwadwadawadawdwadwadawadawdwadwadawadawdwadwadawadawdwadwadawadawdwadwadawadawdwadwadawadawdwadwadaw");
            event.setStartTime(startTime);

            List<Invitee> inviteeList = new ArrayList<>();

            if (i != 2){
                for (Contact contact:contacts
                        ) {
                    Invitee invitee1 = new Invitee();
                    invitee1.setEventUid("" + i);
                    invitee1.setContact(contact);
                    invitee1.setInviteeUid(contact.getContactUid());
                    inviteeList.add(invitee1);
                }
            }

            dbManager.insertInviteeList(inviteeList);

            event.setEndTime(endTime);
            events.add(event);
            startTime = endTime;
        }

        dbManager.insertEventList(events);
    }

    private List<Contact> initContact(){
        List<Contact> contacts = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            Contact contact = new Contact(""+i, "http://img.zybus.com/uploads/allimg/131213/1-131213111353.jpg", "name " + i);
            contacts.add(contact);
            dbManager.insertContact(contact);
        }

        return contacts;
    }
}
