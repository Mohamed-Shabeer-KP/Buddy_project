package com.boss.buddy.CalendarEvent;


import android.Manifest;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.Gravity;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.boss.buddy.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;
import com.google.api.services.calendar.model.Events;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class EventActivity extends Activity implements EasyPermissions.PermissionCallbacks {
    private GoogleAccountCredential mCredential;
    private TextView DispEvent;
    private ProgressDialog mProgress;

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = { CalendarScopes.CALENDAR };
    private int Flag;
    private int SubNo;

    /**
     * Create the main activity.
     * @param savedInstanceState previously saved instance data.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);
        DispEvent =findViewById(R.id.T1);

        // Initialize credentials and service object.
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());

        mProgress = new ProgressDialog(this);
        mProgress.setMessage("CALLING ALL AUTOBOTS");

        Bundle BShow=getIntent().getExtras();
        assert BShow != null;
        Flag = BShow.getInt("flag");
        if(Flag==0)
            setEvents();
        else
            getResults();
    }

    /**
     * Attempt to call the API, after verifying that all the preconditions are
     * satisfied. The preconditions are: Google Play Services installed, an
     * account was selected and the device currently has online access. If any
     * of the preconditions are not satisfied, the app will prompt the user as
     * appropriate.
     */
    @SuppressLint("SetTextI18n")
    private void setEvents() {
        if (isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (isDeviceOnline()) {
            DispEvent.setText("No network connection available.");
        } else {

            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("subjects");
            mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    SubNo = Integer.parseInt(String.valueOf(dataSnapshot.child("subno").getValue()));
                    SubjectDetails[] obj=new SubjectDetails[SubNo];
                    for(int i = 0; i < SubNo; i++)
                    {
                        obj[i]=new SubjectDetails();
                        obj[i].setSummary((String) dataSnapshot.child("sub"+i).child("name").getValue());
                        obj[i].setStartingDate((String) dataSnapshot.child("sub"+i).child("start").getValue());
                        obj[i].setEndingDate((String) dataSnapshot.child("sub"+i).child("end").getValue());

                        new SetEventTask(mCredential,obj[i]).execute();//obj passed as parameter
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }

            });

        }
    }

    @SuppressLint("SetTextI18n")
    private void getResults() {
        if (isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (isDeviceOnline()) {
            DispEvent.setText("No network connection available.");
        } else {
            new getEventTask(mCredential).execute();
        }
    }



    /**
     * Attempts to set the account used with the API credentials. If an account
     * name was previously saved it will use that one; otherwise an account
     * picker dialog will be shown to the user. Note that the setting the
     * account to use with the credentials object requires the app to have the
     * GET_ACCOUNTS permission, which is requested here if it is not already
     * present. The AfterPermissionGranted annotation indicates that this
     * function will be rerun automatically whenever the GET_ACCOUNTS permission
     * is granted.
     */
    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(
                this, Manifest.permission.GET_ACCOUNTS)) {
            String accountName = getPreferences(Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
                if(Flag==1)
                    getResults();
                else
                    setEvents();
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }

    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode code indicating the result of the incoming
     *     activity result.
     * @param data Intent (containing result data) returned by incoming
     *     activity result.
     */
    @SuppressLint("SetTextI18n")
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    DispEvent.setText(
                            "This app requires Google Play Services. Please install " +
                                    "Google Play Services on your device and relaunch this app.");
                } else {
                    if(Flag==1)
                        getResults();
                    else
                    setEvents();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        if(Flag==1)
                            getResults();
                        else
                            setEvents();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    if(Flag==1)
                        getResults();
                    else
                    setEvents();
                }
                break;
        }
    }

    /**
     * Respond to requests for permissions at runtime for API 23 and above.
     * @param requestCode The request code passed in
     *     requestPermissions(android.app.Activity, String, int, String[])
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *     which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }

    /**
     * Callback for when a permission is granted using the EasyPermissions
     * library.
     * @param requestCode The request code associated with the requested
     *         permission
     * @param list The requested permission list. Never null.
     */
    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * Callback for when a permission is denied using the EasyPermissions
     * library.
     * @param requestCode The request code associated with the requested
     *         permission
     * @param list The requested permission list. Never null.
     */
    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * Checks whether the device currently has a network connection.
     * @return true if the device has a network connection, false otherwise.
     */
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connMgr != null;
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo == null || !networkInfo.isConnected());
    }

    /**
     * Check that Google Play services APK is installed and up to date.
     * @return true if Google Play Services is available and up to
     *     date on this device; false otherwise.
     */
    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode != ConnectionResult.SUCCESS;
    }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }


    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     * @param connectionStatusCode code describing the presence (or lack of)
     *     Google Play Services on this device.
     */
    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                EventActivity.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }



    //-----------------EVENT CREATION TASK-----------------------------------------------------------------------------------------//

    @SuppressLint("StaticFieldLeak")
    public class SetEventTask extends AsyncTask<Void, Void, List<String>> {
        com.google.api.services.calendar.Calendar mService;
        private Exception mLastError = null;
        private String summary;
        private String start;
        private String end;

        SetEventTask(GoogleAccountCredential credential, SubjectDetails obj) { //parameterized constructor
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.calendar.Calendar.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("com.boss.buddy")
                    .build();
            summary=obj.getSummary();
            start=obj.getStartingDate();
            end=obj.getEndingDate();
        }


        @Override
        protected List<String> doInBackground(Void... params) {
            try {
                return setEvent();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
        private List<String> setEvent() {

                Event event = new Event()
                        .setSummary(summary)
                        .setLocation("AMRITA SCHOOL OF ARTS AND SCIENCE");
                DateTime startDateTime = new DateTime(start+"+05:30");
                EventDateTime start = new EventDateTime()
                        .setDateTime(startDateTime);
                event.setStart(start);

                DateTime endDateTime = new DateTime(end+"+05:30");
                EventDateTime end = new EventDateTime()
                        .setDateTime(endDateTime);
                event.setEnd(end);

                EventReminder[] reminderOverrides = new EventReminder[]{
                        new EventReminder().setMethod("popup").setMinutes(60),
                };
                Event.Reminders reminders = new Event.Reminders()
                        .setUseDefault(false)
                        .setOverrides(Arrays.asList(reminderOverrides));
                event.setReminders(reminders);

                   try {
                     mService.events().insert("primary", event).execute();
                 } catch (IOException e) {
                     e.printStackTrace();
                  }

            return null;
        }


        @Override
        protected void onPreExecute() {

            mProgress.show();
        }

        @Override
        protected void onPostExecute(List<String> output) {
            mProgress.hide();
            Toast.makeText(EventActivity.this, "Event Update Succeeded",
                    Toast.LENGTH_SHORT).show();
            getResults();

        }

        @Override
        protected void onCancelled() {
            mProgress.hide();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            EventActivity.REQUEST_AUTHORIZATION);
                } else {
                    Toast.makeText(EventActivity.this, "The following error occurred:" +
                            mLastError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(EventActivity.this, "Request Failed.", Toast.LENGTH_SHORT).show();
            }
        }
    }

//-------------------------------EVENT LISTING TASK --------------------------------------------------------------------------------------------//
    /**
     * An asynchronous task that handles the Google Calendar API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    @SuppressLint("StaticFieldLeak")
    public class getEventTask extends AsyncTask<Void, Void, List<String>> {
        com.google.api.services.calendar.Calendar mService;
        private Exception mLastError = null;

        getEventTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.calendar.Calendar.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("com.boss.buddy")
                    .build();

        }



        /**
         * Background task to call Google Calendar API.
         * @param params no parameters needed for this task.
         */
        @Override
        protected List<String> doInBackground(Void... params) {
            try {
                return getEvent();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        private List<String> getEvent() throws IOException {
            // List the next 30 events from the primary calendar.
            DateTime now = new DateTime(System.currentTimeMillis());
            List<String> eventStrings = new ArrayList<>();
            Events events = mService.events().list("primary")
                    .setMaxResults(30)
                    .setTimeMin(now)
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute();
            List<Event> items = events.getItems();

            for (Event event : items) {
                DateTime start = event.getStart().getDateTime();
                DateTime end =event.getEnd().getDateTime();
                if (start == null) {
                    start = event.getStart().getDate();
                }

                eventStrings.add(
                        String.format("%s%s%s", event.getSummary(), start,end));
            }
            return eventStrings;
        }


        @Override
        protected void onPreExecute() {
            DispEvent.setText("");
            mProgress.show();
        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(List<String> output) {
            mProgress.hide();
            if (output == null || output.size() == 0) {
                DispEvent.setText("No Events Found");
                Toast.makeText(EventActivity.this, "No Events Found",
                        Toast.LENGTH_SHORT).show();

            } else {
                output.add(0, "Events Retrieved.,");

                TableLayout stk = (TableLayout) findViewById(R.id.tb);
                TableRow tbrow0 = new TableRow(getApplicationContext());
                TextView tv0 = new TextView(getApplicationContext());
                tv0.setText(" Sl.No ");
                tv0.setTextColor(Color.WHITE);
                tbrow0.addView(tv0);
                TextView tv1 = new TextView(getApplicationContext());
                tv1.setText(" Product ");
                tv1.setTextColor(Color.WHITE);
                tbrow0.addView(tv1);

                stk.addView(tbrow0);
                for (int i = 0; i < 25; i++) {
                    TableRow tbrow = new TableRow(getApplicationContext());
                    TextView t1v = new TextView(getApplicationContext());
                    t1v.setText("" + i);
                    t1v.setTextColor(Color.WHITE);
                    t1v.setGravity(Gravity.CENTER);
                    tbrow.addView(t1v);
                    TextView t2v = new TextView(getApplicationContext());
                    t2v.setText("Product " + i);
                    t2v.setTextColor(Color.WHITE);
                    t2v.setGravity(Gravity.CENTER);
                    tbrow.addView(t2v);

                    stk.addView(tbrow);
                }


                Toast.makeText(EventActivity.this, output.get(1), Toast.LENGTH_SHORT).show();

                Toast.makeText(EventActivity.this, "Events Retrieved",
                        Toast.LENGTH_SHORT).show();

            }
        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void onCancelled() {
            mProgress.hide();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            EventActivity.REQUEST_AUTHORIZATION);
                } else {
                    DispEvent.setText("The following error occurred:\n"
                            + mLastError.getMessage());
                }
            } else {
                DispEvent.setText("Request cancelled.");
            }
        }
    }
}

class SubjectDetails
{
    private String Summary;
    private String StartingDate;
    private String EndingDate;
 SubjectDetails()
 {}

    public String getSummary() {
        return Summary;
    }

    public void setSummary(String summary) {
        Summary = summary;
    }

    public String getStartingDate() {
        return StartingDate;
    }

    public void setStartingDate(String startingDate) {
        StartingDate = startingDate;
    }

    public String getEndingDate() {
        return EndingDate;
    }

    public void setEndingDate(String endingDate) {
        EndingDate = endingDate;
    }
}
