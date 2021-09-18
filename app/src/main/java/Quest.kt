
import android.os.Build
import androidx.annotation.RequiresApi
import java.io.Serializable

class Quest(
    val id: String,
    var completed: Boolean,
    val type: String,
    var description: String,
    val distanceRequired: Int,
    var experience: Int,
    var distanceAtStart: Double
) : Serializable{
    var latRequired = ""
    var lonRequired = ""
    @RequiresApi(Build.VERSION_CODES.O)
    //lateinit var startingDate: LocalDateTime
    var runningStarted = false
    var runningFailed = false

    constructor(): this("", false, "", "", 0, 0, 0.0) {

    }

    fun checkCompletion(totalDistance: Double): Boolean {
        return totalDistance >= this.distanceAtStart + this.distanceRequired
    }
}