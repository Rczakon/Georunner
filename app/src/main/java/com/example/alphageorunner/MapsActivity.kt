package com.example.alphageorunner

import Player
import Quest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener{

    private lateinit var signOutButton: Button
    private lateinit var statsButton: Button
    private lateinit var questButton: Button
    private lateinit var cheatButton: Button
    private var startingDistance = 0.0
    private lateinit var latitudeText: TextView
    private lateinit var longitudeText: TextView
    private lateinit var playerNameText: TextView
    private lateinit var playerSpeedText: TextView
    private lateinit var playerDistanceText: TextView

    private lateinit var Player2: Player
    private lateinit var currentUserID: String
    private val FILE_NAME = "savedPlayerStats.json"
    private val REQUEST_PLAYER = 1


    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    private lateinit var fetchedText: String
    private lateinit var fetchedText2: String
    private var currentSpeed = 0.0
    @RequiresApi(Build.VERSION_CODES.O)
    private var previousTime: LocalDateTime = LocalDateTime.now()
    @RequiresApi(Build.VERSION_CODES.O)
    private var runningQuestStartingDate = LocalDateTime.now()

    private lateinit var locationCallback: LocationCallback
    private var isBeforeFirstLocation = true

    private lateinit var locationRequest: LocationRequest
    private var locationUpdateState = false

    private lateinit var database: DatabaseReference

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val REQUEST_CHECK_SETTINGS = 2
        val STATE_DISTANCE = "Player distance"
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                locationUpdateState = true
                startLocationUpdates()
            }
        }
        if(requestCode == REQUEST_PLAYER && resultCode == Activity.RESULT_OK) {
            Player2 = data?.getSerializableExtra("RETURNED_PLAYER") as Player
            runningQuestStartingDate = data?.getSerializableExtra("RETURNED_QUEST_DATE") as LocalDateTime?
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?)  {
        super.onCreate(savedInstanceState)
        if(savedInstanceState != null) {
            with(savedInstanceState) {
                Log.d("LOG_STATE", "UWAGA BYŁO TU COŚ")
                startingDistance = getDouble(STATE_DISTANCE)
            }
        }
        setContentView(R.layout.activity_maps)

        questButton = findViewById<Button>(R.id.questButton)
        statsButton = findViewById<Button>(R.id.statsButton)
        cheatButton = findViewById(R.id.cheatButton)
        signOutButton = findViewById(R.id.signOutButton)

        currentUserID = FirebaseAuth.getInstance().uid.toString()
        Log.d("LOG_AUTH_ID", currentUserID)
        val userRef = FirebaseDatabase.getInstance().getReference("/users/$currentUserID")
        val userListener = object: ValueEventListener {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val player = dataSnapshot.value
                fetchedText2 = player.toString()
                val gson = Gson()
                val playerType = object : TypeToken<Player>() {}.type
                var newPlayer: Player = gson.fromJson(fetchedText2, playerType)
                Player2 = Player(newPlayer.id, newPlayer.userName, newPlayer.level, newPlayer.totalDistance)
                Player2.email = newPlayer.email
                if(newPlayer.quests.isNullOrEmpty()) {
                    initializePlayerQuests(Player2)
                }else {
                    Player2.quests = newPlayer.quests
                    Player2.quests.forEach {
                        it.description = it.description.replace("-", " ")
                    }
                    Player2.completedVisitingQuests = newPlayer.completedVisitingQuests
                    Player2.completedWalkingQuests = newPlayer.completedWalkingQuests
                    Player2.experiencePoints = newPlayer.experiencePoints
                    Player2.currentQuestStreak = newPlayer.currentQuestStreak
                }
                Player2.currentDistance = startingDistance

            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("Error ", "loadPost:onCancelled", databaseError.toException())
            }
        }
        userRef.addValueEventListener(userListener)
        val moveToStats = View.OnClickListener {
            val intent = Intent(this, StatsActivity::class.java)
            intent.putExtra("PASSED_PLAYER", Player2)
            startActivity(intent)
        }
        val moveToQuests = View.OnClickListener {
            val intent = Intent(this, QuestsActivity::class.java)
            intent.putExtra("PASSED_PLAYER", Player2)
            var currentCity = getAddress(LatLng(lastLocation.latitude, lastLocation.longitude))
            intent.putExtra("CURRENT_CITY", currentCity)
            startActivityForResult(intent, REQUEST_PLAYER)
        }

        val signOut = View.OnClickListener {
            saveUserToDatabase(Player2)
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        val addSomeDistance = View.OnClickListener {
            Player2.currentDistance += 100.0
            Player2.totalDistance += 100.0
        }

        statsButton.setOnClickListener(moveToStats)
        questButton.setOnClickListener(moveToQuests)
        cheatButton.setOnClickListener(addSomeDistance)
        signOutButton.setOnClickListener(signOut)

        locationCallback = object : LocationCallback() {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                var distanceInMeters = 0.0
                val currentDateTime = LocalDateTime.now()
                val timeDifference = ChronoUnit.SECONDS.between(previousTime, currentDateTime)
                Log.d("TIME_CURRENT", currentDateTime.toString())
                Log.d("TIME_PREVIOUS", previousTime.toString())
                Log.d("TIME_DIFFERENCE", timeDifference.toString())
                previousTime = currentDateTime

                if(isBeforeFirstLocation) {
                    lastLocation = p0.lastLocation
                    isBeforeFirstLocation = false
                }else {
                    distanceInMeters = lastLocation.distanceTo(p0.lastLocation).toDouble()
                    currentSpeed = (distanceInMeters / timeDifference)
                    lastLocation = p0.lastLocation
                }

                if(Player2 != null) {
                    Player2.currentDistance = Player2.currentDistance + distanceInMeters
                    checkQuestCompletion()
                    Player2.totalDistance = Player2.totalDistance + distanceInMeters
                    Log.d("LOG_CURRENT_DISTANCE", distanceInMeters.toString())
                    Log.d("LOG_TOTAL_DISTANCE", Player2.currentDistance.toString())
                    Log.d("LOG_CURRENT_SPEED", currentSpeed.toString())
                }
            }
        }
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createLocationRequest()
    }
    override fun onResume() {
        super.onResume()
        if (!locationUpdateState) {
            startLocationUpdates()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        saveUserToDatabase(Player2)
        this.moveTaskToBack(true)
    }
    override fun onMarkerClick(p0: Marker?) = false
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.uiSettings.isScrollGesturesEnabled = false
        map.setOnMarkerClickListener(this)
        setUpMap()
        // 1
        map.isMyLocationEnabled = true

    }
    private fun getAddress(latLng: LatLng): String {
        // 1
        val geocoder = Geocoder(this)
        val addresses: List<Address>?
        val address: Address?
        var addressText = "Default"
        var currentCity = "Unknown"
        try {
            // 2
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            Log.d("ADDRESS", addresses[0].toString())
            // 3
            if (null != addresses && !addresses.isEmpty()) {
                address = addresses[0]
                addressText = address.getAddressLine(0).toString()
                currentCity = addresses.get(0).locality.toString()
                Log.d("CURRENT_CITY", currentCity)
            }
        } catch (e: IOException) {
            Log.e("MapsActivity", e.localizedMessage)
        }

        return currentCity
    }
    private fun placeMarkerOnMap(location: LatLng) {
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 17.0f))
        val markerOptions = MarkerOptions().position(location)
        val titleStr = getAddress(location)  // add these two lines
        markerOptions.title(titleStr)

        map.addMarker(markerOptions)
    }
    private fun createLocationRequest()
    {

        locationRequest = LocationRequest()
        locationRequest.interval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        val client = LocationServices.getSettingsClient(this)
        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            locationUpdateState = true
            startLocationUpdates()
        }
        task.addOnFailureListener { e ->
            if (e is ResolvableApiException) {
                try {
                    e.startResolutionForResult(this@MapsActivity,
                        REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                }
            }
        }
    }
    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE)
            return
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }
    private fun setUpMap() {
        map.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            if (location != null) {
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                placeMarkerOnMap(currentLatLng)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 17f))
            }
        }
    }

    private fun getJsonDataFromAsset(context: Context, fileName: String): String {
        val jsonString: String
        try {
            jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
        } catch (ioException: IOException) {
            ioException.printStackTrace()
            return "NOT_FOUND"
        }
        return jsonString
    }

    private fun saveDataToLocalFile() {
        resetQuests()
        val gsonPretty = GsonBuilder().setPrettyPrinting().create()
        val prettyJsonPlayer: String = gsonPretty.toJson(Player2)
        try {
            val fileStream: FileOutputStream = openFileOutput(FILE_NAME, Context.MODE_PRIVATE)
            fileStream.write(prettyJsonPlayer.toByteArray())
            Toast.makeText(this, "Saved to" + filesDir + "/" + FILE_NAME, Toast.LENGTH_LONG).show()
        } catch(e: FileNotFoundException) {
            Log.d("ERROR", "NO_FILE_FOUND:(")
        } catch(e: IOException) {
            Log.d("ERROR", "NO_IO_EXCEPTION:(")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun checkQuestCompletion() {
        Player2.quests.forEach{
            if(it.type == "walking" && !it.completed) {
                if(currentSpeed <= 5.5) {
                    if(it.distanceAtStart == -1.0) {
                        it.distanceAtStart = Player2.totalDistance
                    }
                    if(Player2.totalDistance >= it.distanceRequired + it.distanceAtStart) {
                        Toast.makeText(this, "Zadanie nr:" + it.id + " wykonane", Toast.LENGTH_LONG).show()
                        it.completed = true
                    }
                }else {
                    Toast.makeText(this, "Poruszasz się zbyt szybko!", Toast.LENGTH_LONG).show()
                }
            } else if(it.type == "visiting" && !it.completed) {
                val results = FloatArray(1)
                Location.distanceBetween(lastLocation.latitude, lastLocation.longitude, it.latRequired.toDouble(), it.lonRequired.toDouble(), results)
                val distanceFromDestination = results[0]
                if(distanceFromDestination < 10) {
                    Toast.makeText(this, "Zadanie nr:" + it.id + " wykonane", Toast.LENGTH_LONG).show()
                    it.completed = true
                }
            } else if(it.type == "running" && !it.completed && it.runningStarted) {
                if(it.distanceAtStart == -1.0) {
                    it.distanceAtStart = Player2.totalDistance
                }
                if(Player2.totalDistance >= it.distanceRequired + it.distanceAtStart) {
                    val currentDateTime = LocalDateTime.now()
                    val questTime = runningQuestStartingDate
                    val timeDifference = ChronoUnit.SECONDS.between(questTime, currentDateTime)
                    val speedOfCompletion = it.distanceRequired / timeDifference
                    if(speedOfCompletion >= 1.8 && speedOfCompletion <= 12.5) {
                        it.completed = true
                        it.runningStarted = false
                    } else {
                        Toast.makeText(this, "Bieg nie został zaliczony", Toast.LENGTH_LONG).show()
                        it.runningFailed = true
                        it.runningStarted = false
                    }
                }
            }

        }
    }

    private fun initializePlayerQuests(player: Player) {
        val gson = Gson()
        val jsonQuestsFile = getJsonDataFromAsset(applicationContext, "quests.json")
        val questListType = object : TypeToken<List<Quest>>() {}.type
        var quests: List<Quest> = gson.fromJson(jsonQuestsFile, questListType)
        player.quests.clear()
        player.quests.add(quests[0])
        player.quests.add(quests[1])
        player.quests.add(quests[2])
    }

    private fun resetQuests(){
        Player2.quests[0].completed = false
        Player2.quests[0].distanceAtStart = -1.0
        Player2.totalDistance = 0.0
        initializePlayerQuests(Player2)
    }

    private fun saveUserToDatabase(player: Player) {
        val uid = FirebaseAuth.getInstance().uid
        player.quests.forEach {
            it.description = it.description.replace(" ", "-")
        }
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid/")
        ref.setValue(player)
            .addOnSuccessListener {
                Log.d("SAVING_USER", "SUCCESS")
            }
            .addOnFailureListener {
                Log.d("SAVING_USER", "FAIL:  ${it.message}")
            }
    }

    private fun fetchUserFromDatabase(){

    }

}
