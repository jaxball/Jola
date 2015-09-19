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
        if(inputText.contains("uber")) {
            Log.d(TAG, "Uber is called");
            // TODO - call Uber API
            ref.child("yelp").setValue();
            returnAction = new UberAPI().openTest(appContext);
        }
        else {
            HashSet<String> keys = new HashSet<String>();
            keys.add("food");
            keys.add("eat");
            keys.add("dinner");
            boolean abtFood = false;
            for (String key : keys) {
                if (inputText.contains(key)) {
                    abtFood = true;
                    // TODO write to firebase
                    ref.child("yelp").setValue(keys);
                    //TODO search for food place
                }
            }
            if(!abtFood){
                Log.d(TAG, "Did not recognize anything");
            }
        }
    }

    public Intent getAction() {
        return returnAction;
    }
}
