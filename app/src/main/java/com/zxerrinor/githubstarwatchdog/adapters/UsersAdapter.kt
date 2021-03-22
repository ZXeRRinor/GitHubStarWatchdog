package com.zxerrinor.githubstarwatchdog.adapters

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.omega_r.libs.omegarecyclerview.sticky_decoration.StickyAdapter
import com.zxerrinor.githubstarwatchdog.R
import java.time.LocalDateTime
import java.time.Month
import java.util.*

class UsersAdapter(
    private val users: Map<Int, List<String>>,
    private val onClickListener: OnUserItemClickListener
) : RecyclerView.Adapter<UserViewHolder>(), StickyAdapter<UserHeaderHolder> {

    private val plainUsers = users.keys.fold(listOf<String>()) { a, elem -> a + users[elem]!! }
    private val userPositions: Map<String, Int> =
        users.entries.fold(mapOf()) { a, elem -> a + elem.value.map { it to elem.key } }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        return UserViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_user_list_item, parent, false)
        )
    }

    override fun getItemCount(): Int = plainUsers.size

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.initialize(plainUsers[position], onClickListener)
    }

    override fun getStickyId(position: Int): Long = findMonthForUserInPosition(position).toLong()

    override fun onCreateStickyViewHolder(parent: ViewGroup?): UserHeaderHolder {
        if (parent == null) throw IllegalStateException("viewGroup is null")
        return UserHeaderHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_user_header_item, parent, false)
        )
    }

    override fun onBindStickyViewHolder(viewHolder: UserHeaderHolder?, position: Int) {
        if (viewHolder == null) throw IllegalStateException("viewHeader is null")
        viewHolder.initialize(findMonthForUserInPosition(position), onClickListener)
    }

    private fun findMonthForUserInPosition(position: Int): Int {
        val userName = plainUsers[position]
        return userPositions[userName] ?: throw Resources.NotFoundException("user not found")
    }
}

class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var listItemUserTextView: TextView = itemView.findViewById(R.id.text_user_name)

    fun bind(text: String) {
        listItemUserTextView.text = text
    }

    fun initialize(item: String, action: OnUserItemClickListener) {
        listItemUserTextView.text = item
        itemView.setOnClickListener {
            action.onUserListItemClick(item, adapterPosition)
        }
    }
}

class UserHeaderHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var listItemUserTextView: TextView = itemView.findViewById(R.id.text_month_name)

    fun bind(month: Int) {
        val currentTime = LocalDateTime.now()
        listItemUserTextView.text = (Month.values()[(month - 1) % 12].name.toLowerCase(Locale.ROOT)
            .capitalize(Locale.ROOT) +
                if (month - 12 > 0) " (${currentTime.year})"
                else " (${currentTime.year - 1})")
    }

    fun initialize(month: Int, action: OnUserItemClickListener) {
        val currentTime = LocalDateTime.now()
        val text = (Month.values()[(month - 1) % 12].name.toLowerCase(Locale.ROOT)
            .capitalize(Locale.ROOT) +
                if (month - 12 > 0) " (${currentTime.year})"
                else " (${currentTime.year - 1})")
        listItemUserTextView.text = text
        itemView.setOnClickListener {
            action.onUserListItemClick(text, adapterPosition)
        }
    }
}

interface OnUserItemClickListener {
    fun onUserListItemClick(item: String, position: Int)
}