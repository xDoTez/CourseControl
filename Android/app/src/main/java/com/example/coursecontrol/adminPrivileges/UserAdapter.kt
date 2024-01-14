package com.example.coursecontrol.adminPrivileges

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.coursecontrol.R
import com.example.coursecontrol.model.User

class UserAdapter(
    private val userDataList: List<User>,
    private val onItemClick: (User) -> Unit
) : RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_users, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val userData = userDataList[position]
        holder.bind(userData)
    }

    override fun getItemCount(): Int {
        return userDataList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textUserName: TextView = itemView.findViewById(R.id.textUserName)

        fun bind(user: User) {
            textUserName.text = user.username

            textUserName.setOnClickListener {
                onItemClick(user)
            }

            Log.d("UserAdapter", "User bound: $user")
        }
    }
}
