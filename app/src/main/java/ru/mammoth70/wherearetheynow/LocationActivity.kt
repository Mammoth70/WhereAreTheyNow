package ru.mammoth70.wherearetheynow

import android.content.Context
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import ru.mammoth70.wherearetheynow.Util.INTENT_EXTRA_SMS_FROM
import ru.mammoth70.wherearetheynow.Util.INTENT_EXTRA_LATITUDE
import ru.mammoth70.wherearetheynow.Util.INTENT_EXTRA_LONGITUDE
import ru.mammoth70.wherearetheynow.Util.INTENT_EXTRA_TIME

abstract class LocationActivity : AppCompatActivity() {
    // Абстрактный класс для создания Activity вывода геолокации, переданной через intent.

    protected var startRecord: PointRecord? = null
    protected var tvName: TextView? = null
    protected var tvDateTime: TextView? = null
    protected var menuButton: Button? = null

    protected fun createFrameTitle(context: Context) {
        // Метод вызывается при создании Activity.
        // Не должен переопределяться, но должен вызываться из onCreate после вызова setContentView.
        tvName = findViewById(R.id.tvName)
        tvDateTime = findViewById(R.id.tvDateTime)
        startRecord = PointRecord(
            intent.getStringExtra(INTENT_EXTRA_SMS_FROM)!!,
            intent.getDoubleExtra(INTENT_EXTRA_LATITUDE, 0.0),
            intent.getDoubleExtra(INTENT_EXTRA_LONGITUDE, 0.0),
            intent.getStringExtra(INTENT_EXTRA_TIME)!!
        )
        tvName!!.text = Util.phone2name[startRecord!!.phone]
        tvDateTime!!.text = MapUtil.timePassed(startRecord!!.datetime, context)

        // Настроить вызов меню со списком контактов.
        Util.menuPhones.clear()
        DBhelper(context).use { dbHelper ->
            dbHelper.menuUsers
        }
        menuButton = findViewById(R.id.btnMenuUsers)
        menuButton!!.setOnClickListener { view: View? ->
            val popupMenu = PopupMenu(context, view)
            for (key in Util.menuPhones) {
                if ((Util.phone2record.containsKey(key)) && (Util.phone2id.containsKey(key))) {
                    val id = Util.phone2id[key]
                    if (id != null) {
                        popupMenu.menu.add(0, id, 0,
                            Util.phone2name[key])
                    }
                }
            }
            popupMenu.setOnMenuItemClickListener { item: MenuItem? ->
                reloadMapFromId(context, item!!.itemId)
                true
            }
            popupMenu.show() // Отобразить меню
        }
    }

    protected open fun reloadMapFromId(context: Context, id: Int) {
        // Метод выводит координаты тектом, меняя заголовок карты.
        // Вызывается из меню со списком контактов.
        // После выполнения, вызывает метод reloadMapFromPoint.
        // Может быть переопределён.
        val phone = Util.id2phone[id]
        tvName!!.text = Util.phone2name[phone]
        tvDateTime!!.text = MapUtil.timePassed(Util.phone2record[phone]!!.datetime, context)
        reloadMapFromPoint( context, Util.phone2record[phone]!!)
    }

    protected abstract fun reloadMapFromPoint(context: Context, rec: PointRecord)
    // Абстрактный класс, должен быть переопределён.
    // Вызывается из reloadMapFromPoint, а также из OnCreate.
    // Метод перестраивает карту по передаваемой записи PoinRecord.
}