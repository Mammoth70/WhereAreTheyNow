package ru.mammoth70.wherearetheynow;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    // Главная activity приложения.
    // Выводит список контактов и bottom Navigatin bar.
    public static DBhelper dbHelper;
    private NavigationBarView navigationBarView;
    private SimpleAdapter sAdapter;
    private ArrayList<Map<String, Object>> data;

    private static final int NM_MAP_ID = 0;
    private static final int NM_USERS_ID = 1;

    private static final String COLUMN_PHONE = "phone";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_COLOR = "color";
    private static final String COLUMN_BACK = "background";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Метод вызывается при создании Activity.
        // Считываются списки и словари контактов из БД.
        // Если не хватает нужных разрешений, сразу вызывается Activity cо списком разрешений.
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.settings),
                (v, insets) -> {
                    Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                    return insets;
                });
        TextView tvName = findViewById(R.id.tvTitle);
        tvName.setText(R.string.titleUsers);
        dbHelper = new DBhelper(this);
        dbHelper.getUsers();

        data = new ArrayList<>(Util.phones.size());
        refreshData();
        String[] from = {COLUMN_PHONE, COLUMN_NAME, COLUMN_COLOR, COLUMN_BACK};
        int[] to = {R.id.itemUserPhone, R.id.itemUserName, R.id.itemUserLabel, R.id.itemUserLayout};

        sAdapter = new SimpleAdapter(this, data, R.layout.item_user, from, to);
        sAdapter.setViewBinder(new ViewBinder());

        ListView lvSimple = findViewById(R.id.lvUsersSimple);
        lvSimple.setAdapter(sAdapter);
        lvSimple.setClickable(true);
        registerForContextMenu(lvSimple);
        lvSimple.setOnItemClickListener((parent, view, position, id)
                -> editUser(position));

        if (!checkAllPermissions()) {
            startPermissionActivity();
        }
        navigationBarView= findViewById(R.id.bottom_navigation);
        navigationBarView.getMenu().getItem(NM_MAP_ID).
                setEnabled(!Objects.equals(MapUtil.getLastAnswer(this).phone, ""));
        navigationBarView.getMenu().getItem(NM_USERS_ID).setChecked(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MapUtil.getLastAnswer(this);
        navigationBarView.getMenu().getItem(NM_MAP_ID).
                setEnabled(!Objects.equals(MapUtil.getLastAnswer(this).phone, ""));
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        // Метод создаёт контекстное меню
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.context_menu, menu);
        menu.setGroupDividerEnabled(true);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        // Метод обрабатывает контекстное меню.
        AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        if (acmi == null) {
            return false;
        }
        switch (item.getItemId()) {
            case (R.id.item_add_user):
                addUser();
                return true;
            case (R.id.item_edit_user):
                editUser(acmi.position);
                return true;
            case (R.id.item_delete_user):
                deleteUser(acmi.position);
                return true;
            case (R.id.item_sms_request_user):
                smsRequestUser(acmi.position);
                return true;
            case (R.id.item_sms_answer_user):
                smsAnswerUser(acmi.position);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void addUser() {
        // Метод добавляет контакт.
        Intent intent = new Intent(this, UserActivity.class);
        intent.putExtra(UserActivity.INTENT_EXTRA_ACTION, UserActivity.ACTION_ADD_USER);
        startActivityUserIntent.launch(intent);
    }
    private void editUser(int position) {
        // Метод редактирует контакт.
        Intent intent = new Intent(this, UserActivity.class);
        intent.putExtra(UserActivity.INTENT_EXTRA_ACTION, UserActivity.ACTION_EDIT_USER);
        String phone;
        phone = Util.phones.get(position);
        intent.putExtra(UserActivity.INTENT_EXTRA_ID, Util.phone2id.get(phone));
        intent.putExtra(UserActivity.INTENT_EXTRA_PHONE, phone);
        intent.putExtra(UserActivity.INTENT_EXTRA_NAME, Util.phone2name.get(phone));
        intent.putExtra(UserActivity.INTENT_EXTRA_COLOR, Util.phone2color.get(phone));
        startActivityUserIntent.launch(intent);
    }

    private void deleteUser(int position) {
        // Метод удаляет контакт.
        Intent intent = new Intent(this, UserActivity.class);
        intent.putExtra(UserActivity.INTENT_EXTRA_ACTION, UserActivity.ACTION_DELETE_USER);
        String phone;
        phone = Util.phones.get(position);
        intent.putExtra(UserActivity.INTENT_EXTRA_ID, Util.phone2id.get(phone));
        intent.putExtra(UserActivity.INTENT_EXTRA_PHONE, phone);
        intent.putExtra(UserActivity.INTENT_EXTRA_NAME, Util.phone2name.get(phone));
        intent.putExtra(UserActivity.INTENT_EXTRA_COLOR, Util.phone2color.get(phone));
        startActivityUserIntent.launch(intent);
    }

    private void smsRequestUser(int position) {
        // Метод посылает контакту запрос координат.
        String phone;
        phone = Util.phones.get(position);
        if (Objects.equals(phone, Util.myphone)) {
            selfPosition();
        } else {
            if (Util.useService) {
                // Метод передаёт обработку запроса геолокации в GetLocationService.
                Intent intent = new Intent(this, GetLocationService.class);
                intent.putExtra(Util.INTENT_EXTRA_SMS_TO, phone);
                intent.putExtra(Util.INTENT_EXTRA_NEW_VERSION_REQUEST, true);
                this.startService(intent);
            } else {
                // Метод передаёт обработку запроса геолокации в GetLocation.
                GetLocation getLocation = new GetLocation();
                getLocation.sendLocation(this, GetLocation.WAY_SMS, phone, true);
            }
        }
    }

    private void smsAnswerUser(int position) {
        // Метод посылает контакту геолокацию.
        String phone;
        phone = Util.phones.get(position);
        if (Objects.equals(phone, Util.myphone)) {
            selfPosition();
        } else {
            if (Util.useService) {
                // Метод передаёт обработку запроса геолокации в GetLocationService.
                Intent intent = new Intent(this, GetLocationService.class);
                intent.putExtra(Util.INTENT_EXTRA_SMS_TO, phone);
                this.startService(intent);
            } else {
                // Метод передаёт обработку запроса геолокации в GetLocation.
                GetLocation getLocation = new GetLocation();
                getLocation.sendLocation(this, GetLocation.WAY_SMS, phone, false);
            }
        }
    }

    private void selfPosition() {
        // Метод определяет собственную геолокацию и вызывает карту.
        GetLocation getLocation = new GetLocation();
        getLocation.sendLocation(this, GetLocation.WAY_LOCAL, "",false);
    }

    private void refreshData() {
        // Метод обновляет данные для списка контактов из БД.
        data.clear();
        for (String phone : Util.phones) {
            Map<String, Object> m = new HashMap<>();
            m.put(COLUMN_PHONE, phone);
            m.put(COLUMN_NAME, Util.phone2name.get(phone));
            m.put(COLUMN_COLOR, Util.phone2color.get(phone));
            m.put(COLUMN_BACK, Util.phone2color.get(phone));
            data.add(m);
        }
    }

    public void onPermissionClicked(MenuItem intem) {
        // Метод - обработчик кнопки меню "разрешения".
        // Вызывает соответствующую Activity.
        startPermissionActivity();
    }

    public void onMapClicked(MenuItem intem) {
        // Метод - обработчик кнопки меню "карта".
        // Вызывает соответствующую Activity.
        PointRecord record = MapUtil.getLastAnswer(this);
        MapUtil.viewLocation(this, record, false);
    }

    public void onSettingsClicked(MenuItem intem) {
        // Метод - обработчик кнопки меню "настройки".
        // Вызывает соответствующую Activity.
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivitySettingsIntent.launch(intent);
    }

    private void startPermissionActivity() {
        // Метод запускает PermissionActivity.
        Intent intent = new Intent(this, PermissionActivity.class);
        startActivity(intent);
    }

    private boolean checkAllPermissions() {
        // Метод проверяет все необходимые разрешения (если их не хватает, нужно запустить PermissionActivity).
        return ((ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) &&
            (ContextCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) &&
            (ContextCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) &&
            (ContextCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED) &&
            (ContextCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED));
    }

    public void onAddUserClicked(View view) {
        // Метод - обработчик кнопки FAB "Добавить контакт".
        addUser();
    }

    public void onAboutClicked(MenuItem intem) {
        // Метод - обработчик кнопки меню "about".
        Bundle bundle = new Bundle();
        String title =
                getString(R.string.app_name);

        bundle.putString(AboutBox.DIALOG_TITLE, title);
        String text =
                getString(R.string.description) + "\n" +
                getString(R.string.version) + " " +
                BuildConfig.VERSION_NAME;
        bundle.putString(AboutBox.DIALOG_MESSAGE, text);
        AboutBox aboutBox = new AboutBox();
        aboutBox.setArguments(bundle);
        aboutBox.show(this.getSupportFragmentManager(), "MESSAGE_DIALOG");
    }

    private static class ViewBinder implements SimpleAdapter.ViewBinder {
        // Класс обрабатывает форматирование вывода на экран списка контактов.
        @Override
        public boolean setViewValue(View view, Object data,
                                    String textRepresentation) {
            String color;
            if (view.getId() == R.id.itemUserLayout) {
                color = ((String) data);
                view.setBackgroundColor(Color.parseColor(AppColors.getColorAlpha16(color)));
                return true;
            } else if (view.getId() == R.id.itemUserLabel) {
                color = ((String) data);
                view.setBackgroundResource(AppColors.getColorMarker(color));
                return true;
            } else {
                return false;
            }
        }
    }

    ActivityResultLauncher<Intent> startActivityUserIntent = registerForActivityResult(
            // Метод возвращает результат вызова формы контакта.
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent intent = result.getData();
                    if (intent != null) {
                        refreshData();
                    }
                    sAdapter.notifyDataSetChanged();
                }
            });

    ActivityResultLauncher<Intent> startActivitySettingsIntent = registerForActivityResult(
            // Метод возвращает результат вызова формы настроек.
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent intent = result.getData();
                    if (intent != null) {
                        Boolean action = intent.getBooleanExtra(SettingsActivity.INTENT_EXTRA_RESULT, false);
                        if (action) {
                            recreate();
                        }
                    }
                }
            });

}