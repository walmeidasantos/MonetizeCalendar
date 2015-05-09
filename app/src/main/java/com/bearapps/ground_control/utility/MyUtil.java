package com.bearapps.ground_control.utility;

import android.content.Context;

import com.bearapps.ground_control.UI.MainActivity;
import com.google.api.client.util.DateTime;

import java.text.SimpleDateFormat;



/**
 * Created by heruoxin on 15/3/4.
 */

public class MyUtil {
    public final static String PACKAGE_NAME = MainActivity.APP_NAME;

    public static int dip2px(Context context, float dipValue){
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int)(dipValue * scale + 0.5f);
    }

    public static int px2dip(Context context, float pxValue){
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int)(pxValue / scale + 0.5f);
    }

    public static String stringLengthCut(String string) {
        return stringLengthCut(string, 200);
    }

    public static String stringLengthCut(String string, int length) {
        string = string.trim();
        return  (string.length() > length) ?
                string.substring(0, length - 2).trim()+"…"
                : string.trim();
    }

    public static String getFormatDate(DateTime date) {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        return dateFormat.format(date);
    }

    public static String getFormatTime(DateTime date) {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("hh-mm");
        return dateFormat.format(date);
    }


}
