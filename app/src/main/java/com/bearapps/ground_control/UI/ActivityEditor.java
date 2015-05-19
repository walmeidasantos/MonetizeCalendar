package com.bearapps.ground_control.UI;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.bearapps.ground_control.R;
import com.bearapps.ground_control.utility.Storage;


public class ActivityEditor extends Activity {

    private String oldText;
    private EditText editText;
    private ImageButton mFAB;
    private InputMethodManager inputMethodManager;
    private Storage db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        oldText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (oldText == null || oldText.equals(getString(R.string.clip_notification_single_text))) {
            oldText = "";
        }

        editText = (EditText) findViewById(R.id.edit_text);
        mFAB = (ImageButton) findViewById(R.id.main_fab);

        editText.setText(oldText);
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                }
            }
        });

        db = Storage.getInstance(this);
        // if is copied form other application.
        if (Intent.ACTION_SEND.equals(intent.getAction()) && "text/plain".equals(intent.getType())) {
            oldText = "";
        }

        String titleText = getString(R.string.title_activity_activity_editor);
        if (oldText.isEmpty()) {
            titleText = getString(R.string.title_activity_editor);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        editText.requestFocus();
    }

    @Override
    public void onPause() {
        super.onPause();
        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

    @Override
    public void onBackPressed() {
        finishAndRemoveTaskWithToast(getString(R.string.toast_no_saved));
    }

    private void saveText() {
        String newText = editText.getText().toString();
        String toastMessage;
        //db.modifyClip(oldText, newText, (isStarred ? 1 : -1));
        if (newText != null && !newText.isEmpty()) {
            toastMessage = getString(R.string.toast_copied, newText + "\n");
        } else {
            toastMessage = getString(R.string.toast_deleted);
        }
        finishAndRemoveTaskWithToast(toastMessage);
    }

    public void saveTextOnClick(View view) {
        saveText();
    }


    private void finishAndRemoveTaskWithToast(String toastMessage) {
        Toast
                .makeText(
                        this,
                        toastMessage,
                        Toast.LENGTH_SHORT
                )
                .show();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            finish();
        } else {
            finishAndRemoveTask();
        }
    }

}
