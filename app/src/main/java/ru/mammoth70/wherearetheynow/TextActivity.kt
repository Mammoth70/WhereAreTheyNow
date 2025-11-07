package ru.mammoth70.wherearetheynow

import android.content.Context
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

class TextActivity : LocationActivity() {
    // Activity выводит текст с геолокацией, переданной через intent.

    override val idLayout = R.layout.activity_text
    override val idActivity = R.id.frameTextActivity

    private val tvLatitude: TextView by lazy { findViewById(R.id.tvLatitude) }
    private val tvLongitude: TextView by lazy { findViewById(R.id.tvLongitude) }

    override fun initMap(context: Context) {
        // Функция делает настройку recyclerView для вывода списка контактов с координатами.
        val geoAdapter = GeoAdapter()
        val recyclerView: RecyclerView = findViewById(R.id.itemGeoRecycler)
        recyclerView.adapter = geoAdapter
    }

    override fun reloadMapFromPoint(context: Context, rec: PointRecord) {
        // Функция выводит текстом широту и долготу по PointRecord.
        tvLatitude.text = String.format(Locale.US,
            PointRecord.FORMAT_DOUBLE, rec.latitude)
        tvLongitude.text = String.format(Locale.US,
            PointRecord.FORMAT_DOUBLE, rec.longitude)
    }

}