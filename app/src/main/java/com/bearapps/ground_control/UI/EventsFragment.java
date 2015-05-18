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
import java.util.List;


public abstract class EventsFragment extends Fragment implements AdapterView.OnItemClickListener {

    private RecyclerView mList;
    private EventCardAdapter mAdapter;
    private Storage db;
    private Context context;
    private String message;



    private int isYHidden = -1;
    private int isXHidden = -1;
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

                                if ( db.getEvents().get(position).getSumary() == context.getString(R.string.empty) ) {
                                    return false;
                                }

                                return true;
                            }
                            @Override
                            public void onDismissedBySwipeLeft(RecyclerView recyclerView, int[] reverseSortedPositions) {
                                for (int position : reverseSortedPositions) {
                                    showSnackbar(position, db.getEvents().get(position), mAdapter, db.EVENT_CANCEL);
                                    mAdapter.remove(position);

                                }
                            }

                            @Override
                            public void onDismissedBySwipeRight(RecyclerView recyclerView, int[] reverseSortedPositions) {
                                for (int position : reverseSortedPositions) {
                                    showSnackbar(position, db.getEvents().get(position), mAdapter, db.EVENT_CONCLUDED);
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

    public int getDefaultItemCount() {
        return 10;
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

    /**
     * Check that Google Play services APK is installed and up to date. Will
     * launch an error dialog for the user to update Google Play Services if
     * possible.
     * @return true if Google Play Services is available and up to
     *     date on this device; false otherwise.
     */
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


    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     * @param connectionStatusCode code describing the presence (or lack of)
     *     Google Play Services on this device.
     */
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
                    mAdapter.AddEvents(db.getEvents());
                } else {
                    chooseAccount();
                }
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Fill the event display with the given List of strings; called from
     * background threads and async tasks that need to update the UI (in the
     * UI thread).
     * @param eventObjects a List of Strings to populate the event display with.
     */
    public void updateEventList(final List<EventObject> eventObjects) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getActivity().setProgressBarIndeterminateVisibility(false);
                if (eventObjects == null) {

                    Toast.makeText(
                            getActivity(),
                            getString(R.string.error_retrivieving),
                            Toast.LENGTH_LONG
                    ).show();

                } else if (eventObjects.size() == 0) {
                    Toast.makeText(
                            getActivity(),
                            getString(R.string.no_events),
                            Toast.LENGTH_LONG
                    ).show();

                } else {

                    Toast.makeText(
                            getActivity(),
                            getString(R.string.updated),
                            Toast.LENGTH_LONG
                    ).show();

                    db.importEvents(eventObjects);
                    mAdapter.AddEvents(eventObjects);
                }
            }
        });
    }



    /**
     * Show a status message in the list header TextView; called from background
     * threads and async tasks that need to update the UI (in the UI thread).
     * @param message a String to display in the UI header TextView.
     */
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



    /**
     * Attempt to get a list of calendar events to display. If the email
     * address isn't known yet, then call chooseAccount() method so the user
     * can pick an account.
     */
    private void refreshEventList() {
        if (credential.getSelectedAccountName() == null) {
            chooseAccount();
        } else {
            if (isDeviceOnline()) {
                getActivity().setProgressBarIndeterminateVisibility(true);
                new EventFecthTask().execute();
            } else {
                Toast.makeText(
                        getActivity(),
                        getString(R.string.no_network),
                        Toast.LENGTH_LONG
                ).show();
            }
        }
    }

    /**
     * Starts an activity in Google Play Services so the user can pick an
     * account.
     */
    private void chooseAccount() {
        startActivityForResult(
                credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
    }

    /**
     * Checks whether the device currently has a network connection.
     * @return true if the device has a network connection, false otherwise.
     */
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

    /**
     * Fetch a list of the next 10 events from the primary calendar.
     * @return List of Strings describing returned events.
     * @throws java.io.IOException
     */
    private List<EventObject> fetchEventsFromCalendar() throws IOException {
        // List the next 10 events from the primary calendar.
        List<String> EventsAttendee = new ArrayList<>();
        List<EventObject> eventsObject =  new ArrayList<>();

        String token = settings.getString(SYNC_TOKEN_CALENDAR, null);

        Events events = mService.events().list("primary").setSyncToken(token).execute();
        List<Event> eventsItems = events.getItems();


        for (Event event : eventsItems) {

            if ( event.getOrganizer().isSelf() ) { //is the event created by the owner of the account?
                List<EventAttendee> contactsid = event.getAttendees();

                for (EventAttendee contact : contactsid) {
                    EventsAttendee.add( contact.getEmail() );
                }

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

        token = events.getNextSyncToken();

        SharedPreferences.Editor editor = settings.edit();
        editor.putString(SYNC_TOKEN_CALENDAR, token);
        editor.commit();

        return eventsObject;
    }

    /**
     * An asynchronous task that handles the Calendar API event list retrieval.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    private class EventFecthTask extends AsyncTask<Void, Void, Void> {

        /**
         * Background task to call Calendar API to fetch event list.
         * @param params no parameters needed for this task.
         */
        @Override
        protected Void doInBackground(Void... params) {
            try {
                clearEvents();

                updateEventList(fetchEventsFromCalendar());

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
