package com.whatever.like.totally;

import com.whatever.like.totally.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.speech.*;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayDeque;
import java.util.ArrayList;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class TotallyLikeWhateverHome extends Activity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * If set, will toggle the system UI visibility upon interaction. Otherwise,
     * will show the system UI visibility upon interaction.
     */
    private static final boolean TOGGLE_ON_CLICK = true;

    /**
     * The flags to pass to {@link SystemUiHider#getInstance}.
     */
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_totally_like_whatever_home);

        final View controlsView = findViewById(R.id.fullscreen_content_controls);

        myText = (TextView)findViewById(R.id.text_area);
        flaggedWords = new ArrayList<String>();
        flaggedWords.add("like");
        flaggedWords.add("yeah");
        flaggedWords.add("um");
        flaggedWords.add("umm");
        flaggedWords.add("so");
        receive.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onBeginningOfSpeech() { }
            public void onRmsChanged(float rmsdB) { }
            public void onEndOfSpeech() {
                buttonEnabled = true;
                /*
                LinearLayout lView = (LinearLayout)findViewById(R.id.text_display_area);
                myText.setText("Finished!");
                myText.setTextColor(getResources().getColor(R.color.black));
                myText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

                lView.addView(myText);*/
            }
            public void onEvent(int eventType, Bundle params) { }
            public void onBufferReceived(byte[] buffer) { }

            public void onPartialResults(Bundle partialResults) {
                onResults(partialResults);
            }

            public void onReadyForSpeech(Bundle bundle) {
                //LinearLayout lView = (LinearLayout)findViewById(R.id.text_display_area);

                myText.setText("Start");
                flaggedCount = 0;

                //lView.addView(myText);
            }

            public void onResults(Bundle results) {
                //LinearLayout lView = (LinearLayout)findViewById(R.id.text_display_area);

                ArrayList<String> returnedStrings = results.getStringArrayList(
                        SpeechRecognizer.RESULTS_RECOGNITION);
                if (returnedStrings != null && returnedStrings.size() > 0) {
                    new CheckFlaggedWordsTask().execute(returnedStrings.get(0));
                    myText.setText(String.format("You've said a flagged word %d times\n\n%s",
                            flaggedCount, returnedStrings.get(0)));

                    //lView.addView(myText);
                }
                else {
                    myText.setText("Nothing returned.");

                    //lView.addView(myText);
                }
            }

            public void onError(int error) {
                //LinearLayout lView = (LinearLayout)findViewById(R.id.text_display_area);

                myText.setText("Error!");

                //lView.addView(myText);
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.dummy_button).setOnClickListener(mPressed);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

    }

    private TextView myText = null;
    private Context self = this;
    private Activity me = this;
    private SpeechRecognizer receive = SpeechRecognizer.createSpeechRecognizer(this);
    private int flaggedCount = 0;
    private ArrayList<String> flaggedWords;
    private boolean buttonEnabled = true;
    View.OnClickListener mPressed = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            // disable button
            if (!buttonEnabled)
                return;
            buttonEnabled = false;

            // listening stuff
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_RESULTS, true);
            intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
            intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 3000L);

            receive.startListening(intent);
        }
    };

    private class CheckFlaggedWordsTask extends AsyncTask<String, Integer, String> {
        /** The system calls this to perform work in a worker thread and
         * delivers it the parameters given to AsyncTask.execute() */
        protected String doInBackground(String... returnedStrings) {
            String[] words = returnedStrings[0].split(" ");
            int newFlaggedCount = 0;
            for (String word : words) {
                if (flaggedWords.contains(word)) {
                    newFlaggedCount++;
                }
            }
            if (newFlaggedCount > flaggedCount) {
                publishProgress(getResources().getColor(R.color.red));
                findViewById(R.id.scrolling_area).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        findViewById(R.id.scrolling_area).setBackgroundColor(
                                getResources().getColor(R.color.cyan));
                    }
                }, 250);
                flaggedCount = newFlaggedCount;
            }
            return returnedStrings[0];
        }

        protected void onProgressUpdate(Integer... color) {
            findViewById(R.id.scrolling_area).setBackgroundColor(color[0]);
        }

        /** The system calls this to perform work in the UI thread and delivers
         * the result from doInBackground() */
        protected void onPostExecute(String returnedString) {
            myText.setText(String.format("You've said a flagged word %d times\n\n%s",
                    flaggedCount, returnedString));
        }
    }
}
