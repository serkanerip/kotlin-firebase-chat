package com.serkanerip.firebasemessanger

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.serkanerip.firebasemessanger.models.User
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class RegisterActivity : AppCompatActivity() {
    var selectedPhotoURI: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        btnRegister.setOnClickListener {
            register()
        }

        tvAlreadyHave.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        btnSelectPhoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data !== null) {
            selectedPhotoURI = data.data

            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoURI)

            val bitmapDrawable = BitmapDrawable(bitmap)
            btnSelectPhoto.setBackgroundDrawable(bitmapDrawable)
        }
    }

    private fun register() {
        val email = etEmail.text.toString()
        val password = etPassword.text.toString()
        val username = etUsername.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this@RegisterActivity, "Please enter email and password", Toast.LENGTH_SHORT)
                .show()
            return
        };

        val auth: FirebaseAuth = FirebaseAuth.getInstance()

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (!it.isSuccessful) {
                    return@addOnCompleteListener
                }

                val user = auth.currentUser
                Log.d("Main", "Successfully created user with uid: ${user!!.uid}")
                Toast.makeText(this@RegisterActivity, "Successfully registered", Toast.LENGTH_SHORT)
                    .show()

                uploadImageToFirebaseStorage()
            }
            .addOnFailureListener {
                Toast.makeText(this@RegisterActivity, it.message, Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun uploadImageToFirebaseStorage() {
        if (selectedPhotoURI == null) return

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")
        ref.putFile(selectedPhotoURI!!)
            .addOnSuccessListener {
                Log.d(RegisterActivity::class.toString(), "File putted to storage")
                ref.downloadUrl.addOnSuccessListener {
                    Log.d(RegisterActivity::class.toString(), "Download url $it")
                    saveUserToFirebaseDatabase(it.toString())
                }
                ref.downloadUrl.addOnFailureListener {
                    Log.d(RegisterActivity::class.toString(), it.message.toString())
                }
            }
    }

    private fun saveUserToFirebaseDatabase(profileImageURL: String) {
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        ref.setValue(User(uid!!, etUsername.text.toString(), profileImageURL))
            .addOnSuccessListener {

                val intent = Intent(this, LatestMessages::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            .addOnFailureListener {
                Log.d(RegisterActivity::class.toString(), it.message.toString())
            }
    }
}