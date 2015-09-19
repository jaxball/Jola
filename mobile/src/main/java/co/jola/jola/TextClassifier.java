package co.jola.jola;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import com.firebase.client.Firebase;

import java.util.ArrayList;
import java.util.HashSet;

import co.jola.jola.apis.UberAPI;

/**
 * Created by jasonlin on 9/19/15.
 */
public class TextClassifier {

    private static String TAG = "Testing text recognition";
    private Context appContext;
    private Intent returnAction;
    private Firebase ref;
    private SpeechRecognizer sr;
    private TextToSpeech jola;
    private Intent intent;

    public TextClassifier(Context appContext, SpeechRecognizer sr, TextToSpeech jola) {
        this.appContext = appContext;
        this.sr = sr;
        this.jola = jola;
        this.returnAction = null;
        Firebase.setAndroidContext(appContext);
        ref = new Firebase("https://jola.firebaseio.com/");
        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, "co.jola.jola");
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 0);
    }

    public void parseText(String inputText) {
        Log.d(TAG, inputText);
        if(UberAPI.UberMatch(inputText)) {
            Log.d(TAG, "Uber is called");
            // TODO - call Uber API
            UberAPI uber = new UberAPI(appContext);

            returnAction = uber.execute();
        }
        else{
            if(inputText.contains("near") || inputText.contains("at")) {

                HashSet<String> keys = new HashSet<String>();
                keys.add("food");
                keys.add("eat");
                keys.add("dinner");
                keys.add("yelp");
                boolean abtFood = false;
                HashSet<String> foundkeys = new HashSet<String>();
                for (String key : keys) {
                    if (inputText.contains(key)) {
                        abtFood = true;
                        Log.d(TAG, "found key " + key);

                        foundkeys.add(key);
                        //TODO search for food place
                    }
                }
                if (abtFood) {
                    ref.child("yelp").setValue(foundkeys);
                } else {
                    Log.d(TAG, "Did not recognize anything");
                }
            }
        }
    }

    public Intent getAction() {
        return returnAction;
    }
}
