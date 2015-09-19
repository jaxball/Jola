package co.jola.jola;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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

    public TextClassifier(Context appContext) {
        this.appContext = appContext;
        this.returnAction = null;
        Firebase.setAndroidContext(appContext);
        ref = new Firebase("https://jola.firebaseio.com/");
    }

    public void parseText(String inputText) {
        Log.d(TAG, inputText);
        if(inputText.contains("uber")) {
            Log.d(TAG, "Uber is called");
            // TODO - call Uber API
            ref.child("uber").setValue("");
            returnAction = new UberAPI().openTest(appContext);
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
