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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.ImageView;
import android.widget.Toast;
import com.boss.buddy.MainMenu;
import com.boss.buddy.R;
import com.bumptech.glide.Glide;
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
    private ProgressDialog mProgress;

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = { CalendarScopes.CALENDAR };
    private int Flag;
    private int SubNo;
    private ImageView img;
    private String url;


    @Override
    public void onBackPressed()
    {
        System.exit(0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        // Initialize credentials and service object.
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());

        mProgress = new ProgressDialog(this);
        mProgress.setMessage(getString(R.string.loading));

        Bundle cal_op=getIntent().getExtras();

        Flag = cal_op.getInt("flag");
        if (Flag==1)
            getResults();
        else
            setEvents();

    }

    /**
     * Attempt to call the API, after verifying that all the preconditions are
     * satisfied. The preconditions are: Google Play Services installed, an
     * account was selected and the device currently has online access. If any
     * of the preconditions are not satisfied, the app will prompt the user as
     * appropriate.
     */

    private void setEvents() {
        if (isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (isDeviceOnline()) {
            Toast.makeText(this, getString(R.string.connection_error), Toast.LENGTH_SHORT).show();
        } else {

            if(Flag==0) {
                DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("Subjects");
                mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        SubNo = Integer.parseInt(String.valueOf(dataSnapshot.child("subNo").getValue()));
                        SubjectDetails[] obj = new SubjectDetails[SubNo];
                        for (int i = 0; i < SubNo; i++) {
                            obj[i] = new SubjectDetails();
                            obj[i].setSummary((String) dataSnapshot.child("sub" + i).child("name").getValue());
                            obj[i].setStartingDate((String) dataSnapshot.child("sub" + i).child("start").getValue());
                            obj[i].setEndingDate((String) dataSnapshot.child("sub" + i).child("end").getValue());

                            new SetEventTask(mCredential, obj[i]).execute();  //obj passed as parameter
                        }
                        getResults();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(EventActivity.this, getString(R.string.app_fetch_error), Toast.LENGTH_SHORT).show();
                    }

                });
            }
            else if(Flag==2)
            {
                new EventDeleteTask(mCredential).execute();
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private void getResults() {

            img = findViewById(R.id.exam_timetable);
            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("exam");
            mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    url = String.valueOf(dataSnapshot.child("timetable").getValue());
                    Glide.with(getApplicationContext()).load(url).into(img);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(EventActivity.this, getString(R.string.app_fetch_error), Toast.LENGTH_SHORT).show();
                }

            });

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
                startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    getString(R.string.app_access_request),
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
                    Toast.makeText(this, getString(R.string.google_play_require), Toast.LENGTH_SHORT).show();
                }
                else {
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
                        .setLocation(getString(R.string.event_loc));
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
            Toast.makeText(EventActivity.this, getString(R.string.event_set_successfull),
                    Toast.LENGTH_SHORT).show();

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
                    Toast.makeText(EventActivity.this, getString(R.string.error_occured)+
                            mLastError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(EventActivity.this, getString(R.string.event_set_not_successfull), Toast.LENGTH_SHORT).show();
            }
        }
    }

//-------------------------------EVENT DELETING TASK --------------------------------------------------------------------------------------------//

    /**
     * An asynchronous task that handles the Google Calendar API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
   @SuppressLint("StaticFieldLeak")
    public class EventDeleteTask extends AsyncTask<Void, Void, List<String>> {
        com.google.api.services.calendar.Calendar mService;
        private Exception mLastError = null;

        EventDeleteTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.calendar.Calendar.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("com.boss.buddy")
                    .build();
        }

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
        @Override
        protected void onProgressUpdate(Void... values) {
        }
        private List<String> getEvent() throws IOException {
            DateTime now = new DateTime(System.currentTimeMillis() - 60 * 24 * 3600 * 1000L);

            List<String> eventStrings = new ArrayList<>();
            Events events = mService.events().list("primary")
                    .setMaxResults(300)
                    .setTimeMin(now)
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute();
            List<Event> items = events.getItems();

            for (Event event : items) {
                String loc = event.getLocation();
                if(loc.equals(getString(R.string.event_loc)))
                {
                    mService.events().delete("primary",event.getId()).execute();
                }
            }
            return eventStrings;
        }


        @Override
        protected void onPreExecute() {
            mProgress.show();
        }

        @SuppressLint({"SetTextI18n", "NewApi"})
        @Override
        protected void onPostExecute(List<String> output) {
            mProgress.hide();
            startActivity(new Intent(getApplicationContext(), MainMenu.class));
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
                    Toast.makeText(EventActivity.this, getString(R.string.error_occured)+ mLastError.getMessage(), Toast.LENGTH_SHORT).show();

                }
            } else {
                Toast.makeText(EventActivity.this, getString(R.string.event_set_not_successfull), Toast.LENGTH_SHORT).show();
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

