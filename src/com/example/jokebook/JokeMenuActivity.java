package com.example.jokebook;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

/**
 * The menu activity is started from the LiveCard's associated implicit intent, whenever it is clicked.
 * This activity manages the list of menu options available for the LiveCard
 */
public class JokeMenuActivity extends Activity {

    private JokeService.JokeBinder mJokeBinder;

    /**
     * The Service Connection onServiceConnected event returns the Binder we can use
     * to call methods on the Service. This is called when bindService is called successfully
     */
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (service instanceof JokeService.JokeBinder) {
                Log.d(TAG, "onServiceConnected");
                mJokeBinder = (JokeService.JokeBinder) service;
                openOptionsMenu();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }

    };

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindService(new Intent(this, JokeService.class), mServiceConnection, 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mResumed = true;
        openOptionsMenu();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mResumed = false;
    }

    @Override
    public void openOptionsMenu() {
        if (mResumed) {
            super.openOptionsMenu();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.tell_another:
                mJokeBinder.getService().displayNewJokeOnLiveCard();
                return true;
            case R.id.quit:
                mJokeBinder.getService().unpublishJokeCard();
                return true;
            default:
                Log.d(TAG, "onOptionsItemSelected: default!");
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onOptionsMenuClosed(Menu menu) {
        super.onOptionsMenuClosed(menu);
        unbindService(mServiceConnection);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.joke_options, menu);
        return true;
    }

}