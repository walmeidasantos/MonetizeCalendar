package com.bearapps.ground_control.UI;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.bearapps.ground_control.utility.Storage;

public class InvoiceMainFragment extends InvoiceFragment {
    private Storage db;


    public static InvoiceMainFragment newInstance() {
        InvoiceMainFragment fragment = new InvoiceMainFragment();
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
    protected InvoiceAdapter getAdapter() {

            return new InvoiceAdapter(db.getInvoice());
    }


}
