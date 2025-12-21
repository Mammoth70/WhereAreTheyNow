package ru.mammoth70.wherearetheynow

import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import android.widget.PopupMenu
import android.widget.TextView
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
        topAppBar.setTitle(phone2name[startRecord.phone])
        topAppBar.setSubtitle(timePassed(startRecord.dateTime, context))
        topAppBar.setNavigationOnClickListener {
            finish()
        }

        DBhelper.dbHelper.readMenuUsers()
        topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.itemUsers -> {
                    val gravityView: TextView = findViewById(R.id.gravityView)
                    val popupMenu = PopupMenu(context, gravityView)
                    menuPhones
                        .filter { phone -> ((phone2record.containsKey(phone)) && (phone2id.containsKey(phone))) }
                        .map { phone ->
                            phone2id[phone]?.let { id ->
                                popupMenu.menu.add(0, id, 0,phone2name[phone])
                            }
                        }
                    popupMenu.setOnMenuItemClickListener { item: MenuItem? ->
                        reloadMapFromId(context, item!!.itemId)
                        true
                    }
                    popupMenu.show()
                    true
                }
                else -> false
            }
        }
    }

    protected open fun reloadMapFromId(context: Context, id: Int) {
        // Функция вызывается из меню со списком контактов.
        // Может быть переопределена, но обычно не переопределяется.
        // Функция выводит в заголовок карты имя контакта и время получения координат.
        // После выполнения, вызывает функцию reloadMapFromPoint.
        val phone = id2phone[id]
        topAppBar.setTitle(phone2name[phone])
        topAppBar.setSubtitle(timePassed(phone2record[phone]!!.dateTime, context))
        reloadMapFromPoint( context, phone2record[phone]!!)
    }

    protected abstract fun initMap(context: Context)
    // Абстрактная функция, должна быть переопределена.
    // Вызывается из onCreate после createFrameTitle и перед reloadMapFromPoint.
    // Функция делает начальную настройку карты.

    protected abstract fun reloadMapFromPoint(context: Context, rec: PointRecord)
    // Абстрактная функция, должна быть переопределена.
    // Вызывается из из onCreate после initMap, а также из reloadMapFromId.
    // Функция перестраивает карту по передаваемой записи PoinRecord.

}