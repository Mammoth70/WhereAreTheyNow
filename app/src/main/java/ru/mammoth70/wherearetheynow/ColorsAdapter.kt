package ru.mammoth70.wherearetheynow

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView

class ColorsAdapter(private val onItemClick: (Int) -> Unit ):
    RecyclerView.Adapter<ColorsAdapter.ViewHolder>() {
    // RecyclerView.Adapter для выбора цвета.

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Представление viewHolder'а для списка цветов.

        init {
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(position)
                }
            }
        }

        val itemColorLabel: TextView = view.findViewById(R.id.itemColorLabel)
        val itemCardColor: MaterialCardView = view.findViewById(R.id.frameItemCardColor)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Функция вызывается LayoutManager'ом, чтобы создать viewHolder'ы и передать им макет,
        // по которому будут отображаться элементы списка.
        return ViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.item_color, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Функция вызывается LayoutManager'ом, чтобы привязать к viewHolder'у данные, которые он должен отображать.
        holder.itemColorLabel.setBackgroundResource(
            AppColors.getMarker(AppColors.colors[position]))
        holder.itemCardColor.setCardBackgroundColor(
            AppColors.getColorAlpha16(AppColors.colors[position]))
    }

    override fun getItemCount(): Int {
        // Функция вызывается LayoutManager'ом и возвращает общее количество элементов в списке.
        return AppColors.colors.size
    }

}