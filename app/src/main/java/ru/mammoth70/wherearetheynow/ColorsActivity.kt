package ru.mammoth70.wherearetheynow

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import ru.mammoth70.wherearetheynow.Util.INTENT_EXTRA_COLOR

class ColorsActivity : AppCompatActivity() {
    // Activity выбора цвета.

    private val topAppBar: MaterialToolbar by lazy { findViewById(R.id.topAppBar) }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Функция вызывается при создании Activity.
        // Обработка данных для подготовки списка меток.
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_colors)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.colors))
        { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top,
                systemBars.right, systemBars.bottom)
            insets
        }

        topAppBar.setTitle(R.string.titleColors)
        topAppBar.setNavigationOnClickListener {
            finish()
        }

        val colorsAdapter = ColorsAdapter()
        colorsAdapter.setOnClickListener(object: ColorsAdapter.OnClickListener {
            override fun onClick(position: Int) {
                val selectedItem = AppColors.colors[position]
                val intent = Intent()
                intent.putExtra(INTENT_EXTRA_COLOR, selectedItem)
                setResult(RESULT_OK, intent)
                finish()
            }
        })
        val recyclerView: RecyclerView = findViewById(R.id.lvColorsRecicler)
        recyclerView.adapter = colorsAdapter
        recyclerView.isClickable = true
    }

}