package co.jola.jola.apis;

import android.os.Bundle;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by jasonlin on 9/19/15.
 */
public class YelpParser {

    private String yelp_response;
    private Bundle yelp_bundle = new Bundle();
    private ArrayList<String> keys = new ArrayList<String>();

    /**
     * Sets Yelp's response for this class
     *
     * @param response
     */
    public void setResponse(String response) {
        yelp_response = response;
    }

    /**
     * Returns the set Yelp response
     *
     * @return string yelp_response
     */
    public String getResponse() {
        return yelp_response;
    }

    /**
     * Parse's yelp's response for the business name; mobile url; and ratings url.
     * Mobile url and ratings url is separated by " ,,, "
     *
     * @throws JSONException
     * @sets yelp_bundle(key = business name)
     * @sets keys arraylist with business name
     */
    public void parseBusiness() throws JSONException {
        JSONObject o1 = new JSONObject(yelp_response);
        JSONArray businesses = o1.getJSONArray("businesses");
        String tmpString;
        for (int i = 0; businesses.length() > i; i++) {
            tmpString = businesses.getJSONObject(i).get("mobile_url").toString() + " ,,, " +
                    businesses.getJSONObject(i).get("rating_img_url").toString();
            keys.add(businesses.getJSONObject(i).get("name").toString());
            yelp_bundle.putString(keys.get(i), tmpString);
        }
    }

    /**
     * Gets the business name, assuming you supply the bundle, key, and int
     * In reality, all it does is pull myKey.get(i), but I added the myBundle
     * param to try and force all information to be there.
     *
     * @param myBundle
     * @param myKey
     * @param i
     * @return myKey.get(i)
     */
    public String getBusinessName(Bundle myBundle, ArrayList<String> myKey, int i) {
        return myKey.get(i);
    }

    /**
     * This gets the business's name, which is stored in the ArrayList keys, using
     * this class's stored results.
     *
     * @param i
     * @return
     */
    public String getBusinessName(int i) {
        return keys.get(i);
    }

    /**
     * This returns the business's mobile URL from myBundle using the key provided at int i
     *
     * @param myBundle
     * @param myKey
     * @param i
     * @return mobileURL
     */
    public String getBusinessMobileURL(Bundle myBundle, ArrayList<String> myKey, int i) {
        String tmp = myBundle.getString(myKey.get(i));
        int x = tmp.indexOf(" ,,, ");
        String mobileURL = tmp.substring(0, x);
        return mobileURL;
    }

    /**
     * This returns the mobile URL using this class's internally stored variables at int i.
     * For ease of use I suggest using this method.
     *
     * @param i
     * @return mobileURL
     */
    public String getBusinessMobileURL(int i) {
        String tmp = yelp_bundle.getString(keys.get(i));
        int x = tmp.indexOf(" ,,, ");
        String mobileURL = tmp.substring(0, x);
        return mobileURL;
    }

    /**
     * This will return the rating URL from the user-supplied Bundle, key, and int i
     *
     * @param myBundle
     * @param myKey
     * @param i
     * @return ratingURL
     */
    public String getRatingURL(Bundle myBundle, ArrayList<String> myKey, int i) {
        String tmp = myBundle.getString(myKey.get(i));
        int x = tmp.indexOf(" ,,, ") + 5;
        String ratingURL = tmp.substring(x);
        return ratingURL;
    }

    /**
     * This will return the rating URL using this class's internal variables.
     * I recommend using this method. Int i is for keys.get(i).
     *
     * @param i
     * @return ratingURL
     */
    public String getRatingURL(int i) {
        String tmp = yelp_bundle.getString(keys.get(i));
        int x = tmp.indexOf(" ,,, ") + 5;
        String ratingURL = tmp.substring(x);
        return ratingURL;
    }

    /**
     * Returns the bundle, key is the business name.
     *
     * @return yelp_bundle
     */
    public Bundle getYelpBundle() {
        return yelp_bundle;
    }

    /**
     * Returns the keys (business names) for the yelpBundle.
     *
     * @return keys (business names)
     */
    public ArrayList<String> getBundleKeys() {
        return keys;
    }

    /**
     * This will return the keys.size(), and is designed to be used with loops
     *
     * @return keys.size()
     */
    public int getBudleKeysSize() {
        int size = keys.size();
        return size;
    }
}