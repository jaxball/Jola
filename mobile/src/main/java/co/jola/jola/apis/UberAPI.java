package co.jola.jola.apis;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;

/**
 * Created by jasonlin on 9/19/15.
 */
public class UberAPI implements API {

    private static String BASE_URI = "uber://?action=setPickup&";
    private static String MOBILE_URL = "https://m.uber.com/sign-up";
    private static String CLIENT_ID = "nrGE4PWCOa6o8VdGZR_zvV7l99rWNvs5";

    // coordinates of where the user is
    private double pickupLatitude;
    private double pickupLongitude;

    // coordinates of where they are going
    private double destinationLatitude;
    private double destinationLongitude;

    // App Context
    private Context context;

    // TODO - integrate Uber API

    public UberAPI(Context c) {
        this.context = c;

        // set some default values

        // Pearson International Airport
        this.pickupLatitude = 43.6767;
        this.pickupLongitude = 79.6306;

        // University of Waterloo
        this.destinationLatitude = 43.4689;
        this.destinationLongitude = 80.5400;
    }

    public void setPickupLocation(double lat, double lon) {
        this.pickupLatitude = lat;
        this.pickupLongitude = lon;
    }

    public void setDestinationLocation(double lat, double lon) {
        this.destinationLatitude = lat;
        this.destinationLongitude = lon;
    }

    private String getURI() {
        StringBuilder sb = new StringBuilder(BASE_URI);

        sb.append("pickup[latitude]=");
        sb.append(this.pickupLatitude);

        sb.append("&pickup[longitude]=");
        sb.append(this.pickupLongitude);

        sb.append("&dropoff[latitude]=");
        sb.append(this.destinationLatitude);

        sb.append("&dropoff[longitude]=");
        sb.append(this.destinationLongitude);

        sb.append("&client_id=");
        sb.append(CLIENT_ID);

        return sb.toString();
    }

    private String getURL() {
        return new StringBuilder(MOBILE_URL).append("?client_id=").append(CLIENT_ID).toString();
    }

    public Intent execute() {
        try {
            PackageManager pm = context.getPackageManager();
            pm.getPackageInfo("com.ubercab", PackageManager.GET_ACTIVITIES);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(getURI()));
            return intent;
        } catch (PackageManager.NameNotFoundException e) {
            // No Uber app! Open mobile website.
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(getURL()));
            return i;
        }
    }

    public static boolean UberMatch(String input) {
        input = input.toLowerCase();

        // TODO - fuzzy string matching
        return input.contains("uber");
    }
}
