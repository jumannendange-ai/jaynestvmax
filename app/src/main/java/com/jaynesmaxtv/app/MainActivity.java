package com.jaynesmaxtv.app;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebResourceRequest;
import android.net.http.SslError;
import android.webkit.SslErrorHandler;

import com.onesignal.OneSignal;
import com.onesignal.debug.LogLevel;

public class MainActivity extends Activity {
    private WebView web;
    private static final String URL = "https://dde.ct.ws/";
    private static final String ONESIGNAL_ID = "10360777-3ada-4145-b83f-00eb0312a53f";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // OneSignal
        OneSignal.getDebug().setLogLevel(LogLevel.NONE);
        OneSignal.initWithContext(this, ONESIGNAL_ID);
        OneSignal.getNotifications().requestPermission(true, null);

        // Fullscreen
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        hideSystemUI();

        web = new WebView(this);
        setContentView(web);

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        s.setUserAgentString("Mozilla/5.0 (Linux; Android 11) AppleWebKit/537.36 Chrome/120.0.0.0 Mobile Safari/537.36");

        web.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedSslError(WebView v, SslErrorHandler h, SslError e) {
                h.proceed();
            }
            @Override
            public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest r) {
                return false;
            }
        });

        web.setWebChromeClient(new WebChromeClient() {
            private View mCustomView;
            private CustomViewCallback mCustomViewCallback;

            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                if (mCustomView != null) {
                    onHideCustomView();
                    return;
                }
                mCustomView = view;
                mCustomViewCallback = callback;
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                setContentView(mCustomView);
                hideSystemUI();
            }

            @Override
            public void onHideCustomView() {
                setContentView(web);
                mCustomView = null;
                if (mCustomViewCallback != null) {
                    mCustomViewCallback.onCustomViewHidden();
                    mCustomViewCallback = null;
                }
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                hideSystemUI();
            }
        });

        web.loadUrl(URL);
    }

    private void hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(false);
            WindowInsetsController c = getWindow().getInsetsController();
            if (c != null) {
                c.hide(android.view.WindowInsets.Type.statusBars() |
                       android.view.WindowInsets.Type.navigationBars());
                c.setSystemBarsBehavior(
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        }
    }

    @Override
    public void onBackPressed() {
        if (web.canGoBack()) web.goBack();
        else super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        web.onResume();
        hideSystemUI();
    }

    @Override
    protected void onPause() {
        super.onPause();
        web.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        web.destroy();
    }
}
