package ru.mammoth70.wherearetheynow

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.RadioGroup
import android.widget.TextView
import com.google.android.material.slider.Slider
import com.google.android.material.textfield.TextInputEditText

class SettingsActivity : AppActivity() {
    // Activity управляет настройками.

    override val idLayout = R.layout.activity_settings
    override val idActivity = R.id.frameSettingsActivity

    companion object {
        const val INTENT_THEME_CHANGED = "themechanged"
        const val INTENT_PHONE_CHANGED = "phonechanged"
    }

    private val edMyPhone: TextInputEditText by lazy { findViewById(R.id.myphone) }
    private val checkBoxService: CheckBox by lazy { findViewById(R.id.checkBoxService) }
    private val sliderColorsSpanCount: Slider by lazy { findViewById(R.id.sliderColorsSpanCount) }

    private val checkBoxCircle: CheckBox by lazy { findViewById(R.id.checkBoxCircle) }
    private val lbMapZoom: TextView by lazy { findViewById(R.id.lbMapZoom) }
    private val sliderMapZoom: Slider by lazy { findViewById(R.id.sliderMapZoom) }
    private val lbMapTilt: TextView by lazy { findViewById(R.id.lbMapTilt) }
    private val sliderMapTilt: Slider by lazy { findViewById(R.id.sliderMapTilt) }
    private val lbCircleRadius: TextView by lazy { findViewById(R.id.lbCircleRadius) }
    private val sliderCircleRadius: Slider by lazy { findViewById(R.id.sliderCircleRadius) }

    private val radioMap: RadioGroup by lazy { findViewById(R.id.radioMap) }
    private val radioThemeColor: RadioGroup by lazy { findViewById(R.id.radioThemeColor) }
    private val radioTheme: RadioGroup by lazy { findViewById(R.id.radioTheme) }
    private val btnAction: Button by lazy { findViewById(R.id.btnAction) }

    private var selectedMapTemp = 0
    private var selectedModeColorTemp = 0
    private var selectedModeNightTemp = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        // Функция вызывается при создании Activity.
        // Весь код управления настройками здесь.

        super.onCreate(savedInstanceState)

        topAppBar.setTitle(R.string.titleSettings)
        topAppBar.setNavigationOnClickListener {
            finish()
        }

        checkBoxService.setChecked(SettingsManager.useService)
        sliderColorsSpanCount.value = SettingsManager.colorsSpanCount.toFloat()
        checkBoxCircle.setChecked(SettingsManager.selectedMapCircle)
        sliderMapZoom.value = SettingsManager.selectedMapZoom
        sliderMapTilt.value = SettingsManager.selectedMapTilt
        sliderCircleRadius.value = SettingsManager.selectedMapCircleRadius
        val myPhoneTemp = DataRepository.myPhone
        if (!DataRepository.myPhone.isEmpty()) {
            edMyPhone.setText(DataRepository.myPhone)
        }

        selectedMapTemp = SettingsManager.selectedMap
        // Назначение кнопки переключателя карт.
        when (selectedMapTemp) {
            MAP_TEXT -> {
                radioMap.check(R.id.frameTextActivity)
                lbMapZoom.visibility = View.GONE
                sliderMapZoom.visibility = View.GONE
                lbMapTilt.visibility = View.GONE
                sliderMapTilt.visibility = View.GONE
                checkBoxCircle.visibility = View.GONE
                lbCircleRadius.visibility = View.GONE
                sliderCircleRadius.visibility = View.GONE
            }

            MAP_OPENSTREET -> {
                radioMap.check(R.id.OpenStreet)
                lbMapZoom.visibility = View.VISIBLE
                sliderMapZoom.visibility = View.VISIBLE
                lbMapTilt.visibility = View.GONE
                sliderMapTilt.visibility = View.GONE
                checkBoxCircle.visibility = View.GONE
                lbCircleRadius.visibility = View.GONE
                sliderCircleRadius.visibility = View.GONE
            }

            MAP_YANDEX -> {
                radioMap.check(R.id.Yandex)
                lbMapZoom.visibility = View.VISIBLE
                sliderMapZoom.visibility = View.VISIBLE
                lbMapTilt.visibility = View.VISIBLE
                sliderMapTilt.visibility = View.VISIBLE
                checkBoxCircle.visibility = View.VISIBLE
                if (checkBoxCircle.isChecked) {
                    lbCircleRadius.visibility = View.VISIBLE
                    sliderCircleRadius.visibility = View.VISIBLE
                }
            }

            else -> {}
        }

        // Обработка переключения состояния переключателя карт.
        radioMap.setOnCheckedChangeListener { _: RadioGroup?, id: Int ->
            // Получение выбранной кнопки.
            when (id) {
                R.id.frameTextActivity -> {
                    selectedMapTemp = MAP_TEXT
                    lbMapZoom.visibility = View.GONE
                    sliderMapZoom.visibility = View.GONE
                    lbMapTilt.visibility = View.GONE
                    sliderMapTilt.visibility = View.GONE
                    checkBoxCircle.visibility = View.GONE
                    lbCircleRadius.visibility = View.GONE
                    sliderCircleRadius.visibility = View.GONE
                }

                R.id.OpenStreet -> {
                    selectedMapTemp = MAP_OPENSTREET
                    lbMapZoom.visibility = View.VISIBLE
                    sliderMapZoom.visibility = View.VISIBLE
                    lbMapTilt.visibility = View.GONE
                    sliderMapTilt.visibility = View.GONE
                    checkBoxCircle.visibility = View.GONE
                    lbCircleRadius.visibility = View.GONE
                    sliderCircleRadius.visibility = View.GONE
                }

                R.id.Yandex -> {
                    selectedMapTemp = MAP_YANDEX
                    lbMapZoom.visibility = View.VISIBLE
                    sliderMapZoom.visibility = View.VISIBLE
                    lbMapTilt.visibility = View.VISIBLE
                    sliderMapTilt.visibility = View.VISIBLE
                    checkBoxCircle.visibility = View.VISIBLE
                    if (checkBoxCircle.isChecked) {
                        lbCircleRadius.visibility = View.VISIBLE
                        sliderCircleRadius.visibility = View.VISIBLE
                    }
                }
            }
        }

        // Обработка переключения состояния чекера круга.
        checkBoxCircle.setOnCheckedChangeListener { _: CompoundButton?,
                                                    isChecked: Boolean ->
            if (isChecked) {
                lbCircleRadius.visibility = View.VISIBLE
                sliderCircleRadius.visibility = View.VISIBLE
            } else {
                lbCircleRadius.visibility = View.GONE
                sliderCircleRadius.visibility = View.GONE
            }
        }

        selectedModeColorTemp = SettingsManager.themeColor
        // Назначение кнопки переключателя цвета темы.
        when (selectedModeColorTemp) {
            COLOR_DYNAMIC_WALLPAPER -> radioThemeColor.check(R.id.themeDynamic)
            COLOR_DYNAMIC_NO -> radioThemeColor.check(R.id.themeDefault)
            COLOR_DYNAMIC_RED -> radioThemeColor.check(R.id.themeRed)
            COLOR_DYNAMIC_YELLOW -> radioThemeColor.check(R.id.themeYellow)
            COLOR_DYNAMIC_GREEN -> radioThemeColor.check(R.id.themeGreen)
            COLOR_DYNAMIC_BLUE -> radioThemeColor.check(R.id.themeBlue)
            COLOR_DYNAMIC_M3 -> radioThemeColor.check(R.id.themeM3)
        }

        // Обработка переключения состояния переключателя режимов цвета.
        radioThemeColor.setOnCheckedChangeListener { _: RadioGroup?, id: Int ->
            // Получение выбранной кнопки.
            when (id) {
                R.id.themeDynamic -> selectedModeColorTemp = COLOR_DYNAMIC_WALLPAPER
                R.id.themeDefault -> selectedModeColorTemp = COLOR_DYNAMIC_NO
                R.id.themeRed -> selectedModeColorTemp = COLOR_DYNAMIC_RED
                R.id.themeYellow -> selectedModeColorTemp = COLOR_DYNAMIC_YELLOW
                R.id.themeGreen -> selectedModeColorTemp = COLOR_DYNAMIC_GREEN
                R.id.themeBlue -> selectedModeColorTemp = COLOR_DYNAMIC_BLUE
                R.id.themeM3 -> selectedModeColorTemp = COLOR_DYNAMIC_M3
            }
        }

        selectedModeNightTemp = SettingsManager.themeMode
        // Назначение кнопки переключателя режимов темы.
        when (selectedModeNightTemp) {
            MODE_NIGHT_YES -> radioTheme.check(R.id.themeNight)
            MODE_NIGHT_NO -> radioTheme.check(R.id.themeDay)
            else -> radioTheme.check(R.id.themeSystem)
        }

        // Обработка переключения состояния переключателя режимов темы.
        radioTheme.setOnCheckedChangeListener { _: RadioGroup?, id: Int ->
            // Получение выбранной кнопки.
            when (id) {
                R.id.themeNight -> selectedModeNightTemp = MODE_NIGHT_YES
                R.id.themeDay -> selectedModeNightTemp = MODE_NIGHT_NO
                R.id.themeSystem -> selectedModeNightTemp = MODE_NIGHT_FOLLOW_SYSTEM
            }
        }

        btnAction.setOnClickListener { _ ->
            // Обработчик кнопки "сохранить настройки".

            DataRepository.myPhone = edMyPhone.text.toString()
                .replace(UserActivity.REGEXP_CLEAR_PHONE.toRegex(), "")
            val isPhoneChanged = (DataRepository.myPhone != myPhoneTemp)

            val isThemeChanged = (SettingsManager.themeColor != selectedModeColorTemp)
            if (isThemeChanged) {
                setAppThemeColor(applicationContext as App, selectedModeColorTemp,
                    true)
            }

            SettingsManager.themeColor = selectedModeColorTemp

            if (SettingsManager.themeMode != selectedModeNightTemp) {
                themeMode(selectedModeNightTemp)
            }
            SettingsManager.themeMode = selectedModeNightTemp

            SettingsManager.selectedMap = selectedMapTemp
            SettingsManager.selectedMapZoom = sliderMapZoom.value
            SettingsManager.selectedMapTilt = sliderMapTilt.value
            SettingsManager.selectedMapCircle = checkBoxCircle.isChecked
            SettingsManager.selectedMapCircleRadius = sliderCircleRadius.value

            SettingsManager.useService = checkBoxService.isChecked

            SettingsManager.colorsSpanCount = sliderColorsSpanCount.value.toInt()

            val intent = Intent().apply {
                putExtra(INTENT_THEME_CHANGED, isThemeChanged)
                putExtra(INTENT_PHONE_CHANGED, isPhoneChanged)
            }
            setResult(RESULT_OK, intent)
            finish()
        }
    }

}