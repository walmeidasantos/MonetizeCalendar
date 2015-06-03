package com.bearapps.ground_control.UI;

import android.content.Context;
import android.net.Uri;
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
import android.widget.QuickContactBadge;
import android.widget.TextView;
import android.widget.Toast;

import com.bearapps.ground_control.R;
import com.bearapps.ground_control.model.ContactObject;
import com.bearapps.ground_control.model.EventObject;
import com.bearapps.ground_control.model.InvoiceObject;
import com.bearapps.ground_control.utility.Storage;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;


public abstract class InvoiceFragment extends Fragment implements AdapterView.OnItemClickListener {

    private RecyclerView mList;
    private InvoiceAdapter mAdapter;
    private Storage db;
    private Context context;
    private String TAG = "ground_control";

    protected abstract RecyclerView.LayoutManager getLayoutManager();

    protected abstract RecyclerView.ItemDecoration getItemDecoration();

    protected abstract InvoiceAdapter getAdapter();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.view_editcontact_recycler, container, false);

        mList = (RecyclerView) rootView.findViewById(R.id.editcontact_list);
        mList.setLayoutManager(getLayoutManager());
        mList.addItemDecoration(getItemDecoration());

        mList.getItemAnimator().setAddDuration(1000);
        mList.getItemAnimator().setChangeDuration(1000);
        mList.getItemAnimator().setMoveDuration(1000);
        mList.getItemAnimator().setRemoveDuration(1000);
        context = getActivity().getBaseContext();

        db = Storage.getInstance(context);

        mAdapter = getAdapter();
        mList.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(this);
        new InvoiceFecthTask().execute();

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

    @Override
    public void onResume() {
        super.onResume();
        mAdapter.AddInvoice(db.getInvoice());
    }

    private List<InvoiceObject> generateInvoice() {

        List<InvoiceObject> NewInvoices = new ArrayList<>();
        List<ContactObject> Contacts    = db.getContacts();
        List<EventObject> ContactEvents = new ArrayList<>();

        final Calendar Cal = Calendar.getInstance();
        Cal.setTimeZone( TimeZone.getDefault() );
        int mYear = Cal.get(Calendar.YEAR);
        int mMonth = Cal.get(Calendar.MONTH);
        int mDay = Cal.get(Calendar.DAY_OF_MONTH);

        for (ContactObject Contact: Contacts) {
            ContactEvents = db.getEvents(Contact);
            switch ( Contact.getPeriod() ) {

                case "Monthly":

                    if (ContactEvents.size() > 0 ) {
                        EventObject event = ContactEvents.get(0);
                        InvoiceObject NewInvoice;
                        Date start = new Date( event.getBeginEvent().getDateTime().getValue() );
                        //case the first event of the contact is the current month ignore
                        if ( mMonth == start.getMonth() || mYear == start.getYear() ) {
                            NewInvoice = new InvoiceObject(Contact,Contact.getAmount(), new Date() );
                            NewInvoices.add(NewInvoice);
                            ContactEvents.get(0).setStatus(db.InvoicePaidCode());
                        }

                        int lastMonth = start.getMonth();
                        for (int count = 1; count < ContactEvents.size(); count++) {
                            event = ContactEvents.get(count);
                            start = new Date( event.getBeginEvent().getDateTime().getValue() );

                            if ( !(lastMonth == start.getMonth() || mYear == start.getYear()) ) {
                                NewInvoice = new InvoiceObject(Contact,Contact.getAmount(), new Date() );
                                NewInvoices.add(NewInvoice);
                                ContactEvents.get(0).setStatus(db.InvoicePaidCode());
                                start = new Date( event.getBeginEvent().getDateTime().getValue() );
                                lastMonth = start.getMonth();


                            }
                        }
                    }


                case "Weekly":
                    if (ContactEvents.size() > 0 ) {
                        EventObject event = ContactEvents.get(0);
                        InvoiceObject NewInvoice;
                        Date start = new Date( event.getBeginEvent().getDateTime().getValue() );
                        //case the first event of the contact is the current month ignore
                        if ( mMonth == start.getMonth() || mYear == start.getYear() ) {
                            NewInvoice = new InvoiceObject(Contact,Contact.getAmount(), new Date() );
                            NewInvoices.add(NewInvoice);
                            ContactEvents.get(0).setStatus(db.InvoicePaidCode());
                        }

                        int lastMonth = start.getMonth();
                        Cal.setTime(start);
                        int lastWeek = Cal.get(Calendar.WEEK_OF_MONTH);

                        for (int count = 1; count < ContactEvents.size(); count++) {
                            event = ContactEvents.get(count);
                            start = new Date( event.getBeginEvent().getDateTime().getValue() );

                            if ( !(lastMonth == start.getMonth() || mYear == start.getYear() || lastWeek == Cal.get(Calendar.WEEK_OF_MONTH) ) ) {
                                NewInvoice = new InvoiceObject(Contact,Contact.getAmount(), new Date() );
                                NewInvoices.add(NewInvoice);
                                start = new Date( event.getBeginEvent().getDateTime().getValue() );
                                lastMonth = start.getMonth();
                                Cal.setTime(start);
                                lastWeek = Cal.get(Calendar.WEEK_OF_MONTH);
                            }


                        }

                    }

                case "Per class":


                    if (ContactEvents.size() > 0 ) {
                        EventObject event = ContactEvents.get(0);
                        InvoiceObject NewInvoice;
                        Date start = new Date( event.getBeginEvent().getDateTime().getValue() );
                        //case the first event of the contact is the current month ignore
                        if ( mMonth == start.getMonth() || mYear == start.getYear() ) {
                            NewInvoice = new InvoiceObject(Contact,Contact.getAmount(), new Date() );
                            NewInvoices.add(NewInvoice);
                            ContactEvents.get(0).setStatus(db.InvoicePaidCode());
                        }

                        int lastMonth = start.getMonth();
                        for (int count = 1; count < ContactEvents.size(); count++) {
                            event = ContactEvents.get(count);
                            start = new Date( event.getBeginEvent().getDateTime().getValue() );

                            if ( !(lastMonth == start.getMonth() || mYear == start.getYear()) ) {
                                NewInvoice = new InvoiceObject(Contact,Contact.getAmount(), new Date() );
                                NewInvoices.add(NewInvoice);
                                ContactEvents.get(0).setStatus(db.InvoicePaidCode());
                                start = new Date( event.getBeginEvent().getDateTime().getValue() );
                                lastMonth = start.getMonth();


                            }
                        }
                    }

                case "Per Hour":
                    if (ContactEvents.size() > 0 ) {
                        EventObject event = ContactEvents.get(0);
                        InvoiceObject NewInvoice;

                        Date start = new Date( event.getBeginEvent().getDateTime().getValue() );
                        Date end   = new Date( event.getEndEvent().getDateTime().getValue() );

                        Date diff = new Date( end.getTime() - start.getTime() );
                        Cal.setTime( diff );
                        int hours = Cal.get(Calendar.HOUR_OF_DAY);
                        int lastMonth = start.getMonth();

                        for (int count = 1; count < ContactEvents.size(); count++) {
                            event = ContactEvents.get(count);

                            if ( (lastMonth == start.getMonth() || mYear == start.getYear()) ) {
                                NewInvoice = new InvoiceObject(Contact,Contact.getAmount(), new Date() );
                                NewInvoices.add(NewInvoice);
                                ContactEvents.get(0).setStatus(db.InvoicePaidCode());
                                start = new Date( event.getBeginEvent().getDateTime().getValue() );
                                lastMonth = start.getMonth();

                            }
                        }
                    }
            }
        }


        return NewInvoices;
    }

    public void updateInvoiceList(final List<InvoiceObject> invoiceObjects) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (invoiceObjects == null) {

                    Toast.makeText(
                            getActivity(),
                            getString(R.string.error_retrivieving),
                            Toast.LENGTH_SHORT
                    ).show();

                } else if (invoiceObjects.size() == 0) {
                    Toast.makeText(
                            getActivity(),
                            getString(R.string.no_events),
                            Toast.LENGTH_SHORT
                    ).show();

                } else {

                    Toast.makeText(
                            getActivity(),
                            getString(R.string.updated),
                            Toast.LENGTH_SHORT
                    ).show();

                    db.importInvoice(invoiceObjects);
                    mAdapter.AddInvoice(invoiceObjects);
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

    /**
     * Created by ursow on 19/05/15.
     */
    public class InvoiceAdapter extends RecyclerView.Adapter<InvoiceAdapter.ClipCardViewHolder> {
        private List<InvoiceObject> invoiceObjects;
        private boolean allowAnimate = true;
        private AdapterView.OnItemClickListener mOnItemClickListener;

        public InvoiceAdapter(List<InvoiceObject> Contacts) {

            invoiceObjects = Contacts;
            notifyDataSetChanged();

        }

        @Override
        public int getItemCount() {
            return invoiceObjects.size();
        }

        @Override
        public void onBindViewHolder(final ClipCardViewHolder clipCardViewHolder, int i) {
            InvoiceObject invoiceObject = invoiceObjects.get(i);

            clipCardViewHolder.vEmail.setText(invoiceObject.getContact().getEmail());
            clipCardViewHolder.vContact.setText(invoiceObject.getContact().getName());
            if (invoiceObject.getContact().getPhotoPath() == null) {
                clipCardViewHolder.vBagde.setImageResource(R.drawable.avatar_empty);
            } else {
                clipCardViewHolder.vBagde.setImageURI(Uri.parse(invoiceObject.getContact().getPhotoPath()));
            }
            clipCardViewHolder.vInvoiceValue.setText(String.valueOf(invoiceObject.getAmount()));
            clipCardViewHolder.vInvoiceNumber.setText(String.valueOf(invoiceObject.getAmount()));


        }

        public void AddInvoice(final List<InvoiceObject> NewInvoices) {
            if (!NewInvoices.isEmpty()) {
                invoiceObjects = NewInvoices;
                notifyDataSetChanged();
            }
        }

        @Override
        public ClipCardViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View itemView = LayoutInflater.
                    from(viewGroup.getContext()).
                    inflate(R.layout.activity_editcontact_card, viewGroup, false);

            return new ClipCardViewHolder(itemView);
        }

        public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
            mOnItemClickListener = onItemClickListener;
        }

        public class ClipCardViewHolder extends RecyclerView.ViewHolder {
            protected TextView vContact;
            protected QuickContactBadge vBagde;
            protected QuickContactBadge vCheckUser;
            protected TextView vEmail;
            protected View vMain;
            protected TextView vInvoiceNumber;
            protected TextView vInvoiceValue;

            public ClipCardViewHolder(View v) {
                super(v);
                vContact = (TextView) v.findViewById(R.id.contacts_names);
                vInvoiceNumber = (TextView) v.findViewById(R.id.text_period);
                vInvoiceValue = (TextView) v.findViewById(R.id.text_invoicevalue);
                vEmail = (TextView) v.findViewById(R.id.text_contact_email);
                vBagde = (QuickContactBadge) v.findViewById(R.id.photo_contact);

                vMain = v;
            }
        }


    }

    public class InvoiceFecthTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            updateInvoiceList(generateInvoice());

            return null;
        }

    }

}
