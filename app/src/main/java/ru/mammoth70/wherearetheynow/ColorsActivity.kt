package ru.mammoth70.wherearetheynow

import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.mammoth70.wherearetheynow.UserActivity.Companion.INTENT_EXTRA_COLOR

class ColorsActivity : AppActivity() {
    // Activity выбора цвета.

    override val idLayout = R.layout.activity_colors
    override val idActivity = R.id.frameColorsActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        // Функция вызывается при создании Activity.
        // Обработка данных для подготовки списка меток.

        super.onCreate(savedInstanceState)

        topAppBar.setTitle(R.string.titleColors)
        topAppBar.setNavigationOnClickListener {
            finish()
        }

        val colorsAdapter = ColorsAdapter(::onClickViewItem)
        val recyclerView: RecyclerView = findViewById(R.id.itemColorsRecycler)
        (recyclerView.layoutManager as GridLayoutManager).spanCount = SettingsManager.colorsSpanCount
        recyclerView.apply {
            adapter = colorsAdapter
            setHasFixedSize(true)
        }
    }

    fun onClickViewItem(position: Int) {
        // Функция вызывается по клику на элемент списка.

        val selectedItem = PinColors.Color.entries[position].hex
        val intent = Intent()
        intent.putExtra(INTENT_EXTRA_COLOR, selectedItem)
        setResult(RESULT_OK, intent)
        finish()
    }
}