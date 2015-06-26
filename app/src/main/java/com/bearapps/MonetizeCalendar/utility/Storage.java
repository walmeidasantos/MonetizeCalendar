package com.bearapps.MonetizeCalendar.utility;

import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.bearapps.MonetizeCalendar.model.ContactObject;
import com.bearapps.MonetizeCalendar.model.EventObject;
import com.bearapps.MonetizeCalendar.model.InvoiceObject;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.EventDateTime;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;


public class Storage {

    public final static String UPDATE_DB = "updateDB";
    public final static String UPDATE_DB_ADD = "updateDbAdd";
    public final static String UPDATE_DB_DELETE = "updateDbDelete";

    public static final String TABLE_CONTACTS = "contacts";
    public static final String CONTACT_GOOGLEID = "googleid";
    public static final String CONTACT_STATUS = "status";   // 0=search for schedules ; 1= no
    public static final String CONTACT_DTINC = "dateinc";
    public static final String CONTACT_ID = "contactid";
    public static final String CONTACT_NAME = "name";
    public static final String CONTACT_EMAIL = "email";
    public static final String CONTACT_PHOTO = "photo";
    public static final String CONTACT_CHARGE = "charge";
    public static final String CONTACT_PERIOD = "period";

    public static final int CONTACT_ACTIVE = 0;
    public static final int CONTACT_INACTIVE = 1;

    public static final String TABLE_EVENTS = "events";
    public static final String EVENTS_GOOGLEID = "googleid";
    public static final String EVENTS_DTINC = "dateinc";
    public static final String EVENTS_DTACTION = "dateaction";
    public static final String EVENTS_ID = "eventid";
    public static final String EVENTS_SUMARY = "sumary";
    public static final String EVENTS_BEGINEVENT = "beginevent";
    public static final String EVENTS_ENDEVENT = "endhour";
    public static final String EVENTS_WHERE = "eventwhere";


    public static final int EVENT_DECISION = 0;
    public static final int EVENT_CONCLUDED = 1;
    public static final int EVENT_CANCEL = 2;
    public static final int EVENT_PAID = 3;

    public static final String TABLE_INVOICE = "invoice";
    public static final String INVOICE_STATUS = "status"; //0 = invoice open ; 1= invoice paid ; 2=invoice cancel
    public static final String INVOICE_AMOUNT = "amount";
    public static final String INVOICE_DTINC = "dateinc";
    public static final String INVOICE_DTACTION = "dateaction";
    public static final String INVOICE_ID = "invoiceid";
    public static final String INVOICE_CONTACTID = "contactid";

    public static final int INVOICE_OPEN = 0;
    public static final int INVOICE_PAID = 1;
    public static final int INVOICE_CANCEL = 2;

    public static final String CHR_TYPE_MONTHLY = "Monthly";
    public static final String CHR_TYPE_WEEKLY = "Weekly";
    public static final String CHR_TYPE_PERCLASS = "Per class";
    public static final String CHR_TYPE_PERHOUR = "Per hour";

    public static final String TABLE_EVENTXCONTACT = "event_contacts";
    public static final String EVENTXCONTACT_CONTACTID = "idcontact_key";
    public static final String EVENTXCONTACT_EVENTID = "idevent_key";
    public static final String EVENTXCONTACT_ID = "tableid";
    public static final String EVENTXCONTACT_STATUS = "event_status";

    public static final String TABLE_INVOICEXEVENTS = "invoice_events";
    public static final String INVOICEXEVENTS_INVOICEID = "idinvoice_key";
    public static final String INVOICEXEVENTS_EVENTID = "idvevent_key";
    public static final String INVOICEXEVENTS_ID = "invoicexeventsid";

    public static final String VIEW_EVENTXCONTACTS = "view_eventxcontacts";
    public static final String VIEW_INVOICEXEVENTS = "view_invoicexevents";

    private static Storage mInstance = null;
    private StorageHelper dbHelper;
    private SQLiteDatabase db;
    private Context context;
    private ClipboardManager eventClipboardManager;

    private List<EventObject> EventsInMemory;
    private List<ContactObject> ContactsInMemory;
    private List<InvoiceObject> InvoiceInMemory;
    private boolean isEventInMemoryChanged = true;
    private boolean isContactInMemoryChanged = true;
    private boolean isInvoiceInMemoryChanged = true;
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

    public int InvoicePaidCode() {
        return EVENT_PAID;
    }

    public List<EventObject> getEvents() {
        return getEvents(null);
    }


    public List<EventObject> getEvents(ContactObject contact) {

        String[] whereParam;
        String whereClause;
        //case getting events for a contact get only events concluded
        if (contact == null) {
            whereParam = new String[]{String.valueOf(EVENT_DECISION)};
            whereClause = EVENTXCONTACT_STATUS + " = ? ";
        } else {
            whereParam = new String[]{String.valueOf(EVENT_CONCLUDED), String.valueOf(contact.getId())};
            whereClause = EVENTXCONTACT_STATUS + " = ? AND " + CONTACT_ID + " = ? ";
            isEventInMemoryChanged = true;
        }

        List<Integer> contactsid = null;
        if (isEventInMemoryChanged) {
            Boolean alreadyOpen = false;
            if (!(db == null)) {
                alreadyOpen = db.isOpen();
            }

            if (!alreadyOpen) {
                open();
            }
            String sortOrder = EVENTS_BEGINEVENT + " ASC ";

            String[] COLUMNS_EVENTS = {EVENTS_GOOGLEID, EVENTS_SUMARY, EVENTS_BEGINEVENT, EVENTS_ENDEVENT, EVENTS_WHERE, CONTACT_ID, EVENTS_ID, CONTACT_NAME, CONTACT_EMAIL};
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
                BeginDate.setTimeZone(TimeZone.getDefault().getID());

                EventDateTime EndDate = new EventDateTime();
                EndDate.setTimeZone(TimeZone.getDefault().getID());
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
                EventsInMemory.get(EventsInMemory.size() - 1).setId(cursor_events.getInt(6));//this makes no sense but is for test purpose

                EventsInMemory.get(EventsInMemory.size() - 1).AddContact(cursor_events.getInt(5));
                EventsInMemory.get(EventsInMemory.size() - 1).AddContactEmail(cursor_events.getString(8));


            }
            cursor_events.close();
            if (!alreadyOpen) {
                close();
            }
            isEventInMemoryChanged = false;
        }

        /*if (EventsInMemory.isEmpty()) {
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
        }*/


        return EventsInMemory;
    }

    public void ChangeStatusContact(String googleid, Integer status) {
        open();
        isContactInMemoryChanged = true;
        ContentValues ContactValues = new ContentValues();

        ContactValues.put(CONTACT_STATUS, status);

        String whereClause = CONTACT_GOOGLEID + " = ?";

        db.update(TABLE_CONTACTS,
                ContactValues,
                whereClause,
                new String[]{String.valueOf(googleid)});

        close();
    }

    public void InactiveContact(String googleId) {
        ChangeStatusContact(googleId, CONTACT_INACTIVE);
    }

    public void ActiveContact(String googleId) {
        ChangeStatusContact(googleId, CONTACT_ACTIVE);
    }

    public void deleteEvent(int EventId) {
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

        List<Integer> contactsId = eventObject.getContactsId();
        Boolean error = false;

        ContentValues EventsValues = new ContentValues();
        ContentValues EventsxContactsValues = new ContentValues();
        EventsValues.put(EVENTS_GOOGLEID, eventObject.getGoogleId());
        EventsValues.put(EVENTS_SUMARY, eventObject.getSumary());
        EventsValues.put(EVENTS_WHERE, eventObject.getWhere());
        if ((eventObject.getBeginEvent().getDateTime() == null)) {
            EventsValues.put(EVENTS_BEGINEVENT, eventObject.getBeginEvent().getDate().getValue());
        } else {
            EventsValues.put(EVENTS_BEGINEVENT, eventObject.getBeginEvent().getDateTime().getValue());
        }
        if (eventObject.getEndEvent().getDateTime() == null) {
            EventsValues.put(EVENTS_ENDEVENT, eventObject.getEndEvent().getDate().getValue());
        } else {
            EventsValues.put(EVENTS_ENDEVENT, eventObject.getEndEvent().getDateTime().getValue());
        }
        db.beginTransaction();

        long row_id = db.insert(TABLE_EVENTS, null, EventsValues);
        if (row_id == -1) {
            Log.e("Storage", "write db error: addEvent " + eventObject.getGoogleId());
            error = true;
        } else {
            eventObject.setId((int) row_id);
        }

        if (!error) {
            for (Integer Contact : contactsId) {
                EventsxContactsValues.put(EVENTXCONTACT_EVENTID, eventObject.getId());
                EventsxContactsValues.put(EVENTXCONTACT_CONTACTID, Contact);
                row_id = db.insert(TABLE_EVENTXCONTACT, null, EventsxContactsValues);
            }
            if (row_id == -1) {
                Log.e("Storage", "write db error: AddEventsxContacts " + eventObject.getGoogleId());
                error = true;

            } else {
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
        ContactsValues.put(CONTACT_EMAIL, contactObject.getEmail().trim().toLowerCase());
        ContactsValues.put(CONTACT_NAME, contactObject.getName());

        long row_id = db.insert(TABLE_CONTACTS, null, ContactsValues);
        if (row_id == -1) {
            Log.e("Storage", "write db error: addContact " + contactObject.getGoogleId());
            error = true;
        } else {
            contactObject.setId((int) row_id);
        }
        isContactInMemoryChanged = true;
        close();
        return !error;
    }

    public ContactObject getContact(int id) {
        open();

        String[] whereParam = new String[]{String.valueOf(id)};
        String whereClause = CONTACT_ID + " = ? ";

        String[] COLUMNS_CONTACTS = {CONTACT_GOOGLEID, CONTACT_NAME, CONTACT_EMAIL, CONTACT_STATUS, CONTACT_PHOTO, CONTACT_PERIOD, CONTACT_CHARGE};

        Cursor cursor_Contacts;

        cursor_Contacts = db.query(TABLE_CONTACTS,
                COLUMNS_CONTACTS,
                whereClause,//where clause
                whereParam,//where params
                null,//groupby
                null,//having
                null);//orderby

        cursor_Contacts.moveToFirst();

        ContactObject contact = new ContactObject(
                cursor_Contacts.getString(0), //googleId
                cursor_Contacts.getString(1), //name
                cursor_Contacts.getString(2), //email
                null,//left disable the status
                cursor_Contacts.getString(4), //photoPath
                cursor_Contacts.getLong(6), //amount
                cursor_Contacts.getString(5)//period
        );

        cursor_Contacts.close();
        close();
        return contact;

    }

    public Integer getContactId(String email) {
        open();
        Integer ContactId = 0;

        String[] whereParam = new String[]{email.trim().toLowerCase()};
        String whereClause = CONTACT_EMAIL + " = ? ";

        String[] COLUMNS_CONTACTS = {CONTACT_ID};

        Cursor cursor_Contacts;

        cursor_Contacts = db.query(TABLE_CONTACTS,
                COLUMNS_CONTACTS,
                whereClause,//where clause
                whereParam,//where params
                null,//groupby
                null,//having
                null);//orderby
        cursor_Contacts.moveToFirst();
        if (cursor_Contacts.getCount() > 0) {
            ContactId = cursor_Contacts.getInt(0);
        } else {
            ContactId = 0;
        }

        cursor_Contacts.close();
        close();
        return ContactId;

    }

    public List<ContactObject> getAllContacts() {

        if (isContactInMemoryChanged) {
            open();

            String sortOrder = CONTACT_NAME + " ASC ";

            String[] COLUMNS_CONTACTS = {CONTACT_GOOGLEID, CONTACT_NAME, CONTACT_EMAIL, CONTACT_STATUS, CONTACT_PHOTO, CONTACT_PERIOD, CONTACT_CHARGE, CONTACT_ID};

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

                ContactsInMemory.get(ContactsInMemory.size() - 1).setId(cursor_Contacts.getInt(7));

            }

            cursor_Contacts.close();
            close();
            isContactInMemoryChanged = false;

        }

        return ContactsInMemory;
    }

    public List<ContactObject> getSelectContacts() {

        if (isContactInMemoryChanged) {
            open();

            String[] whereParam = new String[]{String.valueOf(CONTACT_ACTIVE)};
            String whereClause = CONTACT_STATUS + " = ? ";

            String sortOrder = CONTACT_NAME + " ASC ";

            String[] COLUMNS_CONTACTS = {CONTACT_GOOGLEID, CONTACT_NAME, CONTACT_EMAIL, CONTACT_STATUS, CONTACT_PHOTO, CONTACT_PERIOD, CONTACT_CHARGE};

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

    public EventObject changeEventStatus(EventObject eventObject, int choice) {
        open();
        isEventInMemoryChanged = true;
        ContentValues EventsValues = new ContentValues();

        switch (choice) {
            case EVENT_CANCEL:
                EventsValues.put(EVENTXCONTACT_STATUS, EVENT_CANCEL);
            case EVENT_DECISION:
                EventsValues.put(EVENTXCONTACT_STATUS, EVENT_DECISION);
            case EVENT_CONCLUDED:
                EventsValues.put(EVENTXCONTACT_STATUS, EVENT_CONCLUDED);
        }
        String whereClause = EVENTXCONTACT_EVENTID + " = ? AND " + EVENTXCONTACT_CONTACTID + " = ? ";

        int response = db.update(TABLE_EVENTXCONTACT,
                EventsValues,
                whereClause,
                new String[]{eventObject.getId().toString(), eventObject.getFirstContact().toString()});


        close();
        return eventObject;
    }

    public void importEvents(List<EventObject> newEvents) {
        open();
        isEventInMemoryChanged = true;

        // Populate the map
        Map<String, EventObject> StoredEvents = new HashMap<String, EventObject>();
        for (EventObject storedEvent : getEvents()) {
            StoredEvents.put(storedEvent.getGoogleId(), storedEvent);
        }
        for (EventObject eventObject : newEvents) {
            if (!StoredEvents.containsKey(eventObject.getGoogleId())) {
                addEvent(eventObject);
            }
        }
        close();
        isEventInMemoryChanged = true;
    }

    public void modifyContact(String GoogleId, String period, long amount) {

        open();
        isContactInMemoryChanged = true;
        ContentValues ContactValues = new ContentValues();

        ContactValues.put(CONTACT_CHARGE, amount);
        ContactValues.put(CONTACT_PERIOD, period);

        String whereClause = CONTACT_GOOGLEID + " = ?";

        db.update(TABLE_CONTACTS,
                ContactValues,
                whereClause,
                new String[]{String.valueOf(GoogleId)});

        getAllContacts();
        close();

    }

    public List<InvoiceObject> getInvoices() {
        List<InvoiceObject> invoices = new ArrayList<>();
        String[] whereParam = new String[]{String.valueOf(INVOICE_OPEN)};
        String whereClause = INVOICE_STATUS + " = ? ";

        if (isInvoiceInMemoryChanged) {
            open();

            String sortOrder = INVOICE_DTINC + " ASC ";

            String[] COLUMNS_INVOICE = {INVOICE_CONTACTID, INVOICE_ID, INVOICE_AMOUNT, INVOICE_DTINC, EVENTS_ID};
            Cursor cursor_invoice;

            cursor_invoice = db.query(VIEW_INVOICEXEVENTS,
                    COLUMNS_INVOICE,
                    null,//where clause
                    null,//where params
                    null,//groupby
                    null,//having
                    sortOrder);//orderby

            InvoiceInMemory = new ArrayList<>();
            while (cursor_invoice.moveToNext()) {

                ContactObject contact = getContact(cursor_invoice.getInt(0));

                InvoiceInMemory.add(
                        new InvoiceObject(
                                contact,
                                cursor_invoice.getLong(2),
                                new Date(cursor_invoice.getLong(3))
                        )
                );
                InvoiceInMemory.get(InvoiceInMemory.size() - 1).setId(cursor_invoice.getInt(1));//this makes no sense but is for test purpose

            }
            cursor_invoice.close();
            close();
            isInvoiceInMemoryChanged = false;
        }
        return InvoiceInMemory;
    }


    public void importInvoice(List<InvoiceObject> newInvoices) {

        open();
        for (InvoiceObject invoice : newInvoices) {


            List<Integer> eventsId = invoice.getEventsId();
            Boolean error = false;

            ContentValues InvoiceValues = new ContentValues();
            ContentValues InvoicexEventsValues = new ContentValues();
            InvoiceValues.put(INVOICE_AMOUNT, invoice.getAmount());
            InvoiceValues.put(INVOICE_CONTACTID, invoice.getContact().getId());

            db.beginTransaction();

            long row_id = db.insert(TABLE_INVOICE, null, InvoiceValues);
            if (row_id == -1) {
                Log.e("Storage", "write db error: addInvoice for contact " + invoice.getContact().getName());
                error = true;
            } else {
                invoice.setId((int) row_id);
            }

            if (!error) {
                for (Integer event : eventsId) {
                    InvoicexEventsValues.put(INVOICEXEVENTS_EVENTID, event);
                    InvoicexEventsValues.put(INVOICEXEVENTS_INVOICEID, invoice.getId());
                    row_id = db.insert(TABLE_INVOICEXEVENTS, null, InvoicexEventsValues);
                }
                if (row_id == -1) {
                    Log.e("Storage", "write db error: AddInvoicexEvents for contacts " + invoice.getContact().getName());
                    error = true;

                } else {
                    db.setTransactionSuccessful();
                }

            }

            db.endTransaction();
            if (!error) {
                isInvoiceInMemoryChanged = true;
            }
        }
        close();

        return;
    }


}
