package co.jola.jola;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
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
import android.widget.EditText;
import android.widget.TextView;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Locale;
import android.util.Log;
import com.firebase.client.Firebase;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;

import co.jola.jola.apis.API;
import co.jola.jola.apis.API_Keys;
import co.jola.jola.apis.BraintreeAPI;
import co.jola.jola.apis.SpotifyAPI;
import co.jola.jola.apis.UberAPI;
import co.jola.jola.apis.Yelp;
import co.jola.jola.apis.YelpAPI;
import co.jola.jola.apis.YelpParser;

public class MainActivity extends Activity implements OnClickListener, ConnectionCallbacks, OnConnectionFailedListener {

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    private enum Status {
        NULL,
        UBER_PICKUP_RESPONSE,
        UBER_DEST_RESPONSE
    }

    private TextView yolaText;
    private EditText userText;
    private SpeechRecognizer sr;
    private static final String TAG = "Testing voice";
    private static final String HELLO = "hello";
    private static final String SHUT_UP = "shut up";
    private static final String YOLA = "Yola! How can I help you today?";
    private static final String REPEAT = "Please repeat your request.";
    private static final String YES = "yes";
    private static final String NO = "no";
    private static final String SPOTIFY = "spotify";
    private Intent intent;
    private TextToSpeech jolaspeaks;
    private String userRequest;
    private LocationManager locationManager;
    private Location userLocation;
    private API invoked;
    private Status status;

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private GoogleApiClient mGoogleApiClient;

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Firebase.setAndroidContext(this);
        Firebase ref = new Firebase("https://jola.firebaseio.com/");
        setContentView(R.layout.activity_main);

         mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        yolaText = (TextView) findViewById(R.id.textView);
        userText = (EditText) findViewById(R.id.textView2);
        this.userLocation = null;
        this.status = Status.NULL;
        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, "co.jola.jola");
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 0);
        userRequest = "";
    }

    @Override
    public void onPause() {
        super.onPause();

        jolaspeaks.shutdown();
        sr.destroy();
        Log.d(TAG, "activity paused - stop listening");
    }

    @Override
    public void onResume() {
        super.onResume();

        jolaspeaks = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                jolaspeaks.setLanguage(Locale.US);
            }
        });
        sr = SpeechRecognizer.createSpeechRecognizer(MainActivity.this);
        sr.setRecognitionListener(new listener());
        sr.startListening(intent);
        Log.d(TAG, "activity resumed - listening");
    }

    private void reply(String spoken) {
        sr.stopListening();
        Log.d(TAG, "jola is not listening");
        userText.setText(spoken);
        spoken = spoken.toLowerCase();
        Log.d(TAG, "Got the words: " + spoken);
        if (spoken.equals(HELLO)) {
            jolaspeaks.speak(YOLA, TextToSpeech.QUEUE_FLUSH, null);
            yolaText.setText(YOLA);
        } else if (spoken.equals(SHUT_UP)) {
            System.exit(0);
        } else if (status == Status.UBER_PICKUP_RESPONSE) {
            Log.d(TAG, "destination: " + spoken);
            // TODO - here spoken is an address (hopefully).
            ((UberAPI)invoked).setDestinationAddress(spoken);

            // fuck it ship it
            startActivity(invoked.execute());

        } else if (spoken.equals(YES)) {
            Intent i = null;

            // -- UBER
            if (UberAPI.UberMatch(userRequest)) {
                UberAPI uber = new UberAPI(MainActivity.this.getApplicationContext(), userRequest);

                // TODO - if userLocation is null, use last known location instead
                uber.setPickupLocation(userLocation.getLatitude(), userLocation.getLongitude());

                Log.d(TAG, "user location: (" + userLocation.getLatitude() + ", " + userLocation.getLongitude() + ")");

                invoked = uber;

                // flag
                status = Status.UBER_PICKUP_RESPONSE;
                jolaspeaks.speak("Where would you like to go?", TextToSpeech.QUEUE_FLUSH, null);
                yolaText.setText("Where would you like to go?");
                // TODO - handle if user wants estimation
                if(uber.isFareRequest()) {
                    uber.execute();
                }
            }

            // -- Braintree
            else if (BraintreeAPI.BraintreeMatch(userRequest)) {
                // do braintree stuff
                BraintreeAPI braintree = new BraintreeAPI();
                i = braintree.execute();
            }

            // -- Yelp
            else if (YelpAPI.YelpMatch(userRequest)) {
                Log.d(TAG, "Looking for food...");
                API_Keys api_keys = new API_Keys();
                Log.i(TAG, "location is -----" +userLocation);
                new MyTask(MainActivity.this, userLocation, api_keys).execute();
                Log.e(TAG, "before that if statement");

                // i always false
                if (i != null) {
                    Log.e(TAG, "starting an Activity??");
                    startActivity(i);
                }
            } else if (SpotifyAPI.SpotifyMatch(userRequest)) {
                Log.d(TAG, "spotify match");
                SpotifyAPI spotify = new SpotifyAPI();
                startActivity(spotify.getIntent());
            }
        } else if (spoken.equals(NO)) {
            jolaspeaks.speak(REPEAT, TextToSpeech.QUEUE_FLUSH, null);
            yolaText.setText(REPEAT);
        } else {
            jolaspeaks.speak("I heard " + spoken + ", is that correct?", TextToSpeech.QUEUE_FLUSH, null);
            yolaText.setText("I heard " + spoken + ", is that correct?");
            userRequest = spoken;
        }
        Log.e(TAG, "about to go into the while loop");
        while (jolaspeaks.isSpeaking()) ;
        Log.e(TAG, "about to start listening again");
        sr.startListening(intent);
        Log.d(TAG, "jola is listening");
    }

    class listener implements RecognitionListener {
        public void onReadyForSpeech(Bundle params) {
            Log.d(TAG, "listener - ready for speech");
        }

        public void onBeginningOfSpeech() {
            Log.d(TAG, "listener - beginning of speech");
        }

        public void onRmsChanged(float rmsdB) {
        }

        public void onBufferReceived(byte[] buffer) {
        }

        public void onEndOfSpeech() {
            Log.d(TAG, "listener - hit end of speech");
        }

        public void onError(int error) {
            switch(error) {
                case SpeechRecognizer.ERROR_NO_MATCH:
                    Log.d(TAG, "error: No recognition result matched");
                    break;
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    Log.d(TAG, "error: RecognitionService busy");
                    break;
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    Log.d(TAG, "error: no speech input");
                    break;
                case SpeechRecognizer.ERROR_AUDIO:
                    Log.d(TAG, "error: Audio recording error");
                    break;
                case SpeechRecognizer.ERROR_CLIENT:
                    Log.d(TAG, "error: other client side errors");
                    break;
            }
        }

        public void onResults(Bundle results) {
            Log.d(TAG, "onResults " + results);
            ArrayList data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            String detected = data.get(0).toString();
            reply(detected);
        }

        @Override
        public void onPartialResults(Bundle partialResults) {
            Log.d(TAG, "got partial result");
        }

        @Override
        public void onEvent(int eventType, Bundle params) {
            Log.d(TAG, "got an event?");
        }
    }

    public void onClick(View v) {
        if (v.getId() == R.id.snooze_wakeup) {
            Log.i("in OnClick","snooze wakeup");
            if(sr == null){
                // assuming destroyed
                Log.i(TAG,"sr was null........");
                sr = SpeechRecognizer.createSpeechRecognizer(MainActivity.this);
                sr.setRecognitionListener(new listener());
                sr.startListening(intent);
            } else{
                sr.destroy();
            }
        } else if (v.getId() == R.id.enter){
            reply(userText.getText().toString());
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

    private static class MyTask extends AsyncTask<Void, Void, String> {

        WeakReference<Activity> mWeakRef;
        Location mLocation;
        API_Keys mKeys;

        public MyTask(Activity activity, Location userLocation, API_Keys keys) {
            mWeakRef = new WeakReference<Activity>(activity);
            mLocation = userLocation;
            mKeys = keys;
        }

        @Override
        protected String doInBackground(Void... voids) {
            Yelp yelp = new Yelp(mKeys.getYelpConsumerKey(), mKeys.getYelpConsumerSecret(),
              mKeys.getYelpToken(), mKeys.getYelpTokenSecret());
            if(mLocation == null){
                Log.i(TAG,"location was null!!!!!!!");
                return yelp.search("burritos", 43.6767, 79.6306);
            }
            return yelp.search("burritos", mLocation.getLatitude(), mLocation.getLongitude());
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d(TAG, "got response.............." +s);
            YelpParser yParser = new YelpParser();
            Log.d(TAG, "Enters parser...");
            yParser.setResponse(s);
            try {
                yParser.parseBusiness();
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                Log.d(TAG, "Failed to get restaurants");
                //Do whatever you want with the error, like throw a Toast error report
            }

            int k = 0;
            String mobile_url = yParser.getBusinessMobileURL(k);
            String rating_url = yParser.getRatingURL(k);
            String b_name = yParser.getBusinessName(k);
            Log.d(TAG, "Yelp result: " + mobile_url + rating_url + b_name);
        }
    }
}