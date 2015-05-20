package com.bearapps.ground_control.UI;

import android.content.Context;
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

import com.bearapps.ground_control.R;
import com.bearapps.ground_control.model.ContactObject;
import com.bearapps.ground_control.utility.Storage;

import java.util.List;


public abstract class EditContactsFragment extends Fragment implements AdapterView.OnItemClickListener {

    private RecyclerView mList;
    private EditContactAdapter mAdapter;
    private Storage db;
    private Context context;
    private String TAG = "ground_control";

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

      /**
     * Created by ursow on 19/05/15.
     */
    public class EditContactAdapter extends RecyclerView.Adapter<EditContactAdapter.ClipCardViewHolder> {
        private List<ContactObject> contactObjectList;
        private boolean allowAnimate = true;
        private AdapterView.OnItemClickListener mOnItemClickListener;
        public EditContactAdapter(List<ContactObject> Contacts)  {

            contactObjectList = Contacts;
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
            if ( contactObject.getPhotoPath() == null ) {
                clipCardViewHolder.vBagde.setImageResource(R.drawable.avatar_empty);
            }
            else {
                clipCardViewHolder.vBagde.setImageURI(Uri.parse(contactObject.getPhotoPath()));
            }

            if ( contactObject.IsChoosed() ) {
                clipCardViewHolder.vCheckUser.setVisibility(View.VISIBLE);
            }
            else {
                clipCardViewHolder.vCheckUser.setVisibility(View.INVISIBLE);
            }

            clipCardViewHolder.vBagde.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (contactObject.IsChoosed()) {
                        clipCardViewHolder.vCheckUser.setVisibility(View.INVISIBLE);
                        contactObject.setUnChoosed();
                        db.deleteContact(contactObject.getGoogleId());

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
                        db.deleteContact(contactObject.getGoogleId());

                    } else {
                        // db.addContact(contactObject);
                        clipCardViewHolder.vCheckUser.setVisibility(View.VISIBLE);
                        contactObject.setChoosed();
                    }
                }
            });


            //setAnimation(clipCardViewHolder.vMain, i);

        }

        @Override
        public ClipCardViewHolder onCreateViewHolder( ViewGroup viewGroup, int i) {
            View itemView = LayoutInflater.
                    from(viewGroup.getContext()).
                    inflate(R.layout.activity_contact_card, viewGroup, false);

            return new ClipCardViewHolder(itemView);
        }


        public class ClipCardViewHolder extends RecyclerView.ViewHolder {
            protected TextView vContact;
            protected QuickContactBadge vBagde;
            protected QuickContactBadge vCheckUser;
            protected View vMain;

            public ClipCardViewHolder(View v) {
                super(v);
                vContact = (TextView) v.findViewById(R.id.contact_name);
                vBagde = (QuickContactBadge) v.findViewById(R.id.photo_contact);
                vCheckUser = (QuickContactBadge) v.findViewById(R.id.checkImageView);
                vMain = v;
            }
        }

        public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
            mOnItemClickListener = onItemClickListener;
        }



    }

}
