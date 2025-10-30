package ru.mammoth70.wherearetheynow

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView

class UsersAdapter: RecyclerView.Adapter<UsersAdapter.GenericViewHolder>(), View.OnClickListener, View.OnLongClickListener {
    // RecyclerView.Adapter для списка контактов.

    private var itemLayoutClick: (view: View) -> Unit = { }
    private var itemLayoutLongClick: (view: View) -> Unit = { }
    private var btnMenuClick: (view: View) -> Unit = { }
    private var btnSelfClick: (view: View) -> Unit = { }

    companion object {
       //const val HEADER_VIEW = 1
       const val LIST_ITEM_VIEW = 2
       const val FOOTER_VIEW = 3
    }

    abstract class GenericViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
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
            this.itemUserName.tag = position
            this.itemUserPhone.text = phone
            this.itemUserPhone.tag = position
            this.itemUserLabel.setBackgroundResource(
                AppColors.getMarker(Util.phone2color[phone]))
            this.itemUserLabel.tag = position
            this.itemUserLayout.setBackgroundColor(
                AppColors.getColorAlpha16(Util.phone2color[phone]))
            this.itemUserLayout.tag = position
            this.btnUserMenu.tag = position
            this.btnUserSelf.tag = position
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
        val itemUserFooterLayout: ConstraintLayout = view.findViewById(R.id.itemUserFooterLayout)

        override fun bindView(position: Int) {
            // Функция привязывает к viewHolder'у данные футера.
            this.itemUserFooterLayout.tag = position
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
                ListItemViewHolder(view).apply{
                    itemUserLayout.setOnClickListener(this@UsersAdapter)
                    itemUserLayout.setOnLongClickListener(this@UsersAdapter)
                    btnUserMenu.setOnClickListener(this@UsersAdapter)
                    btnUserSelf.setOnClickListener(this@UsersAdapter)
                }
            }
        }
    }

    override fun onBindViewHolder(holder: GenericViewHolder, position: Int) {
        // Функция вызывается LayoutManager'ом, чтобы привязать к viewHolder'у данные, которые он должен отображать.
        holder.bindView(position)
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

    fun setOnItemLayoutClick(listener: (View) -> Unit) {
        // Функция устанавливает click listener для всего элемента списка.
        itemLayoutClick = listener
    }

    fun setOnBtnMenuClick(listener: (View) -> Unit) {
        // Функция устанавливает click listener для кнопки меню на элементе.
        btnMenuClick = listener
    }

    fun setOnBtnSelfClick(listener: (View) -> Unit) {
        // Функция устанавливает click listener для кнопки self на элементе.
        btnSelfClick = listener
    }

    fun setOnItemLayoutLongClick(listener: (View) -> Unit) {
        // Функция устанавливает long click listener для всего элемента списка.
        itemLayoutLongClick = listener
    }

    override fun onClick(view: View) {
        // Обработка нажатия на элементы списка.
        when (view.id) {
            R.id.itemUserLayout -> itemLayoutClick(view)
            R.id.btnUserMenu -> btnMenuClick(view)
            R.id.btnUserSelf -> btnSelfClick(view)
        }
    }

    override fun onLongClick(view: View): Boolean {
        // Обработка длинного нажатия на элементы списка.
        when (view.id) {
            R.id.itemUserLayout -> {
                itemLayoutLongClick(view)
                return true
            }
            else -> return false
        }
    }

}
