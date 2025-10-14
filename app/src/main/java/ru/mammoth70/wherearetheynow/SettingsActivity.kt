package ru.mammoth70.wherearetheynow

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.slider.Slider
import com.google.android.material.textfield.TextInputEditText
import ru.mammoth70.wherearetheynow.MapUtil.MAP_TEXT
import ru.mammoth70.wherearetheynow.MapUtil.MAP_OPENSTREET
import ru.mammoth70.wherearetheynow.MapUtil.MAP_YANDEX
import ru.mammoth70.wherearetheynow.Util.COLOR_DYNAMIC_WALLPAPER
import ru.mammoth70.wherearetheynow.Util.COLOR_DYNAMIC_NO
import ru.mammoth70.wherearetheynow.Util.COLOR_DYNAMIC_RED
import ru.mammoth70.wherearetheynow.Util.COLOR_DYNAMIC_YELLOW
import ru.mammoth70.wherearetheynow.Util.COLOR_DYNAMIC_GREEN
import ru.mammoth70.wherearetheynow.Util.COLOR_DYNAMIC_BLUE
import ru.mammoth70.wherearetheynow.Util.MODE_NIGHT_YES
import ru.mammoth70.wherearetheynow.Util.MODE_NIGHT_NO
import ru.mammoth70.wherearetheynow.Util.MODE_NIGHT_FOLLOW_SYSTEM
import ru.mammoth70.wherearetheynow.Util.NAME_MY_PHONE
import ru.mammoth70.wherearetheynow.Util.NAME_THEME_COLOR
import ru.mammoth70.wherearetheynow.Util.NAME_THEME_MODE
import ru.mammoth70.wherearetheynow.Util.NAME_USE_SERVICE
import ru.mammoth70.wherearetheynow.MapUtil.NAME_MAP
import ru.mammoth70.wherearetheynow.MapUtil.NAME_MAP_ZOOM
import ru.mammoth70.wherearetheynow.MapUtil.NAME_MAP_TILT
import ru.mammoth70.wherearetheynow.MapUtil.NAME_MAP_CIRCLE
import ru.mammoth70.wherearetheynow.MapUtil.NAME_MAP_CIRCLE_RADIUS

class SettingsActivity : AppCompatActivity() {
    // Activity показывает и позволяет изменять настройки выбора карт.

    companion object {
        const val INTENT_EXTRA_RESULT: String = "refresh"
    }

    private var edMyPhone: TextInputEditText? = null
    private var checkBoxService: CheckBox? = null
    private var checkBoxCircle: CheckBox? = null
    private var lbMapZoom: TextView? = null
    private var sliderMapZoom: Slider? = null
    private var lbMapTilt: TextView? = null
    private var sliderMapTilt: Slider? = null
    private var lbCircleRadius: TextView? = null
    private var sliderCircleRadius: Slider? = null

    private var selectedMapTemp = 0
    private var selectedModeColorTemp = 0
    private var selectedModeNightTemp = 0

    @SuppressLint("NonConstantResourceId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.settings)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top,
                systemBars.right, systemBars.bottom)
            insets
        }

        val tvName = findViewById<TextView>(R.id.tvTitle)
        checkBoxService = findViewById(R.id.checkBoxService)
        checkBoxService!!.setChecked(Util.useService)
        checkBoxCircle = findViewById(R.id.checkBoxCircle)
        checkBoxCircle!!.setChecked(MapUtil.selectedMapCircle)
        lbMapZoom = findViewById(R.id.lbMapZoom)
        sliderMapZoom = findViewById(R.id.sliderMapZoom)
        sliderMapZoom!!.value = MapUtil.selectedMapZoom
        lbMapTilt = findViewById(R.id.lbMapTilt)
        sliderMapTilt = findViewById(R.id.sliderMapTilt)
        sliderMapTilt!!.value = MapUtil.selectedMapTilt
        lbCircleRadius = findViewById(R.id.lbCircleRadius)
        sliderCircleRadius = findViewById(R.id.sliderCircleRadius)
        sliderCircleRadius!!.value = MapUtil.selectedMapCircleRadius
        edMyPhone = findViewById(R.id.myphone)
        if (Util.myphone != "") {
            edMyPhone!!.setText(Util.myphone)
        }
        tvName.setText(R.string.titleSettings)

        selectedMapTemp = MapUtil.selectedMap
        // получаем объект RadioGroup переключателя карт
        val radioMap = findViewById<RadioGroup>(R.id.radioMap)
        when (selectedMapTemp) {
            MAP_TEXT -> {
                radioMap.check(R.id.text)
                lbMapZoom!!.visibility = View.GONE
                sliderMapZoom!!.visibility = View.GONE
                lbMapTilt!!.visibility = View.GONE
                sliderMapTilt!!.visibility = View.GONE
                checkBoxCircle!!.visibility = View.GONE
                lbCircleRadius!!.visibility = View.GONE
                sliderCircleRadius!!.visibility = View.GONE
            }
            MAP_OPENSTREET -> {
                radioMap.check(R.id.OpenStreet)
                lbMapZoom!!.visibility = View.VISIBLE
                sliderMapZoom!!.visibility = View.VISIBLE
                lbMapTilt!!.visibility = View.GONE
                sliderMapTilt!!.visibility = View.GONE
                checkBoxCircle!!.visibility = View.GONE
                lbCircleRadius!!.visibility = View.GONE
                sliderCircleRadius!!.visibility = View.GONE
            }
            MAP_YANDEX -> {
                radioMap.check(R.id.Yandex)
                lbMapZoom!!.visibility = View.VISIBLE
                sliderMapZoom!!.visibility = View.VISIBLE
                lbMapTilt!!.visibility = View.VISIBLE
                sliderMapTilt!!.visibility = View.VISIBLE
                checkBoxCircle!!.visibility = View.VISIBLE
                if (checkBoxCircle!!.isChecked) {
                    lbCircleRadius!!.visibility = View.VISIBLE
                    sliderCircleRadius!!.visibility = View.VISIBLE
                }
            }
            else -> {}
        }

        // Обработка переключения состояния переключателя карт
        radioMap.setOnCheckedChangeListener { radiogroup: RadioGroup?, id: Int ->
            // получаем выбранную кнопку
            when (id) {
                R.id.text -> {
                    selectedMapTemp = MAP_TEXT
                    lbMapZoom!!.visibility = View.GONE
                    sliderMapZoom!!.visibility = View.GONE
                    lbMapTilt!!.visibility = View.GONE
                    sliderMapTilt!!.visibility = View.GONE
                    checkBoxCircle!!.visibility = View.GONE
                    lbCircleRadius!!.visibility = View.GONE
                    sliderCircleRadius!!.visibility = View.GONE
                }
                R.id.OpenStreet -> {
                    selectedMapTemp = MAP_OPENSTREET
                    lbMapZoom!!.visibility = View.VISIBLE
                    sliderMapZoom!!.visibility = View.VISIBLE
                    lbMapTilt!!.visibility = View.GONE
                    sliderMapTilt!!.visibility = View.GONE
                    checkBoxCircle!!.visibility = View.GONE
                    lbCircleRadius!!.visibility = View.GONE
                    sliderCircleRadius!!.visibility = View.GONE
                }
                R.id.Yandex -> {
                    selectedMapTemp = MAP_YANDEX
                    lbMapZoom!!.visibility = View.VISIBLE
                    sliderMapZoom!!.visibility = View.VISIBLE
                    lbMapTilt!!.visibility = View.VISIBLE
                    sliderMapTilt!!.visibility = View.VISIBLE
                    checkBoxCircle!!.visibility = View.VISIBLE
                    if (checkBoxCircle!!.isChecked) {
                        lbCircleRadius!!.visibility = View.VISIBLE
                        sliderCircleRadius!!.visibility = View.VISIBLE
                    }
                }
            }
        }

        // Обработка переключения состояния чекера круга
        checkBoxCircle!!.setOnCheckedChangeListener { buttonView: CompoundButton?,
                                                      isChecked: Boolean ->
            if (isChecked) {
                lbCircleRadius!!.visibility = View.VISIBLE
                sliderCircleRadius!!.visibility = View.VISIBLE
            } else {
                lbCircleRadius!!.visibility = View.GONE
                sliderCircleRadius!!.visibility = View.GONE
            }
        }

        selectedModeColorTemp = Util.themeColor
        // получаем объект RadioGroup переключателя цвета темы
        val radioThemeColor = findViewById<RadioGroup>(R.id.radioThemeColor)
        when (selectedModeColorTemp) {
            COLOR_DYNAMIC_WALLPAPER -> radioThemeColor.check(R.id.themeDynamic)
            COLOR_DYNAMIC_NO -> radioThemeColor.check(R.id.themeDefault)
            COLOR_DYNAMIC_RED -> radioThemeColor.check(R.id.themeRed)
            COLOR_DYNAMIC_YELLOW -> radioThemeColor.check(R.id.themeYellow)
            COLOR_DYNAMIC_GREEN -> radioThemeColor.check(R.id.themeGreen)
            COLOR_DYNAMIC_BLUE -> radioThemeColor.check(R.id.themeBlue)
        }

        // Обработка переключения состояния переключателя режимов цвета
        radioThemeColor.setOnCheckedChangeListener { radiogroup: RadioGroup?, id: Int ->
            // получаем выбранную кнопку
            when (id) {
                R.id.themeDynamic -> selectedModeColorTemp = COLOR_DYNAMIC_WALLPAPER
                R.id.themeDefault -> selectedModeColorTemp = COLOR_DYNAMIC_NO
                R.id.themeRed -> selectedModeColorTemp = COLOR_DYNAMIC_RED
                R.id.themeYellow -> selectedModeColorTemp = COLOR_DYNAMIC_YELLOW
                R.id.themeGreen -> selectedModeColorTemp = COLOR_DYNAMIC_GREEN
                R.id.themeBlue -> selectedModeColorTemp = COLOR_DYNAMIC_BLUE
            }
        }

        selectedModeNightTemp = Util.themeMode
        // получаем объект RadioGroup переключателя режимов темы
        val radioTheme = findViewById<RadioGroup>(R.id.radioTheme)
        when (selectedModeNightTemp) {
            MODE_NIGHT_YES -> radioTheme.check(R.id.themeNight)
            MODE_NIGHT_NO -> radioTheme.check(R.id.themeDay)
            else -> radioTheme.check(R.id.themeSystem)
        }

        // Обработка переключения состояния переключателя режимов темы
        radioTheme.setOnCheckedChangeListener { radiogroup: RadioGroup?, id: Int ->
            // получаем выбранную кнопку
            when (id) {
                R.id.themeNight -> selectedModeNightTemp = MODE_NIGHT_YES
                R.id.themeDay -> selectedModeNightTemp = MODE_NIGHT_NO
                R.id.themeSystem -> selectedModeNightTemp = MODE_NIGHT_FOLLOW_SYSTEM
            }
        }
    }

    fun onActionClicked(@Suppress("UNUSED_PARAMETER") ignored: View?) {
        // Функция - обработчик кнопки "сохранить настройки".
        val settings = getSharedPreferences(Util.NAME_SETTINGS, MODE_PRIVATE)
        val prefEditor = settings.edit()

        Util.myphone = edMyPhone!!.getText().toString()
        Util.myphone = Util.myphone.replace(UserActivity.REGEXP_CLEAR_PHONE.toRegex(),
            "")
        if (Util.myphone != "") {
            prefEditor.putString(NAME_MY_PHONE, Util.myphone)
        }

        val action = (Util.themeColor != selectedModeColorTemp)
        if (action) {
            Util.setAppThemeColor(App.application!!, selectedModeColorTemp,
                true)
        }
        Util.themeColor = selectedModeColorTemp
        prefEditor.putInt(NAME_THEME_COLOR, Util.themeColor)

        if (Util.themeMode != selectedModeNightTemp) {
            Util.themeMode(selectedModeNightTemp)
        }
        Util.themeMode = selectedModeNightTemp
        prefEditor.putInt(NAME_THEME_MODE, Util.themeMode)

        MapUtil.selectedMap = selectedMapTemp
        prefEditor.putInt(NAME_MAP, MapUtil.selectedMap)
        MapUtil.selectedMapZoom = sliderMapZoom!!.value
        prefEditor.putFloat(NAME_MAP_ZOOM, MapUtil.selectedMapZoom)
        MapUtil.selectedMapTilt = sliderMapTilt!!.value
        prefEditor.putFloat(NAME_MAP_TILT, MapUtil.selectedMapTilt)
        MapUtil.selectedMapCircle = checkBoxCircle!!.isChecked
        prefEditor.putBoolean(NAME_MAP_CIRCLE, MapUtil.selectedMapCircle)
        MapUtil.selectedMapCircleRadius = sliderCircleRadius!!.value
        prefEditor.putFloat(NAME_MAP_CIRCLE_RADIUS, MapUtil.selectedMapCircleRadius)

        Util.useService = checkBoxService!!.isChecked
        prefEditor.putBoolean(NAME_USE_SERVICE, Util.useService)

        prefEditor.apply()

        val intent = Intent()
        intent.putExtra(INTENT_EXTRA_RESULT, action)
        setResult(RESULT_OK, intent)
        finish()
    }

}