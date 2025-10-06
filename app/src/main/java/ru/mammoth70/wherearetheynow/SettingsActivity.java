package ru.mammoth70.wherearetheynow;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.slider.Slider;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {
    // Activity показывает и позволяет изменять настройки выбора карт.

    private TextInputEditText edMyPhone;
    private CheckBox checkBoxService;
    private CheckBox checkBoxCircle;
    private TextView lbMapZoom;
    private Slider sliderMapZoom;
    private TextView lbMapTilt;
    private Slider sliderMapTilt;
    private TextView lbCircleRadius;
    private Slider sliderCircleRadius;

    private int selectedMapTemp;
    private int selectedModeColorTemp;
    private int selectedModeNightTemp;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.settings), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        TextView tvName = findViewById(R.id.tvTitle);
        checkBoxService = findViewById(R.id.checkBoxService);
        checkBoxService.setChecked(Util.useService);
        checkBoxCircle = findViewById(R.id.checkBoxCircle);
        checkBoxCircle.setChecked(MapUtil.selectedMapCircle);
        lbMapZoom = findViewById(R.id.lbMapZoom);
        sliderMapZoom = findViewById(R.id.sliderMapZoom);
        sliderMapZoom.setValue(MapUtil.selectedMapZoom);
        lbMapTilt = findViewById(R.id.lbMapTilt);
        sliderMapTilt = findViewById(R.id.sliderMapTilt);
        sliderMapTilt.setValue(MapUtil.selectedMapTilt);
        lbCircleRadius = findViewById(R.id.lbCircleRadius);
        sliderCircleRadius = findViewById(R.id.sliderCircleRadius);
        sliderCircleRadius.setValue(MapUtil.selectedMapCircleRadius);
        edMyPhone = findViewById(R.id.myphone);
        if (!Objects.equals(Util.myphone, "")) {
            edMyPhone.setText(Util.myphone);
        }
        tvName.setText(R.string.titleSettings);

        selectedMapTemp = MapUtil.selectedMap;
        // получаем объект RadioGroup переключателя карт
        RadioGroup radioMap = findViewById(R.id.radioMap);
        switch (selectedMapTemp) {
            case MapUtil.MAP_TEXT:
                radioMap.check(R.id.text);
                lbMapZoom.setVisibility(GONE);
                sliderMapZoom.setVisibility(GONE);
                lbMapTilt.setVisibility(GONE);
                sliderMapTilt.setVisibility(GONE);
                checkBoxCircle.setVisibility(GONE);
                lbCircleRadius.setVisibility(GONE);
                sliderCircleRadius.setVisibility(GONE);
                break;
            case MapUtil.MAP_OPENSTREET:
                radioMap.check(R.id.OpenStreet);
                lbMapZoom.setVisibility(VISIBLE);
                sliderMapZoom.setVisibility(VISIBLE);
                lbMapTilt.setVisibility(GONE);
                sliderMapTilt.setVisibility(GONE);
                checkBoxCircle.setVisibility(GONE);
                lbCircleRadius.setVisibility(GONE);
                sliderCircleRadius.setVisibility(GONE);
                break;
            case MapUtil.MAP_YANDEX:
                radioMap.check(R.id.Yandex);
                lbMapZoom.setVisibility(VISIBLE);
                sliderMapZoom.setVisibility(VISIBLE);
                lbMapTilt.setVisibility(VISIBLE);
                sliderMapTilt.setVisibility(VISIBLE);
                checkBoxCircle.setVisibility(VISIBLE);
                if (checkBoxCircle.isChecked()) {
                    lbCircleRadius.setVisibility(VISIBLE);
                    sliderCircleRadius.setVisibility(VISIBLE);
                }
                break;
            default:
                break;
        }

        // Обработка переключения состояния переключателя карт
        radioMap.setOnCheckedChangeListener((radiogroup, id)-> {
            // получаем выбранную кнопку
            switch (id) {
                case R.id.text:
                    selectedMapTemp = MapUtil.MAP_TEXT;
                    lbMapZoom.setVisibility(GONE);
                    sliderMapZoom.setVisibility(GONE);
                    lbMapTilt.setVisibility(GONE);
                    sliderMapTilt.setVisibility(GONE);
                    checkBoxCircle.setVisibility(GONE);
                    lbCircleRadius.setVisibility(GONE);
                    sliderCircleRadius.setVisibility(GONE);
                    break;
                case R.id.OpenStreet:
                    selectedMapTemp = MapUtil.MAP_OPENSTREET;
                    lbMapZoom.setVisibility(VISIBLE);
                    sliderMapZoom.setVisibility(VISIBLE);
                    lbMapTilt.setVisibility(GONE);
                    sliderMapTilt.setVisibility(GONE);
                    checkBoxCircle.setVisibility(GONE);
                    lbCircleRadius.setVisibility(GONE);
                    sliderCircleRadius.setVisibility(GONE);
                    break;
                case R.id.Yandex:
                    selectedMapTemp = MapUtil.MAP_YANDEX;
                    lbMapZoom.setVisibility(VISIBLE);
                    sliderMapZoom.setVisibility(VISIBLE);
                    lbMapTilt.setVisibility(VISIBLE);
                    sliderMapTilt.setVisibility(VISIBLE);
                    checkBoxCircle.setVisibility(VISIBLE);
                    if (checkBoxCircle.isChecked()) {
                        lbCircleRadius.setVisibility(VISIBLE);
                        sliderCircleRadius.setVisibility(VISIBLE);
                    }
                    break;
            }
        });

        // Обработка переключения состояния чекера круга
        checkBoxCircle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked){
                lbCircleRadius.setVisibility(VISIBLE);
                sliderCircleRadius.setVisibility(VISIBLE);
            } else {
                lbCircleRadius.setVisibility(GONE);
                sliderCircleRadius.setVisibility(GONE);
            }
        }
        );

        selectedModeColorTemp = Util.themeColor;
        // получаем объект RadioGroup переключателя цвета темы
        RadioGroup radioThemeColor = findViewById(R.id.radioThemeColor);
        switch (selectedModeColorTemp) {
            case (Util.COLOR_DYNAMIC_YES):
                radioThemeColor.check(R.id.themeDynamic);
                break;
            case Util.COLOR_DYNAMIC_NO:
                radioThemeColor.check(R.id.themeDefault);
                break;
        }

        // Обработка переключения состояния переключателя режимов цвета
        radioThemeColor.setOnCheckedChangeListener((radiogroup, id)-> {
            // получаем выбранную кнопку
            switch (id) {
                case R.id.themeDynamic:
                    selectedModeColorTemp = Util.COLOR_DYNAMIC_YES;
                    break;
                case R.id.themeDefault:
                    selectedModeColorTemp = Util.COLOR_DYNAMIC_NO;
                    break;
            }
        });

        selectedModeNightTemp = Util.themeMode;
        // получаем объект RadioGroup переключателя режимов темы
        RadioGroup radioTheme = findViewById(R.id.radioTheme);
        switch (selectedModeNightTemp) {
            case (Util.MODE_NIGHT_YES):
                radioTheme.check(R.id.themeNight);
                break;
            case Util.MODE_NIGHT_NO:
                radioTheme.check(R.id.themeDay);
                break;
            default:
                radioTheme.check(R.id.themeSystem);
                break;
        }

        // Обработка переключения состояния переключателя режимов темы
        radioTheme.setOnCheckedChangeListener((radiogroup, id)-> {
            // получаем выбранную кнопку
            switch (id) {
                case R.id.themeNight:
                    selectedModeNightTemp = Util.MODE_NIGHT_YES;
                    break;
                case R.id.themeDay:
                    selectedModeNightTemp = Util.MODE_NIGHT_NO;
                    break;
                case R.id.themeSystem:
                    selectedModeNightTemp = Util.MODE_NIGHT_FOLLOW_SYSTEM;
                    break;
            }
        });

    }

    public void onActionClicked(View view) {
        // Метод - обработчик кнопки "сохранить настройки".
        SharedPreferences settings = getSharedPreferences(Util.NAME_SETTINGS, MODE_PRIVATE);
        SharedPreferences.Editor prefEditor = settings.edit();
        Util.myphone = String.valueOf(edMyPhone.getText());
        Util.myphone = Util.myphone.replaceAll(UserActivity.REGEXP_CLEAR_PHONE,"");
        if (!Objects.equals(Util.myphone, "")) {
            prefEditor.putString(Util.NAME_MY_PHONE, Util.myphone);
        }
        Util.themeColor = selectedModeColorTemp;
        prefEditor.putInt(Util.nameThemeColor, Util.themeColor);
        if (Util.themeMode != selectedModeNightTemp) {
            Util.setThemeMode(selectedModeNightTemp);
        }
        Util.themeMode = selectedModeNightTemp;
        prefEditor.putInt(Util.nameThemeMode, Util.themeMode);
        MapUtil.selectedMap = selectedMapTemp;
        prefEditor.putInt(MapUtil.NAME_MAP, MapUtil.selectedMap);
        MapUtil.selectedMapZoom = sliderMapZoom.getValue();
        prefEditor.putFloat(MapUtil.NAME_MAP_ZOOM, MapUtil.selectedMapZoom);
        MapUtil.selectedMapTilt = sliderMapTilt.getValue();
        prefEditor.putFloat(MapUtil.NAME_MAP_TILT, MapUtil.selectedMapTilt);
        MapUtil.selectedMapCircle = checkBoxCircle.isChecked();
        prefEditor.putBoolean(MapUtil.NAME_MAP_CIRCLE, MapUtil.selectedMapCircle);
        MapUtil.selectedMapCircleRadius = sliderCircleRadius.getValue();
        prefEditor.putFloat(MapUtil.NAME_MAP_CIRCLE_RADIUS, MapUtil.selectedMapCircleRadius);
        Util.useService = checkBoxService.isChecked();
        prefEditor.putBoolean(Util.NAME_USE_SERVICE, Util.useService);
        prefEditor.apply();
        finish();
    }

}