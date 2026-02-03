package ru.mammoth70.wherearetheynow

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationBarView

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
    private val usersAdapter by lazy { UsersAdapter(
        ::editUser,
        ::showContextMenu,
        ::showPopupMenu,
        ::selfPosition) }
    private val floatingActionButtonAdd: FloatingActionButton by lazy { findViewById(R.id.floatingActionButtonAdd) }
    private val recyclerView: RecyclerView by lazy { findViewById(R.id.itemUsersRecycler) }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Функция вызывается при создании Activity.
        // Чтение списков и словарей контактов из БД.
        // Если не хватает нужных разрешений, сразу вызывается Activity cо списком разрешений.

        super.onCreate(savedInstanceState)

        topAppBar.setTitle(R.string.app_name)
        topAppBar.setNavigationOnClickListener {
            finish()
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = usersAdapter
        usersAdapter.submitList(DataRepository.users.toList())

        if (!checkAllPermissions()) {
            startPermissionActivity()
        }

        navBarView.menu[NM_MAP_ID].isEnabled = (DataRepository.lastAnswerRecord != null)
        navBarView.menu[NM_USERS_ID].isChecked = true
        navBarView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.item_map -> {
                    // Обработчик кнопки меню "карта".
                    // Вызывает соответствующую Activity.
                    DataRepository.lastAnswerRecord?.let {
                        viewLocation(this, it, false)
                    }
                }

                R.id.item_settings -> {
                    // Обработчик кнопки меню "настройки".
                    // Вызывает соответствующую Activity.
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivitySettingsIntent.launch(intent)
                }

                R.id.item_permissions -> {
                    // Обработчик кнопки меню "разрешения".
                    // Вызывает соответствующую Activity.
                    startPermissionActivity()
                }

                R.id.item_about -> {
                    // Обработчик кнопки меню "about".
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

            }
            false
        }

        floatingActionButtonAdd.setOnClickListener { _ ->
            // Обработчик кнопки FAB "Добавить контакт".
            addUser()
        }
    }

    override fun onResume() {
        super.onResume()
        navBarView.menu[NM_MAP_ID].isEnabled = (DataRepository.lastAnswerRecord != null)
    }

    private fun showContextMenu(view: View, position: Int) : Boolean {
        // Функция вызывается по длинному клику на элемент списка.

        showPopupMenu(view, position)
        return true
    }

    private fun showPopupMenu(view: View, position: Int) {
        // Функция вызывается по клику на кнопку меню.

        val popupMenu = PopupMenu(this, view)
        popupMenu.inflate(R.menu.user_menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.item_add_user -> {
                    addUser()
                    true
                }

                R.id.item_edit_user -> {
                    editUser(position)
                    true
                }

                R.id.item_delete_user -> {
                    deleteUser(position)
                    true
                }

                R.id.item_sms_request_user -> {
                    smsRequestUser(position)
                    true
                }

                R.id.item_sms_answer_user -> {
                    smsAnswerUser(position)
                    true
                }

                else -> false
            }
        }
        popupMenu.show()
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

        val user = DataRepository.users[position]
        val intent = Intent(this, UserActivity::class.java)
        intent.putExtra(UserActivity.INTENT_EXTRA_ACTION,
            UserActivity.ACTION_EDIT_USER)
        intent.putExtra(UserActivity.INTENT_EXTRA_ID, user.id)
        intent.putExtra(UserActivity.INTENT_EXTRA_PHONE, user.phone)
        intent.putExtra(UserActivity.INTENT_EXTRA_NAME, user.name)
        intent.putExtra(UserActivity.INTENT_EXTRA_COLOR, user.color)
        startActivityUserIntent.launch(intent)
    }

    private fun deleteUser(position: Int) {
        // Функция удаляет контакт.

        val user = DataRepository.users[position]
        val intent = Intent(this, UserActivity::class.java)
        intent.putExtra(UserActivity.INTENT_EXTRA_ACTION,
            UserActivity.ACTION_DELETE_USER)
        intent.putExtra(UserActivity.INTENT_EXTRA_ID, user.id)
        intent.putExtra(UserActivity.INTENT_EXTRA_PHONE, user.phone)
        intent.putExtra(UserActivity.INTENT_EXTRA_NAME, user.name)
        intent.putExtra(UserActivity.INTENT_EXTRA_COLOR, user.color)
        startActivityUserIntent.launch(intent)
    }

    private fun smsRequestUser(position: Int) {
        // Функция посылает контакту запрос координат.

        val user = DataRepository.users[position]
        if (user.phone == DataRepository.myPhone) {
            selfPosition()
        } else {
            if (SettingsManager.useService) {
                // Функция передаёт обработку запроса геолокации в GetLocationService.
                val intent = Intent(this, GetLocationService::class.java)
                intent.putExtra(INTENT_EXTRA_SMS_TO, user.phone)
                intent.putExtra(INTENT_EXTRA_NEW_VERSION_REQUEST, true)
                this.startService(intent)
            } else {
                // Функция передаёт обработку запроса геолокации в GetLocation.
                val getLocation = GetLocation()
                getLocation.sendLocation(this, GetLocation.WAY_SMS,
                    user.phone, true)
            }
        }
    }

    private fun smsAnswerUser(position: Int) {
        // Функция посылает контакту геолокацию.

        val user = DataRepository.users[position]
        if (user.phone == DataRepository.myPhone) {
            selfPosition()
        } else {
            if (SettingsManager.useService) {
                // Функция передаёт обработку запроса геолокации в GetLocationService.
                val intent = Intent(this, GetLocationService::class.java)
                intent.putExtra(INTENT_EXTRA_SMS_TO, user.phone)
                this.startService(intent)
            } else {
                // Функция передаёт обработку запроса геолокации в GetLocation.
                val getLocation = GetLocation()
                getLocation.sendLocation(this, GetLocation.WAY_SMS,
                    user.phone, false)
            }
        }
    }

    private fun selfPosition() {
        // Функция определяет собственную геолокацию и вызывает карту.

        val getLocation = GetLocation()
        getLocation.sendLocation(this, GetLocation.WAY_LOCAL,
            "", false)
    }

    private fun startPermissionActivity() {
        // Функция запускает PermissionActivity.

        val intent = Intent(this, PermissionActivity::class.java)
        startActivity(intent)
    }

    private fun checkAllPermissions(): Boolean {
        // Функция проверяет все необходимые разрешения
        // (если их не хватает, нужно запустить PermissionActivity).

        val basePermissions = ((ContextCompat.checkSelfPermission(
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

        val notificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Проверка уведомлений только для Android 13 (API 33) и выше.
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // На старых версиях считаем, что разрешение есть.
        }

        return basePermissions && notificationPermission
    }

    var startActivityUserIntent = registerForActivityResult(StartActivityForResult()
        // Функция возвращает результат формы контакта

    ){ result: ActivityResult? ->
        if (result!!.resultCode == RESULT_OK) {
            usersAdapter.submitList(DataRepository.users.toList())
        }
    }

    var startActivitySettingsIntent = registerForActivityResult(StartActivityForResult()
        // Функция возвращает результат вызова формы настроек.

    ){ result: ActivityResult? ->
        if (result!!.resultCode == RESULT_OK) {
            val intent = result.data
            intent?.let {
                val themeChanged = intent.getBooleanExtra(SettingsActivity.INTENT_THEME_COLOR_CHANGED, false)
                val phoneChanged = intent.getBooleanExtra(SettingsActivity.INTENT_PHONE_CHANGED, false)
                when {
                    themeChanged -> {
                        recreate()
                    }

                    phoneChanged -> {
                        recyclerView.adapter = usersAdapter
                        usersAdapter.submitList(DataRepository.users.toList())
                    }

                    else -> {
                        //
                    }
                }
            }
        }
    }

}