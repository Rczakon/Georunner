package com.example.alphageorunner

import Player
import Quest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_quests.*
import java.io.IOException
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.random.Random


class QuestsActivity : AppCompatActivity() {


    private lateinit var Player1: Player
    private lateinit var Quest1: Quest
    private lateinit var Quest2: Quest
    private lateinit var Quest3: Quest
    private lateinit var currentCity: String
    private lateinit var exitButton: Button

    private val FILE_NAME = "savedPlayerStats.json"
    private lateinit var fetchedText: String
    //private lateinit var questButton: Button
    private lateinit var questTextView1: TextView
    private lateinit var questTextView2: TextView
    private lateinit var questTextView3: TextView
    private lateinit var levelUpView: TextView

    private lateinit var runningButton1: Button
    private lateinit var runningButton2: Button
    private lateinit var runningButton3: Button

    private lateinit var levelUpAnimation: Animation


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quests)

        val gson = Gson()
        val jsonPlayerFile = getJsonDataFromAsset(applicationContext, "playerStats.json")
        val jsonQuestsFile = getJsonDataFromAsset(applicationContext, "quests.json")
        val playerType = object : TypeToken<Player>() {}.type
        val questListType = object : TypeToken<List<Quest>>() {}.type
        var quests: List<Quest> = gson.fromJson(jsonQuestsFile, questListType)
        var startedRunningQuestDate = LocalDateTime.now()



        Log.d("LOG_QUESTS", quests[0].description.toString())

        Player1 = intent.getSerializableExtra("PASSED_PLAYER") as Player
        currentCity = intent.getSerializableExtra("CURRENT_CITY") as String
        levelUpAnimation = AnimationUtils.loadAnimation(this, R.anim.level_up_animation)

        Log.d("CURRENT_CITY", currentCity)

        exitButton = findViewById(R.id.exitButton)
        questTextView1 = findViewById(R.id.quest1)
        questTextView2 = findViewById(R.id.quest2)
        questTextView3 = findViewById(R.id.quest3)

        runningButton1 = findViewById(R.id.runningButton1)
        runningButton2 = findViewById(R.id.runningButton2)
        runningButton3 = findViewById(R.id.runningButton3)

        levelUpView = findViewById(R.id.levelUpView)

        fillCompletedDays(Player1.currentQuestStreak)

        if(Player1.quests[0].type != "running") {
            runningButton1.visibility = View.GONE
        }
        if(Player1.quests[1].type != "running") {
            runningButton2.visibility = View.GONE
        }
        if(Player1.quests[2].type != "running") {
            runningButton3.visibility = View.GONE
        }

        questTextView1.text = Player1.quests[0].description
        questTextView2.text = Player1.quests[1].description
        questTextView3.text = Player1.quests[2].description


        var loopCounter = 0
        for(quest in Player1.quests) {
            if(quest.completed) {
                when(loopCounter){
                    0 -> questTextView1.setTextColor(Color.GREEN);
                    1->  questTextView2.setTextColor(Color.GREEN);
                    2 -> questTextView3.setTextColor(Color.GREEN);
                }
            }
            loopCounter += 1
        }


        val exitListener = View.OnClickListener {
//            setResult(Activity.RESULT_OK, Intent().putExtra("RETURNED_PLAYER", Player1))
//            setResult(Activity.RESULT_OK, Intent().putExtra("RUNNING_QUEST_DATE", startedRunningQuestDate))
//            finish()
            val output = Intent()
            output.putExtra("RETURNED_PLAYER", Player1)
            output.putExtra("RETURNED_QUEST_DATE", startedRunningQuestDate)
            setResult(Activity.RESULT_OK, output)
            finish()
        }
        exitButton.setOnClickListener(exitListener)

        val claimQuest1 = View.OnClickListener {
            if(Player1.quests[0].completed) {
                it.visibility = View.INVISIBLE
                unlockReward(Player1.quests[0], 0)
                Toast.makeText(this, "Gratulacje!", Toast.LENGTH_LONG).show()
            }
            else {
                Toast.makeText(this, "Zadanie nie zostało jeszcze ukończone", Toast.LENGTH_LONG).show()
            }
        }
        val claimQuest2 = View.OnClickListener {
            if(Player1.quests[1].completed) {
                it.visibility = View.INVISIBLE
                unlockReward(Player1.quests[1], 1)
                Toast.makeText(this, "Gratulacje!", Toast.LENGTH_LONG).show()
            }
            else {
                Toast.makeText(this, "Zadanie nie zostało jeszcze ukończone", Toast.LENGTH_LONG).show()
            }
        }
        val claimQuest3 = View.OnClickListener {
            if(Player1.quests[2].completed) {
                it.visibility = View.INVISIBLE
                unlockReward(Player1.quests[2], 2)
                Toast.makeText(this, "Gratulacje!", Toast.LENGTH_LONG).show()
            }
            else {
                Toast.makeText(this, "Zadanie nie zostało jeszcze ukończone", Toast.LENGTH_LONG).show()
            }
        }

        if(Player1.quests[0].runningStarted) {
            runningButton1.text = "✔"
        }
        if(Player1.quests[1].runningStarted) {
            runningButton1.text = "✔"
        }
        if(Player1.quests[2].runningStarted) {
            runningButton1.text = "✔"
        }

        val startRunningQuest1 = View.OnClickListener {
            if(!Player1.quests[0].runningStarted) {
                startedRunningQuestDate = LocalDateTime.now()
                runningButton1.text = "✔"
                Player1.quests[0].runningStarted = true
            } else {
                runningButton1.text = "►"
            }
        }
        val startRunningQuest2 = View.OnClickListener {
            if(!Player1.quests[1].runningStarted) {
                startedRunningQuestDate = LocalDateTime.now()
                runningButton1.text = "✔"
                Player1.quests[1].runningStarted = true
            } else {
                runningButton1.text = "►"
            }
        }
        val startRunningQuest3 = View.OnClickListener {
            if(!Player1.quests[2].runningStarted) {
                startedRunningQuestDate = LocalDateTime.now()
                runningButton1.text = "✔"
                Player1.quests[2].runningStarted = true
            } else {
                runningButton1.text = "►"
            }
        }

        questTextView1.setOnClickListener(claimQuest1)
        questTextView2.setOnClickListener(claimQuest2)
        questTextView3.setOnClickListener(claimQuest3)

        runningButton1.setOnClickListener(startRunningQuest1)
        runningButton2.setOnClickListener(startRunningQuest2)
        runningButton3.setOnClickListener(startRunningQuest3)
    }

    private fun getJsonDataFromAsset(context: Context, fileName: String): String? {
        val jsonString: String
        try {
            jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
        } catch (ioException: IOException) {
            ioException.printStackTrace()
            return null
        }
        return jsonString
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun unlockReward(completedQuest: Quest, index: Int) {
        Toast.makeText(this, "Oto nagroda: " + completedQuest.experience, Toast.LENGTH_LONG).show()
        if(Player1.lastQuestCompletionDate != null) {
            if(daysDifference(Player1.lastQuestCompletionDate!!) == 1) {
                Player1.currentQuestStreak += 1
                if(Player1.currentQuestStreak == 5) {
                    Toast.makeText(this, "Oto nagroda: 500xp", Toast.LENGTH_LONG).show()
                    Player1.experiencePoints += 500
                }
                Player1.lastQuestCompletionDate = LocalDateTime.now()
            }else if(daysDifference(Player1.lastQuestCompletionDate!!) > 1){
                Player1.currentQuestStreak = 1
                image_day_1.setImageResource((R.drawable.day_1_unlocked))
                image_day_2.setImageResource((R.drawable.day_2_locked))
                image_day_3.setImageResource((R.drawable.day_3_locked))
                image_day_4.setImageResource((R.drawable.day_4_locked))
                image_day_5.setImageResource((R.drawable.day_5_locked))
            }
        } else {
            Player1.currentQuestStreak += 1
            Player1.lastQuestCompletionDate = LocalDateTime.now()
            fillCompletedDays(Player1.currentQuestStreak)
        }
        Player1.lastQuestCompletionDate = LocalDateTime.now()
        Player1.experiencePoints += completedQuest.experience
        if(completedQuest.type == "walking") {
            Player1.completedWalkingQuests += 1
        }else if(completedQuest.type == "visiting"){
            Player1.completedVisitingQuests += 1
        } else {
            Player1.completedRunningQuests += 1
        }
        Player1.quests.removeAt(index)
        checkLevelUp()
        addNewRandomQuest(index)
    }

    private fun addNewRandomQuest(index: Int) {
        val gson = Gson()
        val jsonQuestsFile = getJsonDataFromAsset(applicationContext, "quests.json")
        val questListType = object : TypeToken<List<Quest>>() {}.type
        var quests: List<Quest> = gson.fromJson(jsonQuestsFile, questListType)
        var randomQuest = Random.nextInt(0, quests.size)
        Log.d("QUEST_AMOUNT", quests.size.toString())
        Log.d("QUEST_RANDOM", randomQuest.toString())
        Log.d("QUEST_RANDOM_ID", quests[randomQuest].id)
        if(quests[randomQuest].id.toInt() in 101..199) {
            if(currentCity == "Katowice") {
                while(quests[randomQuest].id.toInt() !in IntRange(100, 124)) {
                    randomQuest = Random.nextInt(0, quests.size)
                }
            } else if(currentCity == "Chorzów") {
                while(quests[randomQuest].id.toInt() !in IntRange(124, 149)) {
                    randomQuest = Random.nextInt(0, quests.size)
                }
            }
        } else if(quests[randomQuest].id.toInt() > 199) {
            when (index) {
                0 -> runningButton1.visibility = View.VISIBLE
                1 -> runningButton2.visibility = View.VISIBLE
                2 -> runningButton3.visibility = View.VISIBLE
            }
        }

        Player1.quests.add(index, quests[randomQuest])
        if(index == 0) {
            questTextView1.text = Player1.quests[index].description
            questTextView1.setTextColor(Color.parseColor("#FFFFFF"))
            questTextView1.visibility = View.VISIBLE
        }else if(index == 1) {
            questTextView2.text = Player1.quests[index].description
            questTextView2.setTextColor(Color.parseColor("#FFFFFF"))
            questTextView2.visibility = View.VISIBLE
        }
        else {
            questTextView3.text = Player1.quests[index].description
            questTextView3.setTextColor(Color.parseColor("#FFFFFF"))
            questTextView3.visibility = View.VISIBLE
        }

    }

    private fun checkLevelUp() {
        Player1.levelRequirements.forEach{
            if(Player1.experiencePoints >= Player1.levelRequirements[Player1.level - 1]) {
                Player1.level += 1
                levelUpView.visibility = View.VISIBLE
                levelUpView.animation = levelUpAnimation
                levelUpView.visibility = View.INVISIBLE
            }
        }
    }

    private fun fillCompletedDays(currentStreak: Int) {
        if(currentStreak > 0) {
            image_day_1.setImageResource((R.drawable.day_1_unlocked))
            if(currentStreak > 1) {
                image_day_2.setImageResource((R.drawable.day_2_unlocked))
                if(currentStreak > 2) {
                    image_day_3.setImageResource((R.drawable.day_3_unlocked))
                    if(currentStreak > 3) {
                        image_day_4.setImageResource((R.drawable.day_4_unlocked))
                        if(currentStreak > 4) {
                            image_day_5.setImageResource((R.drawable.day_5_unlocked))
                        }
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun daysDifference(lastCompletionDate: LocalDateTime): Int {
        var currentDate = LocalDateTime.now()
        return ChronoUnit.DAYS.between(lastCompletionDate, currentDate).toInt()
    }

}

