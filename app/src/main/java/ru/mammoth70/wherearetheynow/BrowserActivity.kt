package ru.mammoth70.wherearetheynow

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.WebView
import android.webkit.WebViewClient
import java.util.Locale

class BrowserActivity : LocationActivity() {
    // Activity выводит карту с геолокацией, переданной через intent.
    // Url карты определяется данными, переданными через intent.

    override val idLayout = R.layout.activity_browser
    override val idActivity = R.id.frameBrowserActivity

    companion object {
        private const val URL_OPENSTREET =
            "https://www.openstreetmap.org/?mlat=%1$.6f&mlon=%2$.6f#map=%3$.0f/%1$.6f/%2$.6f"
    }

    private val webView: WebView by lazy { findViewById(R.id.itemWebView) }

    @SuppressLint("SetJavaScriptEnabled")
    override fun initMap(context: Context) {
        // Функция делает начальную настройку карты.

        webView.setWebViewClient(WebViewClient())
        val webSettings = webView.getSettings()
        webSettings.javaScriptEnabled = true
    }

    override fun reloadMapFromPoint(context: Context, rec: PointRecord) {
        // Функция выводит uri по PointRecord.

        val uri: String?
        if (SettingsManager.selectedMap == MAP_OPENSTREET) {
            uri = String.format(Locale.US, URL_OPENSTREET,
                rec.latitude, rec.longitude, SettingsManager.selectedMapZoom)
            webView.loadUrl(uri)
        } else {
            finish()
        }
    }

}