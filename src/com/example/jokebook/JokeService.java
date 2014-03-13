package com.example.jokebook;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.widget.RemoteViews;
import com.google.android.glass.app.Card;
import com.google.android.glass.timeline.LiveCard;
import com.google.android.glass.timeline.TimelineManager;

import java.util.ArrayList;

/**
 * The JokeService manages a LiveCard object and it's view via a RemoteViews object.
 * It is started via the associated VoiceTrigger defined in the AndroidManifest
 */
public class JokeService extends Service {

    private static final String TAG = "JokeService";
    private static final String LIVE_CARD_TAG = "JokeCard";
    public static final int SECOND_IN_MILIS = 1000;
    private TimelineManager mTimelineManager;
    private LiveCard mLiveCard;
    private Handler mHandler;
    private final JokeBinder mJokeBinder = new JokeBinder();
    private Runnable mMJokeRevealTimeUpdate;
    private int mSecondsUntilReveal;
    private Joke mRandomJokeByType;
    private JokeType mJokeType = JokeType.DEFAULT;

    /**
     * The Binder is an interface between the activity and the service.
     * Once the activity calls bindService(), the client (Activity) can make calls to the Service (Server)
     * using these methods.
     */
    public class JokeBinder extends Binder {

        public JokeService getService(){
            return JokeService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind called. Returning JokeBinder");
        return mJokeBinder;
    }

    @Override
    public void onDestroy() {
        if (mLiveCard != null && mLiveCard.isPublished()) {
            mLiveCard.unpublish();
            mLiveCard = null;
        }
        super.onDestroy();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mTimelineManager = TimelineManager.from(this);
        mHandler = new Handler();
    }

    public void unpublishJokeCard() {
        if (mLiveCard != null && mLiveCard.isPublished()) {
            mLiveCard.unpublish();
        }
    }

    /**
     * called when the service is initially started from the voice-trigger, as mapped in the AndroidManifest
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "startCommand: intent: " + intent + ", flags " + flags + ", startId " + startId);
        //the additional voice prompt input is stored in the intent extras under RecognizerIntent.EXTRA_RESULTS.
        //if the user specified a Joke Type at startup, pull it here and set the joke type
        ArrayList<String> voiceResults = intent.getExtras().getStringArrayList(RecognizerIntent.EXTRA_RESULTS);
        mJokeType = JokeBook.determineRequestedJokeType(voiceResults);
        displayNewJokeOnLiveCard();
        return START_STICKY;
    }

    public void displayNewJokeOnLiveCard() {
        mRandomJokeByType = JokeBook.getInstance().getRandomJokeByType(mJokeType);
        //the number of seconds we will count down until the joke answer is displayed
        mSecondsUntilReveal = 5;

        //setup a RemoteViews object to manage the LiveCard's view
        final RemoteViews remoteViews = new RemoteViews(this.getPackageName(), R.layout.joke_card_layout);

        //if the card isn't null, we've already gotten an instance of a livecard.
        //if it is, create one and set it up with an intent when it is clicked, and publish it
        if (mLiveCard == null) {
            mLiveCard = mTimelineManager.createLiveCard(LIVE_CARD_TAG);
            //set up the intent to be fired when the LiveCard is clicked upon. In this case, our MenuActivity, for displaying application options
            Intent menuIntent = new Intent(this, JokeMenuActivity.class);
            menuIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            mLiveCard.setAction(PendingIntent.getActivity(this, 0, menuIntent, 0));
        } else {
            //if the livecard is already published, unpublish and republish to prevent the livecard not being focused initially.
            // GDK will at some point support focusing an already published card
            if (mLiveCard.isPublished()) {
                mLiveCard.unpublish();
            }
        }
        mLiveCard.publish(LiveCard.PublishMode.REVEAL);
        //set the joke text to the selected joke question
        remoteViews.setTextViewText(R.id.jokeText, mRandomJokeByType.mQuestion);
        mLiveCard.setViews(remoteViews);

        //start a countdown, updating the view every second
        //after reaching 0, reveal the punchline of the joke
        mMJokeRevealTimeUpdate = new Runnable() {
            @Override
            public void run() {
                remoteViews.setTextViewText(R.id.punchlineRevealTime, "joke reveal in " + mSecondsUntilReveal + " secs.");
                //explicitly call setViews on the live card again to force an update.
                mLiveCard.setViews(remoteViews);
                if (mSecondsUntilReveal == 0) {
                    displayJokeAnswerCard();
                } else {
                    mSecondsUntilReveal--;
                    mHandler.postDelayed(mMJokeRevealTimeUpdate, SECOND_IN_MILIS);
                }
            }
        };
        mHandler.postDelayed(mMJokeRevealTimeUpdate, SECOND_IN_MILIS);
    }

    /**
     * changes the livecard's state to display the joke answer
     * also publishes the joke question and answer as a static card to the user's timeline history
     */
    public void displayJokeAnswerCard() {
        final RemoteViews remoteViews = new RemoteViews(this.getPackageName(), R.layout.joke_card_answer_layout);
        remoteViews.setTextViewText(R.id.jokeText, mRandomJokeByType.mAnswer);
        remoteViews.setImageViewResource(R.id.punchlineImage, getPunchlineImage());
        mLiveCard.setViews(remoteViews);
        //upon completing telling the joke, publish the joke as a static card in the user's timeline!
        final Card staticCard = new Card(this);
        staticCard.setText(mRandomJokeByType.mQuestion + " " + mRandomJokeByType.mAnswer);
        staticCard.addImage(getPunchlineImage());
        mTimelineManager.insert(staticCard);
    }

    /**
     * depending on joke category, returns the matching joke graphic to be displayed in the livecard
     */
    private int getPunchlineImage() {
        switch (mRandomJokeByType.mJokeType) {
            case LAWYER:
                return R.drawable.judge_judy;
            case ANTI:
                return R.drawable.antijoke_cat;
            case CHICKEN:
                return R.drawable.chicken;
            default:
                return R.drawable.joke_icon;
        }
    }

}