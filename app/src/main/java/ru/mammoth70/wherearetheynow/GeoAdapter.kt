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
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Представление viewHolder'а для списка контактов с координатами.
        val itemUserName: TextView = view.findViewById(R.id.itemUserName)
        val itemLattitude: TextView = view.findViewById(R.id.itemLattitude)
        val itemLongitude: TextView = view.findViewById(R.id.itemLongitude)
        val itemDate: TextView = view.findViewById(R.id.itemDate)
        val itemCard: MaterialCardView = view.findViewById(R.id.itemUserCard)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Функция вызывается LayoutManager'ом, чтобы создать viewHolder'ы и передать им макет,
        // по которому будут отображаться элементы списка.
        phones2.clear()
        for (phone in Util.phones) {
            if (Util.phone2record.containsKey(phone)) {
                phones2.add(phone)
            }
        }
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_geo, parent, false)
        return ViewHolder(view)
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
        holder.itemCard.setCardBackgroundColor(
            AppColors.getColorAlpha16(Util.phone2color[phone]))
    }

    override fun getItemCount(): Int {
        // Функция вызывается LayoutManager'ом и возвращает общее количество элементов в списке.
        return Util.phone2record.size
    }

}