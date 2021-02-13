package com.serkanerip.firebasemessanger

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.serkanerip.firebasemessanger.models.User
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.GroupieAdapter
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_new_message.*
import kotlinx.android.synthetic.main.user_row_new_message.view.*

class NewMessageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)
        supportActionBar?.title = "Select User"
        rvMessages.adapter = GroupieAdapter()

        fetchUsers()
    }

    companion object {
        val USER_KEY = "USER_KEY"
    }

    private fun fetchUsers() {
        val ref = FirebaseDatabase.getInstance().getReference("/users")
        ref.addValueEventListener(object: ValueEventListener  {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("NewMessage", "Data changed")
                val adapter = GroupAdapter<GroupieViewHolder>()
                for (child in snapshot.children) {
                    val user = child.getValue<User>()
                    if (user != null && user.uid != LatestMessages.currentUser!!.uid) {
                        adapter.add(UserItem(user))
                    }
                    Log.d("NewMessage", user.toString())
                }

                adapter.setOnItemClickListener { item, view ->
                    val userItem = item as UserItem

                    val intent = Intent(view.context, ChatLogActivity::class.java)
                    intent.putExtra(USER_KEY, userItem.user)
                    startActivity(intent)

                    finish()
                }
                rvMessages.adapter = adapter
            }

        })
    }
}

class UserItem(val user: User): Item<GroupieViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.user_row_new_message;
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.tvUsernameRow.text = user.username
        Picasso.get().load(user.profileImageUrl)
            .into(viewHolder.itemView.ivUserProfile)
    }

}
