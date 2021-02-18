package com.zxerrinor.githubstarwatchdog.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.zxerrinor.githubstarwatchdog.R

class UsersAdapter(
    val users: MutableList<String>,
    val onClickListener: OnUserItemClickListener
) :
    RecyclerView.Adapter<LogViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder =
        LogViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.user_list_item, parent, false)
        )

    override fun getItemCount(): Int = users.size

    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        holder.initialize(users[position], onClickListener)
    }
}

class LogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var listItemUserTextView: TextView = itemView.findViewById(R.id.userName)

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

interface OnUserItemClickListener {
    fun onUserListItemClick(item: String, position: Int)
}