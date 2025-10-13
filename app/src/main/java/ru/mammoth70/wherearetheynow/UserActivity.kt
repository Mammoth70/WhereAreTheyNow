package ru.mammoth70.wherearetheynow

import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.content.edit
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.util.Objects

class UserActivity : AppCompatActivity() {
    // Activity выводит карточку контакта для добавления, редактирования, удаления.

    companion object {
        const val INTENT_EXTRA_ACTION: String = "action"
        const val INTENT_EXTRA_RESULT: String = "refresh"
        const val INTENT_EXTRA_ID: String = "id"
        const val INTENT_EXTRA_PHONE: String = "phone"
        const val INTENT_EXTRA_NAME: String = "name"
        const val INTENT_EXTRA_COLOR: String = "color"

        const val REGEXP_CLEAR_PHONE: String = "[- ()]"

        const val ACTION_ADD_USER: String = "add user"
        const val ACTION_EDIT_USER: String = "edit user"
        const val ACTION_DELETE_USER: String = "delete user"
    }

    private var action: String? = null

    private var ilPhone: TextInputLayout? = null
    private var edPhone: TextInputEditText? = null
    private var ilName: TextInputLayout? = null
    private var edName: TextInputEditText? = null
    private var cardColor: MaterialCardView? = null
    private var tvColorError: TextView? = null
    private var id = 0

    private var btnAction: Button? = null
    private var tvColor: TextView? = null
    private var selectedColorTemp = ""

    private var colorOnSurfaceVariant = 0
    private var colorOutline = 0
    private var colorError = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        // Функция вызывается при создании Activity.
        // Подготавливаются структуры данных для вывода карточки контакта.
        super.onCreate(savedInstanceState)
        this.enableEdgeToEdge()
        setContentView(R.layout.activity_user)
        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.user)
        ) { v: View?, insets: WindowInsetsCompat? ->
            val systemBars = insets!!.getInsets(WindowInsetsCompat.Type.systemBars())
            v!!.setPadding(systemBars.left, systemBars.top,
                systemBars.right, systemBars.bottom)
            insets
        }
        val tvName = findViewById<TextView>(R.id.tvTitle)
        tvName.setText(R.string.titleUser)
        val intent = getIntent()
        btnAction = findViewById(R.id.btnAction)
        ilPhone = findViewById(R.id.ilPhone)
        edPhone = findViewById(R.id.edPhone)
        edPhone!!.setOnFocusChangeListener { v: View?, hasFocus: Boolean ->
            if (hasFocus) {
                ilPhone!!.error = null
            }
        }
        ilName = findViewById(R.id.ilName)
        edName = findViewById(R.id.edName)
        edName!!.setOnFocusChangeListener { v: View?, hasFocus: Boolean ->
            if (hasFocus) {
                ilName!!.error = null //
            }
        }
        cardColor = findViewById(R.id.cardColor)
        tvColor = findViewById(R.id.tvColor)
        tvColorError = findViewById(R.id.tvColorError)

        val theme = getTheme()
        val typedValueColorOnSurfaceVariant = TypedValue()
        val typedValueColorOutline = TypedValue()
        val typedValueColorError = TypedValue()
        theme.resolveAttribute(
            com.google.android.material.R.attr.colorOnSurfaceVariant,
            typedValueColorOnSurfaceVariant, true
        )
        theme.resolveAttribute(
            com.google.android.material.R.attr.colorOutline,
            typedValueColorOutline, true
        )
        theme.resolveAttribute(
            androidx.appcompat.R.attr.colorError,
            typedValueColorError, true
        )
        colorOnSurfaceVariant = typedValueColorOnSurfaceVariant.data
        colorOutline = typedValueColorOutline.data
        colorError = typedValueColorError.data

        getAction(intent)
    }

    private fun getAction(intent: Intent) {
        // Функция выясняет, какое действие над записью контакта будем выполнять.
        // Получает через intent поля из запускающей activity.
        // Соотвественно настраивает поля и кнопки.
        action = intent.getStringExtra(INTENT_EXTRA_ACTION)
        when (action) {
            ACTION_ADD_USER -> {
                btnAction!!.setText(R.string.add)
            }
            ACTION_EDIT_USER -> {
                btnAction!!.setText(R.string.edit)
                id = intent.getIntExtra(INTENT_EXTRA_ID, 0)
                if (id == 0) {
                    finish()
                }
                edPhone!!.setText(intent.getStringExtra(INTENT_EXTRA_PHONE))
                edName!!.setText(intent.getStringExtra(INTENT_EXTRA_NAME))
                setColorButton(intent.getStringExtra(INTENT_EXTRA_COLOR)!!)
            }
            ACTION_DELETE_USER -> {
                btnAction!!.setText(R.string.delete)
                id = intent.getIntExtra(INTENT_EXTRA_ID, 0)
                if (id == 0) {
                    finish()
                }
                edPhone!!.setText(intent.getStringExtra(INTENT_EXTRA_PHONE))
                edPhone!!.setEnabled(false)
                edName!!.setText(intent.getStringExtra(INTENT_EXTRA_NAME))
                edName!!.setEnabled(false)
                setColorButton(intent.getStringExtra(INTENT_EXTRA_COLOR)!!)
                tvColor!!.setEnabled(false)
            }
            else -> {
                finish()
            }
        }
    }

    fun onColorClicked(@Suppress("UNUSED_PARAMETER")ignored: View?) {
        // Функция - обработчик кнопки "цвет" (выбор цвета).
        if (selectedColorTemp.isEmpty()) {
            tvColor!!.setTextColor(colorOnSurfaceVariant)
            tvColor!!.setBackgroundResource(R.drawable.ic_pin_empty_64)
            cardColor!!.strokeColor = colorOutline
            tvColorError!!.text = ""
        }
        val intent = Intent(this, ColorsActivity::class.java)
        startActivityIntent.launch(intent)
    }

    fun setColorButton(color: String) {
        // Функция выставляет цвет кнопки
        tvColor!!.text = ""
        selectedColorTemp = color
        when (Objects.requireNonNull(color)) {
            AppColors.COLOR_WHITE -> tvColor!!.setBackgroundResource(R.drawable.ic_pin_white_64)
            AppColors.COLOR_RED -> tvColor!!.setBackgroundResource(R.drawable.ic_pin_red_64)
            AppColors.COLOR_ORANGE -> tvColor!!.setBackgroundResource(R.drawable.ic_pin_orange_64)
            AppColors.COLOR_YELLOW -> tvColor!!.setBackgroundResource(R.drawable.ic_pin_yellow_64)
            AppColors.COLOR_GREEN -> tvColor!!.setBackgroundResource(R.drawable.ic_pin_green_64)
            AppColors.COLOR_DARKGREEN -> tvColor!!.setBackgroundResource(R.drawable.ic_pin_darkgreen_64)
            AppColors.COLOR_CYAN -> tvColor!!.setBackgroundResource(R.drawable.ic_pin_cyan_64)
            AppColors.COLOR_BLUE -> tvColor!!.setBackgroundResource(R.drawable.ic_pin_blue_64)
            AppColors.COLOR_VIOLET -> tvColor!!.setBackgroundResource(R.drawable.ic_pin_violet_64)
            AppColors.COLOR_MAGENTA -> tvColor!!.setBackgroundResource(R.drawable.ic_pin_magenta_64)
            else -> tvColor!!.setBackgroundResource(R.drawable.ic_pin_black_64)
        }
    }

    fun onActionClicked(@Suppress("UNUSED_PARAMETER")ignored: View?) {
        // Функция - обработчик кнопки "действие".
        var phone = edPhone!!.getText().toString()
        val name = (edName!!.getText()).toString()
        phone = phone.replace(REGEXP_CLEAR_PHONE.toRegex(), "")
        if (phone.isEmpty()) {
            // Проверяем телефон на заполнение
            ilPhone!!.error = getString(R.string.err_empty_phone)
        }
        if (action == ACTION_ADD_USER && phone in Util.phones) {
            // Проверяем телефон на уникальность при добавлении
            ilPhone!!.error = getString(R.string.err_not_unique_phone)
        }
        if (name.isEmpty()) {
            // Проверяем имя на заполнение
            ilName!!.error = getString(R.string.err_empty_user)
        }
        if (selectedColorTemp.isEmpty()) {
            // Проверяем метку на заполнение
            cardColor!!.strokeColor = colorError
            tvColor!!.setTextColor(colorError)
            tvColor!!.setBackgroundResource(R.drawable.ic_pin_error_64)
            tvColorError!!.setText(R.string.err_empty_label)
        }

        if (phone.isEmpty() || name.isEmpty() || selectedColorTemp.isEmpty()) {
            // Если хоть что-то незаполнено - выходим.
            return
        }

        if (action == ACTION_ADD_USER) {
            if (phone in Util.phones) {
                // Если при добавлении телефон не уникальный - выходим.
                return
            }
            if (MainActivity.dbHelper!!.addUser(phone, name, selectedColorTemp)) {
                val intent = Intent()
                setPhonesSet()
                intent.putExtra(INTENT_EXTRA_RESULT, ACTION_ADD_USER)
                setResult(RESULT_OK, intent)
                finish()
            } else {
                Toast.makeText(this, R.string.failed_to_add_user,
                    Toast.LENGTH_SHORT).show()
            }
        } else if (action == ACTION_EDIT_USER) {
            if (MainActivity.dbHelper!!.editUser(id, phone, name, selectedColorTemp)) {
                setPhonesSet()
                val intent = Intent()
                intent.putExtra(INTENT_EXTRA_RESULT, ACTION_EDIT_USER)
                setResult(RESULT_OK, intent)
                finish()
            } else {
                Toast.makeText(this, R.string.failed_to_change_user,
                    Toast.LENGTH_SHORT).show()
            }
        } else if (action == ACTION_DELETE_USER) {
            if (MainActivity.dbHelper!!.deleteUser(id)) {
                setPhonesSet()
                val intent = Intent()
                intent.putExtra(INTENT_EXTRA_RESULT, ACTION_DELETE_USER)
                setResult(RESULT_OK, intent)
                finish()
            } else {
                Toast.makeText(this, R.string.failed_to_delete_user,
                    Toast.LENGTH_SHORT).show()
            }
        } else {
            finish()
        }
    }

    fun setPhonesSet() {
        // Функция записывает в SharedPreferences множество разрешенных телефонов.
        // Нужно, чтобы SMSMonitor работал даже в том случае, если не запускалась MainActivity.
        val phonesSet: MutableSet<String?> = HashSet(Util.phones)
        val settings = getSharedPreferences(Util.NAME_SETTINGS, MODE_PRIVATE)
        settings.edit {
            putStringSet(Util.NAME_PHONES, phonesSet)
        }
    }

    var startActivityIntent = registerForActivityResult( // Возвращает результат выбора цвета
        StartActivityForResult()
    ) { result: ActivityResult? ->
        if (result!!.resultCode == RESULT_OK) {
            val intent = result.data
            intent?.let {
                setColorButton(intent.getStringExtra(Util.INTENT_EXTRA_COLOR)!!)
            }
        }
    }

}