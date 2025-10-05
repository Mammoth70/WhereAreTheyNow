package ru.mammoth70.wherearetheynow;

import android.content.Context;
import android.content.Intent;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public abstract class LocationActivity extends AppCompatActivity {
    // Абстрактный класс для создания Activity вывода геолокации, переданной через intent.

    protected Intent intent;
    protected PointRecord startRecord;
    protected TextView tvName;
    protected TextView tvDateTime;
    protected Button menuButton;

    protected void createFrameTitle(Context context) {
        // Метод вызывается при создании Activity.
        // Не должен переопределяться, но должен вызываться из onCreate после вызова setContentView.
        intent = getIntent();
        tvName = findViewById(R.id.tvName);
        tvDateTime = findViewById(R.id.tvDateTime);
        startRecord = new PointRecord(
                intent.getStringExtra(Util.INTENT_EXTRA_SMS_FROM),
                intent.getDoubleExtra(Util.INTENT_EXTRA_LATITUDE,0),
                intent.getDoubleExtra(Util.INTENT_EXTRA_LONGITUDE,0),
                intent.getStringExtra(Util.INTENT_EXTRA_TIME));
        tvName.setText(Util.phone2name.get(startRecord.phone));
        tvDateTime.setText(MapUtil.timePassed(startRecord.datetime,context));

        // Настроить вызов меню со списком контактов.
        Util.menuPhones.clear();
        try  (DBhelper dbHelper = new DBhelper(context)) {
            dbHelper.getMenuUsers();
        }
        menuButton = findViewById(R.id.btnMenuUsers);
        menuButton.setOnClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(context, view);
            for (String key : Util.menuPhones) {
                if ((Util.phone2record.containsKey(key)) && (Util.phone2id.containsKey(key))) {
                    Integer id = Util.phone2id.get(key);
                    if (id != null) {
                        popupMenu.getMenu().add(0, id, 0, Util.phone2name.get(key));
                    }
                }
            }
            popupMenu.setOnMenuItemClickListener(item -> {
                reloadMapFromId(context, item.getItemId());
                return true;
            });
            popupMenu.show(); // Отобразить меню
        });
    }

    protected void reloadMapFromId(Context context, int id) {
        // Метод выводит координаты тектом, меняя заголовок карты.
        // Вызывается из меню со списком контактов.
        // После выполнения, вызывает метод reloadMapFromPoint.
        // Может быть переопределён.
        String phone = Util.id2phone.get(id);
        tvName.setText(Util.phone2name.get(phone));
        tvDateTime.setText(MapUtil.timePassed(
                Objects.requireNonNull(Util.phone2record.get(phone)).datetime,context));
        reloadMapFromPoint(context, Objects.requireNonNull(Util.phone2record.get(phone)));
    }

    protected abstract void reloadMapFromPoint(Context context, PointRecord rec);
    // Абстрактный класс, должен быть переопределён.
    // Вызывается из reloadMapFromPoint, а также из OnCreate.
    // Метод перестраивает карту по передаваемой записи PoinRecord.
}