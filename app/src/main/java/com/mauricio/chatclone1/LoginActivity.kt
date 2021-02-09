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

class LoginActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth


    private lateinit var email_login: EditText
    private lateinit var password_login: EditText
    private lateinit var login_btn : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val toolbar : Toolbar =findViewById(R.id.toolbar_login)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = "Iniciar sesión "
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            val intent = Intent(this,WelcomeActivity::class.java)
            startActivity(intent)
            finish()
        }
        email_login = findViewById(R.id.email_login)
        password_login = findViewById(R.id.password_login)
        login_btn = findViewById(R.id.login_btn)
        mAuth = FirebaseAuth.getInstance()

        login_btn.setOnClickListener {
            loginUser()
        }

    }

    private fun loginUser() {

        val email: String = email_login.text.toString()
        val password: String = password_login.text.toString()

        if(email == ""){
            Toast.makeText(this@LoginActivity,"Por favor introduce un correo electronico valido ", Toast.LENGTH_SHORT).show()
        }
        else if(password == ""){
            Toast.makeText(this@LoginActivity,"Por favor introduce una contraseña valido", Toast.LENGTH_SHORT).show()
        }
        else{
            mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener { task ->
                if(task.isSuccessful){
                    val intent = Intent(this,MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                }
                else{
                    Toast.makeText(this@LoginActivity,"Error al iniciar sesión ", Toast.LENGTH_SHORT).show()

                }
            }
        }
    }
}