package com.example.alphageorunner

import Player
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.truncate

class StatsActivity : AppCompatActivity() {

    private lateinit var exitButton: Button
    private lateinit var rankingButton: Button
    private lateinit var helpButton: Button
    private val FILE_NAME = "savedPlayerStats.json"
    private lateinit var Player1: Player
    private lateinit var PassedPlayer: Player
    private lateinit var fetchedText: String
    private lateinit var totalDistanceView: TextView
    private lateinit var levelView: TextView
    private lateinit var playerClassView: TextView
    private lateinit var imageView1: ImageView
    private lateinit var imageView2: ImageView
    private lateinit var imageView3: ImageView
    private lateinit var imageView4: ImageView
    private lateinit var imageView5: ImageView
    private lateinit var imageView6: ImageView
    private lateinit var imageView7: ImageView
    private lateinit var imageView8: ImageView
    private lateinit var imageView9: ImageView
    private lateinit var imageView10: ImageView
    private lateinit var experienceView: TextView
    private lateinit var questsView: TextView
    private lateinit var usernameView: TextView


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)
        readFile("playerStats.json")
        exitButton = findViewById(R.id.exitButton)
        rankingButton = findViewById(R.id.rankingButton)
        helpButton = findViewById(R.id.helpButton)
        totalDistanceView = findViewById(R.id.totalDistance)
        levelView = findViewById(R.id.level)
        usernameView = findViewById(R.id.userName)

        imageView1 = findViewById<ImageView>(R.id.imageView1)
        imageView2 = findViewById<ImageView>(R.id.imageView2)
        imageView3 = findViewById<ImageView>(R.id.imageView3)
        imageView4 = findViewById<ImageView>(R.id.imageView4)
        imageView5 = findViewById<ImageView>(R.id.imageView5)
        imageView6 = findViewById<ImageView>(R.id.imageView6)
        imageView7 = findViewById<ImageView>(R.id.imageView7)
        imageView8 = findViewById<ImageView>(R.id.imageView11)
        imageView9 = findViewById<ImageView>(R.id.imageView9)
        imageView10 = findViewById<ImageView>(R.id.imageView12)


        experienceView = findViewById(R.id.experience)
        questsView = findViewById(R.id.completedQuests)
        Player1 = intent.getSerializableExtra("PASSED_PLAYER") as Player
        Log.d("LOG_PASSED_PLAYER", Player1.userName)

        levelView.text = "Poziom: " + Player1.level.toString()

        experienceView.text = "Do następnego poziomu: " + Player1.experiencePoints + "/" + Player1.levelRequirements[Player1.level - 1]

        totalDistanceView.text = "Całkowita odległość: " + truncate(Player1.totalDistance).toString() + "m"

        questsView.text = "Wykonane zadania: " + Player1.completedWalkingQuests.toString()

        usernameView.text = Player1.userName
        checkBadges()
        //imageView0.setBackgroundResource(R.drawable.green_badge)

        val exitListener = View.OnClickListener {
            finish()
        }

        val moveToRankingActivity = View.OnClickListener {
            val intent = Intent(this, RankingActivity::class.java)
            startActivity(intent)
        }

        val moveToHelpActivity = View.OnClickListener {
            val intent = Intent(this, HelpActivity::class.java)
            startActivity(intent)
        }

        rankingButton.setOnClickListener(moveToRankingActivity)
        exitButton.setOnClickListener(exitListener)
        helpButton.setOnClickListener(moveToHelpActivity)
    }

    private fun readFile(filePath: String) {
        val fileText: String = applicationContext.assets.open(filePath).bufferedReader().use {
            it.readText()
        }
        Log.d("LOG_TEXT_FROM_FILE", fileText)
    }

    private fun checkBadges() {
        if(Player1.completedWalkingQuests > 0) {
            imageView1.setImageResource(R.drawable.green_badge)
            imageView1.setOnClickListener {
                Toast.makeText(this, "Odznaka za wykonanie pierwszego zadania", Toast.LENGTH_LONG).show()
            }
        }
        if(Player1.completedWalkingQuests > 9) {
            imageView2.setImageResource((R.drawable.green_badge10))
            imageView2.setOnClickListener {
                Toast.makeText(this, "Odznaka za ukończenie 10 zadań", Toast.LENGTH_LONG).show()
            }
        }
        if(Player1.totalDistance >= 1000.0) {
            imageView3.setImageResource((R.drawable.purple_badge1km))
            imageView3.setOnClickListener {
                Toast.makeText(this, "Odznaka za pokonanie 1km", Toast.LENGTH_LONG).show()
            }
        }
        if(Player1.totalDistance >= 5000.0) {
            imageView4.setImageResource((R.drawable.purple_badge5km))
            imageView4.setOnClickListener {
                Toast.makeText(this, "Odznaka za pokonanie 5km", Toast.LENGTH_LONG).show()
            }
        }
        if(Player1.totalDistance >= 10000.0) {
            imageView5.setImageResource((R.drawable.purple_badge10km))
            imageView5.setOnClickListener {
                Toast.makeText(this, "Odznaka za pokonanie 10km", Toast.LENGTH_LONG).show()
            }
        }
        if(Player1.completedVisitingQuests > 0){
            imageView7.setImageResource((R.drawable.orange_badge))
            imageView7.setOnClickListener {
                Toast.makeText(this, "Odznaka za odwiedzenie pierwszej lokacji", Toast.LENGTH_LONG).show()
            }
        }
        if(Player1.completedWalkingQuests > 24) {
            imageView9.setImageResource((R.drawable.green_badge25))
            imageView9.setOnClickListener {
                Toast.makeText(this, "Odznaka za ukończenie 25 zadań", Toast.LENGTH_LONG).show()
            }
        }
        if(Player1.completedVisitingQuests > 4) {
            imageView6.setImageResource((R.drawable.orange_badge5))
            imageView6.setOnClickListener {
                Toast.makeText(this, "Odznaka za ukończenie 5 zadań zwiedzających", Toast.LENGTH_LONG).show()
            }
        }
        if(Player1.completedVisitingQuests > 4) {
            imageView8.setImageResource((R.drawable.orange_badge10))
            imageView8.setOnClickListener {
                Toast.makeText(this, "Odznaka za ukończenie 10 zadań zwiedzających", Toast.LENGTH_LONG).show()
            }
        }
    }



}
