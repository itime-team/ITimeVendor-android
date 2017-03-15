package org.unimelb.itime.test.david;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import org.unimelb.itime.test.R;
import org.unimelb.itime.test.bean.Contact;
import org.unimelb.itime.test.bean.Event;
import org.unimelb.itime.test.bean.Invitee;
import org.unimelb.itime.vendor.agendaview.MonthAgendaView;
import org.unimelb.itime.vendor.dayview.MonthDayView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DavidActivity extends AppCompatActivity {
    private final String TAG= "MyAPP";
    private DBManager dbManager;
    private EventManager eventManager;
    private MonthDayView monthDayView;

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

        doMonthAgendaViewThings();
//        displayAllInvitee();
//        doMonthDayViewThings();
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

    private void doMonthAgendaViewThings(){
        Button back = (Button) findViewById(R.id.back);
        final MonthAgendaView monthDayView = (MonthAgendaView) findViewById(R.id.monthAgendaView);
        monthDayView.setDayEventMap(eventManager.getEventsMap());
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                monthDayView.backToToday();
            }
        });
    }

//    private void doInviteesThings(){
//        InviteeFragment inviteeFragment = new InviteeFragment();
//        getFragmentManager().beginTransaction().add(R.id.fragment, inviteeFragment).commit();
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
//            String[] rec = {"RRULE:FREQ=WEEKLY;INTERVAL=1"};
//            event.setRecurrence(rec);
            this.event = event;
            EventManager.getInstance().addEvent(event);
//            testE = event;
        }
//        final Event e=testE;
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    Event newE = (Event) e.clone();
//                    newE.setEndTime(newE.getNewStartTime() + 60*3600);
//                    EventManager.getInstance().updateRepeatedEvent(newE);
//
//                } catch (CloneNotSupportedException e1) {
//                    e1.printStackTrace();
//                }
//            }
//        }, 5000);

//        EventManager.getInstance().loadRepeatedEvent(nowRepeatedStartAt.getTimeInMillis(),nowRepeatedEndAt.getTimeInMillis());


    }

//    private void doMonthDayViewThings(){
//        final MonthDayView monthDayFragment = (MonthDayView) findViewById(R.id.monthDayView);
//
////        monthDayFragment.postDelayed(new Runnable() {
////            @Override
////            public void run() {
////                Event event = new Event();
////                event.setTitle("new added");
////                event.setNewStartTime(Calendar.getInstance().getTimeInMillis());
////                event.setEndTime(Calendar.getInstance().getTimeInMillis() + 60 * 60 * 1000);
////                EventManager.getInstance().addEvent(event);
////            }
////        },5000);
//    }

//    private void doMonthAgendaViewThings(){
//        MonthAgendaView monthDayFragment = (MonthAgendaView) findViewById(R.id.monthAgendaView);
//
//        monthDayFragment.setDayEventMap(EventManager.getInstance().getEventsMap());
//    }

    private void initDB(){
        Calendar calendar = Calendar.getInstance();
        List<Event> events = new ArrayList<>();
        List<Contact> contacts = initContact();
        int[] type = {0,1,2};
        int[] status = {0,1};
        long interval = 3600 * 1000;
        long startTime = calendar.getTimeInMillis();
        long endTime;
        for (int i = 1; i < 2; i++) {
            endTime = startTime + (3600*1000);
//            long duration = (endTime - startTime);

            Event event = new Event();
            event.setEventUid("" + i);
            event.setTitle("adawdwadwadaw" + i);
            event.setDisplayEventType(0);
            event.setDisplayStatus("#63ADF2|slash|icon_normal");
            event.setLocation("here");
            event.setStartTime(startTime);

            List<Invitee> inviteeList = new ArrayList<>();

            for (Contact contact:contacts
                 ) {
                Invitee invitee1 = new Invitee();
                invitee1.setEventUid("" + i);
                invitee1.setContact(contact);
                invitee1.setInviteeUid(contact.getContactUid());
                inviteeList.add(invitee1);
            }

            dbManager.insertInviteeList(inviteeList);
            event.setInvitee(inviteeList);

            event.setEndTime(endTime);
            events.add(event);

//            startTime= i==2?startTime:endTime;
            startTime = endTime;
//            calendar.setTimeInMillis(startTime + 24*3600*1000);
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
