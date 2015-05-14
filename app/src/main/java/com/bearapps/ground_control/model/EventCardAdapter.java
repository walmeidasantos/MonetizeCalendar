package com.bearapps.ground_control.model;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.bearapps.ground_control.R;
import com.bearapps.ground_control.utility.MyUtil;

import java.util.List;

/**
 * Created by ursow on 11/04/15.
 */
public class EventCardAdapter extends RecyclerView.Adapter<EventCardAdapter.ClipCardViewHolder> {
    private List<EventObject> eventObjectList;
    private boolean allowAnimate = true;
    private AdapterView.OnItemClickListener mOnItemClickListener;

    public EventCardAdapter(List<EventObject> Events)  {

        eventObjectList = Events;
        notifyDataSetChanged();

    }

    public void AddEvents(List<EventObject> Events) {
        eventObjectList.clear();
        eventObjectList.addAll(Events);

        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return eventObjectList.size();
    }

    @Override
    public void onBindViewHolder(final ClipCardViewHolder clipCardViewHolder, int i) {
        final EventObject eventObject = eventObjectList.get(i);

        clipCardViewHolder.vDate.setText(eventObject.getBeginEvent().toString()  );
        clipCardViewHolder.vTime.setText(eventObject.getBeginEvent().toString() );
        clipCardViewHolder.vText.setText(MyUtil.stringLengthCut(eventObject.getSumary() ));

    }

    @Override
    public ClipCardViewHolder onCreateViewHolder( ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.activity_main_card, viewGroup, false);

        return new ClipCardViewHolder(itemView);
    }

    public void add(int position, EventObject eventObject) {
        eventObjectList.add(position, eventObject);
        notifyItemInserted(position);
    }

    public void remove(int position) {
        eventObjectList.remove(position);
        notifyItemRemoved(position);
    }

    public class ClipCardViewHolder extends RecyclerView.ViewHolder {
        protected TextView vTime;
        protected TextView vDate;
        protected TextView vText;
        protected View vShare;
        protected View vMain;

        public ClipCardViewHolder(View v) {
            super(v);
            vTime = (TextView) v.findViewById(R.id.activity_main_card_time);
            vDate = (TextView) v.findViewById(R.id.activity_main_card_date);
            vText = (TextView) v.findViewById(R.id.sumary_event);
            vMain = v;
        }
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }



}