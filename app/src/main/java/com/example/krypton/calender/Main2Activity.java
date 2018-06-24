package com.example.krypton.calender;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;

import java.io.IOException;
import java.util.Arrays;

public class Main2Activity extends ActivityAPI{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        new Task1().execute();

    }
    class Task1 extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
        }
        @Override
        protected String doInBackground(Void... arg0)
        {

            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            com.google.api.services.calendar.Calendar service = new com.google.api.services.calendar.Calendar.Builder(
                    transport, jsonFactory, mCredential)
                    .setApplicationName("com.example.krypton.calender")
                    .build();


           Event event = new Event()
                    .setSummary("Event- April 2016");
             //       .setLocation("Dhaka")
               //     .setDescription("New Event 1");

            DateTime startDateTime = new DateTime("2018-06-27T18:10:00+06:00");
            EventDateTime start = new EventDateTime()
                    .setDateTime(startDateTime);
            //.setTimeZone("Asia/Dhaka");
            event.setStart(start);

            DateTime endDateTime = new DateTime("2018-06-28T18:40:00+06:00");
            EventDateTime end = new EventDateTime()
                    .setDateTime(endDateTime);
                    //.setTimeZone("Asia/Dhaka");
            event.setEnd(end);
/*
            String[] recurrence = new String[]{"RRULE:FREQ=DAILY;COUNT=2"};
            event.setRecurrence(Arrays.asList(recurrence));

            EventAttendee[] attendees = new EventAttendee[]{
                    new EventAttendee().setEmail("abir@aksdj.com"),
                    new EventAttendee().setEmail("asdasd@andlk.com"),
            };
            event.setAttendees(Arrays.asList(attendees));

            EventReminder[] reminderOverrides = new EventReminder[]{
                    new EventReminder().setMethod("email").setMinutes(24 * 60),
                    new EventReminder().setMethod("popup").setMinutes(10),
            };
            Event.Reminders reminders = new Event.Reminders()
                    .setUseDefault(false)
                    .setOverrides(Arrays.asList(reminderOverrides));
            event.setReminders(reminders);
*/
           String calendarId = "primary";
          try {
              event = service.events().insert(calendarId, event).execute();
           } catch (IOException e) {
               e.printStackTrace();
           }
            System.out.printf("Event created: %s\n", event.getHtmlLink());


            return null;
        }

        @Override
        protected void onPostExecute(String result)
        {
            super.onPostExecute(result);

        }
    }
}
