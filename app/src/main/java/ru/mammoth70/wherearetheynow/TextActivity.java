package ru.mammoth70.wherearetheynow;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class TextActivity extends LocationActivity {
    // Activity выводит текст с геолокацией, переданной через intent.
    private ArrayList<Map<String, Object>> data;
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_BACK = "background";
    private static final String COLUMN_LATITUDE = "latitude";
    private static final String COLUMN_LONGITUDE = "longitude";
    private static final String COLUMN_DATE = "date";
    private TextView tvLatitude;
    private TextView tvLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Метод вызывается при создании Activity.
        // Из intent получаются и выводятся координаты.
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_text);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.text), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        createFrameTitle(this);

        tvLatitude = findViewById(R.id.tvLatitude);
        tvLongitude = findViewById(R.id.tvLongitude);
        data = new ArrayList<>(Util.phone2record.size());
        refreshData();
        String[] from = {COLUMN_NAME, COLUMN_BACK, COLUMN_LATITUDE, COLUMN_LONGITUDE, COLUMN_DATE};
        int[] to = {R.id.itemUserName, R.id.itemUserGeoLayout, R.id.itemLattitude, R.id.itemLongitude, R.id.itemDate};

        SimpleAdapter sAdapter = new SimpleAdapter(this, data, R.layout.item_geo, from, to);
        sAdapter.setViewBinder(new ViewBinder());

        ListView lvSimple = findViewById(R.id.lvGeoSimple);
        lvSimple.setAdapter(sAdapter);

        reloadMapFromPoint(this, startRecord);
    }

    @Override
    protected void reloadMapFromPoint(Context context, PointRecord rec) {
        // Метод выводит текстом широту и долготу по PointRecord.
        tvLatitude.setText(String.format(Locale.US,PointRecord.FORMAT_DOUBLE, rec.latitude));
        tvLongitude.setText(String.format(Locale.US,PointRecord.FORMAT_DOUBLE, rec.longitude));
    }

    private void refreshData() {
        // Метод обновляет данные для списка контактов с координатами.
        data.clear();
        for (String phone : Util.phones) {
            if (Util.phone2record.containsKey(phone)) {
                PointRecord value = Util.phone2record.get(phone);
                if (value != null) {
                    Map<String, Object> m = new HashMap<>();
                    m.put(COLUMN_NAME, Util.phone2name.get(phone));
                    m.put(COLUMN_BACK, Util.phone2color.get(phone));
                    m.put(COLUMN_LATITUDE, String.format(Locale.US,PointRecord.FORMAT_DOUBLE,
                            value.latitude));
                    m.put(COLUMN_LONGITUDE, String.format(Locale.US,PointRecord.FORMAT_DOUBLE,
                            value.longitude));
                    m.put(COLUMN_DATE, value.datetime);
                    data.add(m);
                }
            }
        }
    }

    private static class ViewBinder implements SimpleAdapter.ViewBinder {
        // Класс обрабатывает форматирование вывода на экран
        // списка контактов с координатами и датами получения геолокации.
        @Override
        public boolean setViewValue(View view, Object data,
                                    String textRepresentation) {
            String color;
            if (view.getId() == R.id.itemUserGeoLayout) {
                color = ((String) data);
                view.setBackgroundColor(Color.parseColor(AppColors.getColorAlpha16(color)));
                return true;
            } else {
                return false;
            }
        }
    }

}