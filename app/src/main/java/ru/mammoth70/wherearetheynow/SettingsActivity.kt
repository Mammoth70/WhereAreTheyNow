package ru.mammoth70.wherearetheynow

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.slider.Slider
import com.google.android.material.textfield.TextInputEditText

class SettingsActivity : AppActivity() {
    // Activity управляет настройками.

    override val idLayout = R.layout.activity_settings
    override val idActivity = R.id.frameSettingsActivity

    companion object {
        const val INTENT_THEME_COLOR_CHANGED = "ThemeColorChanged"
        const val INTENT_PHONE_CHANGED = "PhoneChanged"
    }

    private val edMyPhone: TextInputEditText by lazy { findViewById(R.id.myphone) }

    private val radioThemeColor: RadioGroup by lazy { findViewById(R.id.radioThemeColor) }
    private val radioTheme: RadioGroup by lazy { findViewById(R.id.radioTheme) }

    private val radioMap: RadioGroup by lazy { findViewById(R.id.radioMap) }

    private val lbMapZoom: TextView by lazy { findViewById(R.id.lbMapZoom) }
    private val sliderMapZoom: Slider by lazy { findViewById(R.id.sliderMapZoom) }
    private val lbMapTilt: TextView by lazy { findViewById(R.id.lbMapTilt) }
    private val sliderMapTilt: Slider by lazy { findViewById(R.id.sliderMapTilt) }
    private val checkBoxCircle: MaterialSwitch by lazy { findViewById(R.id.checkBoxCircle) }
    private val lbCircleRadius: TextView by lazy { findViewById(R.id.lbCircleRadius) }
    private val sliderCircleRadius: Slider by lazy { findViewById(R.id.sliderCircleRadius) }


    private val checkBoxService: MaterialSwitch by lazy { findViewById(R.id.checkBoxService) }

    private val sliderColorsSpanCount: Slider by lazy { findViewById(R.id.sliderColorsSpanCount) }

    private val btnAction: Button by lazy { findViewById(R.id.btnAction) }


    override fun onCreate(savedInstanceState: Bundle?) {
        // Функция вызывается при создании Activity.
        // Весь код управления настройками здесь.

        super.onCreate(savedInstanceState)

        topAppBar.setTitle(R.string.titleSettings)
        topAppBar.setNavigationOnClickListener {
            finish()
        }


        // Заполнение поля "мой номер телефона".
        if (!DataRepository.myPhone.isEmpty()) {
            edMyPhone.setText(DataRepository.myPhone)
        }


        // Назначение кнопки переключателя цвета темы.
        var selectedModeColorTemp = SettingsManager.themeColor
        when (selectedModeColorTemp) {
            COLOR_DYNAMIC_WALLPAPER -> radioThemeColor.check(R.id.themeDynamic)
            COLOR_DYNAMIC_NO -> radioThemeColor.check(R.id.themeDefault)
            COLOR_DYNAMIC_RED -> radioThemeColor.check(R.id.themeRed)
            COLOR_DYNAMIC_YELLOW -> radioThemeColor.check(R.id.themeYellow)
            COLOR_DYNAMIC_GREEN -> radioThemeColor.check(R.id.themeGreen)
            COLOR_DYNAMIC_BLUE -> radioThemeColor.check(R.id.themeBlue)
            COLOR_DYNAMIC_M3 -> radioThemeColor.check(R.id.themeM3)
        }

        // Обработчик переключения состояния переключателя цвета темы.
        radioThemeColor.setOnCheckedChangeListener { group, id ->
            val radioButton = group.findViewById<RadioButton>(id)
            if (radioButton != null && radioButton.isPressed) {
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
        }

        // Назначение кнопки переключателя режимов темы.
        var selectedModeNightTemp = SettingsManager.themeMode
        when (selectedModeNightTemp) {
            MODE_NIGHT_YES -> radioTheme.check(R.id.themeNight)
            MODE_NIGHT_NO -> radioTheme.check(R.id.themeDay)
            else -> radioTheme.check(R.id.themeSystem)
        }

        // Обработчик переключения состояния переключателя режимов темы.
        radioTheme.setOnCheckedChangeListener { group, id ->
            val radioButton = group.findViewById<RadioButton>(id)
            if (radioButton != null && radioButton.isPressed) {
                when (id) {
                    R.id.themeNight -> selectedModeNightTemp = MODE_NIGHT_YES
                    R.id.themeDay -> selectedModeNightTemp = MODE_NIGHT_NO
                    R.id.themeSystem -> selectedModeNightTemp = MODE_NIGHT_FOLLOW_SYSTEM
                }
            }
        }


        // Назначение кнопки переключателя карт и видимости настроек карты, в зависимости от выбранной.
        var selectedMapTemp = SettingsManager.selectedMap
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

        // Обработчик переключения состояния переключателя карт.
        radioMap.setOnCheckedChangeListener { group, id ->
            val radioButton = group.findViewById<RadioButton>(id)
            if (radioButton != null && radioButton.isPressed) {
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
        }


        // Назначение слайдера масштаба карты.
        sliderMapZoom.value = SettingsManager.selectedMapZoom


        // Назначение слайдера наклона камеры Яндекс-карты.
        sliderMapTilt.value = SettingsManager.selectedMapTilt


        // Назначение переключателя показа кругов вокруг метки на Яндекс-карте.
        checkBoxCircle.setChecked(SettingsManager.selectedMapCircle)

        // Обработчик переключения состояния чекера круга.
        checkBoxCircle.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed) {
                if (isChecked) {
                    lbCircleRadius.visibility = View.VISIBLE
                    sliderCircleRadius.visibility = View.VISIBLE
                } else {
                    lbCircleRadius.visibility = View.GONE
                    sliderCircleRadius.visibility = View.GONE
                }
            }
        }

        // Назначение слайдера диаметра кругов вокруг метки на Яндекс-карте.
        sliderCircleRadius.value = SettingsManager.selectedMapCircleRadius
        if (SettingsManager.selectedMapCircle) {
            lbCircleRadius.visibility = View.VISIBLE
            sliderCircleRadius.visibility = View.VISIBLE
        } else {
            lbCircleRadius.visibility = View.GONE
            sliderCircleRadius.visibility = View.GONE
        }


        // Назначение переключателя включения сервиса.
        checkBoxService.setChecked(SettingsManager.useService)


        // Назначение слайдера, определяющего количество колонок в списке выбора метки.
        sliderColorsSpanCount.value = SettingsManager.colorsSpanCount.toFloat()


        // Обработчик кнопки "сохранить настройки".
        btnAction.setOnClickListener { _ ->

            // Мой телефон.
            val myPhoneOld = DataRepository.myPhone
            DataRepository.myPhone = edMyPhone.text.toString()
                .replace(UserActivity.REGEXP_CLEAR_PHONE.toRegex(), "")
            val isPhoneChanged = (DataRepository.myPhone != myPhoneOld)

            // Цвет темы.
            val isThemeColorChanged = (SettingsManager.themeColor != selectedModeColorTemp)
            if (isThemeColorChanged) {
                setAppThemeColor(applicationContext as App, selectedModeColorTemp,
                    true)
            }
            SettingsManager.themeColor = selectedModeColorTemp

            // Режим темы.
            if (SettingsManager.themeMode != selectedModeNightTemp) {
                themeMode(selectedModeNightTemp)
            }
            SettingsManager.themeMode = selectedModeNightTemp

            // Выбор карты.
            SettingsManager.selectedMap = selectedMapTemp

            // Настройки карты.
            SettingsManager.selectedMapZoom = sliderMapZoom.value
            SettingsManager.selectedMapTilt = sliderMapTilt.value
            SettingsManager.selectedMapCircle = checkBoxCircle.isChecked
            SettingsManager.selectedMapCircleRadius = sliderCircleRadius.value

            // Флаг работы сервиса.
            SettingsManager.useService = checkBoxService.isChecked

            // Количество колонок выбора цвета.
            SettingsManager.colorsSpanCount = sliderColorsSpanCount.value.toInt()

            // Intent информирует об изменении настроек, требующих действий в MainActivity.
            val intent = Intent().apply {
                putExtra(INTENT_THEME_COLOR_CHANGED, isThemeColorChanged)
                putExtra(INTENT_PHONE_CHANGED, isPhoneChanged)
            }
            setResult(RESULT_OK, intent)
            finish()
        }
    }
}