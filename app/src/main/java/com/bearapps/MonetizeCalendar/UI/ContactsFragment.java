package com.bearapps.MonetizeCalendar.UI;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.QuickContactBadge;
import android.widget.TextView;
import android.widget.Toast;

import com.bearapps.MonetizeCalendar.R;
import com.bearapps.MonetizeCalendar.model.ContactObject;
import com.bearapps.MonetizeCalendar.utility.Storage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public abstract class ContactsFragment extends Fragment implements AdapterView.OnItemClickListener {

    private RecyclerView mList;
    private ContactAdapter mAdapter;
    private Storage db;
    private Context context;
    private String TAG = "MonetizeCalendar";
    SharedPreferences settings;


    /**
     * Required Overrides for Sample Fragments
     */

    protected abstract RecyclerView.LayoutManager getLayoutManager();

    protected abstract RecyclerView.ItemDecoration getItemDecoration();

    protected abstract ContactAdapter getAdapter();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.view_contact_recycler, container, false);

        mList = (RecyclerView) rootView.findViewById(R.id.contact_list);
        mList.setLayoutManager(getLayoutManager());
        mList.addItemDecoration(getItemDecoration());

        mList.getItemAnimator().setAddDuration(1000);
        mList.getItemAnimator().setChangeDuration(1000);
        mList.getItemAnimator().setMoveDuration(1000);
        mList.getItemAnimator().setRemoveDuration(1000);
        context = getActivity().getBaseContext();

        db = Storage.getInstance(context);

        mAdapter = getAdapter();
        //mAdapter.AddContacts(db.getAllContacts());
        mAdapter.setOnItemClickListener(this);
        mList.setAdapter(mAdapter);

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
        refreshContactList();

    }

    /**
     * Fill the event display with the given List of strings; called from
     * background threads and async tasks that need to update the UI (in the
     * UI thread).
     *
     * @param contactObjects a List of Strings to populate the event display with.
     */
    public void updateContactList(final List<ContactObject> contactObjects) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getActivity().setProgressBarIndeterminateVisibility(false);
                    if (contactObjects == null) {

                        Toast.makeText(
                                getActivity(),
                                getString(R.string.error_retrieving_contacts),
                                Toast.LENGTH_LONG
                        ).show();

                    } else if (contactObjects.size() == 0) { //TODO REMOVE THIS BEFORE RELEASE
                        Toast.makeText(
                                getActivity(),
                                getString(R.string.no_contacts),
                                Toast.LENGTH_LONG
                        ).show();

                    } else {
                        //TODO remove this
                        Toast.makeText(
                                getActivity(),
                                getString(R.string.contacts_arrived),
                                Toast.LENGTH_LONG
                        ).show();

                        List<ContactObject> ChoosedContacts = db.getAllContacts();
                        for (int count = 0; count < contactObjects.size(); count++) {
                            for (int count2 = 0; count2 < ChoosedContacts.size(); count2++) {
                                if (ChoosedContacts.get(count2).getGoogleId().contentEquals(contactObjects.get(count).getGoogleId())) {
                                    contactObjects.get(count).setChoosed();

                                }
                            }
                        }

                        mAdapter = new ContactAdapter(contactObjects);
                        mList.setAdapter(mAdapter);
                        mAdapter.notifyDataSetChanged();
                    }
                }
            });
        }
    }

    /**
     * Show a status message in the list header TextView; called from background
     * threads and async tasks that need to update the UI (in the UI thread).
     *
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
    private void refreshContactList() {

        getActivity().setProgressBarIndeterminateVisibility(true);
        Toast.makeText(getActivity(),
                context.getString(R.string.wait_contacts),
                Toast.LENGTH_SHORT).show();
        new ContactFecthTask().execute();
    }

    /**
     * Fetch a list of the next 10 events from the primary calendar.
     * @return List of Strings describing returned events.
     * @throws IOException
     */

    /**
     * Created by ursow on 11/04/15.
     */
    public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ClipCardViewHolder> {
        private List<ContactObject> contactObjectList;
        private AdapterView.OnItemClickListener mOnItemClickListener;

        public ContactAdapter(List<ContactObject> Contacts) {

            contactObjectList = Contacts;
            notifyDataSetChanged();

        }

        public void AddContacts(List<ContactObject> Contacts) {
            contactObjectList.clear();
            contactObjectList.addAll(Contacts);

            notifyDataSetChanged();
        }

        @Override
        public int getItemCount() {
            return contactObjectList.size();
        }

        @Override
        public void onBindViewHolder(final ClipCardViewHolder clipCardViewHolder, int i) {
            final ContactObject contactObject = contactObjectList.get(i);

            clipCardViewHolder.vContact.setText(contactObject.getName());
            if (contactObject.getPhotoPath() == null) {
                clipCardViewHolder.vBagde.setImageResource(R.drawable.avatar_empty);
            } else {
                clipCardViewHolder.vBagde.setImageURI(Uri.parse(contactObject.getPhotoPath()));
            }

            if (contactObject.IsChoosed()) {
                clipCardViewHolder.vCheckUser.setVisibility(View.VISIBLE);
            } else {
                clipCardViewHolder.vCheckUser.setVisibility(View.INVISIBLE);
            }

            clipCardViewHolder.vBagde.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (contactObject.IsChoosed()) {
                        clipCardViewHolder.vCheckUser.setVisibility(View.INVISIBLE);
                        contactObject.setUnChoosed();
                        db.InactiveContact(contactObject.getGoogleId());

                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(EventsFragment.SYNC_TOKEN_CALENDAR, null);
                        editor.commit();

                    } else {
                        db.addContact(contactObject);
                        clipCardViewHolder.vCheckUser.setVisibility(View.VISIBLE);
                        contactObject.setChoosed();
                    }
                }
            });

            clipCardViewHolder.vCheckUser.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (contactObject.IsChoosed()) {
                        clipCardViewHolder.vCheckUser.setVisibility(View.INVISIBLE);
                        contactObject.setUnChoosed();
                        db.InactiveContact(contactObject.getGoogleId());

                    } else {
                        // db.addContact(contactObject);
                        clipCardViewHolder.vCheckUser.setVisibility(View.VISIBLE);
                        contactObject.setChoosed();
                        db.ActiveContact(contactObject.getGoogleId());

                    }
                }
            });


            //setAnimation(clipCardViewHolder.vMain, i);

        }

        @Override
        public ClipCardViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View itemView = LayoutInflater.
                    from(viewGroup.getContext()).
                    inflate(R.layout.activity_contact_card, viewGroup, false);

            return new ClipCardViewHolder(itemView);
        }

        public void add(int position, ContactObject contactObject) {
            contactObjectList.add(position, contactObject);
            notifyItemInserted(position);
        }

        public void remove(int position) {
            contactObjectList.remove(position);
            notifyItemRemoved(position);
        }

        public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
            mOnItemClickListener = onItemClickListener;
        }

        public class ClipCardViewHolder extends RecyclerView.ViewHolder {
            protected TextView vContact;
            protected QuickContactBadge vBagde;
            protected QuickContactBadge vCheckUser;
            protected View vMain;

            public ClipCardViewHolder(View v) {
                super(v);
                vContact = (TextView) v.findViewById(R.id.user_name);
                vBagde = (QuickContactBadge) v.findViewById(R.id.photo_contact);
                vCheckUser = (QuickContactBadge) v.findViewById(R.id.checkImageView);
                vMain = v;
            }
        }


    }

    /**
     * An asynchronous task that handles the Calendar API event list retrieval.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    private class ContactFecthTask extends AsyncTask<Void, Void, Void> {

        /**
         * Background task to call Calendar API to fetch event list.
         *
         * @param params no parameters needed for this task.
         */
        @Override
        protected Void doInBackground(Void... params) {
            try {
                updateContactList(fecthContacts(getActivity()));
            } catch (IOException e) {
                updateStatus("The following error occurred: " +
                        e.getMessage());
            }
            return null;
        }

        private List<ContactObject> fecthContacts(Activity mActivity) throws IOException {

            Uri contactsUri = ContactsContract.Contacts.CONTENT_URI;
            List<ContactObject> Contacts = new ArrayList<>();
            if ( mActivity.getContentResolver() != null ) {


                // Querying the table ContactsContract.Contacts to retrieve all the contacts
                Cursor contactsCursor = mActivity.getContentResolver().query(contactsUri, null, null, null,
                        ContactsContract.Contacts.DISPLAY_NAME + " ASC ");

                if (contactsCursor.moveToFirst()) {
                    do {
                        long contactId = contactsCursor.getLong(contactsCursor.getColumnIndex("_ID"));


                        Uri dataUri = ContactsContract.Data.CONTENT_URI;

                        // Querying the table ContactsContract.Data to retrieve individual items like
                        // home phone, mobile phone, work email etc corresponding to each contact
                        Cursor dataCursor = mActivity.getContentResolver().query(dataUri, null,
                                ContactsContract.Data.CONTACT_ID + "=" + contactId,
                                null, null);


                        String displayName = "";
                        String nickName = "";
                        String homePhone = "";
                        String mobilePhone = "";
                        String workPhone = "";
                        String photoPath = "" + R.drawable.avatar_empty;
                        byte[] photoByte = null;
                        String homeEmail = "";
                        String workEmail = "";
                        String companyName = "";
                        String title = "";
                        String Id = "";


                        if (dataCursor != null && dataCursor.moveToFirst()) {
                            // Getting Display Name
                            displayName = dataCursor.getString(dataCursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
                            Id = dataCursor.getString(dataCursor.getColumnIndex(ContactsContract.Data._ID));
                            do {

                                // Getting NickName
                                if (dataCursor.getString(dataCursor.getColumnIndex("mimetype")).equals(ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE))
                                    nickName = dataCursor.getString(dataCursor.getColumnIndex("data1"));

                                // Getting Phone numbers
                                if (dataCursor.getString(dataCursor.getColumnIndex("mimetype")).equals(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)) {
                                    switch (dataCursor.getInt(dataCursor.getColumnIndex("data2"))) {
                                        case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
                                            homePhone = dataCursor.getString(dataCursor.getColumnIndex("data1"));
                                            break;
                                        case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
                                            mobilePhone = dataCursor.getString(dataCursor.getColumnIndex("data1"));
                                            break;
                                        case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
                                            workPhone = dataCursor.getString(dataCursor.getColumnIndex("data1"));
                                            break;
                                    }
                                }

                                // Getting EMails
                                if (dataCursor.getString(dataCursor.getColumnIndex("mimetype")).equals(ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)) {
                                    switch (dataCursor.getInt(dataCursor.getColumnIndex("data2"))) {
                                        case ContactsContract.CommonDataKinds.Email.TYPE_HOME:
                                            homeEmail = dataCursor.getString(dataCursor.getColumnIndex("data1"));
                                            break;
                                        case ContactsContract.CommonDataKinds.Email.TYPE_WORK:
                                            workEmail = dataCursor.getString(dataCursor.getColumnIndex("data1"));
                                            break;
                                    }
                                }

                                // Getting Organization details
                                if (dataCursor.getString(dataCursor.getColumnIndex("mimetype")).equals(ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)) {
                                    companyName = dataCursor.getString(dataCursor.getColumnIndex("data1"));
                                    title = dataCursor.getString(dataCursor.getColumnIndex("data4"));
                                }

                                // Getting Photo
                                if (dataCursor.getString(dataCursor.getColumnIndex("mimetype")).equals(ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)) {
                                    photoByte = dataCursor.getBlob(dataCursor.getColumnIndex("data15"));

                                    if (photoByte != null) {
                                        Bitmap bitmap = BitmapFactory.decodeByteArray(photoByte, 0, photoByte.length);

                                        // Getting Caching directory
                                        File cacheDirectory = mActivity.getBaseContext().getCacheDir();

                                        // Temporary file to store the contact image
                                        File tmpFile = new File(cacheDirectory.getPath() + "/wpta_" + contactId + ".png");

                                        // The FileOutputStream to the temporary file
                                        try {
                                            FileOutputStream fOutStream = new FileOutputStream(tmpFile);

                                            // Writing the bitmap to the temporary file as png file
                                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOutStream);
                                            displayName = dataCursor.getString(dataCursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));

                                            // Flush the FileOutputStream
                                            fOutStream.flush();

                                            //Close the FileOutputStream
                                            fOutStream.close();

                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                        photoPath = tmpFile.getPath();
                                    } else {
                                        photoPath = null;
                                    }

                                }


                            } while (dataCursor.moveToNext());



                    /*
                    String details = "";

                    // Concatenating various information to single string
                    if(homePhone != null && !homePhone.equals("") )
                        details = "HomePhone : " + homePhone + "\n";
                    if(mobilePhone != null && !mobilePhone.equals("") )
                        details += "MobilePhone : " + mobilePhone + "\n";
                    if(workPhone != null && !workPhone.equals("") )
                        details += "WorkPhone : " + workPhone + "\n";
                    if(nickName != null && !nickName.equals("") )
                        details += "NickName : " + nickName + "\n";
                    if(homeEmail != null && !homeEmail.equals("") )
                        details += "HomeEmail : " + homeEmail + "\n";
                    if(workEmail != null && !workEmail.equals("") )
                        details += "WorkEmail : " + workEmail + "\n";
                    if(companyName != null && !companyName.equals("") )
                        details += "CompanyName : " + companyName + "\n";
                    if(title != null && !title.equals("") )
                        details += "Title : " + title + "\n";
                    */

                            // Adding id, display name, path to photo and other details to cursor

                        }

                        Contacts.add(
                                new ContactObject(
                                        Id,
                                        displayName,
                                        homeEmail,
                                        null,
                                        photoPath
                                )
                        );

                    } while (contactsCursor.moveToNext());
                    contactsCursor.close();
                }
            }
            return Contacts;

        }
    }


}
