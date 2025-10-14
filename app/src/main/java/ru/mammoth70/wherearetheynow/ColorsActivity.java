package ru.mammoth70.wherearetheynow;

import static ru.mammoth70.wherearetheynow.R.*;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ColorsActivity extends AppCompatActivity {
    // Activity выбора цвета.
    private static final String COLUMN_COLOR = "color";
    private static final String COLUMN_BACK = "background";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Метод вызывается при создании Activity.
        // Подготавливаются структуры данных для вывода списка цветов.

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_colors);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.colors),
                (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        TextView tvName = findViewById(R.id.tvTitle);
        tvName.setText(string.titleColors);
        SimpleAdapter sAdapter = simpleAdapter();
        ListView lvSimple = findViewById(id.lvColorsSimple);
        lvSimple.setAdapter(sAdapter);
        lvSimple.setClickable(true);
        lvSimple.setOnItemClickListener((parent, view, position, id) -> {
            String selectedItem = AppColors.colors.get(position);
            Intent intent = new Intent();
            intent.putExtra(Util.INTENT_EXTRA_COLOR, selectedItem);
            setResult(RESULT_OK, intent);
            finish();
        });
    }

    @NonNull
    private SimpleAdapter simpleAdapter() {
        // Метод создаёт и заполняет SimpleAdapter.
        ArrayList<Map<String, Object>> data = new ArrayList<>(AppColors.colors.size());
        for (String color : AppColors.colors) {
            Map<String, Object> m;
            m = new HashMap<>();
            m.put(COLUMN_COLOR, color);
            m.put(COLUMN_BACK, color);
            data.add(m);
        }
        String[] from = {COLUMN_COLOR, COLUMN_BACK};
        int[] to = { id.itemColorLabel, id.itemColorLayout };
        SimpleAdapter sAdapter = new SimpleAdapter(this, data, layout.item_color, from, to);
        sAdapter.setViewBinder(new ViewBinder());
        return sAdapter;
    }

    private static class ViewBinder implements SimpleAdapter.ViewBinder {
        // Класс обрабатывает форматирование вывода на экран списка цветов.
        @Override
        public boolean setViewValue(View view, Object data, String textRepresentation) {
            switch (view.getId()) {
                case (R.id.itemColorLayout): {
                    view.setBackgroundColor(Color.parseColor(AppColors.getColorAlpha16((String) data)));
                    return true;
                }
                case (R.id.itemColorLabel): {
                    view.setBackgroundResource(AppColors.getColorMarker((String) data));
                    return true;
                }
                default:
                    return false;
            }
        }
    }

}