package com.bearapps.MonetizeCalendar.Materials;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import com.bearapps.MonetizeCalendar.UI.MainActivity;

/**
 * An {@link android.app.IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class EventObjectActionBridge extends IntentService {
    public final static int ACTION_OPEN_MAIN = 5;
    public final static String ACTION_CODE = "bearcode.actionCode";

    public Handler mHandler;
    Intent intent;

    public EventObjectActionBridge() {
        super("EventObjectActionBridge");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new Handler();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        sendBroadcast(it);

        this.intent = intent;

        if (intent == null) return;

        String clips = intent.getStringExtra(Intent.EXTRA_TEXT);
        int actionCode = intent.getIntExtra(ACTION_CODE, 0);
        Log.v(MainActivity.APP_NAME, "ACTION_CODE: " + actionCode);
        switch (actionCode) {
            case 0:
                break;
            case ACTION_OPEN_MAIN:
                openMainActivity();
                return;
        }
    }

    private void openMainActivity() {
        //open by this will be auto closed when copy.
        Intent i = new Intent(this, MainActivity.class)
                .putExtra(MainActivity.EXTRA_IS_FROM_NOTIFICATION, true)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }


}
