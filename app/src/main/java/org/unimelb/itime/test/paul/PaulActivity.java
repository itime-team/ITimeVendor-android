package org.unimelb.itime.test.paul;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;


import com.google.android.gms.common.api.GoogleApiClient;

import org.unimelb.itime.test.R;
import org.unimelb.itime.test.bean.Event;
import org.unimelb.itime.test.bean.TimeSlot;
import org.unimelb.itime.test.david.DBManager;
import org.unimelb.itime.test.david.EventManager;
import org.unimelb.itime.vendor.dayview.EventController;
import org.unimelb.itime.vendor.dayview.TimeSlotController;
import org.unimelb.itime.vendor.unitviews.DraggableEventView;
import org.unimelb.itime.vendor.unitviews.DraggableTimeSlotView;
import org.unimelb.itime.vendor.weekview.WeekView;
import org.unimelb.itime.vendor.wrapper.WrapperTimeSlot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

public class PaulActivity extends AppCompatActivity {

    private static final int PICK_PHOTO = 1;
    private static final String TAG = "MyAPP";
    private List<String> mResults;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    private Event event;

    private ArrayList<TimeSlot> slots = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paul);
//        loadData();

        Button back = (Button) findViewById(R.id.back);
        final WeekView weekView = (WeekView) findViewById(R.id.week_view);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                weekView.backToToday();
            }
        });
        weekView.setEventClassName(Event.class);
        weekView.setOnBodyOuterListener(new EventController.OnEventListener() {
            @Override
            public boolean isDraggable(DraggableEventView eventView) {
                return false;
            }

            @Override
            public void onEventCreate(DraggableEventView eventView) {
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(eventView.getStartTimeM());
            }

            @Override
            public void onEventClick(DraggableEventView eventView) {
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(eventView.getEvent().getStartTime());
            }

            @Override
            public void onEventDragStart(DraggableEventView eventView) {

            }

            @Override
            public void onEventDragging(DraggableEventView eventView, int x, int y) {

            }

            @Override
            public void onEventDragDrop(DraggableEventView eventView) {
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(eventView.getStartTimeM());
            }

        });
        weekView.enableTimeSlot();
//        weekView.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                weekView.showTimeslotAnim(slots);
//            }
//        },5000);
        weekView.setDayEventMap(EventManager.getInstance().getEventsMap());

        weekView.setOnTimeSlotOuterListener(new TimeSlotController.OnTimeSlotListener() {
            @Override
            public void onTimeSlotCreate(final DraggableTimeSlotView draggableTimeSlotView) {
                // popup timeslot create page
//                TimeSlot timeSlot = new TimeSlot();
//                timeSlot.setTimeSlotUid(UUID.randomUUID().toString());
//                timeSlot.setStartTime(draggableTimeSlotView.getNewStartTime());
//                timeSlot.setEndTime(draggableTimeSlotView.getNewEndTime());
//                weekView.addTimeSlot(timeSlot);
//                weekView.reloadTimeSlots(false);
//                slots.add(timeSlot);
            }

            @Override
            public void onTimeSlotClick(DraggableTimeSlotView draggableTimeSlotView) {
            }

            @Override
            public void onTimeSlotDragStart(DraggableTimeSlotView draggableTimeSlotView) {

            }

            @Override
            public void onTimeSlotDragging(DraggableTimeSlotView draggableTimeSlotView, int i, int i1) {

            }

            @Override
            public void onTimeSlotDragDrop(DraggableTimeSlotView draggableTimeSlotView, long startTime, long endTime) {
                if (draggableTimeSlotView.getTimeslot() != null){
                    draggableTimeSlotView.getTimeslot().setStartTime(startTime);
                    draggableTimeSlotView.getTimeslot().setEndTime(endTime);
//                    weekView.reloadTimeSlots(false);
                }

//                for (TimeSlot slot : slots
//                     ) {
//                    weekView.showTimeslotAnim(slot);
//                }

            }

        });

        for (int i = 0; i < 1; i++) {
            long interval = i * 24 * 3600 * 1000;
            TimeSlot slot = new TimeSlot();
            Calendar calendar = Calendar.getInstance();
            slot.setStartTime(interval + calendar.getTimeInMillis());
            slot.setEndTime(interval + calendar.getTimeInMillis()+ 3600*1000);
            slot.setTimeSlotUid(UUID.randomUUID().toString());
            this.slots.add(slot);
            WrapperTimeSlot slotwrapper = new WrapperTimeSlot(slot);
            slotwrapper.setAnimated(true);
            weekView.addTimeSlot(slotwrapper);
//            weekView.showTimeslotAnim(slot);
        }

//        weekView.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                weekView.resetTimeSlots();
//
//                for (TimeSlot slot : slots
//                        ) {
//
//                    weekView.addTimeSlot(slot);
//                }
//
//                weekView.reloadTimeSlots(true);
//            }
//        },3000);

        weekView.reloadTimeSlots(false);

        weekView.postDelayed(new Runnable() {
            @Override
            public void run() {
                weekView.updateTimeSlotsDuration(2*3600*1000,true);
            }
        },5000);

    }

    private void timeslotDrop(DraggableTimeSlotView draggableTimeSlotView, long startTime, long endTime) {
        // update timeslot struct

    }

    private void showResult(ArrayList<String> paths) {
        if (mResults == null) {
            mResults = new ArrayList<String>();

        }
        mResults.clear();
        mResults.addAll(paths);

    }


    private void loadData() {
        List<Event> allEvents = DBManager.getInstance(getApplicationContext()).getAllEvents();
//        EventManager.getInstance().getEventsMap().clearPackage();
        for (Event event : allEvents
                ) {
            this.event = event;
            EventManager.getInstance().addEvent(event);
        }

    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
//        client.connect();
//        Action viewAction = Action.newAction(
//                Action.TYPE_VIEW, // TODO: choose an action type.
//                "Paul Page", // TODO: Define a title for the content shown.
//                // TODO: If you have web page content that matches this app activity's content,
//                // make sure this auto-generated web page URL is correct.
//                // Otherwise, set the URL to null.
//                Uri.parse("http://host/path"),
//                // TODO: Make sure this auto-generated app URL is correct.
//                Uri.parse("android-app://org.unimelb.itime.test.paul/http/host/path")
//        );
//        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
//        Action viewAction = Action.newAction(
//                Action.TYPE_VIEW, // TODO: choose an action type.
//                "Paul Page", // TODO: Define a title for the content shown.
//                // TODO: If you have web page content that matches this app activity's content,
//                // make sure this auto-generated web page URL is correct.
//                // Otherwise, set the URL to null.
//                Uri.parse("http://host/path"),
//                // TODO: Make sure this auto-generated app URL is correct.
//                Uri.parse("android-app://org.unimelb.itime.test.paul/http/host/path")
//        );
//        AppIndex.AppIndexApi.end(client, viewAction);
//        client.disconnect();
    }
}
