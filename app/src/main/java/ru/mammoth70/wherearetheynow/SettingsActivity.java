package ru.mammoth70.wherearetheynow;

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
    private Slider sliderMapZoom;
    private Slider sliderMapTilt;

    private static int selectedMapTemp;
    private static int selectedModeNightTemp;

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
        sliderMapZoom = findViewById(R.id.sliderMapZoom);
        sliderMapZoom.setValue(MapUtil.selectedMapZoom);
        sliderMapTilt = findViewById(R.id.sliderMapTilt);
        sliderMapTilt.setValue(MapUtil.selectedMapTilt);
        edMyPhone = findViewById(R.id.myphone);
        if (!Objects.equals(Util.myphone, "")) {
            edMyPhone.setText(Util.myphone);
        }
        tvName.setText(R.string.titleSettings);

        selectedMapTemp = MapUtil.selectedMap;
        // получаем объект RadioGroup
        RadioGroup radios = findViewById(R.id.radios);
        switch (selectedMapTemp) {
            case MapUtil.MAP_TEXT:
                radios.check(R.id.text);
                break;
            case MapUtil.MAP_OPENSTREET:
                radios.check(R.id.OpenStreet);
                break;
            case MapUtil.MAP_YANDEX:
                radios.check(R.id.Yandex);
                break;
            default:
                break;
        }

        // Обработка переключения состояния переключателя
        radios.setOnCheckedChangeListener((radiogroup, id)-> {
            // получаем выбранную кнопку
            switch (id) {
                case R.id.text:
                    selectedMapTemp = MapUtil.MAP_TEXT;
                    break;
                case R.id.OpenStreet:
                    selectedMapTemp = MapUtil.MAP_OPENSTREET;
                    break;
                case R.id.Yandex:
                    selectedMapTemp = MapUtil.MAP_YANDEX;
                    break;
            }
        });

        selectedModeNightTemp = Util.modeNight;
        // получаем объект RadioGroup
        RadioGroup theme = findViewById(R.id.theme);
        switch (selectedModeNightTemp) {
            case (Util.MODE_NIGHT_YES):
                theme.check(R.id.themeNight);
                break;
            case Util.MODE_NIGHT_NO:
                theme.check(R.id.themeDay);
                break;
            default:
                theme.check(R.id.themeSystem);
                break;
        }

        // Обработка переключения состояния переключателя
        theme.setOnCheckedChangeListener((radiogroup, id)-> {
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
        SharedPreferences settings = getSharedPreferences(Util.nameSettings, MODE_PRIVATE);
        SharedPreferences.Editor prefEditor = settings.edit();
        Util.myphone = String.valueOf(edMyPhone.getText());
        Util.myphone = Util.myphone.replaceAll(UserActivity.REGEXP_CLEAR_PHONE,"");
        if (!Objects.equals(Util.myphone, "")) {
            prefEditor.putString(Util.nameMyPhone, Util.myphone);
        }
        if (Util.modeNight != selectedModeNightTemp) {
            Util.setNightTheme(selectedModeNightTemp);
        }
        Util.modeNight = selectedModeNightTemp;
        prefEditor.putInt(Util.nameThemeMode, Util.modeNight);
        MapUtil.selectedMap = selectedMapTemp;
        prefEditor.putInt(MapUtil.nameMap, MapUtil.selectedMap);
        MapUtil.selectedMapZoom = sliderMapZoom.getValue();
        prefEditor.putFloat(MapUtil.nameMapZoom, MapUtil.selectedMapZoom);
        MapUtil.selectedMapTilt = sliderMapTilt.getValue();
        prefEditor.putFloat(MapUtil.nameMapTilt, MapUtil.selectedMapTilt);
        MapUtil.selectedMapCircle = checkBoxCircle.isChecked();
        prefEditor.putBoolean(MapUtil.nameMapCircle, MapUtil.selectedMapCircle);
        Util.useService = checkBoxService.isChecked();
        prefEditor.putBoolean(Util.nameUseService, Util.useService);
        prefEditor.apply();
        finish();
    }

}