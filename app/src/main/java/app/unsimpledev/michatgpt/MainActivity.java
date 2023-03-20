package app.unsimpledev.michatgpt;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.util.Locale;

public class MainActivity extends AppCompatActivity  implements SpeechRecognizer.Listener,  TextToSpeech.OnInitListener{

    private Gpt3Api gpt3Api;
    private EditText editTextQuestion;
    private LinearLayout chatLayout;
    private ProgressBar progressBar;
    private static final int REQUEST_CODE = 1;
    private SpeechRecognizer speechRecognizer;
    private TextToSpeech textToSpeech;
    private boolean isVoiceEnabled;

    private static final String MESSAGE_TYPE_RESPONSE = "RESPONSE";
    private static final String MESSAGE_TYPE_REQUEST = "REQUEST";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gpt3Api = new Gpt3Api(this);
        editTextQuestion = findViewById(R.id.editTextQuestion);
        chatLayout = findViewById(R.id.layoutInput);
        progressBar = findViewById(R.id.progressBar);

        speechRecognizer = new SpeechRecognizer(this, REQUEST_CODE, this);
        textToSpeech = new TextToSpeech(this, this);
        isVoiceEnabled = getSharedPreferences("MICHATGPT", MODE_PRIVATE).getBoolean("CHECKRESPVOICE", true);

        Button buttonAsk = findViewById(R.id.buttonAsk);
        buttonAsk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String prompt = editTextQuestion.getText().toString();
                editTextQuestion.setText("");
                callChatGpt(prompt);
            }
        });

        Button buttonSpeech = findViewById(R.id.buttonSpeech);
        buttonSpeech.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speechRecognizer.start();
            }
        });

        CheckBox checkboxVoiceResult = findViewById(R.id.checkVoiceResut);
        checkboxVoiceResult.setChecked(isVoiceEnabled);
        checkboxVoiceResult.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isVoiceEnabled = isChecked;
                getSharedPreferences("MICHATGPT", MODE_PRIVATE).edit().putBoolean("CHECKRESPVOICE", isChecked).apply();
            }
        });
    }

    private void addChatMessage(String message, String type) {
        if (message.startsWith("\n\n")) {
            message = message.replaceFirst("\n\n", "");
        }
        else if (message.startsWith("\n")) {
            message = message.replaceFirst("\n", "");
        }
        TextView textView = new TextView(this);
        textView.setText(message);
        if (MESSAGE_TYPE_RESPONSE.equals(type)){
            textView.setTypeface(null, Typeface.BOLD_ITALIC);
        }
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(30, 5, 30, 5);
        textView.setLayoutParams(params);
        chatLayout.addView(textView);
        if (MESSAGE_TYPE_RESPONSE.equals(type)){
            callTextToSpeech(message);
        }
    }

    private void callChatGpt(String prompt){
        addChatMessage(prompt, MESSAGE_TYPE_REQUEST);
        progressBar.setVisibility(View.VISIBLE);
        gpt3Api.generateText(prompt,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressBar.setVisibility(View.GONE);
                        addChatMessage(response, MESSAGE_TYPE_RESPONSE);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressBar.setVisibility(View.GONE);
                        addChatMessage("Error: " + error.getMessage(), MESSAGE_TYPE_RESPONSE);
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        speechRecognizer.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onSpeechRecognized(String text) {
        callChatGpt(text);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = textToSpeech.setLanguage(Locale.getDefault());
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TextToSpeech", "Lenguaje no soportado");
            }
        } else {
            Log.e("TextToSpeech", "Inicialización fallida");
        }
    }

    private void callTextToSpeech(String text) {
        if (isVoiceEnabled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
            } else {
                textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
            }
        }
    }


}