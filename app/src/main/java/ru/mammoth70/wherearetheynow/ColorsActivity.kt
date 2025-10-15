package ru.mammoth70.wherearetheynow

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.graphics.toColorInt
import ru.mammoth70.wherearetheynow.AppColors.getColorAlpha16
import ru.mammoth70.wherearetheynow.AppColors.getColorMarker
import ru.mammoth70.wherearetheynow.Util.INTENT_EXTRA_COLOR

class ColorsActivity : AppCompatActivity() {
    // Activity выбора цвета.

    companion object {
        private const val COLUMN_COLOR = "color"
        private const val COLUMN_BACK = "background"
    }

    private val tvTitle: TextView by lazy { findViewById(R.id.tvTitle)}
    private val lvSimple: ListView by lazy { findViewById(R.id.lvColorsSimple) }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Функция вызывается при создании Activity.
        // Подготавливаются структуры данных для вывода списка цветов.

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_colors)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.colors)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top,
                systemBars.right, systemBars.bottom)
            insets
        }

        tvTitle.setText(R.string.titleColors)
        lvSimple.setAdapter(simpleAdapter)
        lvSimple.isClickable = true
        lvSimple.setOnItemClickListener { parent: AdapterView<*>?, view: View?,
                                          position: Int, id: Long ->
            val selectedItem = AppColors.colors[position]
            val intent = Intent()
            intent.putExtra(INTENT_EXTRA_COLOR, selectedItem)
            setResult(RESULT_OK, intent)
            finish()
        }
    }

    private val simpleAdapter: SimpleAdapter
        get() {
            // Функция создаёт и заполняет SimpleAdapter.
            val data = ArrayList<Map<String?, Any?>?>(AppColors.colors.size)
            for (color in AppColors.colors) {
                val m = HashMap<String?, Any?>()
                m.put(COLUMN_COLOR, color)
                m.put(COLUMN_BACK, color)
                data.add(m)
            }
            val from = arrayOf<String?>(
                COLUMN_COLOR,
                COLUMN_BACK
            )
            val to = intArrayOf(R.id.itemColorLabel, R.id.itemColorLayout)
            val sAdapter = SimpleAdapter(this, data, R.layout.item_color, from, to)
            sAdapter.viewBinder = ViewBinder()
            return sAdapter
        }

    private class ViewBinder : SimpleAdapter.ViewBinder {
        // Класс обрабатывает форматирование вывода на экран списка цветов.
        override fun setViewValue(view: View, data: Any?, textRepresentation: String?): Boolean {
            when (view.id) {
                R.id.itemColorLayout -> {
                    view.setBackgroundColor(getColorAlpha16(data as String?).toColorInt())
                    return true
                }
                R.id.itemColorLabel -> {
                    view.setBackgroundResource(getColorMarker(data as String?))
                    return true
                }
                else -> return false
            }
        }
    }

}