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

import com.bearapps.ground_control.R;
import com.bearapps.ground_control.model.ContactObject;
import com.bearapps.ground_control.model.EventObject;
import com.bearapps.ground_control.model.InvoiceObject;
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
    public static final String CONTACT_PHOTO = "photo";
    public static final String CONTACT_CHARGE = "charge";
    public static final String CONTACT_PERIOD = "period";

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
    public static final String EVENTXCONTACT_EVENTID = "idevent";
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
        return getEvents(null);
    }


    public List<EventObject> getEvents(ContactObject contact) {

        String[] whereParam;
        String whereClause;

        if ( contact == null ) {
            whereParam = new String[] {String.valueOf(EVENT_DECISION) };
            whereClause = EVENTS_STATUS + " = ? " ;
        }
        else {
            whereParam = new String[] {String.valueOf(EVENT_DECISION), String.valueOf( contact.getId() ) };
            whereClause = EVENTS_STATUS + " = ? AND " + CONTACT_ID + " = ? " ;
        }

        List<Integer> contactsid = null;
        if (isEventInMemoryChanged) {
            open();

            String sortOrder = EVENTS_BEGINEVENT + " ASC ";

            String[] COLUMNS_EVENTS = {EVENTS_GOOGLEID, EVENTS_SUMARY, EVENTS_BEGINEVENT, EVENTS_ENDEVENT, EVENTS_WHERE, CONTACT_EMAIL, EVENTS_ID, CONTACT_NAME};
            Cursor cursor_events;

            cursor_events = db.query(VIEW_EVENTXCONTACTS,
                    COLUMNS_EVENTS,
                    whereClause,//where clause
                    whereParam,//where params
                    null,//groupby
                    null,//having
                    sortOrder);//orderby

            EventsInMemory = new ArrayList<>();
            while (cursor_events.moveToNext()) {

                    EventDateTime BeginDate = new EventDateTime();
                    BeginDate.setDateTime(new DateTime(cursor_events.getLong(2)));
                    BeginDate.setTimeZone( TimeZone.getDefault().getID());

                    EventDateTime EndDate = new EventDateTime();
                    EndDate.setTimeZone( TimeZone.getDefault().getID());
                    EndDate.setDateTime(new DateTime(cursor_events.getLong(3)));

                    EventsInMemory.add(
                            new EventObject(
                                    cursor_events.getString(0),
                                    cursor_events.getString(1),
                                    BeginDate,
                                    EndDate,
                                    cursor_events.getString(4)
                            )
                    );
                    EventsInMemory.get( EventsInMemory.size() -1 ).setId(cursor_events.getInt(6));//this makes no sense but is for test purpose

                EventsInMemory.get(EventsInMemory.size() - 1).AddContact(cursor_events.getString(5));


            }
            cursor_events.close();
            close();
            isEventInMemoryChanged = false;
        }

        if (EventsInMemory.isEmpty()) {
            DateTime start = new DateTime(now, TimeZone.getTimeZone(TimeZone.getDefault().getID()));
            EventDateTime StartEvent = new EventDateTime();
            StartEvent.setDate(start);
            StartEvent.setDateTime(start);
            StartEvent.setTimeZone(TimeZone.getDefault().getID());

            EventsInMemory.add(
                    new EventObject(
                            "",
                            context.getString(R.string.empty),
                            StartEvent,
                            StartEvent,
                            context.getString(R.string.empty),
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
                CONTACT_GOOGLEID + "='" + googleId + "'",
                null
        );
        close();
        if (row_id == -1) {
            Log.e("Storage", "write db error: Event id " + googleId + ".");
        }
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
    }

    private boolean addEvent(EventObject eventObject) {

        List<String> ContactsEmails = eventObject.getContactsEmails();
        Boolean error = false;

        ContentValues EventsValues = new ContentValues();
        ContentValues EventsxContactsValues = new ContentValues();
        EventsValues.put(EVENTS_GOOGLEID, eventObject.getGoogleId());
        EventsValues.put(EVENTS_SUMARY, eventObject.getSumary() );
        EventsValues.put(EVENTS_WHERE, eventObject.getWhere());
        EventsValues.put(EVENTS_BEGINEVENT, eventObject.getBeginEvent().getDateTime().getValue()  );
        EventsValues.put(EVENTS_ENDEVENT, eventObject.getEndEvent().getDateTime().getValue()  );

        db.beginTransaction();

        long row_id = db.insert(TABLE_EVENTS, null, EventsValues);
        if (row_id == -1) {
            Log.e("Storage", "write db error: addEvent " + eventObject.getGoogleId());
            error = true;
        }
        else {
            eventObject.setId((int) row_id);
        }

      if (!error) {
            for (String Contact: ContactsEmails) {
                EventsxContactsValues.put(EVENTXCONTACT_EVENTID,   eventObject.getId());
                EventsxContactsValues.put(EVENTXCONTACT_CONTACTID, Contact);
                row_id = db.insert(TABLE_EVENTXCONTACT, null, EventsxContactsValues);
            }
            if (row_id == -1) {
                Log.e("Storage", "write db error: AddEventsxContacts " + eventObject.getGoogleId());
                error = true;

            }
            else{
                db.setTransactionSuccessful();
            }

        }

        db.endTransaction();

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
        ContactsValues.put(CONTACT_EMAIL, contactObject.getEmail());
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


        if (isContactInMemoryChanged) {
            open();

            String sortOrder = CONTACT_NAME + " ASC ";

            String[] COLUMNS_CONTACTS = {CONTACT_GOOGLEID, CONTACT_NAME, CONTACT_EMAIL, CONTACT_STATUS,CONTACT_PHOTO,CONTACT_PERIOD,CONTACT_CHARGE };

            Cursor cursor_Contacts;

            cursor_Contacts = db.query(TABLE_CONTACTS,
                    COLUMNS_CONTACTS,
                    null,//where clause
                    null,//where params
                    null,//groupby
                    null,//having
                    sortOrder);//orderby

            ContactsInMemory = new ArrayList<ContactObject>();
            while (cursor_Contacts.moveToNext()) {

                ContactsInMemory.add(
                        new ContactObject(
                                cursor_Contacts.getString(0), //googleId
                                cursor_Contacts.getString(1), //name
                                cursor_Contacts.getString(2), //email
                                null,//left disable the status
                                cursor_Contacts.getString(4), //photoPath
                                cursor_Contacts.getLong(6), //amount
                                cursor_Contacts.getString(5)//period
                        )
                );

            }

            cursor_Contacts.close();
            close();
            isContactInMemoryChanged = false;

        }

        return ContactsInMemory;
    }


    public EventObject changeEventStatus(EventObject eventObject,int Deciscion) {
        open();
        latsUpdate = new Date();
        isEventInMemoryChanged = true;
        ContentValues EventsValues = new ContentValues();

        switch (Deciscion) {
            case EVENT_CANCEL:
                EventsValues.put(EVENTS_STATUS, EVENT_CANCEL);
            case EVENT_DECISION:
                EventsValues.put(EVENTS_STATUS, EVENT_CANCEL);
            case EVENT_CONCLUDED:
                EventsValues.put(EVENTS_STATUS, EVENT_CANCEL);
        }
        String whereClause = EVENTS_ID + " = ?";

        db.update(TABLE_EVENTS,
                EventsValues,
                  whereClause,
                new String[] { eventObject.getId().toString() } );

        close();
        return eventObject;
    }

    public void importEvents(List<EventObject> eventObjects) {
        open();
        for (EventObject eventObject : eventObjects) {
            addEvent(eventObject);
        }
        close();
        isEventInMemoryChanged = true;
    }

   public void modifyContact( String GoogleId,String period, long amount ) {

       open();
       isContactInMemoryChanged = true;
       ContentValues ContactValues = new ContentValues();

       ContactValues.put(CONTACT_CHARGE, amount);
       ContactValues.put(CONTACT_PERIOD, period);

       String whereClause = CONTACT_GOOGLEID + " = ?";

       db.update(TABLE_CONTACTS,
               ContactValues,
               whereClause,
               new String[]{ String.valueOf(GoogleId) });

       getContacts();
       close();

    }

    public List<InvoiceObject> getInvoice() {
        List<InvoiceObject> invoices = new ArrayList<>();

        return invoices;
    }


    public void importInvoice(List<InvoiceObject> newInvoices) {

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


 }
