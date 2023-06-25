package galaxyraiders.core.game

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.io.File
import java.io.EOFException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.annotation.JsonIgnore


class ScoreRecorder (
    var score: Int = 0,
    var asteroidsDestroyed: Int = 0,
    var date: String = ""
) {

    @JsonIgnore
    val mapper = jacksonObjectMapper()
    
    fun setDate() {
        val current = ZonedDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss Z") 
        this.date = current.format(formatter)
    }

    fun saveScore() {
        checkFiles()
        
        var currentScoreList: MutableList<ScoreRecorder>
        val scoreboard = File("./core/score/Scoreboard.json")
        
        if (scoreboard.length() != 0L) currentScoreList = mapper.readValue(scoreboard)
        else currentScoreList = mutableListOf<ScoreRecorder>()
        
        if (!findAndReplace(currentScoreList)) currentScoreList.add(this)
        mapper.writeValue(scoreboard, currentScoreList)

        updateLeaderboard()
    }

    private fun checkFiles() {
        var directory = File("./core")
        if (!directory.isDirectory()) {
            directory.mkdir()
        }
        directory = File("./core/score")
        if (!directory.isDirectory()) {
            directory.mkdir()
        }
        var file = File("./core/score/Scoreboard.json")
        if (!file.exists()) {
            file.createNewFile()
        }
        file = File("./core/score/Leaderboard.json")
        if (!file.exists()) {
            file.createNewFile()
        }
    }


    private fun updateLeaderboard() {
        var bestScores: MutableList<ScoreRecorder>
        val leaderboard = File("./core/score/Leaderboard.json")
            
        if (leaderboard.length() != 0L) bestScores = mapper.readValue(leaderboard)
        else bestScores = mutableListOf<ScoreRecorder>()

        if (!findAndReplace(bestScores)) bestScores.add(this)
        bestScores.sortByDescending {it.score}
        if (bestScores.size > 3) bestScores.removeLast()

        mapper.writeValue(File("./core/score/Leaderboard.json"), bestScores)

    }

    private fun findAndReplace(list: MutableList<ScoreRecorder>): Boolean {
        var len = list.size
        if (len == 0) return false

        for (i in len - 1 downTo 0) {
            if (list[i].date.compareTo(this.date) == 0) {
                list[i] = this
                return true
            }
        }
        return false
    }

}