package com.bearapps.MonetizeCalendar.UI;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageButton;

import com.bearapps.MonetizeCalendar.R;
import com.bearapps.MonetizeCalendar.utility.Storage;


public class MainActivity extends ActionBarActivity implements NavigationDrawerCallbacks {

    public final static String EXTRA_IS_FROM_NOTIFICATION = "teste";
    public static String APP_NAME;
    private static Context context;
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private Toolbar mToolbar;
    private ImageButton mFAB;
    private Storage db;
    private CharSequence mTitle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_topdrawer);
        mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        mTitle = getTitle();

        context = getApplicationContext();
        APP_NAME = getString(R.string.app_name);

        db = Storage.getInstance(context);

        mFAB = (ImageButton) findViewById(R.id.main_fab);

        mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_drawer);
        // Set up the drawer_layout.
        mNavigationDrawerFragment.setUp(
                R.id.fragment_drawer,
                (DrawerLayout) findViewById(R.id.drawer),
                mToolbar
        );
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        switch (position) {
            case 0:
                ft.replace(R.id.container, EventMainFragment.newInstance());
                break;
            case 1:
                ft.replace(R.id.container, ContactMainFragment.newInstance());
                break;

            case 2:
                ft.replace(R.id.container, EditContactMainFragment.newInstance());
                break;

            case 3:
                ft.replace(R.id.container, InvoiceMainFragment.newInstance());
                break;

            default:
                //Do nothing
                break;
        }

        ft.commit();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }


    @Override
    public boolean onSearchRequested() {
        return true;
    }

}
