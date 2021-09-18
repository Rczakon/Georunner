package com.example.alphageorunner

import Player
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {

    private lateinit var loginRedirect: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        loginRedirect = findViewById(R.id.textView4)
        if (checkForCurrentSession()) {
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
        }
        val username = usernameText.text.toString()

        val moveToLoginActivity = View.OnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        val createUser = View.OnClickListener {
            val username = usernameText.text.toString()
            val email = emailText.text.toString()
            val password = passwordText.text.toString()
            val password2 = passwordText2.text.toString()
            Log.d("Register attempt: ", "Loading")
            if (!username.isNullOrEmpty() && !email.isNullOrEmpty() && !password.isNullOrEmpty() && !password2.isNullOrEmpty()) {
                if (password2 == password) {
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener() {
                            if(!it.isSuccessful){
                                return@addOnCompleteListener
                            }
                            else{
                                if(FirebaseAuth.getInstance().currentUser != null) {
                                    FirebaseAuth.getInstance().currentUser!!.sendEmailVerification()
                                }
                                registerErrorOutput.text = ""
                                Log.d("Register attempt: ", "Success")
                                addUserToDatabase()
                            }
                        }
                        .addOnFailureListener() {
                            Log.d("Register attempt: ", "Failed ${it.message}")
                            if(it.message == "The given password is invalid. [ Password should be at least 6 characters ]") {
                                registerErrorOutput.text = "Hasło jest za krótkie."
                            }
                            else if(it.message == "The email address is already in use by another account.") {
                                registerErrorOutput.text = "Podany adres email jest zajęty."
                            }
                        }
                }else {
                    registerErrorOutput.text = "Hasła nie są zgodne."
                }
            } else {
                registerErrorOutput.text = "Wszystkie pola muszą zostać wypełnione."
            }
        }
        registerButton.setOnClickListener(createUser)
        loginRedirect.setOnClickListener(moveToLoginActivity)

    }

    private fun addUserToDatabase() {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(emailText.text.toString(), passwordText.text.toString())
            .addOnCompleteListener() {
                if(!it.isSuccessful){
                    return@addOnCompleteListener
                }
                else{
                    Log.d("Login attempt: ", "Success")
                    val uid = FirebaseAuth.getInstance().uid
                    val ref = FirebaseDatabase.getInstance().getReference("/users/$uid/")
                    val player = Player(uid.toString(), usernameText.text.toString(), 1, 0.0)
                    player.email = emailText.text.toString()
                    ref.setValue(player)
                    Toast.makeText(this, "Wysłano email w celu weryfikacji", Toast.LENGTH_LONG).show()
                    FirebaseAuth.getInstance().signOut()
                }
    }

    }

    fun checkForCurrentSession(): Boolean {
        Log.d("CHECK:IS_THERE_ANYONE", "???")
        if(FirebaseAuth.getInstance().currentUser != null) {
            Log.d("CHECK:YES", "!!!")
            if(FirebaseAuth.getInstance().currentUser!!.isEmailVerified) {
                return true
            }
            else {
                Log.d("CHECK:YES", " NOT VERIFIED")
                return false
            }
        } else {
            Log.d("CHECK:NO", ":(")
            return false
        }
    }
}
