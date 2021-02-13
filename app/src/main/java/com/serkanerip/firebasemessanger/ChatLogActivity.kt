package com.serkanerip.firebasemessanger

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.serkanerip.firebasemessanger.models.User
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.*

class ChatLogActivity : AppCompatActivity() {
    val adapter = GroupAdapter<GroupieViewHolder>()
    var chatRefPath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        supportActionBar?.title = user!!.username
        chatRefPath = "/user-messages/${FirebaseAuth.getInstance().uid}/${user.uid}"

        rvChatLog.adapter = adapter

        listenForMessages()

        btnSend.setOnClickListener {
            performSendMessage()
        }
    }

    private fun listenForMessages() {
        val ref = FirebaseDatabase.getInstance().getReference(chatRefPath!!)

        ref.addChildEventListener(object: ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java)
                if (chatMessage != null) {
                    Log.d("ChatLog", chatMessage.text)

                    if (FirebaseAuth.getInstance().uid == chatMessage.fromId) {
                        adapter.add(ChatFromItem(chatMessage.text, LatestMessages.currentUser!!))
                    } else {
                        val toUser = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
                        adapter.add(ChatToItem(chatMessage.text, toUser!!))
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
            }

        })
    }

    class ChatMessage(val id:String, val text:String, val fromId: String, val toId:String, val timestamp: Long) {
        constructor(): this("", "", "", "", -1)
    }

    private fun performSendMessage() {
        val text = etEnterMessage.text.toString()

        if (text.isEmpty()) {
            return Toast.makeText(this, "Type a message!!", Toast.LENGTH_SHORT).show()
        }
        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY) ?: return

        val fromReference = FirebaseDatabase.getInstance().getReference(chatRefPath!!).push()
        val toReference = FirebaseDatabase.getInstance().getReference("/user-messages/${user.uid}/${FirebaseAuth.getInstance().uid}").push()

        val toId = user.uid
        val fromId = FirebaseAuth.getInstance().currentUser!!.uid

        val chatMessage = ChatMessage(fromReference.key!!, text, fromId, toId, System.currentTimeMillis() / 1000)
        fromReference.setValue(chatMessage)
            .addOnSuccessListener {
                Log.d("ChatLog", "Saved our chat message: ${fromReference.key}")
                etEnterMessage.setText("")
                rvChatLog.scrollToPosition(adapter.itemCount - 1)
            }
        toReference.setValue(chatMessage)

        var latestMessageRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId/$toId")
        latestMessageRef.setValue(chatMessage)
        latestMessageRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$toId/$fromId")
        latestMessageRef.setValue(chatMessage)
    }
}

class ChatFromItem(val text:String, private val user:User): Item<GroupieViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.tvFrom.text = text
        Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.ivFrom)
    }

}

class ChatToItem(val text:String, private val user:User): Item<GroupieViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.tvTo.text = text
        Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.ivTo)
    }

}