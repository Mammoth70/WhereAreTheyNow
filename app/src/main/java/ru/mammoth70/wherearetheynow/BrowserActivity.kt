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
import ru.mammoth70.wherearetheynow.MapUtil.MAP_OPENSTREET

class BrowserActivity : LocationActivity() {
    // Activity выводит карту с геолокацией, переданной через intent.
    // url карты определяется данными, переданными через intent.

    companion object {
        private const val URL_OPENSTREET =
            "https://www.openstreetmap.org/?mlat=%1$.6f&mlon=%2$.6f#map=%3$.0f/%1$.6f/%2$.6f"
    }

    private val webView: WebView by lazy { findViewById(R.id.webView) }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        // Функция вызывается при создании Activity.
        // Получение из intent данных, создание на основе них uri.
        // Обработка данных для вывода uri через броузер.
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_browser)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.browser))
        { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top,
                systemBars.right, systemBars.bottom)
            insets
        }

        createFrameTitle(this)

        webView.setWebViewClient(WebViewClient())
        val webSettings = webView.getSettings()
        webSettings.javaScriptEnabled = true
        reloadMapFromPoint(this, startRecord)
    }

    override fun reloadMapFromPoint(context: Context, rec: PointRecord) {
        // Функция выводит uri по PointRecord.
        val uri: String?
        if (MapUtil.selectedMap == MAP_OPENSTREET) {
            uri = String.format(Locale.US, URL_OPENSTREET,
                rec.latitude, rec.longitude, MapUtil.selectedMapZoom)
            webView.loadUrl(uri)
        } else {
            finish()
        }
    }

    fun onCloseClicked(@Suppress("UNUSED_PARAMETER") ignored: View?) {
        // Функция - обработчик кнопки "назад".
        finish()
    }

}