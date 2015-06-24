package com.bearapps.MonetizeCalendar.UI;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.bearapps.MonetizeCalendar.R;
import com.bearapps.MonetizeCalendar.utility.Storage;


public class ActivityEditor extends ActionBarActivity {

    private EditText TextAmount;
    private ImageButton mFAB;
    private InputMethodManager inputMethodManager;
    private Storage db;
    private Toolbar mToolbar;
    private RadioGroup period;
    private String IdContact;
    private long amount;
    private String TextPeriod;
    private RadioButton radio_Monthly;
    private RadioButton radio_Weekly;
    private RadioButton radio_perclass;
    private RadioButton radio_perhour;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        period = (RadioGroup) findViewById(R.id.radioGroup);
        radio_Monthly = (RadioButton) findViewById(R.id.radioMonthly);
        radio_Weekly = (RadioButton) findViewById(R.id.radioWeekly);
        radio_perclass = (RadioButton) findViewById(R.id.radioPerClass);
        radio_perhour = (RadioButton) findViewById(R.id.radioPerHour);

        radio_Monthly.setId(0);
        radio_Weekly.setId(1);
        radio_perclass.setId(2);
        radio_perhour.setId(3);


        IdContact = intent.getStringExtra("id");
        amount = Long.parseLong(intent.getStringExtra("amount"));
        TextPeriod = intent.getStringExtra("period");

        if (TextPeriod.equals(radio_Monthly.getText())) {
            radio_Monthly.setChecked(true);
        } else {
            radio_Monthly.setChecked(false);
        }

        if (TextPeriod.equals(radio_Weekly.getText())) {
            radio_Weekly.setChecked(true);
        } else {
            radio_Weekly.setChecked(false);
        }

        if (TextPeriod.equals(radio_perclass.getText())) {
            radio_perclass.setChecked(true);
        } else {
            radio_perclass.setChecked(false);
        }

        if (TextPeriod.equals(radio_perhour.getText())) {
            radio_perhour.setChecked(true);
        } else {
            radio_perhour.setChecked(false);
        }


        TextAmount = (EditText) findViewById(R.id.amount);
        mFAB = (ImageButton) findViewById(R.id.main_fab);
        mFAB.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        saveTextOnClick(v);
                    }
                });

        TextAmount.setText(String.valueOf(amount));
        TextAmount.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                }
            }
        });

        db = Storage.getInstance(this);

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        TextAmount.requestFocus();
    }

    @Override
    public void onPause() {
        super.onPause();
        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(TextAmount.getWindowToken(), 0);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAndRemoveTaskWithToast(getString(R.string.toast_no_saved));
    }

    private void saveContact() {
        String newText = TextAmount.getText().toString();
        String toastMessage;

        if (period.getCheckedRadioButtonId() == -1) {

        } else {
            int selectedId = period.getCheckedRadioButtonId();
            // find the radiobutton by returned id
            RadioButton selectedRadioButton = (RadioButton) findViewById(selectedId);
            if (selectedRadioButton.getId() == 0) {
                TextPeriod = Storage.CHR_TYPE_MONTHLY;
            } else if (selectedRadioButton.getId() == 1) {
                TextPeriod = Storage.CHR_TYPE_WEEKLY;
            } else if (selectedRadioButton.getId() == 2) {
                TextPeriod = Storage.CHR_TYPE_PERCLASS;
            } else if (selectedRadioButton.getId() == 3) {
                TextPeriod = Storage.CHR_TYPE_PERHOUR;
            }
        }
        amount = Long.parseLong(TextAmount.getText().toString());
        db.modifyContact(IdContact, TextPeriod, amount);
        toastMessage = getString(R.string.action_save, newText + "\n");
        finishAndRemoveTaskWithToast(toastMessage);
    }

    public void saveTextOnClick(View view) {
        saveContact();
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
