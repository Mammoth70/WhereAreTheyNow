package ru.mammoth70.wherearetheynow

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import java.util.Locale

class GeoAdapter(
    private val data: List<User> // Передаем уже отфильтрованный список телефонов
) : RecyclerView.Adapter<GeoAdapter.GeoViewHolder>() {
    // RecyclerView adapter для списка контактов с координатами.


    companion object {
        private const val FIRST_ITEM_VIEW = 1
        private const val CENTER_ITEM_VIEW = 2
        private const val LAST_ITEM_VIEW = 3
    }


    class GeoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Представление viewHolder'а для списка контактов с координатами.

        private val itemUserName: TextView = view.findViewById(R.id.itemUserNameGeo)
        private val itemLattitude: TextView = view.findViewById(R.id.itemLattitudeGeo)
        private val itemLongitude: TextView = view.findViewById(R.id.itemLongitudeGeo)
        private val itemDate: TextView = view.findViewById(R.id.itemDateGeo)
        private val itemCardGeo: MaterialCardView = view.findViewById(R.id.frameItemCardGeo)

        fun bind(user: User) {
            val record = user.lastRecord ?: return
            itemUserName.text = user.name
            itemLattitude.text = String.format(Locale.US, PointRecord.FORMAT_DOUBLE, record.latitude)
            itemLongitude.text = String.format(Locale.US, PointRecord.FORMAT_DOUBLE, record.longitude)
            itemDate.text = record.dateTime
            itemCardGeo.setCardBackgroundColor(PinColors.getColorAlpha16(user.color))
        }

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GeoViewHolder {
        // Функция вызывается LayoutManager'ом, чтобы создать viewHolder'ы и передать им макет,
        // по которому будут отображаться элементы списка.

        val layoutRes = when (viewType) {
            FIRST_ITEM_VIEW -> R.layout.item_geo_first
            LAST_ITEM_VIEW -> R.layout.item_geo_last
            else -> R.layout.item_geo
        }
        val view = LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)
        return GeoViewHolder(view)
    }


    override fun onBindViewHolder(holder: GeoViewHolder, position: Int) {
        // Функция вызывается LayoutManager'ом, чтобы привязать к viewHolder'у данные, которые он должен отображать.

        holder.bind(data[position])
    }


    override fun getItemCount(): Int {
        // Функция вызывается LayoutManager'ом и возвращает общее количество элементов в списке.

        return data.size
    }


    override fun getItemViewType(position: Int): Int {
        // Функция определяет тип элемента.

        if (data.size <= 1) return CENTER_ITEM_VIEW
        return when (position) {
            0 -> FIRST_ITEM_VIEW
            (data.size-1) -> LAST_ITEM_VIEW
            else -> CENTER_ITEM_VIEW
        }
    }

}