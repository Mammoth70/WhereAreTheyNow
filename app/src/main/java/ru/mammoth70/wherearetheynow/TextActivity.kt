package ru.mammoth70.wherearetheynow

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.graphics.toColorInt
import java.util.Locale
import ru.mammoth70.wherearetheynow.AppColors.getColorAlpha16

class TextActivity : LocationActivity() {
    // Activity выводит текст с геолокацией, переданной через intent.

    companion object {
        private const val COLUMN_NAME = "name"
        private const val COLUMN_BACK = "background"
        private const val COLUMN_LATITUDE = "latitude"
        private const val COLUMN_LONGITUDE = "longitude"
        private const val COLUMN_DATE = "date"
    }

    private lateinit var data: ArrayList<MutableMap<String?, Any?>?>
    private val tvLatitude: TextView by lazy { findViewById(R.id.tvLatitude) }
    private val tvLongitude: TextView by lazy { findViewById(R.id.tvLongitude) }
    private val lvSimple: ListView by lazy { findViewById(R.id.lvGeoSimple) }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Функция вызывается при создании Activity.
	// Получение из intent данных.
        // Подготовка данных для вывода таблицы контактов с координатами.
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_text)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.text)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top,
                systemBars.right, systemBars.bottom)
            insets
        }

        createFrameTitle(this)

        val sAdapter = simpleAdapter
        lvSimple.setAdapter(sAdapter)

        reloadMapFromPoint(this, startRecord)
    }

    private fun refreshData() {
        // Функция обновляет данные для списка контактов с координатами.
        data.clear()
        for (phone in Util.phones) {
            if (Util.phone2record.containsKey(phone)) {
                val value = Util.phone2record[phone]
                value?.let {
                    val m: MutableMap<String?, Any?> = HashMap()
                    m.put(COLUMN_NAME, Util.phone2name[phone])
                    m.put(COLUMN_BACK, Util.phone2color[phone])
                    m.put(
                        COLUMN_LATITUDE, String.format(
                            Locale.US, PointRecord.FORMAT_DOUBLE,
                            value.latitude
                        )
                    )
                    m.put(
                        COLUMN_LONGITUDE, String.format(
                            Locale.US, PointRecord.FORMAT_DOUBLE,
                            value.longitude
                        )
                    )
                    m.put(COLUMN_DATE, value.dateTime)
                    data.add(m)
                }
            }
        }
    }

    override fun reloadMapFromPoint(context: Context, rec: PointRecord) {
        // Функция выводит текстом широту и долготу по PointRecord.
        tvLatitude.text = String.format(Locale.US,
            PointRecord.FORMAT_DOUBLE, rec.latitude)
        tvLongitude.text = String.format(Locale.US,
            PointRecord.FORMAT_DOUBLE, rec.longitude)
    }

    private val simpleAdapter: SimpleAdapter
        get() {
            // Функция создаёт и заполняет SimpleAdapter.
            data = ArrayList(Util.phone2record.size)
            refreshData()
            val from = arrayOf<String?>(
                COLUMN_NAME,
                COLUMN_BACK,
                COLUMN_LATITUDE,
                COLUMN_LONGITUDE,
                COLUMN_DATE
            )
            val to = intArrayOf(
                R.id.itemUserName,
                R.id.itemUserGeoLayout,
                R.id.itemLattitude,
                R.id.itemLongitude,
                R.id.itemDate
            )
            val sAdapter = SimpleAdapter(this, data, R.layout.item_geo, from, to)
            sAdapter.viewBinder = ViewBinder()
            return sAdapter
        }

    private class ViewBinder : SimpleAdapter.ViewBinder {
        // Класс обрабатывает форматирование вывода на экран
        // списка контактов с координатами и датами получения геолокации.
        override fun setViewValue(
            view: View, data: Any?,
            textRepresentation: String?
        ): Boolean {
            val color: String?
            if (view.id == R.id.itemUserGeoLayout) {
                color = (data as String?)
                view.setBackgroundColor(getColorAlpha16(color).toColorInt())
                return true
            } else {
                return false
            }
        }
    }

}