package com.serkanerip.firebasemessanger.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase

object UserRepository {

    fun getUser(refId: String): Task<DataSnapshot> {
        val ref = FirebaseDatabase.getInstance().getReference("/users/${refId}")

        return ref.get()
    }
}