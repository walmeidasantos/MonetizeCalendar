package com.bearapps.MonetizeCalendar.utility;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import static com.bearapps.MonetizeCalendar.utility.Storage.CONTACT_CHARGE;
import static com.bearapps.MonetizeCalendar.utility.Storage.CONTACT_DTINC;
import static com.bearapps.MonetizeCalendar.utility.Storage.CONTACT_EMAIL;
import static com.bearapps.MonetizeCalendar.utility.Storage.CONTACT_GOOGLEID;
import static com.bearapps.MonetizeCalendar.utility.Storage.CONTACT_ID;
import static com.bearapps.MonetizeCalendar.utility.Storage.CONTACT_NAME;
import static com.bearapps.MonetizeCalendar.utility.Storage.CONTACT_PERIOD;
import static com.bearapps.MonetizeCalendar.utility.Storage.CONTACT_PHOTO;
import static com.bearapps.MonetizeCalendar.utility.Storage.CONTACT_STATUS;
import static com.bearapps.MonetizeCalendar.utility.Storage.EVENTS_BEGINEVENT;
import static com.bearapps.MonetizeCalendar.utility.Storage.EVENTS_DTACTION;
import static com.bearapps.MonetizeCalendar.utility.Storage.EVENTS_DTINC;
import static com.bearapps.MonetizeCalendar.utility.Storage.EVENTS_ENDEVENT;
import static com.bearapps.MonetizeCalendar.utility.Storage.EVENTS_GOOGLEID;
import static com.bearapps.MonetizeCalendar.utility.Storage.EVENTS_ID;
import static com.bearapps.MonetizeCalendar.utility.Storage.EVENTS_SUMARY;
import static com.bearapps.MonetizeCalendar.utility.Storage.EVENTS_WHERE;
import static com.bearapps.MonetizeCalendar.utility.Storage.EVENTXCONTACT_CONTACTID;
import static com.bearapps.MonetizeCalendar.utility.Storage.EVENTXCONTACT_EVENTID;
import static com.bearapps.MonetizeCalendar.utility.Storage.EVENTXCONTACT_ID;
import static com.bearapps.MonetizeCalendar.utility.Storage.EVENTXCONTACT_STATUS;
import static com.bearapps.MonetizeCalendar.utility.Storage.INVOICEXEVENTS_EVENTID;
import static com.bearapps.MonetizeCalendar.utility.Storage.INVOICEXEVENTS_ID;
import static com.bearapps.MonetizeCalendar.utility.Storage.INVOICEXEVENTS_INVOICEID;
import static com.bearapps.MonetizeCalendar.utility.Storage.INVOICE_AMOUNT;
import static com.bearapps.MonetizeCalendar.utility.Storage.INVOICE_CONTACTID;
import static com.bearapps.MonetizeCalendar.utility.Storage.INVOICE_DTACTION;
import static com.bearapps.MonetizeCalendar.utility.Storage.INVOICE_DTINC;
import static com.bearapps.MonetizeCalendar.utility.Storage.INVOICE_ID;
import static com.bearapps.MonetizeCalendar.utility.Storage.TABLE_CONTACTS;
import static com.bearapps.MonetizeCalendar.utility.Storage.TABLE_EVENTS;
import static com.bearapps.MonetizeCalendar.utility.Storage.TABLE_EVENTXCONTACT;
import static com.bearapps.MonetizeCalendar.utility.Storage.TABLE_INVOICE;
import static com.bearapps.MonetizeCalendar.utility.Storage.TABLE_INVOICEXEVENTS;
import static com.bearapps.MonetizeCalendar.utility.Storage.VIEW_EVENTXCONTACTS;
import static com.bearapps.MonetizeCalendar.utility.Storage.VIEW_INVOICEXEVENTS;


/**
 * Created by ursow on 17/04/15.
 */
public class StorageHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "MonetizeCalendar.db";

    private static final String CREATE_CONTACTS =
            "CREATE TABLE " + TABLE_CONTACTS + " ( `" +
                    CONTACT_ID + "` INTEGER PRIMARY KEY AUTOINCREMENT, `" +
                    CONTACT_GOOGLEID + "` TEXT, `" +
                    CONTACT_STATUS + "` INTEGER DEFAULT '1', `" +
                    CONTACT_DTINC + "` INTEGER DEFAULT 'CURRENT_TIMESTAMP' , `" +
                    CONTACT_NAME + "` TEXT, `" +
                    CONTACT_PHOTO + "` TEXT, `" +
                    CONTACT_CHARGE + "` REAL DEFAULT '0', `" +
                    CONTACT_PERIOD + "` TEXT, `" +
                    CONTACT_EMAIL + "` TEXT " +
                    "); ";

    private static final String CREATE_EVENTS =
            "CREATE TABLE " + TABLE_EVENTS + " ( `" +
                    EVENTS_ID + "` INTEGER PRIMARY KEY AUTOINCREMENT, `" +
                    EVENTS_GOOGLEID + "` TEXT, `" +
                    EVENTS_DTINC + "` INTEGER DEFAULT 'CURRENT_TIMESTAMP', `" +
                    EVENTS_DTACTION + "` INTEGER , `" +
                    EVENTS_BEGINEVENT + "` INTEGER  , `" +
                    EVENTS_ENDEVENT + "` INTEGER ,  `" +
                    EVENTS_WHERE + "` TEXT , `" +
                    EVENTS_SUMARY + "` TEXT  " +
                    "); ";


    private static final String CREATE_INVOICE =
            "CREATE TABLE " + TABLE_INVOICE + " (`" +
                    INVOICE_ID + "` INTEGER PRIMARY KEY AUTOINCREMENT, `" +
                    INVOICE_DTINC + "` INTEGER DEFAULT 'CURRENT_TIMESTAMP', `" +
                    INVOICE_DTACTION + "` INTEGER DEFAULT 'CURRENT_TIMESTAMP', `" +
                    INVOICE_AMOUNT + "` REAL DEFAULT '0', `" +
                    INVOICE_CONTACTID + "` INTEGER, " +
                    "  FOREIGN KEY( `" + INVOICE_CONTACTID + "`) REFERENCES `" + TABLE_CONTACTS + "`(`" + CONTACT_ID + "`) " +
                    "); ";


    private static final String CREATE_EVENTSXCONTACTS =
            "CREATE TABLE " + TABLE_EVENTXCONTACT + " ( `" +
                    EVENTXCONTACT_ID + "` INTEGER PRIMARY KEY AUTOINCREMENT, `" +
                    EVENTXCONTACT_CONTACTID + "` TEXT , `" +
                    EVENTXCONTACT_STATUS + "` INTEGER DEFAULT '0', `" +
                    EVENTXCONTACT_EVENTID + "` INTEGER, " +
                    "  FOREIGN KEY( `" + EVENTXCONTACT_CONTACTID + "` ) REFERENCES `" + TABLE_CONTACTS + "` ( `" + CONTACT_EMAIL + "`), " +
                    "  FOREIGN KEY( `" + EVENTXCONTACT_EVENTID + "` ) REFERENCES `" + TABLE_EVENTS + "` (`" + EVENTS_ID + "`) " +
                    "); ";


    private static final String CREATE_INVOICEXEVENTS =
            "CREATE TABLE " + TABLE_INVOICEXEVENTS + " (`" +
                    INVOICEXEVENTS_ID + "` INTEGER PRIMARY KEY AUTOINCREMENT, `" +
                    INVOICEXEVENTS_EVENTID + "` INTEGER , `" +
                    INVOICEXEVENTS_INVOICEID + "` INTEGER, " +
                    "  FOREIGN KEY( `" + INVOICEXEVENTS_INVOICEID + "`) REFERENCES `" + TABLE_CONTACTS + "` (`" + CONTACT_ID + "`), " +
                    "  FOREIGN KEY( `" + INVOICEXEVENTS_EVENTID + "`) REFERENCES `" + TABLE_INVOICE + "` (`" + INVOICE_ID + "`) " +
                    "); ";
    private static final String CREATE_VIEW_EVENTSXCONTACTS =
            "CREATE VIEW " + VIEW_EVENTXCONTACTS + " AS SELECT a.*,b.*,c.* " +
                    " FROM " + TABLE_EVENTXCONTACT + " AS c " +
                    " JOIN " + TABLE_EVENTS + " AS a ON " +
                    EVENTXCONTACT_EVENTID + " = a." + EVENTS_ID +
                    " JOIN " + TABLE_CONTACTS + " AS b ON " +
                    EVENTXCONTACT_CONTACTID + " = b." + CONTACT_ID + "; ";
    private static final String CREATE_VIEW_INVOICEXEVENTS =
            "CREATE VIEW " + VIEW_INVOICEXEVENTS + " AS SELECT a.*,b.* " +
                    " FROM " + TABLE_INVOICEXEVENTS + " AS c " +
                    " JOIN " + TABLE_EVENTS + " AS a ON " +
                    INVOICEXEVENTS_EVENTID + " = a." + EVENTS_ID +
                    " JOIN " + TABLE_INVOICE + " AS b ON " +
                    INVOICEXEVENTS_INVOICEID + " = b." + INVOICE_ID + "; ";
    private String CREATE_INDICE_INVOICE_ID =
            " CREATE UNIQUE INDEX `invoice_id` ON `" + TABLE_INVOICE + "` (`" + INVOICE_ID + "` ASC); ";
    private String CREATE_INDICE_INVOICE_EVENTS_IDINVOICE =
            "CREATE INDEX `invoice_events_idinvoice` ON `" + TABLE_INVOICEXEVENTS + "` (`" + INVOICEXEVENTS_EVENTID + "` ASC); ";
    private String CREATE_INDICE_INVOICE_EVENTS_IDEVENT =
            "CREATE INDEX `invoice_events_idevent` ON `" + TABLE_INVOICEXEVENTS + "` (`" + INVOICEXEVENTS_EVENTID + "` ASC); ";
    private String CREATE_INDICE_INVOICE_CONTACTID =
            "CREATE UNIQUE INDEX `invoice_contactid` ON `" + TABLE_INVOICE + "` (`" + CONTACT_ID + "` ASC); ";
    private String CREATE_INDICE_EVENTS_IDGOOGLE =
            "CREATE UNIQUE INDEX `events_idgoogle` ON `" + TABLE_EVENTS + "` (`" + EVENTS_GOOGLEID + "` ASC); ";
    private String CREATE_INDICE_EVENTS_ID =
            "CREATE UNIQUE INDEX `events_id` ON `" + TABLE_EVENTS + "` (`" + EVENTS_ID + "` ASC); ";
    private String CREATE_INDICE_CONTACTS_IDEVENT =
            "CREATE INDEX `events_contacts_idevent` ON `" + TABLE_EVENTXCONTACT + "` (`" + EVENTXCONTACT_EVENTID + "` ASC); ";
    private String CREATE_INDICE_CONTACT_IDCONTACT =
            "CREATE INDEX `events_contact_idcontact` ON `" + TABLE_EVENTXCONTACT + "` (`" + EVENTXCONTACT_CONTACTID + "` ASC);; ";
    private String CREATE_INDICE_CONTACTS_IDGOOGLE =
            "CREATE INDEX `contacts_idgoogle` ON `" + TABLE_CONTACTS + "` (`" + CONTACT_GOOGLEID + "` ASC); ";
    private String CREATE_INDICE_CONTACTS_ID =
            "CREATE UNIQUE INDEX `contacts_id` ON `" + TABLE_CONTACTS + "` (`" + CONTACT_ID + "`  ASC); ";
    private String CREATE_INDICE_CONTACTS_EMAIL =
            "CREATE INDEX `contacts_email` ON `" + TABLE_CONTACTS + "` (`" + CONTACT_EMAIL + "`  ASC); ";

    public StorageHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(CREATE_CONTACTS);
        db.execSQL(CREATE_EVENTS);
        db.execSQL(CREATE_EVENTSXCONTACTS);
        db.execSQL(CREATE_INVOICE);
        db.execSQL(CREATE_INVOICEXEVENTS);

        db.execSQL(CREATE_INDICE_CONTACT_IDCONTACT);
        db.execSQL(CREATE_INDICE_CONTACTS_ID);
        db.execSQL(CREATE_INDICE_CONTACTS_EMAIL);
        db.execSQL(CREATE_INDICE_CONTACTS_IDEVENT);
        db.execSQL(CREATE_INDICE_CONTACTS_IDGOOGLE);
        db.execSQL(CREATE_INDICE_EVENTS_ID);
        db.execSQL(CREATE_INDICE_EVENTS_IDGOOGLE);
        db.execSQL(CREATE_INDICE_INVOICE_CONTACTID);
        db.execSQL(CREATE_INDICE_INVOICE_EVENTS_IDEVENT);
        db.execSQL(CREATE_INDICE_INVOICE_EVENTS_IDINVOICE);
        db.execSQL(CREATE_INDICE_INVOICE_ID);

        db.execSQL(CREATE_VIEW_EVENTSXCONTACTS);
        db.execSQL(CREATE_VIEW_INVOICEXEVENTS);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.v(MyUtil.PACKAGE_NAME, " Nothing to do now ");
    }
}
