package com.bearapps.ground_control.UI;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;

import com.bearapps.ground_control.R;
import com.bearapps.ground_control.model.EventObject;
import com.bearapps.ground_control.utility.Storage;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity implements NavigationDrawerCallbacks {

    public static String APP_NAME;
    public final static String EXTRA_IS_FROM_NOTIFICATION = "teste";
    public final static String FIRST_LAUNCH = "pref_is_first_launch";
    private static int TRANSLATION_FAST = 400;
    private static int TRANSLATION_SLOW = 1000;

    private NavigationDrawerFragment mNavigationDrawerFragment;

    private RecyclerView mRecList;
    private LinearLayoutManager linearLayoutManager;
    private Toolbar mToolbar;
    private ImageButton mFAB;
    private static Context context;
    private Storage db;
    private List<EventObject> Events;
    private ArrayList<EventObject> deleteQueue = new ArrayList<>();
    private CharSequence mTitle;

     //FAB
    private int isYHidden = -1;
    private int isXHidden = -1;
    private boolean isRotating = false;
    private boolean isStarred = false;

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
            default:
                //Do nothing
                break;
        }

        ft.commit();
    }

    @Override
    protected void onPause() {
        super.onPause();
        clearDeleteQueue();
    }

    @Override
    protected void onDestroy() {
        //LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onDestroy();
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            return super.onCreateOptionsMenu(menu);
        }

        /*starItem = menu.findItem(R.id.action_star);
        searchItem = menu.findItem(R.id.action_search);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) searchItem.getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                searchView.setIconified(false);
                searchView.requestFocus();
                queryText = searchView.getQuery().toString();
                lastStorageUpdate = null;
                setView();
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                searchView.clearFocus();
                queryText = null;
                lastStorageUpdate = null;
                setView();
                return true;
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                searchItem.collapseActionView();
                queryText = null;
                initView();
                lastStorageUpdate = null;
                setView();
                return false;
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                queryText = newText;
                lastStorageUpdate = null;
                setView();
                return true;
            }
        });
*/
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onSearchRequested() {
        return true;
    }

    private void mFabRotation(boolean clockwise, long time) {
        if (isRotating) return;
        mFAB.setRotation(0);
        float rotateDegree = (clockwise ? 360 : -360);
        isRotating = true;
        mFAB.animate()
                .rotation(rotateDegree)
                .setDuration(time);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                isRotating = false;
            }
        }, time - 400);
    }

    /*public void mFabOnClick(View view) {
        mFabRotation(true, TRANSLATION_FAST);
        final Intent intent = new Intent(this, ActivityEditor.class)
                .putExtra(EventObjectActionBridge.STATUE_IS_STARRED, isStarred);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //ActivityOptions options = ActivityOptions
            //        .makeSceneTransitionAnimation(this, mFAB, getString(R.string.action_star));
            //startActivity(intent, options.toBundle());
            startActivity(intent);
        } else {
            startActivity(intent);
        }
    }*/

    private void clearDeleteQueue() {
        for (EventObject eventObject : deleteQueue) {
            //db.modifyClip(eventObject.getText(), null);
        }
        deleteQueue.clear();
    }

    private void setItemsVisibility() {
        /*if (clipCardAdapter.getItemCount() == 0) {
            mRecLayout.setVisibility(View.INVISIBLE);
            Animation animation = AnimationUtils.loadAnimation(context, android.R.anim.fade_out);
            mRecLayout.setAnimation(animation);
        } else {
            mRecLayout.setVisibility(View.VISIBLE);
            Animation animation = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
            mRecLayout.setAnimation(animation);
        }*/
        //TODO verificar visibilidade do item no fragment (ver se o mesmo método se aplica
    }

   public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
    }










}
