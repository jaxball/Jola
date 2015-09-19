package co.jola.jola.apis;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;

/**
 * Created by jasonlin on 9/19/15.
 */
public class UberAPI {

    // coordinates of where the user is
    private double pickupLatitude;
    private double pickupLongitude;

    // coordinates of where they are going
    private double destinationLatitude;
    private double destinationLongitude;

    // TODO - integreate Uber API

    public Intent openTest(Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            pm.getPackageInfo("com.ubercab", PackageManager.GET_ACTIVITIES);
            String uri =
                    "uber://?action=setPickup&pickup[latitude]=37.775818&pickup[longitude]=-122.418028&client_id=nrGE4PWCOa6o8VdGZR_zvV7l99rWNvs5";
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(uri));
            return intent;
        } catch (PackageManager.NameNotFoundException e) {
            // No Uber app! Open mobile website.
            String url = "https://m.uber.com/sign-up?client_id=YOUR_CLIENT_ID";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            return i;
        }
    }
}
