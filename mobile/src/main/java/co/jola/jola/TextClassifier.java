package co.jola.jola;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import co.jola.jola.apis.UberAPI;

/**
 * Created by jasonlin on 9/19/15.
 */
public class TextClassifier {

    private static String TAG = "Testing text recognition";
    private Context appContext;
    private Intent returnAction;

    public TextClassifier(Context appContext) {
        this.appContext = appContext;
        this.returnAction = null;
    }

    public void parseText(String inputText) {
        if(inputText.contains("uber")) {
            Log.d(TAG, "Uber is called");
            // TODO - call Uber API
            returnAction = new UberAPI().openTest(appContext);
        } else {
            Log.d(TAG, "Did not recognize anything");
        }
    }

    public Intent getAction() {
        return returnAction;
    }
}
