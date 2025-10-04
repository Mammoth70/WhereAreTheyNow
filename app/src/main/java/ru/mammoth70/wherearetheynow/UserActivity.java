package ru.mammoth70.wherearetheynow;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class UserActivity extends AppCompatActivity {
    // Activity выводит карточку контакта для добавления, редактирования, удаления.

    public static final String INTENT_EXTRA_ACTION = "action";
    public static final String INTENT_EXTRA_RESULT = "refresh";
    public static final String INTENT_EXTRA_ID = "id";
    public static final String INTENT_EXTRA_PHONE = "phone";
    public static final String INTENT_EXTRA_NAME = "name";
    public static final String INTENT_EXTRA_COLOR = "color";

    public static final String REGEXP_CLEAR_PHONE = "[- ()]";

    public static final String ACTION_ADD_USER = "add user";
    public static final String ACTION_EDIT_USER = "edit user";
    public static final String ACTION_DELETE_USER = "delete user";
    private String action;

    private TextInputLayout ilPhone;
    private TextInputEditText edPhone;
    private TextInputLayout ilName;
    private TextInputEditText edName;
    private MaterialCardView cardColor;
    private TextView tvColorError;
    private int id;

    private Button btnAction;
    private TextView tvColor;
    private String selectedColorTemp = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Метод вызывается при создании Activity.
        // Подготавливаются структуры данных для вывода карточки контакта.
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.user), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        TextView tvName = findViewById(R.id.tvTitle);
        tvName.setText(R.string.titleUser);
        Intent intent = getIntent();
        btnAction = findViewById(R.id.btnAction);
        ilPhone = findViewById(R.id.ilPhone);
        edPhone = findViewById(R.id.edPhone);
        edPhone.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                ilPhone.setError(null);
            }
        });
        ilName = findViewById(R.id.ilName);
        edName = findViewById(R.id.edName);
        edName.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                ilName.setError(null);//
            }
        });
        cardColor = findViewById(R.id.cardColor);
        tvColor = findViewById(R.id.tvColor);
        tvColorError = findViewById(R.id.tvColorError);
        getAction(intent);
    }

    private void getAction(Intent intent) {
        // Метод выясняет, какое действие над записью контакта будем выполнять.
        // Получает через intent поля из запускающей activity.
        // Соотвественно настраивает поля и кнопки.
        action = intent.getStringExtra(INTENT_EXTRA_ACTION);
        if (Objects.equals(action, ACTION_ADD_USER)) {
            btnAction.setText(R.string.add);
        } else if (Objects.equals(action, ACTION_EDIT_USER)){
            btnAction.setText(R.string.edit);
            id = intent.getIntExtra(INTENT_EXTRA_ID,0);
            if (id == 0) { finish(); }
            edPhone.setText(intent.getStringExtra(INTENT_EXTRA_PHONE));
            edName.setText(intent.getStringExtra(INTENT_EXTRA_NAME));
            setColorButton(intent.getStringExtra(INTENT_EXTRA_COLOR));
        } else if (Objects.equals(action, ACTION_DELETE_USER)) {
            btnAction.setText(R.string.delete);
            id = intent.getIntExtra(INTENT_EXTRA_ID,0);
            if (id == 0) { finish(); }
            edPhone.setText(intent.getStringExtra(INTENT_EXTRA_PHONE));
            edPhone.setEnabled(false);
            edName.setText(intent.getStringExtra(INTENT_EXTRA_NAME));
            edName.setEnabled(false);
            setColorButton(intent.getStringExtra(INTENT_EXTRA_COLOR));
            tvColor.setEnabled(false);
        } else {
            finish();
        }
    }

    public void onColorClicked(View view) {
        // Метод - обработчик кнопки "цвет" (выбор цвета).
        if (selectedColorTemp.isEmpty()) {
            tvColor.setTextColor(getResources().getColor(R.color.md_theme_onSurfaceVariant, null));
            tvColor.setBackgroundResource(R.drawable.ic_pin_empty_64);
            cardColor.setStrokeColor(getResources().getColor(R.color.md_theme_outline, null));
            tvColorError.setText("");
        }
        Intent intent = new Intent(this, ColorsActivity.class);
        startActivityIntent.launch(intent);
  }

    public void setColorButton(String color) {
        // Метод выставляет цвет кнопки
        tvColor.setText("");
        selectedColorTemp = color;
        switch (Objects.requireNonNull(color)) {
            case (AppColors.COLOR_WHITE):
                tvColor.setBackgroundResource(R.drawable.ic_pin_white_64);
                break;
            case (AppColors.COLOR_RED):
                tvColor.setBackgroundResource(R.drawable.ic_pin_red_64);
                break;
            case (AppColors.COLOR_ORANGE):
                tvColor.setBackgroundResource(R.drawable.ic_pin_orange_64);
                break;
            case (AppColors.COLOR_YELLOW):
                tvColor.setBackgroundResource(R.drawable.ic_pin_yellow_64);
                break;
            case (AppColors.COLOR_GREEN):
                tvColor.setBackgroundResource(R.drawable.ic_pin_green_64);
                break;
            case (AppColors.COLOR_DARKGREEN):
                tvColor.setBackgroundResource(R.drawable.ic_pin_darkgreen_64);
                break;
            case (AppColors.COLOR_CYAN):
                tvColor.setBackgroundResource(R.drawable.ic_pin_cyan_64);
                break;
            case (AppColors.COLOR_BLUE):
                tvColor.setBackgroundResource(R.drawable.ic_pin_blue_64);
                break;
            case (AppColors.COLOR_VIOLET):
                tvColor.setBackgroundResource(R.drawable.ic_pin_violet_64);
                break;
            case (AppColors.COLOR_MAGENTA):
                tvColor.setBackgroundResource(R.drawable.ic_pin_magenta_64);
                break;
            default:
                tvColor.setBackgroundResource(R.drawable.ic_pin_black_64);
        }
    }
    public void onActionClicked(View view) {
        // Метод - обработчик кнопки "действие".
        String phone = String.valueOf(edPhone.getText());
        String name = String.valueOf((edName.getText()));
        phone = phone.replaceAll(REGEXP_CLEAR_PHONE,"");
        String color = String.valueOf((tvColor.getText()));
        if (phone.isEmpty()) {
            // Проверяем телефон на заполнение
            ilPhone.setError(getString(R.string.err_empty_phone));
        }
        if (Objects.equals(action, ACTION_ADD_USER) && Util.phones.contains(phone)) {
            // Проверяем телефон на уникальность при добавлении
            ilPhone.setError(getString(R.string.err_not_unique_phone));
        }
        if (name.isEmpty()) {
            // Проверяем имя на заполнение
            ilName.setError(getString(R.string.err_empty_user));
        }
        if (selectedColorTemp.isEmpty()) {
            // Проверяем метку на заполнение
            cardColor.setStrokeColor(getResources().getColor(R.color.md_theme_error,null));
            tvColor.setTextColor(getColor(R.color.md_theme_error));
            tvColor.setBackgroundResource(R.drawable.ic_pin_error_64);
            tvColorError.setText(R.string.err_empty_label);
        }

        if (phone.isEmpty() || name.isEmpty() || selectedColorTemp.isEmpty()) {
            // Если хоть что-то незаполнено - выходим.
            return;
        }

        if (Objects.equals(action, ACTION_ADD_USER)) {
            if (Util.phones.contains(phone)) {
                // Если при добавлении телефон не уникальный - выходим.
                return;
            }
            if (MainActivity.dbHelper.addUser(phone, name, selectedColorTemp)) {
                Intent intent = new Intent();
                setPhonesSet();
                intent.putExtra(INTENT_EXTRA_RESULT, ACTION_ADD_USER);
                setResult(RESULT_OK, intent);
                finish();
            } else {
                Toast.makeText(this, R.string.failed_to_add_user,Toast.LENGTH_SHORT).show();
            }
        } else if (Objects.equals(action, ACTION_EDIT_USER)){
            if (MainActivity.dbHelper.editUser(id, phone, name, selectedColorTemp)) {
                setPhonesSet();
                Intent intent = new Intent();
                intent.putExtra(INTENT_EXTRA_RESULT, ACTION_EDIT_USER);
                setResult(RESULT_OK, intent);
                finish();
            } else {
                Toast.makeText(this, R.string.failed_to_change_user,Toast.LENGTH_SHORT).show();
            }
        } else if (Objects.equals(action, ACTION_DELETE_USER)) {
            if (MainActivity.dbHelper.deleteUser(id)) {
                setPhonesSet();
                Intent intent = new Intent();
                intent.putExtra(INTENT_EXTRA_RESULT, ACTION_DELETE_USER);
                setResult(RESULT_OK, intent);
                finish();
            } else {
                Toast.makeText(this, R.string.failed_to_delete_user,Toast.LENGTH_SHORT).show();
            }
        } else {
            finish();
        }
    }

    public void setPhonesSet() {
        // Метод записывает в SharedPreferences множество разрешенных телефонов.
        // Нужно, чтобы SMSMonitor работал даже в том случае, если не запускалась MainActivity.
        Set<String> phonesSet = new HashSet<>(Util.phones);
        SharedPreferences settings = getSharedPreferences(Util.nameSettings, MODE_PRIVATE);
        SharedPreferences.Editor prefEditor  = settings.edit();
        prefEditor.putStringSet(Util.namePhones, phonesSet);
        prefEditor.apply();
    }

    ActivityResultLauncher<Intent> startActivityIntent = registerForActivityResult(
            // Возвращает результат выбора цвета
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent intent = result.getData();
                    if (intent != null) {
                        setColorButton(intent.getStringExtra(Util.INTENT_EXTRA_COLOR));
                    }
                }
            });

}