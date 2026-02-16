package ru.mammoth70.wherearetheynow

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.appbar.MaterialToolbar

abstract class AppActivity : AppCompatActivity() {
    // Абстрактный класс для создания Activity приложения.

    @get:LayoutRes
    protected abstract val idLayout : Int
    @get:IdRes
    protected abstract val idActivity : Int

    protected open val topAppBar: MaterialToolbar by lazy { findViewById(R.id.topAppBar) }


    override fun onCreate(savedInstanceState: Bundle?) {
        // Функция вызывается при создании Activity.
        // Может (и даже должна) быть переопределена.

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(idLayout)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(idActivity))
        { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top,
                systemBars.right, systemBars.bottom)
            insets
        }
    }

}