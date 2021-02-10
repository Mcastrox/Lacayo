package com.mauricio.chatclone1

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var refUsers: DatabaseReference

    private lateinit var register_btn: Button
    private lateinit var username_register : EditText
    private lateinit var email_register: EditText
    private lateinit var password_register : EditText
    private var firebaseUserID: String =""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val toolbar : Toolbar =findViewById(R.id.toolbar_register)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = "Crear cuenta"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            val intent = Intent(this,WelcomeActivity::class.java)
            startActivity(intent)
            finish()

        }
        mAuth = FirebaseAuth.getInstance()
        register_btn= findViewById(R.id.register_btn)
        username_register = findViewById(R.id.username_register)
        password_register = findViewById(R.id.password_register)
        email_register = findViewById(R.id.email_register)

        register_btn.setOnClickListener {
            registerUser()
        }

    }

    private fun registerUser() {
        val username: String = username_register.text.toString()
        val email: String = email_register.text.toString()
        val password: String = password_register.text.toString()

        if(username == ""){
        Toast.makeText(this@RegisterActivity,"Por favor introduce un nombre de usuario ",Toast.LENGTH_SHORT).show()
        }
        else if(email == ""){
            Toast.makeText(this@RegisterActivity,"Por favor introduce un correo electronico ",Toast.LENGTH_SHORT).show()
        }
        else if(password == ""){
            Toast.makeText(this@RegisterActivity,"Por favor introduce una contraseña ",Toast.LENGTH_SHORT).show()
        }
        else{
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task->
                if(task.isSuccessful){
                    firebaseUserID = mAuth.currentUser!!.uid
                    refUsers = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUserID)

                    val userHashMap = HashMap<String ,Any>()
                    userHashMap["uid"]=firebaseUserID
                    userHashMap["username"]= username
                    userHashMap["profile"]="https://firebasestorage.googleapis.com/v0/b/chatclone1.appspot.com/o/profile.png?alt=media&token=2916f630-1809-49e0-a194-1450fac3fb15"
                    userHashMap["cover"]="https://firebasestorage.googleapis.com/v0/b/chatclone1.appspot.com/o/cover.jpg?alt=media&token=c7d04b22-2d28-40a7-91fc-4b8442ff5f70"
                    userHashMap["status"]="offline"
                    userHashMap["search"]=username.toLowerCase()
                    // Acceso a tus redes sociales
                    userHashMap["facebook"]="https://m.facebook.com"
                    userHashMap["instagram"]="https://m.instagram.com"
                    userHashMap["website"]="https://www.google.com"

                    refUsers.updateChildren(userHashMap)
                            .addOnCompleteListener { task ->
                                if(task.isSuccessful){
                                    val intent = Intent(this,LoginActivity::class.java)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                    startActivity(intent)
                                    finish()
                                }
                            }
                }
                else{
                    Toast.makeText(this@RegisterActivity,"Error al crear el usuario",Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}