package ru.mammoth70.wherearetheynow;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Locale;

public class BrowserActivity extends LocationActivity {
    // Activity выводит карту с геолокацией, переданной через intent.
    // url карты определяется данными, переданными через intent.

    private WebView webView;
    private int mapChange;
    private float mapZoom;
    private static final String URL_OPENSTREET =
            "https://www.openstreetmap.org/?mlat=%1$.6f&mlon=%2$.6f#map=%3$.0f/%1$.6f/%2$.6f";

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Метод вызывается при создании Activity.
        // Из intent получается uri и выводится в браузер.
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_browser);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.browser), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        createFrameTitle(this);

        webView = findViewById(R.id.webView);
        webView.setWebViewClient(new WebViewClient());
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        mapChange = intent.getIntExtra(Util.INTENT_EXTRA_MAP, MapUtil.MAP_OPENSTREET);
        mapZoom = intent.getFloatExtra(Util.INTENT_EXTRA_MAP_ZOOM, MapUtil.MAP_ZOOM_DEFAULT);

        reloadMapFromPoint(this, startRecord);
    }

    @Override
    protected  void reloadMapFromPoint(Context context, PointRecord rec) {
        // Метод выводит uri по PointRecord.
        String uri;
        if (mapChange == MapUtil.MAP_OPENSTREET) {
            uri = String.format(Locale.US, URL_OPENSTREET, rec.latitude, rec.longitude, mapZoom);
            webView.loadUrl(uri);
        } else {
            finish();
        }
    }

}