package com.bearapps.ground_control.utility;

import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.bearapps.ground_control.model.ContactObject;
import com.bearapps.ground_control.model.EventObject;
import com.bearapps.ground_control.model.EventObjectActionBridge;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.EventDateTime;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;


public class Storage {

    public final static String UPDATE_DB = "updateDB";
    public final static String UPDATE_DB_ADD = "updateDbAdd";
    public final static String UPDATE_DB_DELETE = "updateDbDelete";

    public static final String TABLE_CONTACTS = "contacts";
    public static final String CONTACT_GOOGLEID = "googleid";
    public static final String CONTACT_STATUS = "status";   // 0=search for schedules ; 1= no
    public static final String CONTACT_DTINC = "dateinc";
    public static final String CONTACT_ID = "id";
    public static final String CONTACT_NAME = "name";
    public static final String CONTACT_EMAIL = "email";

    public static final int CONTACT_ACTIVE = 0;
    public static final int CONTACT_INACTIVE = 1;

    public static final String TABLE_EVENTS = "events";
    public static final String EVENTS_GOOGLEID = "googleid";
    public static final String EVENTS_STATUS = "status"; // 0=decision to made ; 1=event concluded ; 2=event cancel
    public static final String EVENTS_DTINC = "dateinc";
    public static final String EVENTS_DTACTION = "dateaction";
    public static final String EVENTS_ID = "id";
    public static final String EVENTS_SUMARY = "sumary";
    public static final String EVENTS_BEGINEVENT = "beginevent";
    public static final String EVENTS_ENDEVENT = "endhour";
    public static final String EVENTS_WHERE = "eventwhere";


    public static final int EVENT_DECISION = 0;
    public static final int EVENT_CONCLUDED = 1;
    public static final int EVENT_CANCEL = 2;

    public static final String TABLE_INVOICE = "invoice";
    public static final String INVOICE_STATUS = "status"; //0 = invoice open ; 1= invoice paid ; 2=invoice cancel
    public static final String INVOICE_AMOUNT = "amount";
    public static final String INVOICE_DTINC = "dateinc";
    public static final String INVOICE_DTACTION = "dateaction";
    public static final String INVOICE_ID = "id";
    public static final String INVOICE_CONTACTID = "contactid";

    public static final int INVOICE_OPEN = 0;
    public static final int INVOICE_PAID = 1;
    public static final int INVOICE_CANCEL = 2;

    public static final String TABLE_EVENTXCONTACT = "event_contacts";
    public static final String EVENTXCONTACT_CONTACTID = "idcontact";
    public static final String EVENTXCONTACT_EVENTID = "idvevent";
    public static final String EVENTXCONTACT_ID = "id";

    public static final String TABLE_INVOICEXEVENTS = "invoice_events";
    public static final String INVOICEXEVENTS_INVOICEID = "idinvoice";
    public static final String INVOICEXEVENTS_EVENTID = "idvevent";
    public static final String INVOICEXEVENTS_ID = "id";

    public static final String VIEW_EVENTXCONTACTS = "view_eventxcontacts";
    public static final String VIEW_INVOICEXEVENTS = "view_invoicexevents";


    private static Storage mInstance = null;
    private StorageHelper dbHelper;
    private SQLiteDatabase db;
    private Context context;
    private ClipboardManager eventClipboardManager;
    private Date latsUpdate = new Date();
    private List<EventObject> EventsInMemory;
    private List<ContactObject> ContactsInMemory;
    private boolean isEventInMemoryChanged = true;
    private boolean isContactInMemoryChanged = true;
    private Date now = new Date();
    private String ContactsId;

    private Storage(Context context) {
        this.context = context;
        this.eventClipboardManager = (ClipboardManager) this.context.getSystemService(Context.CLIPBOARD_SERVICE);
        this.dbHelper = new StorageHelper(this.context);
    }

    public static Storage getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new Storage(context.getApplicationContext());
        }
        return mInstance;
    }

    private String sqliteEscape(String keyWord) {
        return DatabaseUtils.sqlEscapeString(keyWord);
    }

    private void open() {
        if (db == null) {
            db = dbHelper.getWritableDatabase();
        } else if (!db.isOpen()) {
            db = dbHelper.getWritableDatabase();
        }
    }

    private void close() {
        if (db != null) {
            if (db.isOpen()) {
                db.close();
            }
        }
    }

    public List<EventObject> getEvents() {
        EventsInMemory = getEvents(null,null) ;
            return EventsInMemory;
    }

    public List<EventObject> getEvents(Integer id, Integer status) {
        String[] whereParam = {String.valueOf(status), String.valueOf(id)};
        String whereClause = EVENTS_STATUS + " = ? AND " + EVENTS_ID + " = ? ";

        List<Integer> contactsid = null;
        if (isEventInMemoryChanged) {
            open();

            String sortOrder = EVENTS_DTINC + " ASC ";
            if (id == null) {
                whereParam = null;
                whereClause = null;
            }
            String[] COLUMNS_EVENTS = {EVENTS_GOOGLEID, EVENTS_SUMARY, EVENTS_BEGINEVENT, EVENTS_ENDEVENT, EVENTS_WHERE, CONTACT_EMAIL};
            Cursor cursor_events;

            cursor_events = db.query(VIEW_EVENTXCONTACTS,
                    COLUMNS_EVENTS,
                    whereClause,//where clause
                    whereParam,//where params
                    null,//groupby
                    null,//having
                    sortOrder);//orderby

            String googleidStorage = "";

            EventsInMemory = new ArrayList<>();
            while (cursor_events.moveToNext()) {


                if (googleidStorage.equals(cursor_events.getString(0))) {
                    contactsid.add(cursor_events.getInt(1));
                    EventsInMemory.get(EventsInMemory.size() - 1).AddContact(cursor_events.getString(6));
                } else {
                    googleidStorage = cursor_events.getString(0);

                    EventDateTime BeginDate = null;
                    BeginDate.setDateTime(new DateTime(cursor_events.getLong(2)));

                    EventDateTime EndDate = null;
                    EndDate.setDateTime(new DateTime(cursor_events.getLong(3)));

                    EventsInMemory.add(
                            new EventObject(
                                    cursor_events.getString(0),
                                    cursor_events.getString(1),
                                    BeginDate,
                                    EndDate,
                                    cursor_events.getString(4),
                                    contactsid
                            )

                    );
                }

            }
            cursor_events.close();
            close();
            isEventInMemoryChanged = false;
        }

        if (EventsInMemory.isEmpty()) {
            DateTime start = new DateTime(now, TimeZone.getTimeZone(TimeZone.getDefault().getID()));

            EventsInMemory.add(
                    new EventObject(
                            "",
                            "Empty",
                            new EventDateTime().setDateTime(start),
                            new EventDateTime().setDateTime(start),
                            "Empty",
                            contactsid
                    )
            );
        }


        return EventsInMemory;
    }

    public void deleteContact(String googleId) {
        latsUpdate = new Date();
        isContactInMemoryChanged = true;
        open();
        int row_id = db.delete(
                TABLE_CONTACTS,
                CONTACT_GOOGLEID + "=`" + googleId + "`",
                null
        );
        close();
        if (row_id == -1) {
            Log.e("Storage", "write db error: Event id " + googleId + ".");
        }
        refreshAllEventsList(true, null);
    }


    public void deleteEvent(int EventId) {
        latsUpdate = new Date();
        isEventInMemoryChanged = true;
        open();
        int row_id = db.delete(
                TABLE_EVENTS,
                EVENTS_ID + "=`" + EventId + "`",
                null
        );
        close();
        if (row_id == -1) {
            Log.e("Storage", "write db error: Event id " + EventId + ".");
        }
        refreshAllEventsList(true, null);
    }

    private boolean addEvent(EventObject eventObject) {

        List<String> ContactsId = eventObject.getContactsId();
        Boolean error = false;

        ContentValues EventsValues = new ContentValues();
        ContentValues EventsxContactsValues = new ContentValues();
        EventsValues.put(EVENTS_GOOGLEID, eventObject.getGoogleId());
        EventsValues.put(EVENTS_SUMARY, eventObject.getSumary() );
        EventsValues.put(EVENTS_WHERE, eventObject.getWhere());
        EventsValues.put(EVENTS_BEGINEVENT, eventObject.getBeginEvent().getValue()  );
        EventsValues.put(EVENTS_ENDEVENT, eventObject.getEndEvent().getValue()  );

        long row_id = db.insert(TABLE_EVENTS, null, EventsValues);
        if (row_id == -1) {
            Log.e("Storage", "write db error: addEvent " + eventObject.getGoogleId());
            error = true;
        }
        else {
            eventObject.setId((int) row_id);
        }

  /*      if (!error) {
            for (String Contact: ContactsId) {
                EventsxContactsValues.put(EVENTXCONTACT_EVENTID,   eventObject.getId());
                EventsxContactsValues.put(EVENTXCONTACT_CONTACTID, Contact);
            }
            row_id = db.insert(TABLE_EVENTXCONTACT, null, EventsxContactsValues);
            if (row_id == -1) {
                Log.e("Storage", "write db error: AddEventsxContacts " + eventObject.getGoogleId());
                error = true;
            }

        }*/

        return !error;
    }

    public void importContacts(List<ContactObject> contactObjects) {
        open();
        for (ContactObject contactObject : contactObjects) {
            addContact(contactObject);
        }
        close();
        isContactInMemoryChanged = true;
    }

    public boolean addContact(ContactObject contactObject) {

        ContactsId = contactObject.getGoogleId();
        Boolean error = false;
        open();

        if (ContactsInMemory.contains(contactObject)) {
            return true;
        }

        ContentValues ContactsValues = new ContentValues();
        ContactsValues.put(CONTACT_GOOGLEID, contactObject.getGoogleId());
        ContactsValues.put(CONTACT_EMAIL, contactObject.getGoogleId());
        ContactsValues.put(CONTACT_NAME, contactObject.getName());

        long row_id = db.insert(TABLE_CONTACTS, null, ContactsValues);
        if (row_id == -1) {
            Log.e("Storage", "write db error: addContact " + contactObject.getGoogleId());
            error = true;
        }
        else {
            contactObject.setId((int) row_id);
        }
        isContactInMemoryChanged = true;
        close();
        return !error;
    }

    public List<ContactObject> getContacts() {
        ContactsInMemory = getContacts(null,null) ;
        return ContactsInMemory;
    }

    public List<ContactObject> getContacts(Integer id, Integer status) {
        String[] whereParam = {String.valueOf(status),String.valueOf(id) };
        String whereClause  = CONTACT_STATUS + " = ? AND " + CONTACT_ID + " = ? "  ;

        if (isContactInMemoryChanged) {
            open();

            String sortOrder = CONTACT_NAME + " ASC ";
            if ( id == null){
                whereParam = null;
                whereClause = null;
            }
            String[] COLUMNS_CONTACTS = {CONTACT_GOOGLEID, CONTACT_NAME, CONTACT_EMAIL, CONTACT_STATUS };

            Cursor cursor_Contacts;

            cursor_Contacts = db.query(VIEW_EVENTXCONTACTS,
                    COLUMNS_CONTACTS,
                    whereClause,//where clause
                    whereParam,//where params
                    null,//groupby
                    null,//having
                    sortOrder);//orderby

            ContactsInMemory = new ArrayList<ContactObject>();
            while (cursor_Contacts.moveToNext()) {

                ContactsInMemory.add(
                        new ContactObject(
                                cursor_Contacts.getString(0),
                                cursor_Contacts.getString(1),
                                cursor_Contacts.getString(2),
                                cursor_Contacts.getString(3)
                        )
                );
            }

            cursor_Contacts.close();
            close();
            isContactInMemoryChanged = false;

        }

        return ContactsInMemory;
    }


    public EventObject changeEventStatus(EventObject eventObject) {
        open();
        latsUpdate = new Date();
        isEventInMemoryChanged = true;
        refreshAllEventsList(false, null);
        return eventObject;
    }

    public void importEvents(List<EventObject> eventObjects) {
        open();
        for (EventObject eventObject : eventObjects) {
            addEvent(eventObject);
        }
        close();
        latsUpdate = new Date();
        isEventInMemoryChanged = true;
        refreshAllEventsList(true, null);
    }

   public void modifyClip(Integer oldEvent, Integer newEvent ) {
        //Log.v(MyUtil.PACKAGE_NAME, "modifyClip(" + oldEvent + ", " + newEvent + ", " + isImportant + ")");

        open();
        //TODO fazer a alteração do evento caso seja necessário
        close();
        latsUpdate = new Date();
        isEventInMemoryChanged = true;

        //refreshAllEventsList(!newEvent.isEmpty(), oldEvent);

    }

    private void refreshAllEventsList(Boolean added, String deletedString) {
        updateDbBroadcast(context, added, deletedString);
        context.startService(new Intent(context, EventObjectActionBridge.class)
                        .putExtra(EventObjectActionBridge.ACTION_CODE, EventObjectActionBridge.ACTION_REFRESH_WIDGET)
        );
    }

    public static void updateDbBroadcast(Context context, Boolean added, String deletedString) {
        Intent intent = new Intent(UPDATE_DB);
        if (added) {
            intent.putExtra(UPDATE_DB_ADD, true);
        }
        if (deletedString != null) {
            if (!deletedString.isEmpty()) {
                intent.putExtra(UPDATE_DB_DELETE, deletedString);
            }
        }
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public Date getLatsUpdateDate() {
        return latsUpdate;
    }


    public boolean updateDb() {

        //sync system clipboard and storage.

        String topClipInStack;
        if (getEvents().size() > 0) {
            topClipInStack = getEvents().get(0).getSumary();
        } else {
            topClipInStack = "";
        }

        String clipString;
        if (!eventClipboardManager.hasPrimaryClip()) {
            eventClipboardManager.setText(topClipInStack);
            return true;
        }
        try {
            //Don't use CharSequence .toString()!
            CharSequence charSequence = eventClipboardManager.getPrimaryClip().getItemAt(0).getText();
            clipString = String.valueOf(charSequence);
        } catch (Error ignored) {
            eventClipboardManager.setText(topClipInStack);
            return true;
        }

        if (!topClipInStack.equals(clipString)) {
            eventClipboardManager.setText(topClipInStack);
            return true;
        }
        return false;
    }



}
