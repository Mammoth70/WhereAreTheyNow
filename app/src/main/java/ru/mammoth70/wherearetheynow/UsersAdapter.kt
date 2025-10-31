package ru.mammoth70.wherearetheynow

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView

class UsersAdapter: RecyclerView.Adapter<UsersAdapter.GenericViewHolder>() {
    // RecyclerView.Adapter для списка контактов.

    private var itemViewClick: (position: Int) -> Unit = { }
    private var itemViewLongClick: (view: View) -> Boolean = { false }
    private var btnMenuClick: (view: View) -> Unit = { }
    private var btnSelfClick: (view: View) -> Unit = { }

    companion object {
       //const val HEADER_VIEW = 1
       const val LIST_ITEM_VIEW = 2
       const val FOOTER_VIEW = 3
    }

    abstract class GenericViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Абстрактный класс, от которого наследуются viewHolder'ы для списка контактов и футера.
        abstract fun bindView(position: Int)
    }

    private class ListItemViewHolder(view: View) : GenericViewHolder(view) {
        // Представление viewHolder'а для списка контактов.
        val itemUserName: TextView = view.findViewById(R.id.itemUserName)
        val itemUserPhone: TextView = view.findViewById(R.id.itemUserPhone)
        val itemUserLabel: TextView = view.findViewById(R.id.itemUserLabel)
        val btnUserMenu: Button = view.findViewById(R.id.btnUserMenu)
        val itemUserLayout: ConstraintLayout = view.findViewById(R.id.itemUserLayout)
        val btnUserSelf: Button = view.findViewById(R.id.btnUserSelf)

        override fun bindView(position: Int) {
            // Функция привязывает к viewHolder'у данные списка контактов.
            val phone = Util.phones[position]
            this.itemUserName.text = Util.phone2name[phone]
            this.itemUserPhone.text = phone
            this.itemUserLabel.setBackgroundResource(
                AppColors.getMarker(Util.phone2color[phone]))
            this.itemUserLayout.setBackgroundColor(
                AppColors.getColorAlpha16(Util.phone2color[phone]))
            this.itemUserLayout.tag = position
            this.btnUserMenu.tag = position
            if (phone == Util.myphone) {
                this.btnUserSelf.isEnabled = true
                this.btnUserSelf.visibility = View.VISIBLE
            } else {
                this.btnUserSelf.isEnabled = false
                this.btnUserSelf.visibility = View.GONE
            }
        }
    }

    private class FooterViewHolder(view: View) : GenericViewHolder(view) {
        // Представление viewHolder для футера.

        override fun bindView(position: Int) {
            // Функция привязывает к viewHolder'у данные футера.
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenericViewHolder {
        // Функция вызывается LayoutManager'ом, чтобы создать viewHolder'ы и передать им макет.
        val view: View
        return when(viewType) {
            FOOTER_VIEW -> {
                view = LayoutInflater.from(parent.context).
                inflate(R.layout.item_user_footer, parent, false)
                FooterViewHolder(view)
            }

            else -> {
                view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_user, parent, false)
                ListItemViewHolder(view).apply {
                    btnUserMenu.setOnClickListener(btnMenuClick)
                    btnUserSelf.setOnClickListener(btnSelfClick)
                }
            }
        }
    }

    override fun onBindViewHolder(holder: GenericViewHolder, position: Int) {
        // Функция вызывается LayoutManager'ом, чтобы привязать к viewHolder'у данные, которые он должен отображать.
        holder.bindView(position)
        if (holder is ListItemViewHolder) {
            holder.itemView.setOnClickListener { itemViewClick(position) }
            holder.itemView.setOnLongClickListener { itemViewLongClick(holder.itemUserLayout) }
        }
    }

    override fun getItemCount(): Int {
        // Функция вызывается LayoutManager'ом и возвращает общее количество элементов в списке + футер.
        return Util.phones.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        // Функция определяет тип элемента.
        return when (position) {
            Util.phones.size -> FOOTER_VIEW
            else -> LIST_ITEM_VIEW
        }
    }

    fun setOnItemViewClick(listener: (Int) -> Unit) {
        // Функция устанавливает click listener для всего элемента списка.
        itemViewClick = listener
    }

    fun setOnItemViewLongClick(listener: (View) -> Boolean) {
        // Функция устанавливает long click listener для всего элемента списка.
        itemViewLongClick = listener
    }
    fun setOnBtnMenuClick(listener: (View) -> Unit) {
        // Функция устанавливает click listener для кнопки меню на элементе.
        btnMenuClick = listener
    }

    fun setOnBtnSelfClick(listener: (View) -> Unit) {
        // Функция устанавливает click listener для кнопки self на элементе.
        btnSelfClick = listener
    }

}