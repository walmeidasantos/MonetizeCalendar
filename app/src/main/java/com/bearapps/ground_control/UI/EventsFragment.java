package com.bearapps.ground_control.UI;

import android.accounts.AccountManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import com.bearapps.ground_control.R;
import com.bearapps.ground_control.model.ContactObject;
import com.bearapps.ground_control.model.EventCardAdapter;
import com.bearapps.ground_control.model.EventObject;
import com.bearapps.ground_control.utility.Storage;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.Events;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.nispok.snackbar.listeners.ActionClickListener;
import com.nispok.snackbar.listeners.EventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;


public abstract class EventsFragment extends Fragment implements AdapterView.OnItemClickListener {

    private RecyclerView mList;
    private EventCardAdapter mAdapter;
    private Storage db;
    private Context context;
    private String message;
    private List<EventObject> events;

    private int isSnackbarShow = 0;


    /** Required Overrides for Sample Fragments */

    protected abstract RecyclerView.LayoutManager getLayoutManager();
    protected abstract RecyclerView.ItemDecoration getItemDecoration();
    protected abstract EventCardAdapter getAdapter();

    GoogleAccountCredential credential;
    public com.google.api.services.calendar.Calendar mService;
    public static final int REQUEST_ACCOUNT_PICKER = 1000;
    public static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    private static final String PREF_ACCOUNT_NAME = "APP_DEFAULT_ACCOUNT";
    private static final String SYNC_TOKEN_CALENDAR = "SYNC_TOKEN";
    private static final String[] SCOPES = {CalendarScopes.CALENDAR_READONLY};
    final HttpTransport transport = AndroidHttp.newCompatibleTransport();
    final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
    SharedPreferences settings ;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.view_main_recycler, container, false);

        mList = (RecyclerView) rootView.findViewById(R.id.cardList);
        mList.setLayoutManager(getLayoutManager());
        mList.addItemDecoration(getItemDecoration());

        mList.getItemAnimator().setAddDuration(1000);
        mList.getItemAnimator().setChangeDuration(1000);
        mList.getItemAnimator().setMoveDuration(1000);
        mList.getItemAnimator().setRemoveDuration(1000);
        context = getActivity().getBaseContext();

        db = Storage.getInstance(context);
        mAdapter = getAdapter();
        mAdapter.setOnItemClickListener(this);
        mList.setAdapter(mAdapter);

        settings = getActivity().getPreferences(Context.MODE_PRIVATE);

        credential = GoogleAccountCredential.usingOAuth2(
                getActivity().getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff())
                .setSelectedAccountName(settings.getString(PREF_ACCOUNT_NAME, null));

        mService = new com.google.api.services.calendar.Calendar.Builder(
                transport, jsonFactory, credential)
                .setApplicationName("Calendar API Android Quickstart")
                .build();


        SwipeableRecyclerViewTouchListener swipeDeleteTouchListener =
                new SwipeableRecyclerViewTouchListener(
                        mList,
                        new SwipeableRecyclerViewTouchListener.SwipeListener() {
                            @Override
                            public boolean canSwipe(int position) {

                                return true;
                            }
                            @Override
                            public void onDismissedBySwipeLeft(RecyclerView recyclerView, int[] reverseSortedPositions) {
                                for (int position : reverseSortedPositions) {
                                    showSnackbar(position, events.get(position), mAdapter, db.EVENT_CANCEL);
                                    mAdapter.remove(position);

                                }
                            }

                            @Override
                            public void onDismissedBySwipeRight(RecyclerView recyclerView, int[] reverseSortedPositions) {
                                for (int position : reverseSortedPositions) {
                                    showSnackbar(position, events.get(position), mAdapter, db.EVENT_CONCLUDED);
                                    mAdapter.remove(position);
                                }
                            }
                        });

        RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                    if (isSnackbarShow > 0) return;
                }


        };
        mList.setOnScrollListener(scrollListener);
        mList.addOnItemTouchListener(swipeDeleteTouchListener);





        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //inflater.inflate(R.menu.grid_options, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
           return super.onOptionsItemSelected(item);

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Toast.makeText(getActivity(),
                "Clicked: " + position + ", index " + mList.indexOfChild(view),
                Toast.LENGTH_SHORT).show();
    }

    /**
     * Called whenever this activity is pushed to the foreground, such as after
     * a call to onCreate().
     */
    @Override
    public void onResume() {
        super.onResume();
        if (isGooglePlayServicesAvailable()) {
            refreshEventList();
        } else {
            Toast.makeText(
                    getActivity(),
                    getString(R.string.google_play_services),
                    Toast.LENGTH_LONG
            ).show();
        }
    }


    private boolean isGooglePlayServicesAvailable() {
        final int connectionStatusCode =
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());
        if (GooglePlayServicesUtil.isUserRecoverableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
            return false;
        } else if (connectionStatusCode != ConnectionResult.SUCCESS ) {
            return false;
        }
        return true;
    }



    public void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
                        connectionStatusCode,
                        getActivity(),
                        REQUEST_GOOGLE_PLAY_SERVICES);
                dialog.show();
            }
        });
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
    @Override
    public void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode == getActivity().RESULT_OK) {
                    refreshEventList();
                } else {
                    isGooglePlayServicesAvailable();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == getActivity().RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        credential.setSelectedAccountName(accountName);
                        SharedPreferences settings =
                                getActivity().getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.commit();
                        refreshEventList();
                    }
                } else if (resultCode == getActivity().RESULT_CANCELED) {
                    Toast.makeText(
                            getActivity(),
                            getString(R.string.account_unspecified),
                            Toast.LENGTH_LONG
                    ).show();
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == getActivity().RESULT_OK) {
                    refreshEventList();
                } else {
                    chooseAccount();
                }
                break;
        }

    }


    public void updateEventList(final List<EventObject> eventObjects) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (eventObjects == null) {

                    Toast.makeText(
                            getActivity(),
                            getString(R.string.error_retrivieving),
                            Toast.LENGTH_LONG
                    ).show();

                } else if (eventObjects.size() == 0) {
                    events = db.getEvents();
                    mAdapter.AddEvents(events);
                    Toast.makeText(
                            getActivity(),
                            getString(R.string.no_events),
                            Toast.LENGTH_LONG
                    ).show();

                } else {

                    db.importEvents(eventObjects);
                    events = db.getEvents();
                    mAdapter.AddEvents(events);

                    Toast.makeText(
                            getActivity(),
                            getString(R.string.updated),
                            Toast.LENGTH_LONG
                    ).show();


                }
            }
        });
    }


    public void updateStatus(final String message) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(
                        getActivity(),
                        message,
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }



    private void refreshEventList() {

        if (credential.getSelectedAccountName() == null) {
            chooseAccount();
        } else {
            if (isDeviceOnline()) {
                getActivity().setProgressBarIndeterminateVisibility(true);
                new EventFetchTask(this).execute();

            } else {
                Toast.makeText(
                        getActivity(),
                        getString(R.string.no_network),
                        Toast.LENGTH_LONG
                ).show();
            }
        }
    }


    private void chooseAccount() {
        startActivityForResult(
                credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
    }


    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    public void clearEvents() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(
                        getActivity(),
                        getString(R.string.retrieving_events),
                        Toast.LENGTH_LONG
                ).show();


            }
        });
    };

   private class EventFetchTask extends AsyncTask<Void, Void, Void> {
        private EventsFragment mActivity;

        EventFetchTask(EventsFragment activity) { this.mActivity = activity;  }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                clearEvents();
                mActivity.updateEventList(fetchEventsFromCalendar());


            } catch (final GooglePlayServicesAvailabilityIOException availabilityException) {
                showGooglePlayServicesAvailabilityErrorDialog(
                        availabilityException.getConnectionStatusCode());

            } catch (UserRecoverableAuthIOException userRecoverableException) {
                startActivityForResult(
                        userRecoverableException.getIntent(),
                        REQUEST_AUTHORIZATION);

            } catch (IOException e) {
                updateStatus("The following error occurred: " +
                        e.getMessage());
            }
            return null;
        }
       /**
        * Fetch a list of the next 10 events from the primary calendar.
        * @return List of Strings describing returned events.
        * @throws java.io.IOException
        */
       private List<EventObject> fetchEventsFromCalendar() throws IOException {
           // List the next 10 events from the primary calendar.
           List<String> EventsAttendee = new ArrayList<>();
           List<EventObject> eventsObject =  new ArrayList<>();
           Events events;
           String token = settings.getString(SYNC_TOKEN_CALENDAR, null);
           token = null;

           if (token != null ) {
               mService.events().list("primary");
               events = mService.events().list("primary").setAlwaysIncludeEmail(true).setMaxResults(2500).setSyncToken(token).execute();

           } else {
               Calendar Cal = Calendar.getInstance();
               Cal.setTimeZone( TimeZone.getDefault() );
               Cal.add(Calendar.YEAR, -1);
               Date oneYearAgo = Cal.getTime();
               events = mService.events().list("primary").setAlwaysIncludeEmail(true).setMaxResults(2500).setTimeMin(new DateTime(oneYearAgo, TimeZone.getDefault())).execute();

           }
           //mService.events().list("primary").setAlwaysIncludeEmail(true);
           //mService.events().list("primary").setMaxResults(2500);
           //google example enters a loop infinite
        /*String pageToken = null;
        List<Event> eventsItems = new ArrayList<Event>();;
        do {
            mService.events().list("primary").setPageToken(pageToken);

            try {

                events = mService.events().list("primary").execute();

            } catch (GoogleJsonResponseException e) {
                if (e.getStatusCode() == 410 ) {
                    // A 410 status code, "Gone", indicates that the sync token is invalid.
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString(SYNC_TOKEN_CALENDAR, null);
                    editor.commit();
                    throw e;
                } else {
                    throw e;
                }
            }

            eventsItems.addAll(events.getItems());
            pageToken = events.getNextPageToken();


        } while (pageToken != null);*/
        /*
        events = mService.events().list("primary").execute();
        */
           List<Event> eventsItems = new ArrayList<Event>();
           eventsItems.addAll(events.getItems());

           for (Event event : eventsItems) {

               if ( event.getOrganizer().isSelf() ) { //is the event created by the owner of the account?

                   // Populate the map
                   Map<String,ContactObject> seletedContats = new HashMap<String,ContactObject>();
                   for( ContactObject contact : db.getAllContacts() ) {
                       seletedContats.put(contact.getEmail(), contact);
                   }

                   List<EventAttendee> contactsid = event.getAttendees();
                   if ( !event.isEndTimeUnspecified() && contactsid != null   ) {
                       for (EventAttendee contact : contactsid) {
                           if ( seletedContats.containsKey( contact.getEmail() ) ) {//if there is not any Attendees not record the event
                               EventsAttendee.add(contact.getEmail());
                           }
                       }

                       if (EventsAttendee.size() > 0 ) {
                           eventsObject.add(
                                   new EventObject(
                                           event.getId(),
                                           event.getSummary(),
                                           event.getStart(),
                                           event.getEnd(),
                                           event.getLocation(),
                                           EventsAttendee
                                   )
                           );
                       }
                   }
               }
           }


           token = events.getNextSyncToken();

           SharedPreferences.Editor editor = settings.edit();
           editor.putString(SYNC_TOKEN_CALENDAR, token);
           editor.commit();

           return eventsObject;
       }

    }

    private void showSnackbar(final int position, final EventObject eventObject, final EventCardAdapter eventCardAdapter, final int decision) {
        final boolean[] isUndo = new boolean[1];


        if (decision == db.EVENT_CANCEL) {
            message = getString(R.string.event_cancel);
        }
        else if (decision == db.EVENT_CONCLUDED) {
            message = getString(R.string.event_concluded);
        }


        SnackbarManager.show(
                Snackbar.with(getActivity())
                        .text(message)
                        .actionLabel(getString(R.string.toast_undo))
                        .actionColor(getResources().getColor(R.color.accent))
                        .duration(Snackbar.SnackbarDuration.LENGTH_LONG)
                        .eventListener(new EventListener() {
                            @Override
                            public void onShow(Snackbar snackbar) {
                                if (position >= (eventCardAdapter.getItemCount() - 1) && eventCardAdapter.getItemCount() > 6) {
                                    mList.animate().translationY(-snackbar.getHeight());
                                }
                            }

                            @Override
                            public void onShowByReplace(Snackbar snackbar) {
                                isSnackbarShow += 1;
                            }

                            @Override
                            public void onShown(Snackbar snackbar) {
                                isSnackbarShow += 1;
                            }

                            @Override
                            public void onDismiss(Snackbar snackbar) {
                                isSnackbarShow -= 1;
                                if (!isUndo[0]) {
                                    db.changeEventStatus(eventObject, decision);
                                }
                            }

                            @Override
                            public void onDismissByReplace(Snackbar snackbar) {
                                isSnackbarShow -= 1;
                                if (!isUndo[0]) {
                                    db.changeEventStatus(eventObject, decision);
                                }
                            }

                            @Override
                            public void onDismissed(Snackbar snackbar) {
                                if (isSnackbarShow <= 0) {
                                    isSnackbarShow = 0;
                                    mList.animate().translationY(0);
                                }

                            }
                        })
                        .actionListener(new ActionClickListener() {
                            @Override
                            public void onActionClicked(Snackbar snackbar) {
                                isUndo[0] = true;
                                mAdapter.add(position, eventObject);
                                mList.animate().translationY(0);
                                db.changeEventStatus(eventObject, db.EVENT_DECISION);
                                getLayoutManager().scrollToPosition(position);
                            }
                        })
                , getActivity());
    }






}
