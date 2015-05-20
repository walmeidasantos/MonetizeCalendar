package com.bearapps.ground_control.UI;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.bearapps.ground_control.model.EventCardAdapter;
import com.bearapps.ground_control.utility.Storage;

public class EventMainFragment extends EventsFragment {
    private Storage db;


    public static EventMainFragment newInstance() {
        EventMainFragment fragment = new EventMainFragment();
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
    protected EventCardAdapter getAdapter() {

            return new EventCardAdapter(db.getEvents());
    }


}
