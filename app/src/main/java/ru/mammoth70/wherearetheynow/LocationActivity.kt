package ru.mammoth70.wherearetheynow

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.PopupMenu
import com.google.android.material.appbar.MaterialToolbar

abstract class LocationActivity : AppActivity() {
    // Абстрактный класс для создания Activity вывода геолокации, переданной через intent.


    protected val startRecord: PointRecord by lazy { PointRecord(
        intent.getStringExtra(INTENT_EXTRA_SMS_FROM)!!,
        intent.getDoubleExtra(INTENT_EXTRA_LATITUDE, 0.0),
        intent.getDoubleExtra(INTENT_EXTRA_LONGITUDE, 0.0),
        intent.getStringExtra(INTENT_EXTRA_TIME)!!) }

    override val topAppBar: MaterialToolbar by lazy { findViewById(R.id.topAppBarMap) }


    override fun onCreate(savedInstanceState: Bundle?) {
        // Функция вызывается при создании Activity.
        // Может быть переопределена, но обычно не переопределяется.

        super.onCreate(savedInstanceState)

        createFrameTitle(this)
        initMap(this)
        reloadMapFromPoint(this, startRecord)
    }


    protected fun createFrameTitle(context: Context) {
        // Функция вызывается из onCreate после вызова setContentView.
        // Не может переопределяться.
        // Функция выводит в заголовок карты имя контакта и время получения координат из записи startRecord,
        // а также создаёт меню со списком контактов в панели заголовка.

        topAppBar.setTitle(DataRepository.getUser(startRecord.phone)?.name)
        topAppBar.setSubtitle(timePassed(startRecord.dateTime, context))
        topAppBar.setNavigationOnClickListener {
            finish()
        }

        topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.itemUsers -> {
                    val anchorView = findViewById<View>(R.id.itemUsers)

                    // Если вдруг не нашли (бывает в редких случаях), используем сам тулбар
                    val popupMenu = PopupMenu(context, anchorView ?: topAppBar)
                    popupMenu.gravity = Gravity.END

                    val menuMapping = mutableMapOf<Int, Long>() // Карта соответствия id и пункта меню.

                    DataRepository.menuPhones.forEachIndexed { index, phone ->
                        DataRepository.getUser(phone)?.let { user ->
                            menuMapping[index] = user.id
                            popupMenu.menu.add(0, index, 0, user.name)
                        }
                    }

                    popupMenu.setOnDismissListener {
                        menuMapping.clear()
                    }

                    popupMenu.setOnMenuItemClickListener { item ->
                        val userId = menuMapping[item.itemId]
                        userId?.let { reloadMapFromId(context, userId) }
                        true
                    }

                    popupMenu.show()
                    true
                }

                else -> false
            }
        }
    }


    protected open fun reloadMapFromId(context: Context, id: Long) {
        // Функция вызывается из меню со списком контактов.
        // Может быть переопределена, но обычно не переопределяется.
        // Функция выводит в заголовок карты имя контакта и время получения координат.
        // После выполнения, вызывает функцию reloadMapFromPoint.

        val user = DataRepository.getUser(id) ?: return
        val record = user.lastRecord ?: return
        topAppBar.setTitle(user.name)
        topAppBar.setSubtitle(timePassed(record.dateTime, context))
        reloadMapFromPoint(context, record)
    }


    protected abstract fun initMap(context: Context)
    // Абстрактная функция, должна быть переопределена.
    // Вызывается из onCreate после createFrameTitle и перед reloadMapFromPoint.
    // Функция делает начальную настройку карты.


    protected abstract fun reloadMapFromPoint(context: Context, rec: PointRecord)
    // Абстрактная функция, должна быть переопределена.
    // Вызывается из onCreate после initMap, вызывается также из reloadMapFromId.
    // Функция перестраивает карту по передаваемой записи PointRecord.

}