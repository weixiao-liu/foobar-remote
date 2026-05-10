package com.foobar.remote;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "FoobarRemote";
    private static final String PLAYER_URL = "http://192.168.1.6:8888/albumart_minimal/player.html";
    private static final int REQ_AUDIO = 100;

    private WebView webView;
    private FloatingActionButton micFab;
    private TextView voiceStatus;
    private SpeechRecognizer recognizer;
    private boolean isListening = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initWebView();
        initVoice();
        initMicButton();
    }

    /* ——— WebView ——— */
    private void initWebView() {
        webView = findViewById(R.id.webView);
        WebSettings ws = webView.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setMediaPlaybackRequiresUserGesture(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ws.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                Log.d(TAG, "Page loaded: " + url);
            }
        });
        webView.loadUrl(PLAYER_URL);
    }

    /* ——— 璇锋眰璇嗗浘鏉冮檺 ——— */
    private void initVoice() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            Toast.makeText(this, "Device not support speech recognition", Toast.LENGTH_LONG).show();
            return;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO}, REQ_AUDIO);
        }
    }

    /* ——— 璇峰崱鎸夐挳 ——— */
    private void initMicButton() {
        micFab = findViewById(R.id.micFab);
        voiceStatus = findViewById(R.id.voiceStatus);

        micFab.setOnClickListener(v -> {
            if (isListening) {
                stopListening();
            } else {
                startListening();
            }
        });
    }

    /* ———寮€濮嬬洃鍚?*/
    private void startListening() {
        if (recognizer != null) {
            recognizer.destroy();
        }
        recognizer = SpeechRecognizer.createSpeechRecognizer(this);
        recognizer.setRecognitionListener(new RecognitionListener() {
            @Override public void onReadyForSpeech(Bundle params) {
                isListening = true;
                micFab.setBackgroundTintList(
                        ContextCompat.getColorStateList(MainActivity.this, android.R.color.holo_red_light));
                showStatus("Listening...");
            }
            @Override public void onBeginningOfSpeech() {}
            @Override public void onRmsChanged(float rmsdB) {}
            @Override public void onBufferReceived(byte[] buffer) {}
            @Override public void onEndOfSpeech() {
                showStatus("Recognizing...");
            }
            @Override public void onError(int error) {
                isListening = false;
                resetMic();
                String msg = error == SpeechRecognizer.ERROR_NO_MATCH ? "No match, try again" : "Error: " + error;
                showStatus(msg);
                Log.e(TAG, "Speech error: " + error);
            }
            @Override public void onResults(Bundle results) {
                isListening = false;
                resetMic();
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String text = matches.get(0);
                    Log.d(TAG, "Heard: " + text);
                    showStatus("Heard: " + text);
                    handleVoiceCommand(text);
                } else {
                    showStatus("Nothing recognized");
                }
            }
            @Override public void onPartialResults(Bundle partialResults) {}
            @Override public void onEvent(int eventType, Bundle params) {}
        });

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "zh-CN");
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false);

        recognizer.startListening(intent);
    }

    /* ———鍋滄瘮杩旇浇—*/
    private void stopListening() {
        if (recognizer != null) {
            recognizer.stopListening();
            recognizer.cancel();
        }
        isListening = false;
        resetMic();
    }

    private void resetMic() {
        micFab.setBackgroundTintList(
                ContextCompat.getColorStateList(this, android.R.color.holo_blue_light));
        voiceStatus.setVisibility(View.GONE);
    }

    /* ———澶勭悊璇嗗浘鎸囦护 ——— */
    private void handleVoiceCommand(String cmd) {
        String c = cmd.replaceAll("\\s+", "");
        Log.d(TAG, "Command: " + c);

        if (c.contains("鎾绘敹") || c.contains("鎾愭敹")) {
            injectJS("document.getElementById('playBtn').click();", "Play");
        }
        else if (c.contains("鏆傚仠") || c.contains("鍋滄"):
            injectJS("document.getElementById('playBtn').click();", "Pause");
        }
        else if (c.contains("澶уソ") || c.contains("澶т竴鐐") || c.contains("闊抽噺澧?) || c.contains("澹伴煶澶") || c.contains("闊抽噺澧炲姞")) {
            injectVolumeJS(true);
        }
        else if (c.contains("灏忓ソ") || c.contains("灏忎竴鐐") || c.contains("闊抽噺灏?) || c.contains("澹伴煶灏?) || c.contains("闊抽噺鍑忓皯")) {
            injectVolumeJS(false);
        }
        else if (c.contains("涓婁竴鏇?) || c.contains("涓婁竴棣?) || c.contains("涓婃洸")) {
            injectJS("document.getElementById('prevBtn').click();", "Prev");
        }
        else if (c.contains("涓嬩竴鏇?) || c.contains("涓嬩竴棣?) || c.contains("涓嬫洸")) {
            injectJS("document.getElementById('nextBtn').click();", "Next");
        }
        else {
            showStatus("Unknown: " + cmd);
            Log.d(TAG, "No match: " + cmd);
        }
    }

    /* ———娉ㄥ叆JS鎵цă钮鐐瑰幓 ——— */
    private void injectJS(String js, String action) {
        runOnUiThread(() -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                webView.evaluateJavascript(js, result ->
                        Log.d(TAG, action + " result: " + result));
            } else {
                webView.loadUrl("javascript:" + js);
            }
            Toast.makeText(MainActivity.this, action, Toast.LENGTH_SHORT).show();
            showStatus("Done: " + action);
        });
    }

    /* ———闊抽噺璋冭妭JS ——— */
    private void injectVolumeJS(boolean up) {
        String js = "(function() {" +
                "  var s = document.getElementById('volSlider');" +
                "  if (!s) return 'no-slider';" +
                "  var step = 10;" +
                "  var v = parseInt(s.value) || 50;" +
                "  v = " + (up ? "Math.min(100, v + step);" : "Math.max(0, v - step);") +
                "  s.value = v;" +
                "  s.dispatchEvent(new Event('input'));" +
                "  s.dispatchEvent(new Event('change'));" +
                "  return 'volume:' + v;" +
                "})()";

        runOnUiThread(() -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                webView.evaluateJavascript(js, result ->
                        Log.d(TAG, "Volume: " + result));
            } else {
                webView.loadUrl("javascript:" + js);
            }
            String msg = up ? "Volume +10" : "Volume -10";
            Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
            showStatus(msg);
        });
    }

    /* ———鐘舵€佹彁绀?—*/
    private void showStatus(String text) {
        runOnUiThread(() -> {
            voiceStatus.setText(text);
            voiceStatus.setVisibility(View.VISIBLE);
            voiceStatus.removeCallbacks(null);
            voiceStatus.postDelayed(() ->
                    voiceStatus.setVisibility(View.GONE), 3000);
        });
    }

    /* ———鏉冮檺鍥炶皟 ——— */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Mic OK", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Need mic permission", Toast.LENGTH_LONG).show();
            }
        }
    }

    /* ———杩斿洖閿鏃ф帶鍒禤ebView ——— */
    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        if (recognizer != null) {
            recognizer.destroy();
        }
        if (webView != null) {
            webView.destroy();
        }
        super.onDestroy();
    }
}