package ru.mammoth70.wherearetheynow

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView

class ColorsAdapter(private val onItemClick: (Int) -> Unit ):
    RecyclerView.Adapter<ColorsAdapter.ColorViewHolder>() {
    // RecyclerView.Adapter для выбора цвета.

    class ColorViewHolder(view: View, onItemClick: (Int) -> Unit) : RecyclerView.ViewHolder(view) {
        // Представление viewHolder'а для списка цветов.

        private val itemColorLabel: TextView = view.findViewById(R.id.itemColorLabel)
        private val itemCardColor: MaterialCardView = view.findViewById(R.id.frameItemCardColor)

        init {
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(position)
                }
            }
        }

        fun bind(color: String) {
            // Функция связывает цвет элементами макета.

            itemColorLabel.setBackgroundResource(AppColors.getMarker(color))
            itemCardColor.setCardBackgroundColor(AppColors.getColorAlpha16(color))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        // Функция вызывается LayoutManager'ом, чтобы создать viewHolder'ы и передать им макет,
        // по которому будут отображаться элементы списка.

        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_color,
            parent, false)
        return ColorViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
        // Функция вызывается LayoutManager'ом, чтобы привязать к viewHolder'у данные, которые он должен отображать.

        holder.bind(AppColors.colors[position])
    }

    override fun getItemCount(): Int {
        // Функция вызывается LayoutManager'ом и возвращает общее количество элементов в списке.

        return AppColors.colors.size
    }

}