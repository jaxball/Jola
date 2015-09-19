package co.jola.jola;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.widget.Button;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Locale;

import android.util.Log;

public class MainActivity extends Activity  implements OnClickListener {

    private TextView mText;
    private SpeechRecognizer sr;
    private static final String TAG = "Testing voice";
    private Intent intent;
    private TextToSpeech jolaspeaks;
    private String userRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Button speakButton = (Button) findViewById(R.id.btn_speak);
        mText = (TextView) findViewById(R.id.textView1);
        //speakButton.setOnClickListener(this);
        jolaspeaks =new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                jolaspeaks.setLanguage(Locale.US);
            }
        }
        );

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

    class listener implements RecognitionListener
    {
        public void onReadyForSpeech(Bundle params) {}
        public void onBeginningOfSpeech() {}
        public void onRmsChanged(float rmsdB)
        {
//            if(jolaspeaks.isSpeaking()){
//                Log.i(TAG, "jola is sleepy");
//            }
            //Log.d(TAG, "onRmsChanged");
        }
        public void onBufferReceived(byte[] buffer) {}
        public void onEndOfSpeech() {}
        public void onError(int error)
        {
            Log.d(TAG, "error " + error);
            mText.setText("error " + error);
        }
        private void reply(String spoken){
            sr.stopListening();
            spoken = spoken.toLowerCase();
            Log.d(TAG, "Got the words: " + spoken);
            if(spoken.equals("hello")) {
                jolaspeaks.speak("Yola! How can I help you today?", TextToSpeech.QUEUE_FLUSH, null);
            } else if(spoken.equals("shut up")) {
                System.exit(0);
            } else if(spoken.equals("yes")) {
                TextClassifier tc = new TextClassifier(MainActivity.this.getApplicationContext());
                tc.parseText(userRequest);

                // If something the user said doesn't match an app to open, the the intent would be
                // null.
                Intent i = tc.getAction();
                if(i != null) {
                    startActivity(tc.getAction());
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
        public void onResults(Bundle results)
        {
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