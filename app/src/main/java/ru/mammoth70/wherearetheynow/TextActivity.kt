package ru.mammoth70.wherearetheynow

import android.content.Context
import android.os.Bundle
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

class TextActivity : LocationActivity() {
    // Activity выводит текст с геолокацией, переданной через intent.

    override val idLayout = R.layout.activity_text
    override val idActivity = R.id.frameTextActivity

    private val tvLatitude: TextView by lazy { findViewById(R.id.tvLatitude) }
    private val tvLongitude: TextView by lazy { findViewById(R.id.tvLongitude) }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Функция вызывается при создании Activity.
        // Получение из intent данных.
        // Подготовка данных для вывода таблицы контактов с координатами.
        super.onCreate(savedInstanceState)

        val geoAdapter = GeoAdapter()
        val recyclerView: RecyclerView = findViewById(R.id.itemGeoRecycler)
        recyclerView.adapter = geoAdapter

        reloadMapFromPoint(this, startRecord)
    }

    override fun reloadMapFromPoint(context: Context, rec: PointRecord) {
        // Функция выводит текстом широту и долготу по PointRecord.
        tvLatitude.text = String.format(Locale.US,
            PointRecord.FORMAT_DOUBLE, rec.latitude)
        tvLongitude.text = String.format(Locale.US,
            PointRecord.FORMAT_DOUBLE, rec.longitude)
    }

}