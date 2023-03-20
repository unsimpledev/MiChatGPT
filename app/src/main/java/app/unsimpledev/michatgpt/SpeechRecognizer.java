package app.unsimpledev.michatgpt;

import android.app.Activity;
import android.content.Intent;
import android.speech.RecognizerIntent;

import java.util.List;

public class SpeechRecognizer {
    private Activity activity;
    private int requestCode;
    private SpeechRecognizer.Listener listener;

    public SpeechRecognizer(Activity activity, int requestCode, SpeechRecognizer.Listener listener) {
        this.activity = activity;
        this.requestCode = requestCode;
        this.listener = listener;
    }

    public void start() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "El chat te escucha");
        activity.startActivityForResult(intent, requestCode);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == this.requestCode && resultCode == Activity.RESULT_OK) {
            List<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String spokenText = result.get(0);
            listener.onSpeechRecognized(spokenText);
        }
    }

    public interface Listener {
        void onSpeechRecognized(String text);
    }

}
