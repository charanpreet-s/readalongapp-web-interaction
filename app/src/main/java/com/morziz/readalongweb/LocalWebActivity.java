package com.morziz.readalongweb;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


public class LocalWebActivity extends AppCompatActivity {

    private WebView webView;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.s_web_view);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
        webView = findViewById(R.id.webView);
        progressBar = findViewById(R.id.progressBar);
        startLoading();
    }

    public void startLoading() {
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                getSupportActionBar().setTitle(url);
                progressBar.setVisibility(View.VISIBLE);
                if (url.contains("/finish")) {
                    String id = url.substring(url.lastIndexOf("finish/") + 7);
                }
                invalidateOptionsMenu();
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                webView.loadUrl(url);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
                getSupportActionBar().setTitle(view.getTitle());
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                progressBar.setVisibility(View.GONE);
            }
        });
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW | WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        webView.setWebContentsDebuggingEnabled(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(this, "androidInteract");
        webView.loadUrl("file:///android_asset/index.html");
    }

    @JavascriptInterface
    public void triggerReadAlong(String bookId) {
        Toast.makeText(this, "WebView Triggered App", Toast.LENGTH_SHORT).show();
        openBolo(bookId);
    }


    private void openBolo(String bookId) {
        Toast.makeText(this, "Opening book Id : " + bookId, Toast.LENGTH_LONG).show();
        Intent intent = new Intent();
        intent.setAction("com.google.android.apps.seekh.READBOOK");
        intent.putExtra("assessment_mode", true);
        intent.putExtra("intent_open_book_id", bookId);
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        boloLauncher.launch(intent);
    }

    ActivityResultLauncher<Intent> boloLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                int resultCode = result.getResultCode();
                if (resultCode == Activity.RESULT_OK) {
                    int correctWords = result.getData().getIntExtra("correct_words", 0);
                    long totalTime = result.getData().getLongExtra("total_time", 0L);
                    if(totalTime < 1000) totalTime = 1000;
                    long totalTimeInSeconds = totalTime / 1000;
                    int wordsPerMinute = (int) ((correctWords * 60) / totalTimeInSeconds);
                    Toast.makeText(this, "Read Along : Correct words : " + correctWords + " : Time taken : " + l, Toast.LENGTH_LONG).show();
                    webView.evaluateJavascript("onReadAlongResult(" + correctWords + "," + totalTimeInSeconds + ")", null);
                } else {
                    Toast.makeText(this, "Read Along Result Code : " + resultCode, Toast.LENGTH_LONG).show();
                }
            });


    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return false;
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            finish();
        }
    }
}
