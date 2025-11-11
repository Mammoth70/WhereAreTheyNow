package ru.mammoth70.wherearetheynow

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import java.util.Locale

class GeoAdapter: RecyclerView.Adapter<GeoAdapter.ViewHolder>() {
    // RecyclerView.Adapter для списка контактов с координатами.

    companion object {
        private val phones2: ArrayList<String> = ArrayList() // список телефонов, у которых есть point
        const val FIRST_ITEM_VIEW = 1
        const val CENTER_ITEM_VIEW = 2
        const val LAST_ITEM_VIEW = 3
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Представление viewHolder'а для списка контактов с координатами.
        val itemUserName: TextView = view.findViewById(R.id.itemUserNameGeo)
        val itemLattitude: TextView = view.findViewById(R.id.itemLattitudeGeo)
        val itemLongitude: TextView = view.findViewById(R.id.itemLongitudeGeo)
        val itemDate: TextView = view.findViewById(R.id.itemDateGeo)
        val itemCardGeo: MaterialCardView = view.findViewById(R.id.frameItemCardGeo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Функция вызывается LayoutManager'ом, чтобы создать viewHolder'ы и передать им макет,
        // по которому будут отображаться элементы списка.
        phones2.clear()
        Util.phones.filter { Util.phone2record.containsKey(it) }.map { phones2.add(it) }

        return when (viewType) {
            FIRST_ITEM_VIEW -> {
                ViewHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_geo_first, parent, false))
            }

            LAST_ITEM_VIEW -> {
                ViewHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_geo_last, parent, false))
            }

            else -> {
                ViewHolder(view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_geo, parent, false))
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Функция вызывается LayoutManager'ом, чтобы привязать к viewHolder'у данные, которые он должен отображать.
        val phone = phones2[position]
        val value = Util.phone2record[phone]
        holder.itemUserName.text = Util.phone2name[phone]
        holder.itemLattitude.text = String.format(
            Locale.US, PointRecord.FORMAT_DOUBLE,
            value!!.latitude
        )
        holder.itemLongitude.text = String.format(
            Locale.US, PointRecord.FORMAT_DOUBLE,
            value.longitude
        )
        holder.itemDate.text = value.dateTime
        holder.itemCardGeo.setCardBackgroundColor(
            AppColors.getColorAlpha16(Util.phone2color[phone]))
    }

    override fun getItemCount(): Int {
        // Функция вызывается LayoutManager'ом и возвращает общее количество элементов в списке.
        return Util.phone2record.size
    }

    override fun getItemViewType(position: Int): Int {
        // Функция определяет тип элемента.
        return when (position) {
            0 -> FIRST_ITEM_VIEW
            (Util.phone2record.size-1) -> LAST_ITEM_VIEW
            else -> CENTER_ITEM_VIEW
        }
    }

}