package ru.mammoth70.wherearetheynow

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView

class ColorsAdapter: RecyclerView.Adapter<ColorsAdapter.ViewHolder>() {
    // RecyclerView.Adapter для выбора цвета.

    private var onClickListener: OnClickListener? = null

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Представление viewHolder'а для списка цветов.
        val itemColorLabel: TextView = view.findViewById(R.id.itemColorLabel)
        val itemColorLayout: ConstraintLayout = view.findViewById(R.id.itemColorLayout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Функция вызывается LayoutManager'ом, чтобы создать viewHolder'ы и передать им макет,
        // по которому будут отображаться элементы списка.
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_color, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Функция вызывается LayoutManager'ом, чтобы привязать к viewHolder'у данные, которые он должен отображать.
        holder.itemColorLabel.setBackgroundResource(
            AppColors.getMarker(AppColors.colors[position]))
        holder.itemColorLayout.setBackgroundColor(
            AppColors.getColorAlpha16(AppColors.colors[position]))
        holder.itemView.setOnClickListener {
            onClickListener?.onClick(position)
        }
    }

    override fun getItemCount(): Int {
        // Функция вызывается LayoutManager'ом и возвращает общее количество элементов в списке.
        return AppColors.colors.size
    }

    fun setOnClickListener(listener: OnClickListener?) {
        // Функция устанавливает click listener для адаптера
        this.onClickListener = listener
    }

    interface OnClickListener {
        // Интерфейс для click listener
        fun onClick(position: Int)
    }

}