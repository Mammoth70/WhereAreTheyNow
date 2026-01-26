package ru.mammoth70.wherearetheynow

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView

class UsersAdapter(
    private val itemViewClick: (position: Int) -> Unit,
    private val itemViewLongClick: (view: View) -> Boolean,
    private val btnMenuClick: (view: View) -> Unit,
    private val btnSelfClick: (view: View) -> Unit
): RecyclerView.Adapter<UsersAdapter.GenericViewHolder>() {
    // RecyclerView.Adapter для списка контактов.

    companion object {
       //const val HEADER_VIEW = 1
       const val LIST_ITEM_VIEW = 2
       const val FOOTER_VIEW = 3
    }

    sealed class GenericViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Запечатанный (абстрактный) класс, от которого наследуются viewHolder'ы для списка контактов и футера.
        abstract fun bindView(position: Int)
    }

    private class FooterViewHolder(view: View) : GenericViewHolder(view) {
        // Представление viewHolder для футера.

        override fun bindView(position: Int) {
            // Функция привязывает к viewHolder'у данные футера.
        }
    }

    private inner class ListItemViewHolder(view: View) : GenericViewHolder(view) {
        // Представление viewHolder'а для списка контактов.
        val itemUserName: TextView = view.findViewById(R.id.itemUserName)
        val itemUserPhone: TextView = view.findViewById(R.id.itemUserPhone)
        val itemUserLabel: TextView = view.findViewById(R.id.itemUserLabel)
        val btnUserMenu: Button = view.findViewById(R.id.btnUserMenu)
        val itemCardUser: MaterialCardView = view.findViewById(R.id.frameItemCardUser)
        val btnUserSelf: Button = view.findViewById(R.id.btnUserSelf)

        init {
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) itemViewClick.invoke(position)
            }

            itemView.setOnLongClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    itemViewLongClick.invoke(itemCardUser)
                    true
                } else {
                    false
                }
            }

            btnUserMenu.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) btnMenuClick.invoke(it)
            }

            btnUserSelf.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) btnSelfClick.invoke(it)
            }
        }

        override fun bindView(position: Int) {
            // Функция привязывает к viewHolder'у данные списка контактов.
            val phone = phones[position]
            this.itemUserName.text = phone2name[phone]
            this.itemUserPhone.text = phone
            this.itemUserLabel.setBackgroundResource(
                AppColors.getMarker(phone2color[phone]))
            this.itemCardUser.setCardBackgroundColor(
                AppColors.getColorAlpha16(phone2color[phone]))
            this.itemCardUser.tag = position
            this.btnUserMenu.tag = position
            if (phone == myphone) {
                this.btnUserSelf.isEnabled = true
                this.btnUserSelf.visibility = View.VISIBLE
            } else {
                this.btnUserSelf.isEnabled = false
                this.btnUserSelf.visibility = View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenericViewHolder {
        // Функция вызывается LayoutManager'ом, чтобы создать viewHolder'ы и передать им макет.
        return when(viewType) {
            FOOTER_VIEW -> {
                FooterViewHolder(LayoutInflater.from(parent.context).
                inflate(R.layout.item_user_footer, parent, false))
            }

            else -> {
                ListItemViewHolder(view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_user, parent, false)).apply {
                    btnUserMenu.setOnClickListener(btnMenuClick)
                    btnUserSelf.setOnClickListener(btnSelfClick)
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
        return phones.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        // Функция определяет тип элемента.
        return when (position) {
            phones.size -> FOOTER_VIEW
            else -> LIST_ITEM_VIEW
        }
    }

}