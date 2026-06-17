package ru.mammoth70.wherearetheynow

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationBarView

class MainActivity : AppActivity() {
    // Главная activity приложения.
    // Выводит список контактов и bottom Navigation bar.


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
    private val btnSync: MaterialButton by lazy { findViewById(R.id.btnSync) }


    override fun onCreate(savedInstanceState: Bundle?) {
        // Функция вызывается при создании Activity.
        // Чтение списков и словарей контактов из БД.
        // Если не хватает нужных разрешений, сразу вызывается Activity со списком разрешений.

        super.onCreate(savedInstanceState)

        topAppBar.setTitle(R.string.app_name)
        topAppBar.setNavigationOnClickListener {
            finish()
        }

        btnSync.setOnClickListener {
            // Обработчик кнопки "синхронизация через интернет".
            // Вызывает функцию параллельной отправки и приёма геолокации через интернет-сервер.
            if (!isInternetAvailable()) {
                Toast.makeText(this, getString(R.string.noInternet), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            syncLocationsInternet()
        }

        setButtonSync()

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
                    AboutDialog().show(supportFragmentManager, "ABOUT_DIALOG")
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


    @Suppress("SameReturnValue")
    private fun showContextMenu(view: View, position: Int) : Boolean {
        // Функция вызывается по длинному клику на элемент списка.

        showPopupMenu(view, position)
        return true
    }


    private fun setButtonSync() {
        // Функция настраивает видимость и доступность кнопки синхронизации.
        if (SettingsManager.useInternet
            && SettingsManager.InternetServer.isNotEmpty() && SettingsManager.InternetToken.isNotEmpty()) {
            btnSync.isEnabled = true
            btnSync.visibility = View.VISIBLE
        } else {
            btnSync.visibility = View.GONE
            btnSync.isEnabled = false
        }
    }


    private fun showPopupMenu(view: View, position: Int) {
        // Функция вызывается по клику на кнопку меню.

        val popupMenu = PopupMenu(this, view)
        popupMenu.gravity = Gravity.END
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
            // Функция передаёт обработку запроса геолокации в GetLocation.
            getAndSendLocationAsync(this, WAY_SMS,
           user.phone, true)
        }
    }


    private fun smsAnswerUser(position: Int) {
        // Функция посылает контакту геолокацию.

        val user = DataRepository.users[position]
        if (user.phone == DataRepository.myPhone) {
            selfPosition()
        } else {
            // Функция передаёт обработку запроса геолокации в GetLocation.
            getAndSendLocationAsync(this, WAY_SMS,
           user.phone, false)
        }
    }


    private fun selfPosition() {
        // Функция определяет собственную геолокацию и вызывает карту.

        getAndSendLocationAsync(this, WAY_LOCAL,
            "", false)
    }


    private fun syncLocationsInternet() {
        // Функция параллельно делает отправку и приём геолокации через интернет-сервер.
        // После выполнения обоих задач вызывает карту.

        btnSync.isEnabled = false
        var sendError: String? = null
        var recvError: String? = null
        var activeTasks = 2

        fun checkAllTasksFinished() {
            // Функция проверяет, завершились ли обе задачи и выводит результат
            activeTasks--
            if (activeTasks == 0) {
                btnSync.isEnabled = true
                if (sendError == null && recvError == null) {
                    DataRepository.lastAnswerRecord?.let {
                        viewLocation(this, it, false)
                    }
                } else {
                    val message = listOfNotNull(sendError, recvError).joinToString("\n")
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                }
            }
        }

        // Отправка геолокации на интернет-сервер.
        getAndSendLocationAsync(this, WAY_INTERNET, "", false,
            onFinished = { checkAllTasksFinished() },
            onResult = { result ->
                result.onFailure { _ ->
                    sendError = getString(R.string.setLocationError)
                }
            }
        )

        // Получение группы геолокаций с интернет-сервера.
        getLocationsInternetAsync(
            onFinished = { checkAllTasksFinished() },
            onResult = { result ->
                result.onFailure { _ ->
                    recvError = getString(R.string.getLocationsError)
                }
            }
        )

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
        // Функция возвращает результат формы контакта.

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
                setButtonSync()
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