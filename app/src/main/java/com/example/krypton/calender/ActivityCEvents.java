package com.example.krypton.calender;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;
import com.google.common.collect.MapMaker;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

import static com.example.krypton.calender.ActivityAPI.*;

public class ActivityCEvents extends ActivityAPI {



    public void setEvent() {

  /*    Event event = new Event()
                .setSummary("Google I/O 2015");
             //   .setLocation("800 Howard St., San Francisco, CA 94103")
              //  .setDescription("A chance to hear more about Google's developer products.");

        DateTime startDateTime = new DateTime("2018-06-28T09:00:00-07:00");
        EventDateTime start = new EventDateTime()
                .setDateTime(startDateTime);
                //.setTimeZone("America/Los_Angeles");
        event.setStart(start);

        DateTime endDateTime = new DateTime("2018-08-29T17:00:00-07:00");
        EventDateTime end = new EventDateTime()
                .setDateTime(endDateTime);
               // .setTimeZone("America/Los_Angeles");
        event.setEnd(end);

        //String[] recurrence = new String[] {"RRULE:FREQ=DAILY;COUNT=2"};
        //event.setRecurrence(Arrays.asList(recurrence));

        //EventAttendee[] attendees = new EventAttendee[] {
          //      new EventAttendee().setEmail("lpage@example.com"),
            //    new EventAttendee().setEmail("sbrin@example.com"),
        //};
        //event.setAttendees(Arrays.asList(attendees));

        //EventReminder[] reminderOverrides = new EventReminder[] {
            //    new EventReminder().setMethod("email").setMinutes(24 * 60),

    //    new EventReminder().setMethod("popup").setMinutes(10),
        //};
        //Event.Reminders reminders = new Event.Reminders()
          //      .setUseDefault(false)
            //    .setOverrides(Arrays.asList(reminderOverrides));
       // event.setReminders(reminders);


        String CID = "primary";
        try {
            event = Ser.events().insert(CID,event).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.printf("Event created: %s\n", event.getHtmlLink());
*/
    }


}

