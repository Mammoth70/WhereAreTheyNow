package ru.mammoth70.wherearetheynow

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.Locale
import ru.mammoth70.wherearetheynow.Util.INTENT_EXTRA_MAP
import ru.mammoth70.wherearetheynow.Util.INTENT_EXTRA_MAP_ZOOM
import ru.mammoth70.wherearetheynow.MapUtil.MAP_OPENSTREET
import ru.mammoth70.wherearetheynow.MapUtil.MAP_ZOOM_DEFAULT

class BrowserActivity : LocationActivity() {
    // Activity выводит карту с геолокацией, переданной через intent.
    // url карты определяется данными, переданными через intent.

    companion object {
        private const val URL_OPENSTREET =
            "https://www.openstreetmap.org/?mlat=%1$.6f&mlon=%2$.6f#map=%3$.0f/%1$.6f/%2$.6f"
    }

    private var webView: WebView? = null
    private var mapChange = 0
    private var mapZoom = 0f

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        // Метод вызывается при создании Activity.
        // Из intent получается uri и выводится в браузер.
        super.onCreate(savedInstanceState)
        this.enableEdgeToEdge()
        setContentView(R.layout.activity_browser)
        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.browser)
        ) { v: View?, insets: WindowInsetsCompat? ->
            val systemBars = insets!!.getInsets(WindowInsetsCompat.Type.systemBars())
            v!!.setPadding(systemBars.left, systemBars.top,
                systemBars.right, systemBars.bottom)
            insets
        }
        createFrameTitle(this)

        webView = findViewById(R.id.webView)
        webView!!.setWebViewClient(WebViewClient())
        val webSettings = webView!!.getSettings()
        webSettings.javaScriptEnabled = true
        mapChange = intent!!.getIntExtra(INTENT_EXTRA_MAP,
            MAP_OPENSTREET)
        mapZoom = intent!!.getFloatExtra(INTENT_EXTRA_MAP_ZOOM,
            MAP_ZOOM_DEFAULT)

        reloadMapFromPoint(this, startRecord!!)
    }

    override fun reloadMapFromPoint(context: Context, rec: PointRecord) {
        // Метод выводит uri по PointRecord.
        val uri: String?
        if (mapChange == MAP_OPENSTREET) {
            uri = String.format(Locale.US, URL_OPENSTREET,
                rec.latitude, rec.longitude, mapZoom)
            webView!!.loadUrl(uri)
        } else {
            finish()
        }
    }

}