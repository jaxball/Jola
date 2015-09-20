package co.jola.jola.apis;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Created by jasonlin on 9/19/15.
 */
public class YelpAPI implements API{

    // coordinates of where the user is
    private static String TAG = "Testing text recognition";
    private double currentLatitude;
    private double currentLongitude;
    private Context context;
    private String term;
    private static String BASE_URI = "yelp4://biz/";
    private static String SEARCH_URI = "http://api.yelp.com/v2/search?";

    public YelpAPI(Context c) {
        this.context = c;
    }

    public static boolean YelpMatch(String input) {
        return input.contains("food") || input.contains("eat") || input.contains("hungry");
    }

    public void setSearchTerm(String term){
        this.term = term;
    }
    public void setUserDefinedLocation(String location){
        try {
            List<Address> result = new Geocoder(context).getFromLocationName(location, 1);
            this.setCurrentLocation(((Address)result).getLatitude(),((Address)result).getLongitude());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void setCurrentLocation(double lat, double lon) {
        this.currentLatitude = lat;
        this.currentLongitude = lon;
    }

    private String getURI() {
        StringBuilder sb = new StringBuilder(BASE_URI+"search?");

        sb.append("&cll=");
        sb.append(this.currentLatitude+","+this.currentLongitude);

        sb.append("&term=");
        sb.append(this.term);

        return sb.toString();
    }

    private String getURL() {
        return new StringBuilder(SEARCH_URI).append("&cll=").append(this.currentLatitude + "," + this.currentLongitude).append("&term=").append(this.term).toString();
    }

    public Intent execute() {
        try {
            PackageManager pm = context.getPackageManager();
            pm.getPackageInfo("co.jola.jola", PackageManager.GET_ACTIVITIES);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(getURI()));
            return intent;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG,"exception " + e);
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(getURL()));
            return i;
        }
    }
}
