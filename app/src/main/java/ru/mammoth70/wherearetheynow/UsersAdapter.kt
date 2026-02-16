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
) : ListAdapter<User, UsersAdapter.ListItemViewHolder>(UserDiffCallback()) {
    // ListAdapter для списка контактов.


    inner class ListItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {

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
                safePos { pos -> itemViewClick(pos) }
            }

            itemView.setOnLongClickListener {
                safePos { pos -> itemViewLongClick(btnUserMenu, pos) } ?: false
            }

            btnUserMenu.setOnClickListener {
                safePos { pos -> btnMenuClick(btnUserMenu, pos) }
            }

            btnUserSelf.setOnClickListener {
                safePos { btnSelfClick() }
            }
        }

        private inline fun <T> safePos(block: (Int) -> T): T? {
            val pos = bindingAdapterPosition
            return if (pos != RecyclerView.NO_POSITION) block(pos) else null
        }

        fun bind(user: User) {
            // Функция привязывает к viewHolder'у данные списка контактов.

            this.itemUserName.text = user.name
            this.itemUserPhone.text = user.phone

            // Работа с ресурсами
            this.itemUserLabel.setBackgroundResource(
                PinColors.getPin(user.color))
            this.itemCardUser.setCardBackgroundColor(
                PinColors.getColorAlpha16(user.color))

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


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListItemViewHolder {
        // Функция вызывается LayoutManager'ом, чтобы создать viewHolder'ы и передать им макет.

        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_user, parent, false)
        return ListItemViewHolder(view)
    }


    override fun onBindViewHolder(holder: ListItemViewHolder, position: Int) {
        // Функция вызывается LayoutManager'ом, чтобы привязать к viewHolder'у данные, которые он должен отображать.
        holder.bind(getItem(position))
    }


    class UserDiffCallback : DiffUtil.ItemCallback<User>() {
        // Callback для рассчёта разницы между двумя элементами.

        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean =
            oldItem == newItem
    }

}