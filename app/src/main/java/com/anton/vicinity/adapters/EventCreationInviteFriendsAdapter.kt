package com.anton.vicinity.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anton.vicinity.R
import com.anton.vicinity.data.User
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.friend_item_view.*

class EventCreationInviteFriendsAdapter(
    private val context: Context):
    RecyclerView.Adapter<EventCreationInviteFriendsAdapter.FriendHolder>() {

    private lateinit var data: MutableList<User>

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FriendHolder {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.friend_item_view, parent, false)
        return FriendHolder(view)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(
        holder: FriendHolder,
        position: Int
    ) {
        val friend = data[position]
        holder.setData(friend.userImage!!.toInt(),
            friend.userName!!)
    }

    fun update(newList: MutableList<User>) {
        data = newList
        notifyDataSetChanged()
    }

    class FriendHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        LayoutContainer{
        override val containerView: View?
            get() = itemView
        fun setData(friendImage: Int,
                    friendName: String){
            friendViewUserName.text = friendName
            //friendViewUserImage.
        }
    }
}