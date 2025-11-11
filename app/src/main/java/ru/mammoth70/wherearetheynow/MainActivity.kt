package ru.mammoth70.wherearetheynow

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationBarView
import ru.mammoth70.wherearetheynow.Util.INTENT_EXTRA_NEW_VERSION_REQUEST
import ru.mammoth70.wherearetheynow.Util.INTENT_EXTRA_SMS_TO

class MainActivity : AppActivity() {
    // Главная activity приложения.
    // Выводит список контактов и bottom Navigatin bar.

    override val idLayout = R.layout.activity_main
    override val idActivity = R.id.frameMainActivity

    companion object {
        private const val NM_MAP_ID = 0
        private const val NM_USERS_ID = 1
    }

    private val navBarView: NavigationBarView by lazy { findViewById(R.id.bottom_navigation) }
    private val usersAdapter by lazy { UsersAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Функция вызывается при создании Activity.
        // Чтение списков и словарей контактов из БД.
        // Если не хватает нужных разрешений, сразу вызывается Activity cо списком разрешений.
        super.onCreate(savedInstanceState)

        topAppBar.setTitle(R.string.app_name)
        topAppBar.setNavigationOnClickListener {
            finish()
        }

        val recyclerView: RecyclerView =  findViewById(R.id.itemUsersRecycler)
        recyclerView.layoutManager = LinearLayoutManager(this)
        usersAdapter.setOnBtnMenuClick(::showPopupMenu)
        usersAdapter.setOnBtnSelfClick(::selfPosition)
        usersAdapter.setOnItemViewClick(::editUser)
        usersAdapter.setOnItemViewLongClick(::showContextMenu)
        recyclerView.adapter = usersAdapter

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

    private fun showContextMenu(view: View) : Boolean {
        // Функция вызывается по длинному клику на элемент списка.
        showPopupMenu(view)
        return true
    }

    private fun showPopupMenu(view: View) {
        // Функция вызывается по клику на кнопку меню.
        val position: Int? = view.tag as Int?
        position?.let {
            val popupMenu = PopupMenu(this, view)
            popupMenu.inflate(R.menu.user_menu)
            popupMenu.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {
                override fun onMenuItemClick(item: MenuItem): Boolean {
                    when (item.itemId) {
                        R.id.item_add_user -> {
                            addUser()
                            return true
                        }

                        R.id.item_edit_user -> {
                            editUser(position)
                            return true
                        }

                        R.id.item_delete_user -> {
                            deleteUser(position)
                            return true
                        }

                        R.id.item_sms_request_user -> {
                            smsRequestUser(position)
                            return true
                        }

                        R.id.item_sms_answer_user -> {
                            smsAnswerUser(position)
                            return true
                        }

                        else -> return false
                    }
                }
            })
            popupMenu.show()
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
    private fun selfPosition(@Suppress("UNUSED_PARAMETER")ignored: View) {
        // Функция вызывается по клику на кнопку self.
        selfPosition()
    }
    private fun selfPosition() {
        // Функция определяет собственную геолокацию и вызывает карту.
        val getLocation = GetLocation()
        getLocation.sendLocation(this, GetLocation.WAY_LOCAL,
            "", false)
    }

    fun onPermissionClicked(@Suppress("UNUSED_PARAMETER")ignored: MenuItem?) {
        // Функция - обработчик кнопки меню "разрешения".
        // Вызывает соответствующую Activity.
        startPermissionActivity()
    }

    fun onMapClicked(@Suppress("UNUSED_PARAMETER")ignored: MenuItem?) {
        // Функция - обработчик кнопки меню "карта".
        // Вызывает соответствующую Activity.
        Util.lastAnswerRecord?.let {
            MapUtil.viewLocation(this, it, false)
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

    @SuppressLint("NotifyDataSetChanged")
    var startActivityUserIntent = registerForActivityResult(StartActivityForResult()
        // Функция возвращает результат формы контакта
    ){ result: ActivityResult? ->
        if (result!!.resultCode == RESULT_OK) {
            usersAdapter.notifyDataSetChanged()
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