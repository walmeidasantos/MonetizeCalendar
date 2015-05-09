package com.bearapps.ground_control.model;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.bearapps.ground_control.R;
import com.bearapps.ground_control.utility.MyUtil;
import com.bearapps.ground_control.utility.Storage;
import com.bearapps.ground_control.model.EventObject;

import java.util.List;


/**
 * Created by heruoxin on 15/3/13.
 */


public class AppWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new AppWidgetRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}

class AppWidgetRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private Context mContext;
    private int mAppWidgetId;
    private Storage db;
    private List<EventObject> eventObjects;
    private boolean mIsStarred;

    public AppWidgetRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
        mIsStarred = intent.getBooleanExtra(AppWidget.WIDGET_IS_STARRED, false);

    }

    public void onCreate() {
        db = Storage.getInstance(mContext);
    }

    public void onDestroy() {
    }

    public int getCount() {
        return eventObjects.size();
    }

    public RemoteViews getViewAt(int position) {
        EventObject eventObject = eventObjects.get(position);
        RemoteViews remoteViews = new RemoteViews(mContext.getPackageName(), R.layout.app_widget_card);

        remoteViews.setTextViewText(R.id.widget_card_date, MyUtil.getFormatDate(eventObject.getBeginEvent()));
        remoteViews.setTextViewText(R.id.widget_card_time, MyUtil.getFormatTime(eventObject.getBeginEvent()));
        remoteViews.setTextViewText(R.id.widget_card_text, MyUtil.stringLengthCut(eventObject.getSumary()));

        Intent fillInEditorIntent = new Intent();
        final Bundle editorExtras = new Bundle();
        editorExtras.putInt(EventObjectActionBridge.ACTION_CODE, EventObjectActionBridge.ACTION_EDIT);
        editorExtras.putString(Intent.EXTRA_TEXT, eventObject.getSumary());
        fillInEditorIntent.putExtras(editorExtras);
        remoteViews.setOnClickFillInIntent(
                R.id.widget_card_click_edit,
                fillInEditorIntent
        );
        Intent fillInStarIntent = new Intent();
        final Bundle starExtras = new Bundle();
        starExtras.putInt(EventObjectActionBridge.ACTION_CODE, EventObjectActionBridge.ACTION_STAR);
        fillInStarIntent.putExtras(starExtras);
        remoteViews.setOnClickFillInIntent(
                R.id.widget_card_click_star,
                fillInStarIntent
        );
        return remoteViews;
    }

    public RemoteViews getLoadingView() {
        // We aren't going to return a default loading view in this sample
        return null;
    }

    public int getViewTypeCount() {
        return 1;
    }

    public long getItemId(int position) {
        return position;
    }

    public boolean hasStableIds() {
        return false;
    }

    public void onDataSetChanged() {
        eventObjects = null;
        eventObjects = db.getEvents();

    }
}
