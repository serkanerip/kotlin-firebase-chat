package com.serkanerip.firebasemessanger

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import com.serkanerip.firebasemessanger.models.User
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_latest_messages.*
import kotlinx.android.synthetic.main.latest_message_row.view.*

class LatestMessages : AppCompatActivity() {
    companion object {
        var currentUser: User? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_latest_messages)


        fetchCurrentUser()
        listenForLatestMessages()
        verifyUserIsLoggedIn()
    }

    private fun listenForLatestMessages() {
        val fromId = FirebaseAuth.getInstance().currentUser!!.uid
        var latestMessageRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId")

        latestMessageRef.addValueEventListener(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val adapter = GroupAdapter<GroupieViewHolder>()
                snapshot.children.forEach {
                    val msg = it.getValue<ChatLogActivity.ChatMessage>()
                    val userId = if (FirebaseAuth.getInstance().uid == msg!!.fromId) msg.toId else msg.fromId
                    val ref = FirebaseDatabase.getInstance().getReference("users/${userId}")
                    ref.addListenerForSingleValueEvent(object: ValueEventListener {
                        override fun onCancelled(error: DatabaseError) {
                        }

                        override fun onDataChange(snapshot: DataSnapshot) {
                            val user = snapshot.getValue<User>()
                            adapter.add(LatestMessageItem(msg, user!!))
                        }

                    })

                }

                adapter.setOnItemClickListener { item, view ->
                    val lmItem = item as LatestMessageItem
                    val intent = Intent(view.context, ChatLogActivity::class.java)
                    intent.putExtra(NewMessageActivity.USER_KEY, lmItem.user)
                    startActivity(intent)
                }
                rvLatestMessages.adapter = adapter
            }

        })
    }

    class LatestMessageItem(val msg: ChatLogActivity.ChatMessage, val user: User) : Item<GroupieViewHolder>() {
        override fun getLayout(): Int {
            return R.layout.latest_message_row
        }

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            viewHolder.itemView.lmTvMessage.text = msg.text
            viewHolder.itemView.lmTvUsername.text = user!!.username
            Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.lmIvUser)


        }

    }


    private fun fetchCurrentUser() {
        val ref = FirebaseDatabase.getInstance().getReference("users/${FirebaseAuth.getInstance().uid}")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                currentUser = snapshot.getValue<User>()
            }

        })
    }

    private fun verifyUserIsLoggedIn() {
        val uid = FirebaseAuth.getInstance().uid
        if (uid === null) {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.menuNewMessage -> {
                val intent = Intent(this, NewMessageActivity::class.java)
                startActivity(intent)
            }
            R.id.menuSignOut -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }
}