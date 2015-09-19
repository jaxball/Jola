package co.jola.jola.apis;

import android.content.Intent;

/**
 * Created by jasonlin on 9/19/15.
 */
public class BraintreeAPI implements API {
    // TODO - integrate Braintree API

    public static boolean BraintreeMatch(String input) {
        return input.contains("pay");
    }

    public Intent execute() {
        return new Intent();
    }
}
