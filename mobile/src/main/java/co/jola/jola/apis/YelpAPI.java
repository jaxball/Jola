package co.jola.jola.apis;

import android.content.Intent;

/**
 * Created by jasonlin on 9/19/15.
 */
public class YelpAPI implements API{

    public static boolean YelpMatch(String input) {
        return input.contains("food") || input.contains("eat") || input.contains("hungry");
    }

    public Intent execute() {
        return new Intent();
    }
}
