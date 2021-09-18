package com.example.alphageorunner

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private lateinit var registerRedirect: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        registerRedirect = findViewById(R.id.textView4)
        val moveToRegisterActivity = View.OnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
        registerRedirect.setOnClickListener(moveToRegisterActivity)
        if (checkForCurrentSession()) {
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
        }
        val attemptToLogin = View.OnClickListener {
            val email = emailText.text.toString()
            val password = passwordText.text.toString()
            if (!email.isNullOrEmpty() && !password.isNullOrEmpty()) {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener() {
                        if(!it.isSuccessful){
                            return@addOnCompleteListener
                        }
                        else{
                            if(FirebaseAuth.getInstance().currentUser!!.isEmailVerified) {
                                Log.d("Login attempt: ", "Success")
                                val intent = Intent(this, MapsActivity::class.java)
                                startActivity(intent)
                            } else {
                                Toast.makeText(this, "Adres email nie został potwierdzony", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                    .addOnFailureListener() {
                        Log.d("Login attempt: ", "Failed")
                        //loginErrorOutput.text = it.message.toString()
                        if(it.message == "The email address is badly formatted.") {
                            loginErrorOutput.text = "Nieprawidłowy format adresu email."
                        }
                        else if(it.message == "There is no user record corresponding to this identifier. The user may have been deleted.") {
                            loginErrorOutput.text = "Użytkownik nie istnieje."
                        }
                    }
            } else {
                loginErrorOutput.text = "Wszystkie pola muszą zostać wypełnione."
            }
        }
        logButton.setOnClickListener(attemptToLogin)
    }

    fun checkForCurrentSession(): Boolean {
        Log.d("CHECKING_FOR_SESSION", "")
        if(FirebaseAuth.getInstance().currentUser != null) {
            Log.d("SESSION_ACTIVE", "")
            if(FirebaseAuth.getInstance().currentUser!!.isEmailVerified) {
                return true
            }
            else {
                Log.d("EMAIL_NOT_VERIFIED", "")
                Toast.makeText(this, "Adres email nie został potwierdzony", Toast.LENGTH_LONG).show()
                return false
            }
        } else {
            Log.d("NO_ACTIVE_SESSION", "")
            return false
        }
    }
}


