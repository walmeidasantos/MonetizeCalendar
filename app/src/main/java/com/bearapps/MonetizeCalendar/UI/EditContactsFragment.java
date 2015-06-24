package com.bearapps.MonetizeCalendar.UI;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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

import com.bearapps.MonetizeCalendar.R;
import com.bearapps.MonetizeCalendar.model.ContactObject;
import com.bearapps.MonetizeCalendar.utility.Storage;

import java.util.List;


public abstract class EditContactsFragment extends Fragment implements AdapterView.OnItemClickListener {

    private RecyclerView mList;
    private EditContactAdapter mAdapter;
    private Storage db;
    private Context context;
    private String TAG = "MonetizeCalendar";
    private ContactObject contactObject;

    protected abstract RecyclerView.LayoutManager getLayoutManager();

    protected abstract RecyclerView.ItemDecoration getItemDecoration();

    protected abstract EditContactAdapter getAdapter();

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
        mAdapter.AddContacts(db.getAllContacts());
    }


    /**
     * Created by ursow on 19/05/15.
     */
    public class EditContactAdapter extends RecyclerView.Adapter<EditContactAdapter.ClipCardViewHolder> {
        private List<ContactObject> contactObjectList;
        private boolean allowAnimate = true;
        private AdapterView.OnItemClickListener mOnItemClickListener;

        public EditContactAdapter(List<ContactObject> Contacts) {

            contactObjectList = Contacts;
            notifyDataSetChanged();

        }

        @Override
        public int getItemCount() {
            return contactObjectList.size();
        }

        @Override
        public void onBindViewHolder(final ClipCardViewHolder clipCardViewHolder, int i) {
            contactObject = contactObjectList.get(i);

            clipCardViewHolder.vEmail.setText(contactObject.getEmail());
            clipCardViewHolder.vContact.setText(contactObject.getName());
            if (contactObject.getPhotoPath() == null) {
                clipCardViewHolder.vBagde.setImageResource(R.drawable.avatar_empty);
            } else {
                clipCardViewHolder.vBagde.setImageURI(Uri.parse(contactObject.getPhotoPath()));
            }
            clipCardViewHolder.vAmount.setText(String.valueOf(contactObject.getAmount()));
            clipCardViewHolder.vPeriod.setText(contactObject.getPeriod());
            clipCardViewHolder.vBagde.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent i = new Intent(getActivity(), ActivityEditor.class)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            .putExtra("period", clipCardViewHolder.vPeriod.getText())
                            .putExtra("id", contactObjectList.get(clipCardViewHolder.getAdapterPosition()).getGoogleId())
                            .putExtra("amount", clipCardViewHolder.vAmount.getText()
                            );
                    startActivity(i);

                }
            });

            //setAnimation(clipCardViewHolder.vMain, i);

        }

        public void AddContacts(final List<ContactObject> NewContacts) {
            if (!NewContacts.isEmpty()) {
                contactObjectList = NewContacts;
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
            protected TextView vPeriod;
            protected TextView vAmount;

            public ClipCardViewHolder(View v) {
                super(v);
                vContact = (TextView) v.findViewById(R.id.contacts_names);
                vPeriod = (TextView) v.findViewById(R.id.text_period);
                vAmount = (TextView) v.findViewById(R.id.text_invoicevalue);
                vEmail = (TextView) v.findViewById(R.id.text_contact_email);
                vBagde = (QuickContactBadge) v.findViewById(R.id.photo_contact);

                vMain = v;
            }
        }


    }

}
