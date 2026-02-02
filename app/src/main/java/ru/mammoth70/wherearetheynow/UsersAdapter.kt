package ru.mammoth70.wherearetheynow

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.ListAdapter
import com.google.android.material.card.MaterialCardView

class UsersAdapter(
    private val itemViewClick: (pos: Int) -> Unit,
    private val itemViewLongClick: (view: View, pos: Int) -> Boolean,
    private val btnMenuClick: (view: View, pos: Int) -> Unit,
    private val btnSelfClick: () -> Unit
) : ListAdapter<User, UsersAdapter.GenericViewHolder>(UserDiffCallback()) {
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

    private inner class ListItemViewHolder(
        view: View,
        private val itemViewClick: (Int) -> Unit,
        private val itemViewLongClick: (View, Int) -> Boolean,
        private val btnMenuClick: (View, Int) -> Unit,
        private val btnSelfClick: () -> Unit
    ) : GenericViewHolder(view) {
        // Представление viewHolder'а для списка контактов.

        private val itemUserName: TextView = view.findViewById(R.id.itemUserName)
        private val itemUserPhone: TextView = view.findViewById(R.id.itemUserPhone)
        private val itemUserLabel: TextView = view.findViewById(R.id.itemUserLabel)
        private val btnUserMenu: Button = view.findViewById(R.id.btnUserMenu)
        private val itemCardUser: MaterialCardView = view.findViewById(R.id.frameItemCardUser)
        private val btnUserSelf: Button = view.findViewById(R.id.btnUserSelf)

        init {
            // Привязка листенеров.

            itemView.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) itemViewClick(pos)
            }

            itemView.setOnLongClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) itemViewLongClick(itemCardUser, pos) else false
            }

            btnUserMenu.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) btnMenuClick(itemCardUser, pos)
            }

            btnUserSelf.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) btnSelfClick()
            }
        }

        override fun bindView(position: Int) {
            // Функция привязывает к viewHolder'у данные списка контактов.

            val user = getItem(position)

            this.itemUserName.text = user.name
            this.itemUserPhone.text = user.phone

            // Работа с ресурсами
            this.itemUserLabel.setBackgroundResource(
                AppColors.getMarker(user.color))
            this.itemCardUser.setCardBackgroundColor(
                AppColors.getColorAlpha16(user.color))

            // Логика кнопки "self"
            if (user.phone == DataRepository.myPhone) {
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

        val inflater = LayoutInflater.from(parent.context)
        return when(viewType) {
            FOOTER_VIEW -> FooterViewHolder(inflater.inflate(R.layout.item_user_footer,
                parent, false))
            else -> ListItemViewHolder(
                inflater.inflate(R.layout.item_user,
                    parent, false),
                 itemViewClick, itemViewLongClick, btnMenuClick, btnSelfClick
            )
        }
    }
    override fun onBindViewHolder(holder: GenericViewHolder, position: Int) {
        // Функция вызывается LayoutManager'ом, чтобы привязать к viewHolder'у данные, которые он должен отображать.

        holder.bindView(position)
    }

    override fun getItemCount(): Int {
        // Функция вызывается LayoutManager'ом и возвращает общее количество элементов в списке + футер.
        return currentList.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        // Функция определяет тип элемента.
        return if (position == currentList.size) FOOTER_VIEW else LIST_ITEM_VIEW
    }

    class UserDiffCallback : DiffUtil.ItemCallback<User>() {
        // Callback для рассчёта разницы между двумя элементами.

        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean =
            oldItem == newItem
    }

}