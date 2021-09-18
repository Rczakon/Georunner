package com.example.alphageorunner

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class HelpActivity : AppCompatActivity() {

    private lateinit var exitButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)

        val exitListener = View.OnClickListener {
            finish()
        }
        exitButton = findViewById(R.id.exitButton)
        exitButton.setOnClickListener(exitListener)
    }
}
