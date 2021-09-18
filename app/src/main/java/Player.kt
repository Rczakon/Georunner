import java.io.Serializable
import java.time.LocalDateTime

class Player(val id: String, val userName: String, var level: Int, var totalDistance: Double) :
    Serializable {
    var currentDistance = 0.0
    var experiencePoints = 0
    var email = "default"
    var quests: MutableList<Quest> = mutableListOf()
    var completedWalkingQuests = 0
    var completedVisitingQuests = 0
    var completedRunningQuests = 0
    val levelRequirements = listOf(50, 400, 700, 1000, 3000, 5000, 10000, 20000, 50000, 100000)
    var currentQuestStreak: Int = 0
    var lastQuestCompletionDate: LocalDateTime? = null

    constructor(): this("", "", 0, 0.0) {

    }
}