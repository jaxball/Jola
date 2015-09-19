package co.jola.jola;


import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

import android.util.Log;

import com.firebase.client.Firebase;

import co.jola.jola.apis.API;
import co.jola.jola.apis.BraintreeAPI;
import co.jola.jola.apis.UberAPI;
import co.jola.jola.apis.YelpAPI;

public class MainActivity extends Activity implements OnClickListener {

    private TextView mText;
    private SpeechRecognizer sr;
    private static final String TAG = "Testing voice";
    private Intent intent;
    private TextToSpeech jolaspeaks;
    private String userRequest;
    private LocationManager locationManager;
    private Location userLocation;

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Firebase.setAndroidContext(this);
        Firebase ref = new Firebase("https://jola.firebaseio.com/");
        setContentView(R.layout.activity_main);
        mText = (TextView) findViewById(R.id.textView1);
        jolaspeaks = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                jolaspeaks.setLanguage(Locale.US);
            }
        }
        );
        this.userLocation = null;

        // set up location monitoring
        // Acquire a reference to the system Location Manager
        this.locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                MainActivity.this.userLocation = location;
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };

        // Register the listener with the Location Manager to receive location updates
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    public void requestPermissions(@NonNull String[] permissions, int requestCode)
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);

        sr = SpeechRecognizer.createSpeechRecognizer(this);
        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, "co.jola.jola");
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 0);
        sr.setRecognitionListener(new listener());
        sr.startListening(intent);
        userRequest = "";
    }

    @Override
    public void onPause() {
        super.onPause();

        sr.stopListening();
    }

    @Override
    public void onResume() {
        super.onResume();

        sr.startListening(intent);
    }

    class listener implements RecognitionListener {
        public void onReadyForSpeech(Bundle params) {
        }

        public void onBeginningOfSpeech() {
        }

        public void onRmsChanged(float rmsdB) {
//            if(jolaspeaks.isSpeaking()){
//                Log.i(TAG, "jola is sleepy");
//            }
            //Log.d(TAG, "onRmsChanged");
        }

        public void onBufferReceived(byte[] buffer) {
        }

        public void onEndOfSpeech() {
        }

        public void onError(int error) {
            Log.d(TAG, "error " + error);
            mText.setText("error " + error);
        }

        private void reply(String spoken) {
            sr.stopListening();
            spoken = spoken.toLowerCase();
            Log.d(TAG, "Got the words: " + spoken);
            if (spoken.equals("hello")) {
                jolaspeaks.speak("Yola! How can I help you today?", TextToSpeech.QUEUE_FLUSH, null);
            } else if (spoken.equals("shut up")) {
                System.exit(0);
            } else if (spoken.equals("yes")) {
                Intent i = new Intent();

                // -- UBER
                if (UberAPI.UberMatch(userRequest)) {
                    UberAPI uber = new UberAPI(MainActivity.this.getApplicationContext());

                    // TODO - if userLocation is null, use last known location instead
                    uber.setPickupLocation(userLocation.getLatitude(), userLocation.getLongitude());

                    // TODO - ask user for destination
                    // TODO - handle if user wants estimation

                    i = uber.execute();
                }

                // -- Braintree
                else if(BraintreeAPI.BraintreeMatch(userRequest)) {
                    // do braintree stuff
                    BraintreeAPI braintree = new BraintreeAPI();
                    i = braintree.execute();
                }

                // -- Yelp
                else if(YelpAPI.YelpMatch(userRequest)) {
                    // do yelp stuff
                    YelpAPI yelp = new YelpAPI();
                    i = yelp.execute();
                }

                if(i != null) {
                    startActivity(i);
                }
            } else if(spoken.equals("no")){
                jolaspeaks.speak("Please repeat your request.", TextToSpeech.QUEUE_FLUSH, null);
            } else {
                jolaspeaks.speak("I heard " + spoken + ", is that correct?", TextToSpeech.QUEUE_FLUSH, null);
                userRequest = spoken;
            }
            while(jolaspeaks.isSpeaking());
            sr.startListening(intent);
        }

        public void onResults(Bundle results) {
            Log.d(TAG, "onResults " + results);
            ArrayList data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            String detected = data.get(0).toString();
            reply(detected);
        }
        public void onPartialResults(Bundle partialResults) {}
        public void onEvent(int eventType, Bundle params) {}
    }

    public void onClick(View v) {
        if (v.getId() == R.id.btn_speak) {
            Log.i("in OnClick","listening");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}