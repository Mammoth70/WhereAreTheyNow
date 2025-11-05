package ru.mammoth70.wherearetheynow

import android.content.Context
import android.view.MenuItem
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import ru.mammoth70.wherearetheynow.Util.INTENT_EXTRA_LATITUDE
import ru.mammoth70.wherearetheynow.Util.INTENT_EXTRA_LONGITUDE
import ru.mammoth70.wherearetheynow.Util.INTENT_EXTRA_SMS_FROM
import ru.mammoth70.wherearetheynow.Util.INTENT_EXTRA_TIME

abstract class LocationActivity : AppCompatActivity() {
    // Абстрактный класс для создания Activity вывода геолокации, переданной через intent.

    protected val startRecord: PointRecord by lazy { PointRecord(
               intent.getStringExtra(INTENT_EXTRA_SMS_FROM)!!,
               intent.getDoubleExtra(INTENT_EXTRA_LATITUDE, 0.0),
               intent.getDoubleExtra(INTENT_EXTRA_LONGITUDE, 0.0),
               intent.getStringExtra(INTENT_EXTRA_TIME)!!) }
    protected val topAppBar: MaterialToolbar by lazy { findViewById(R.id.topAppBarMap) }

    protected fun createFrameTitle(context: Context) {
        // Функция вызывается при создании Activity.
        // Не должна переопределяться, но должна вызываться из onCreate после вызова setContentView.
        topAppBar.setTitle(Util.phone2name[startRecord.phone])
        topAppBar.setSubtitle(MapUtil.timePassed(startRecord.dateTime, context))
        topAppBar.setNavigationOnClickListener {
            finish()
        }

        // Настройка меню со списком контактов.
        DBhelper.dbHelper.readMenuUsers()
        topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.itemUsers -> {
                    val gravityView: TextView = findViewById(R.id.gravityView)
                    val popupMenu = PopupMenu(context, gravityView)
                    for (key in Util.menuPhones) {
                        if ((Util.phone2record.containsKey(key)) && (Util.phone2id.containsKey(key))) {
                            val id = Util.phone2id[key]
                            id?.let {
                                popupMenu.menu.add(0, id, 0,
                                    Util.phone2name[key])
                            }
                        }
                    }
                    popupMenu.setOnMenuItemClickListener { item: MenuItem? ->
                        reloadMapFromId(context, item!!.itemId)
                        true
                    }
                    popupMenu.show() // Отображение меню со списком контактов.
                    true
                }
                else -> false
            }
        }
    }

    protected open fun reloadMapFromId(context: Context, id: Int) {
        // Функция выводит координаты тектом, меняя заголовок карты.
        // Вызывается из меню со списком контактов.
        // После выполнения, вызывает функцию reloadMapFromPoint.
        // Может быть переопределена.
        val phone = Util.id2phone[id]
        topAppBar.setTitle(Util.phone2name[phone])
        topAppBar.setSubtitle(MapUtil.timePassed(Util.phone2record[phone]!!.dateTime, context))
        reloadMapFromPoint( context, Util.phone2record[phone]!!)
    }

    protected abstract fun reloadMapFromPoint(context: Context, rec: PointRecord)
    // Абстрактная функция, должна быть переопределена.
    // Вызывается из reloadMapFromPoint, а также из OnCreate.
    // Функция перестраивает карту по передаваемой записи PoinRecord.

}