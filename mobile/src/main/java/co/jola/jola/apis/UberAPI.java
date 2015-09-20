package co.jola.jola.apis;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * Created by jasonlin on 9/19/15.
 */
public class UberAPI implements API {

    private static String TAG = "Uber API";

    private static String BASE_URI = "uber://?action=setPickup&";
    private static String API_BASE_URL = "https://api.uber.com/";
    private static String MOBILE_URL = "https://m.uber.com/sign-up";

    // credentials
    private static String CLIENT_ID = "nrGE4PWCOa6o8VdGZR_zvV7l99rWNvs5";
    private static String CLIENT_SECRET = "h1ppblwE7hBVujN53_NTTyfNFEJgLckCAqiJ3-b1";
    private static String SERVER_TOKEN = "x_glD6FRCmSoVFcRgBRLdpBQlii_UrwMGg9JhKuV";

    // coordinates of where the user is
    private double pickupLatitude;
    private double pickupLongitude;

    // coordinates of where they are going
    private double destinationLatitude;
    private double destinationLongitude;
    private String destinationAddress;
    private boolean useDestinationCoordinates;

    // App Context
    private Context context;

    // flags
    private boolean pickupIsSet;
    private boolean destinationIsSet;
    private boolean isFareRequest;

    // TODO - integrate Uber API

    public UberAPI(Context c, String data) {
        this.context = c;

        // set some default values

        // Pearson International Airport
        this.pickupLatitude = 43.6767;
        this.pickupLongitude = 79.6306;

        // University of Waterloo
        this.destinationLatitude = 43.4689;
        this.destinationLongitude = 80.5400;
        this.destinationAddress = "";
        this.useDestinationCoordinates = false;

        this.pickupIsSet = false;
        this.destinationIsSet = false;
        this.isFareRequest = false;

        // the user already specified the location
        if(data.contains("to")) {
            String location = data.split("to")[0];

            // now need to geocode the location since it's probably an address
        }

        if(data.contains("how") || data.contains("much") || data.contains("cost")) {
            // do an estimate
            this.isFareRequest = true;
        }
    }

    public void setPickupLocation(double lat, double lon) {
        this.pickupLatitude = lat;
        this.pickupLongitude = lon;
        this.pickupIsSet = true;
    }

    public void setDestinationLocationCoordinates(double lat, double lon) {
        this.destinationLatitude = lat;
        this.destinationLongitude = lon;
        this.destinationIsSet = true;
        this.useDestinationCoordinates = true;
    }

    public void setDestinationAddress(String dest) {
        this.destinationAddress = dest;
        this.useDestinationCoordinates = false;
        this.destinationIsSet = true;
    }

    public boolean isPickupSet() {
        return this.pickupIsSet;
    }

    public boolean isDestinationIsSet() {
        return this.destinationIsSet;
    }

    public boolean isFareRequest() {
        return this.isFareRequest;
    }

    private String getURI() {
        StringBuilder sb = new StringBuilder(BASE_URI);

        sb.append("pickup[latitude]=");
        sb.append(this.pickupLatitude);

        sb.append("&pickup[longitude]=");
        sb.append(this.pickupLongitude);

        if(this.useDestinationCoordinates) {
            sb.append("&dropoff[latitude]=");
            sb.append(this.destinationLatitude);

            sb.append("&dropoff[longitude]=");
            sb.append(this.destinationLongitude);
        } else {
            sb.append("&dropoff[formatted_address]=");
            sb.append(this.destinationAddress);
        }

        sb.append("&client_id=");
        sb.append(CLIENT_ID);

        return sb.toString().replace(" ", "%20");
    }

    private String getURL() {
        return new StringBuilder(MOBILE_URL).append("?client_id=").append(CLIENT_ID).toString();
    }

    private String getFareEstimateURL() {
        StringBuilder sb = new StringBuilder(API_BASE_URL);
        sb.append("/v1/estimates/price");

        sb.append("?server_token=").append(SERVER_TOKEN);

        sb.append("&start_latitude=").append(this.pickupLatitude);
        sb.append("&start_longitude=").append(this.pickupLongitude);

        sb.append("&end_latitude=").append(this.destinationLatitude);
        sb.append("&end_longitude=").append(this.destinationLongitude);



        return sb.toString();
    }

    public Intent execute() {

//        if(!this.useDestinationCoordinates) {
//            new GeocoderTask().execute(this.destinationAddress);
//        }

        if(this.isFareRequest) {
            ConnectivityManager connMgr = (ConnectivityManager)this.context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if(networkInfo != null && networkInfo.isConnected()) {
                new DownloadUberFares().execute(getFareEstimateURL());
            } else {
                Log.d(TAG, "can't do netwerking");
            }
            return null;
        }

        try {
            PackageManager pm = context.getPackageManager();
            pm.getPackageInfo("co.jola.jola", PackageManager.GET_ACTIVITIES);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Log.d(TAG, "opening app with URI: " + getURI());
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

    private class DownloadUberFares extends AsyncTask<String, Void, String> {

        // Reads an InputStream and converts it to a String.
        private String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
            Reader reader = null;
            reader = new InputStreamReader(stream, "UTF-8");
            char[] buffer = new char[len];
            reader.read(buffer);
            return new String(buffer);
        }

        private String downloadFares(String myurl) throws IOException {
            InputStream is = null;
            // Only display the first 500 characters of the retrieved
            // web page content.
            int len = 500;

            try {
                URL url = new URL(myurl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                // Starts the query
                conn.connect();
                int response = conn.getResponseCode();
                Log.d(TAG, "The response is: " + response);
                is = conn.getInputStream();

                // Convert the InputStream into a string
                String contentAsString = readIt(is, len);
                return contentAsString;

                // Makes sure that the InputStream is closed after the app is
                // finished using it.
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        }

        @Override
        protected String doInBackground(String... urls) {
            try {
                return downloadFares(urls[0]);
            } catch(IOException e) {
                return "Unable to do it bruh";
            }
        }

        @Override
        protected void onPostExecute(String result) {

        }
    }

    // An AsyncTask class for accessing the GeoCoding Web Service
    private class GeocoderTask extends AsyncTask<String, Void, List<Address>>{

        @Override
        protected List<Address> doInBackground(String... locationName) {
            // Creating an instance of Geocoder class
            Geocoder geocoder = new Geocoder(UberAPI.this.context);
            List<Address> addresses = null;

            try {
                // Getting a maximum of 3 Address that matches the input text
                addresses = geocoder.getFromLocationName(locationName[0], 3);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return addresses;
        }


        @Override
        protected void onPostExecute(List<Address> addresses) {
            if (addresses == null || addresses.size() == 0){
                Toast.makeText(UberAPI.this.context, "No Location found", Toast.LENGTH_SHORT).show();
            }

            Address address = addresses.get(0);

            UberAPI.this.destinationLatitude = address.getLatitude();
            UberAPI.this.destinationLongitude = address.getLongitude();

            UberAPI.this.execute();
        }
    }
}
