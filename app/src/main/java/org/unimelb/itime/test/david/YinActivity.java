package org.unimelb.itime.test.david;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.unimelb.itime.test.R;
import org.unimelb.itime.test.bean.Contact;
import org.unimelb.itime.test.bean.Event;
import org.unimelb.itime.test.bean.Invitee;
import org.unimelb.itime.vendor.agendaview.MonthAgendaView;
import org.unimelb.itime.vendor.dayview.EventController;
import org.unimelb.itime.vendor.dayview.MonthDayView;
import org.unimelb.itime.vendor.dayview.New_MonthView;
import org.unimelb.itime.vendor.unitviews.DraggableEventView;
import org.unimelb.itime.vendor.util.MyCalendar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class YinActivity extends AppCompatActivity {
    private final String TAG= "MyAPP";
    private DBManager dbManager;
    private EventManager eventManager;
    private New_MonthView monthDayView;

    private Event event;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yin);

        eventThing();
    }
    private void eventThing(){
        dbManager = DBManager.getInstance(this);
        eventManager = EventManager.getInstance();
        initData();
        loadData();
//        doInviteesThings();

        doMonthDayViewThings();
    }

    private void doMonthDayViewThings(){
        Button back = (Button) findViewById(R.id.back);
        monthDayView = (New_MonthView) findViewById(R.id.monthDayView);
    }

    private void initData(){
        this.dbManager.clearDB();
        this.initDB();
    }

    private void loadData() {
        List<Event> allEvents = dbManager.getAllEvents();
        EventManager.getInstance().getEventsMap().clearPackage();
        Event testE = null;
        for (Event event : allEvents
                ) {
//            String[] rec = {"RRULE:FREQ=WEEKLY;INTERVAL=1"};
//            event.setRecurrence(rec);
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
        for (int i = 1; i < 10; i++) {
            endTime = startTime + (3600*1000);

            Event event = new Event();
            event.setEventUid("" + i);
            event.setTitle("adawdwadwadaw" + i);
            event.setDisplayEventType(1);
            event.setDisplayStatus("#63ADF2|slash|icon_normal");
            event.setLocation("here");
            event.setStartTime(startTime);

            List<Invitee> inviteeList = new ArrayList<>();

            Invitee invitee1 = new Invitee();
            invitee1.setEventUid("" + i);
            invitee1.setContact(contacts.get(0));
            invitee1.setInviteeUid(contacts.get(0).getContactUid());
            inviteeList.add(invitee1);

            Invitee invitee2 = new Invitee();
            invitee2.setEventUid("" + i);
            invitee2.setContact(contacts.get(1));
            invitee2.setInviteeUid(contacts.get(1).getContactUid());
            inviteeList.add(invitee2);

            dbManager.insertInviteeList(inviteeList);
            event.setInvitee(inviteeList);

            event.setEndTime(endTime);
            events.add(event);

            startTime = startTime + 24*3600*1000;
        }

        dbManager.insertEventList(events);
    }

    private List<Contact> initContact(){
        List<Contact> contacts = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            Contact contact = new Contact(""+i, "http://img.zybus.com/uploads/allimg/131213/1-131213111353.jpg", "name " + i);
            contacts.add(contact);
            dbManager.insertContact(contact);
        }

        return contacts;
    }
}
