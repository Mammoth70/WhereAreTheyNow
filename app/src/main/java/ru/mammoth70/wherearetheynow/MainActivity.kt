package ru.mammoth70.wherearetheynow

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.ContextMenu
import android.view.ContextMenu.ContextMenuInfo
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView.AdapterContextMenuInfo
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.get
import com.google.android.material.navigation.NavigationBarView
import ru.mammoth70.wherearetheynow.Util.INTENT_EXTRA_NEW_VERSION_REQUEST
import ru.mammoth70.wherearetheynow.Util.INTENT_EXTRA_SMS_TO

class MainActivity : AppCompatActivity() {
    // Главная activity приложения.
    // Выводит список контактов и bottom Navigatin bar.

    companion object {
        private const val NM_MAP_ID = 0
        private const val NM_USERS_ID = 1

        private const val COLUMN_PHONE = "phone"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_COLOR = "color"
        private const val COLUMN_BACK = "background"
    }

    private val tvTitle : TextView by lazy { findViewById(R.id.tvTitle) }
    private val navBarView: NavigationBarView by lazy { findViewById(R.id.bottom_navigation) }
    private val lvSimple: ListView by lazy { findViewById(R.id.lvUsersSimple) }
    private lateinit var sAdapter: SimpleAdapter
    private lateinit var data: ArrayList<MutableMap<String, Any>>

    override fun onCreate(savedInstanceState: Bundle?) {
        // Функция вызывается при создании Activity.
        // Чтение списков и словарей контактов из БД.
        // Если не хватает нужных разрешений, сразу вызывается Activity cо списком разрешений.
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main))
        { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                systemBars.left, systemBars.top,
                systemBars.right, systemBars.bottom
            )
            insets
        }

        tvTitle.setText(R.string.titleUsers)
        sAdapter = simpleAdapter
        lvSimple.setAdapter(sAdapter)
        lvSimple.isClickable = true
        registerForContextMenu(lvSimple)
        lvSimple.setOnItemClickListener { parent, view, position, id ->
            editUser(position)
        }

        if (!checkAllPermissions()) {
            startPermissionActivity()
        }

        navBarView.menu[NM_MAP_ID].isEnabled = (Util.lastAnswerRecord != null)
        navBarView.menu[NM_USERS_ID].isChecked = true
    }

    override fun onResume() {
        super.onResume()
        navBarView.menu[NM_MAP_ID].isEnabled = (Util.lastAnswerRecord != null)
    }

    override fun onCreateContextMenu(
        menu: ContextMenu, v: View?,
        menuInfo: ContextMenuInfo?
    ) {
        // Функция создаёт контекстное меню
        super.onCreateContextMenu(menu, v, menuInfo)
        val inflater = MenuInflater(this)
        inflater.inflate(R.menu.context_menu, menu)
        menu.setGroupDividerEnabled(true)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        // Функция обрабатывает контекстное меню.
        val acmi = item.menuInfo as AdapterContextMenuInfo?
        if (acmi == null) {
            return false
        }
        when (item.itemId) {
            R.id.item_add_user -> {
                addUser()
                return true
            }
            R.id.item_edit_user -> {
                editUser(acmi.position)
                return true
            }
            R.id.item_delete_user -> {
                deleteUser(acmi.position)
                return true
            }
            R.id.item_sms_request_user -> {
                smsRequestUser(acmi.position)
                return true
            }
            R.id.item_sms_answer_user -> {
                smsAnswerUser(acmi.position)
                return true
            }
            else -> return super.onContextItemSelected(item)
        }
    }

    private fun addUser() {
        // Функция добавляет контакт.
        val intent = Intent(this, UserActivity::class.java)
        intent.putExtra(UserActivity.INTENT_EXTRA_ACTION,
            UserActivity.ACTION_ADD_USER)
        startActivityUserIntent.launch(intent)
    }

    private fun editUser(position: Int) {
        // Функция редактирует контакт.
        val intent = Intent(this, UserActivity::class.java)
        intent.putExtra(UserActivity.INTENT_EXTRA_ACTION,
            UserActivity.ACTION_EDIT_USER)
        val phone = Util.phones[position]
        intent.putExtra(UserActivity.INTENT_EXTRA_ID, Util.phone2id[phone])
        intent.putExtra(UserActivity.INTENT_EXTRA_PHONE, phone)
        intent.putExtra(UserActivity.INTENT_EXTRA_NAME, Util.phone2name[phone])
        intent.putExtra(UserActivity.INTENT_EXTRA_COLOR, Util.phone2color[phone])
        startActivityUserIntent.launch(intent)
    }

    private fun deleteUser(position: Int) {
        // Функция удаляет контакт.
        val intent = Intent(this, UserActivity::class.java)
        intent.putExtra(UserActivity.INTENT_EXTRA_ACTION,
            UserActivity.ACTION_DELETE_USER)
        val phone = Util.phones[position]
        intent.putExtra(UserActivity.INTENT_EXTRA_ID, Util.phone2id[phone])
        intent.putExtra(UserActivity.INTENT_EXTRA_PHONE, phone)
        intent.putExtra(UserActivity.INTENT_EXTRA_NAME, Util.phone2name[phone])
        intent.putExtra(UserActivity.INTENT_EXTRA_COLOR, Util.phone2color[phone])
        startActivityUserIntent.launch(intent)
    }

    private fun smsRequestUser(position: Int) {
        // Функция посылает контакту запрос координат.
        val phone = Util.phones[position]
        if (phone == Util.myphone) {
            selfPosition()
        } else {
            if (Util.useService) {
                // Функция передаёт обработку запроса геолокации в GetLocationService.
                val intent = Intent(this, GetLocationService::class.java)
                intent.putExtra(INTENT_EXTRA_SMS_TO, phone)
                intent.putExtra(INTENT_EXTRA_NEW_VERSION_REQUEST, true)
                this.startService(intent)
            } else {
                // Функция передаёт обработку запроса геолокации в GetLocation.
                val getLocation = GetLocation()
                getLocation.sendLocation(this, GetLocation.WAY_SMS,
                    phone, true)
            }
        }
    }

    private fun smsAnswerUser(position: Int) {
        // Функция посылает контакту геолокацию.
        val phone = Util.phones[position]
        if (phone == Util.myphone) {
            selfPosition()
        } else {
            if (Util.useService) {
                // Функция передаёт обработку запроса геолокации в GetLocationService.
                val intent = Intent(this, GetLocationService::class.java)
                intent.putExtra(INTENT_EXTRA_SMS_TO, phone)
                this.startService(intent)
            } else {
                // Функция передаёт обработку запроса геолокации в GetLocation.
                val getLocation = GetLocation()
                getLocation.sendLocation(this, GetLocation.WAY_SMS,
                    phone, false)
            }
        }
    }

    private fun selfPosition() {
        // Функция определяет собственную геолокацию и вызывает карту.
        val getLocation = GetLocation()
        getLocation.sendLocation(this, GetLocation.WAY_LOCAL,
            "", false)
    }

    private fun refreshData() {
        // Функция обновляет данные для списка контактов из БД.
        data.clear()
        for (phone in Util.phones) {
            val m: MutableMap<String, Any> = HashMap()
            m.put(COLUMN_PHONE, phone)
            m.put(COLUMN_NAME, Util.phone2name[phone]!!)
            m.put(COLUMN_COLOR, Util.phone2color[phone]!!)
            m.put(COLUMN_BACK, Util.phone2color[phone]!!)
            data.add(m)
        }
    }

    fun onPermissionClicked(@Suppress("UNUSED_PARAMETER")ignored: MenuItem?) {
        // Функция - обработчик кнопки меню "разрешения".
        // Вызывает соответствующую Activity.
        startPermissionActivity()
    }

    fun onMapClicked(@Suppress("UNUSED_PARAMETER")ignored: MenuItem?) {
        // Функция - обработчик кнопки меню "карта".
        // Вызывает соответствующую Activity.
        Util.lastAnswerRecord.let {
            MapUtil.viewLocation(this, Util.lastAnswerRecord!!, false)
        }
    }

    fun onSettingsClicked(@Suppress("UNUSED_PARAMETER")ignored: MenuItem?) {
        // Функция - обработчик кнопки меню "настройки".
        // Вызывает соответствующую Activity.
        val intent = Intent(this, SettingsActivity::class.java)
        startActivitySettingsIntent.launch(intent)
    }

    private fun startPermissionActivity() {
        // Функция запускает PermissionActivity.
        val intent = Intent(this, PermissionActivity::class.java)
        startActivity(intent)
    }

    private fun checkAllPermissions(): Boolean {
        // Функция проверяет все необходимые разрешения
        // (если их не хватает, нужно запустить PermissionActivity).
        return ((ContextCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) == PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.RECEIVE_SMS
                ) == PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.SEND_SMS
                ) == PackageManager.PERMISSION_GRANTED))
    }

    fun onAddUserClicked(@Suppress("UNUSED_PARAMETER")ignored: View?) {
        // Функция - обработчик кнопки FAB "Добавить контакт".
        addUser()
    }

    fun onAboutClicked(@Suppress("UNUSED_PARAMETER")ignored: MenuItem?) {
        // Функция - обработчик кнопки меню "about".
        val bundle = Bundle()
        bundle.putString(AboutBox.DIALOG_TITLE, getString(R.string.app_name))
        val text =
            getString(R.string.description) + "\n" +
                    getString(R.string.version) + " " +
                    BuildConfig.VERSION_NAME
        bundle.putString(AboutBox.DIALOG_MESSAGE, text)
        val aboutBox = AboutBox()
        aboutBox.setArguments(bundle)
        aboutBox.show(this.supportFragmentManager, "MESSAGE_DIALOG")
    }

    private val simpleAdapter: SimpleAdapter
        get() {
            // Функция создаёт и заполняет SimpleAdapter.
            data = ArrayList(Util.phones.size)
            refreshData()
            val from = arrayOf(COLUMN_PHONE, COLUMN_NAME, COLUMN_COLOR, COLUMN_BACK)
            val to = intArrayOf(
                R.id.itemUserPhone,
                R.id.itemUserName,
                R.id.itemUserLabel,
                R.id.itemUserLayout
            )
            val sAdapter = SimpleAdapter(this, data, R.layout.item_user, from, to)
            sAdapter.viewBinder = ViewBinder()
            return sAdapter
        }

    private class ViewBinder : SimpleAdapter.ViewBinder {
        // Функция обрабатывает форматирование вывода на экран списка контактов.
        override fun setViewValue(view: View, data: Any?, textRepresentation: String?): Boolean {
            when (view.id) {
                R.id.itemUserLayout -> {
                    view.setBackgroundColor(AppColors.getColorAlpha16(data as String?))
                    return true
                }
                R.id.itemUserLabel -> {
                    view.setBackgroundResource(AppColors.getMarker(data as String?))
                    return true
                }
                else -> return false
            }
        }
    }

    var startActivityUserIntent = registerForActivityResult(StartActivityForResult()
        // Функция возвращает результат формы контакта
    ){ result: ActivityResult? ->
        if (result!!.resultCode == RESULT_OK) {
            refreshData()
            sAdapter.notifyDataSetChanged()
        }
    }

    var startActivitySettingsIntent = registerForActivityResult(StartActivityForResult()
        // Функция возвращает результат вызова формы настроек.
    ){ result: ActivityResult? ->
        if (result!!.resultCode == RESULT_OK) {
            val intent = result.data
            intent?.let {
                if (intent.getBooleanExtra(SettingsActivity.INTENT_EXTRA_RESULT,
                        false)) {
                    recreate()
                }
            }
        }
    }

}