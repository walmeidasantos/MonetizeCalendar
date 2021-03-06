package com.bearapps.MonetizeCalendar.UI;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.bearapps.MonetizeCalendar.utility.Storage;

public class ContactMainFragment extends ContactsFragment {
    private Storage db;


    public static ContactMainFragment newInstance() {
        ContactMainFragment fragment = new ContactMainFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.db = Storage.getInstance(activity);
    }

    @Override
    protected RecyclerView.LayoutManager getLayoutManager() {
        return new GridLayoutManager(getActivity(), 4, GridLayoutManager.VERTICAL, false);
    }

    @Override
    protected RecyclerView.ItemDecoration getItemDecoration() {
        return new InsetDecoration(getActivity());
    }

    @Override
    public int getDefaultItemCount() {
        return 40;
    }

    @Override
    protected ContactAdapter getAdapter() {

        return new ContactAdapter(db.getAllContacts());
    }


}
