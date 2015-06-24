package com.bearapps.MonetizeCalendar.UI;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.bearapps.MonetizeCalendar.utility.Storage;

public class EditContactMainFragment extends EditContactsFragment {
    private Storage db;


    public static EditContactMainFragment newInstance() {
        EditContactMainFragment fragment = new EditContactMainFragment();
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
        return new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
    }

    @Override
    protected RecyclerView.ItemDecoration getItemDecoration() {
        return new InsetDecoration(getActivity());
    }

    @Override
    protected EditContactAdapter getAdapter() {

        return new EditContactAdapter(db.getAllContacts());
    }


}
