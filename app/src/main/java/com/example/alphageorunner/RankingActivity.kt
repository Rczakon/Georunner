package com.example.alphageorunner

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class RankingActivity : AppCompatActivity() {

    lateinit var playerDistanceList: MutableList<String>
    lateinit var playerUsernameList: MutableList<String>
    private lateinit var exitButton: Button


    private val usersListener = object: ValueEventListener {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            if(dataSnapshot!!.exists()) {
                var counter = 0
                for(p in dataSnapshot.children) {
                    Log.d("Uż1: ", counter.toString())
                    playerUsernameList.add(p.child("userName").value.toString())
                    val truncatedDistance = String.format("%.2f", p.child("totalDistance").value)
                    playerDistanceList.add(truncatedDistance)
                    Log.d("Uż2: ", playerUsernameList[counter])
                    Log.d("Uż3: ", playerDistanceList[counter])
                    counter++
                }
                var username1 = findViewById<TextView>(R.id.player1)
                val username2 = findViewById<TextView>(R.id.player2)
                val username3 = findViewById<TextView>(R.id.player3)
                val username4 = findViewById<TextView>(R.id.player4)
                val username5 = findViewById<TextView>(R.id.player5)

                val distance1 = findViewById<TextView>(R.id.distance1)
                val distance2 = findViewById<TextView>(R.id.distance2)
                val distance3 = findViewById<TextView>(R.id.distance3)
                val distance4 = findViewById<TextView>(R.id.distance4)
                val distance5 = findViewById<TextView>(R.id.distance5)

                val listLength = playerDistanceList.size
                username1.text = playerUsernameList[listLength-1]
                username2.text = playerUsernameList[listLength-2]
                username3.text = playerUsernameList[listLength-3]
                username4.text = playerUsernameList[listLength-4]
                username5.text = playerUsernameList[listLength-5]

                distance1.text = playerDistanceList[listLength-1] + "m"
                distance2.text = playerDistanceList[listLength-2] + "m"
                distance3.text = playerDistanceList[listLength-3] + "m"
                distance4.text = playerDistanceList[listLength-4] + "m"
                distance5.text = playerDistanceList[listLength-5] + "m"
            }
        }
        override fun onCancelled(databaseError: DatabaseError) {
            Log.w("Error ", "loadPost:onCancelled", databaseError.toException())
        }
    }

    val exitListener = View.OnClickListener {
        finish()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ranking)
        playerDistanceList = mutableListOf()
        playerUsernameList = mutableListOf()
        exitButton = findViewById(R.id.exitButton)
        exitButton.setOnClickListener(exitListener)
        val rankingQuery: Query = FirebaseDatabase.getInstance().getReference("/users/")
            .orderByChild("totalDistance")
            .startAt(1.0)
        rankingQuery.addListenerForSingleValueEvent(usersListener)

    }
}
