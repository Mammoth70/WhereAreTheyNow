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
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.util.Objects
import kotlin.getValue

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

    private val action: String by lazy { intent.getStringExtra(INTENT_EXTRA_ACTION)!! }

    private val tvTitle: TextView by lazy { findViewById(R.id.tvTitle) }
    private val ilPhone: TextInputLayout by lazy { findViewById(R.id.ilPhone) }
    private val edPhone: TextInputEditText by lazy { findViewById(R.id.edPhone) }
    private val ilName: TextInputLayout by lazy { findViewById(R.id.ilName) }
    private val edName: TextInputEditText by lazy { findViewById(R.id.edName) }
    private val cardColor: MaterialCardView by lazy { findViewById(R.id.cardColor) }
    private val tvColorError: TextView by lazy { findViewById(R.id.tvColorError) }
    private val id: Int  by lazy { intent.getIntExtra(INTENT_EXTRA_ID, 0) }

    private val btnAction: Button by lazy { findViewById(R.id.btnAction) }
    private val tvMark: TextView by lazy {findViewById(R.id.tvMark)}
    private var selectedColorTemp = ""

    private var colorOnSurfaceVariant = 0
    private var colorOutline = 0
    private var colorError = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        // Функция вызывается при создании Activity.
        // Подготовка структуры данных для вывода карточки контакта.
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_user)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.user))
        { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top,
                systemBars.right, systemBars.bottom)
            insets
        }

        tvTitle.setText(R.string.titleUser)
        val intent = getIntent()
        edPhone.setOnFocusChangeListener { v: View?, hasFocus: Boolean ->
            if (hasFocus) {
                ilPhone.error = null
            }
        }
        edName.setOnFocusChangeListener { v: View?, hasFocus: Boolean ->
            if (hasFocus) {
                ilName.error = null //
            }
        }

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
        // Соответственно настраивает поля и кнопки.
        when (action) {
            ACTION_ADD_USER -> {
                btnAction.setText(R.string.add)
            }

            ACTION_EDIT_USER -> {
                btnAction.setText(R.string.edit)
                if (id == 0) {
                    finish()
                }
                edPhone.setText(intent.getStringExtra(INTENT_EXTRA_PHONE))
                edName.setText(intent.getStringExtra(INTENT_EXTRA_NAME))
                setMarkColor(intent.getStringExtra(INTENT_EXTRA_COLOR)!!)
            }

            ACTION_DELETE_USER -> {
                btnAction.setText(R.string.delete)
                if (id == 0) {
                    finish()
                }
                edPhone.setText(intent.getStringExtra(INTENT_EXTRA_PHONE))
                edPhone.setEnabled(false)
                edName.setText(intent.getStringExtra(INTENT_EXTRA_NAME))
                edName.setEnabled(false)
                setMarkColor(intent.getStringExtra(INTENT_EXTRA_COLOR)!!)
                tvMark.setEnabled(false)
            }

            else -> {
                finish()
            }
        }
    }

    fun onMarkClicked(@Suppress("UNUSED_PARAMETER")ignored: View?) {
        // Функция - обработчик кнопки "метка" (выбор цвета).
        if (selectedColorTemp.isEmpty()) {
            tvMark.setTextColor(colorOnSurfaceVariant)
            tvMark.setBackgroundResource(R.drawable.ic_pin_empty)
            cardColor.strokeColor = colorOutline
            tvColorError.text = ""
        }
        val intent = Intent(this, ColorsActivity::class.java)
        startActivityIntent.launch(intent)
    }

    fun setMarkColor(color: String) {
        // Функция выставляет цвет метки.
        tvMark.text = ""
        selectedColorTemp = color
        when (Objects.requireNonNull(color)) {
            AppColors.COLOR_WHITE -> tvMark.setBackgroundResource(R.drawable.ic_pin_white)
            AppColors.COLOR_RED -> tvMark.setBackgroundResource(R.drawable.ic_pin_red)
            AppColors.COLOR_ORANGE -> tvMark.setBackgroundResource(R.drawable.ic_pin_orange)
            AppColors.COLOR_YELLOW -> tvMark.setBackgroundResource(R.drawable.ic_pin_yellow)
            AppColors.COLOR_GREEN -> tvMark.setBackgroundResource(R.drawable.ic_pin_green)
            AppColors.COLOR_DARKGREEN -> tvMark.setBackgroundResource(R.drawable.ic_pin_darkgreen)
            AppColors.COLOR_CYAN -> tvMark.setBackgroundResource(R.drawable.ic_pin_cyan)
            AppColors.COLOR_BLUE -> tvMark.setBackgroundResource(R.drawable.ic_pin_blue)
            AppColors.COLOR_VIOLET -> tvMark.setBackgroundResource(R.drawable.ic_pin_violet)
            AppColors.COLOR_MAGENTA -> tvMark.setBackgroundResource(R.drawable.ic_pin_magenta)
            else -> tvMark.setBackgroundResource(R.drawable.ic_pin_black)
        }
    }

    fun onActionClicked(@Suppress("UNUSED_PARAMETER")ignored: View?) {
        // Функция - обработчик кнопки "действие".
        val phone = edPhone.getText().toString().replace(REGEXP_CLEAR_PHONE.toRegex(), "")
        val name = edName.getText().toString()
        if (phone.isEmpty()) {
            // проверяем телефон на заполнение
            ilPhone.error = getString(R.string.err_empty_phone)
        }
        if (action == ACTION_ADD_USER && phone in Util.phones) {
            // проверяем телефон на уникальность при добавлении
            ilPhone.error = getString(R.string.err_not_unique_phone)
        }
        if (name.isEmpty()) {
            // проверяем имя на заполнение
            ilName.error = getString(R.string.err_empty_user)
        }
        if (selectedColorTemp.isEmpty()) {
            // проверяем метку на заполнение
            cardColor.strokeColor = colorError
            tvMark.setTextColor(colorError)
            tvMark.setBackgroundResource(R.drawable.ic_pin_error)
            tvColorError.setText(R.string.err_empty_label)
        }

        if (phone.isEmpty() || name.isEmpty() || selectedColorTemp.isEmpty()) {
            // если хоть что-то незаполнено - выходим
            return
        }

        when (action) {
            ACTION_ADD_USER -> {
                if (phone in Util.phones) {
                    // если при добавлении телефон не уникальный - выходим
                    return
                }
                if (DBhelper.dbHelper.addUser(phone, name, selectedColorTemp)) {
                    val intent = Intent()
                    intent.putExtra(INTENT_EXTRA_RESULT, ACTION_ADD_USER)
                    setResult(RESULT_OK, intent)
                    finish()
                } else {
                    Toast.makeText(
                        this, R.string.failed_to_add_user,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            ACTION_EDIT_USER -> {
                if (DBhelper.dbHelper.editUser(id, phone, name, selectedColorTemp)) {
                    val intent = Intent()
                    intent.putExtra(INTENT_EXTRA_RESULT, ACTION_EDIT_USER)
                    setResult(RESULT_OK, intent)
                    finish()
                } else {
                    Toast.makeText(
                        this, R.string.failed_to_change_user,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            ACTION_DELETE_USER -> {
                if (DBhelper.dbHelper.deleteUser(id)) {
                    val intent = Intent()
                    intent.putExtra(INTENT_EXTRA_RESULT, ACTION_DELETE_USER)
                    setResult(RESULT_OK, intent)
                    finish()
                } else {
                    Toast.makeText(
                        this, R.string.failed_to_delete_user,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            else -> {
                finish()
            }
        }
    }

    var startActivityIntent = registerForActivityResult(StartActivityForResult()
    // Возвращает результат выбора цвета.
    ){ result: ActivityResult? ->
        if (result!!.resultCode == RESULT_OK) {
            val intent = result.data
            intent?.let {
                setMarkColor(intent.getStringExtra(Util.INTENT_EXTRA_COLOR)!!)
            }
        }
    }

    fun onCloseClicked(@Suppress("UNUSED_PARAMETER") ignored: View?) {
        // Функция - обработчик кнопки "назад".
        finish()
    }

}