package galaxyraiders.core.game

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.io.File
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue



class ScoreRecorder (
    var score: Int = 0,
    var asteroidsDestroyed: Int = 0,
    var date: String = ""
) {

    val mapper = jacksonObjectMapper()
    
    fun setDate() {
        val current = ZonedDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:SS Z") 
        this.date = current.format(formatter)
    }

    fun saveScore() {
        checkFiles()
        
        val currentScoreList: MutableList<ScoreRecorder> = mapper.readValue(File("./core/score/Scoreboard.json"))
        currentScoreList.add(this)
        mapper.writeValue(File("./core/score/Scoreboard.json"), currentScoreList)

        updateLeaderboard()
    }

    private fun checkFiles() {
        val directory = File("./core/score")
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
    val bestScores: MutableList<ScoreRecorder> = mapper.readValue(File("./core/score/Leaderboard.json"))
    
    if (this.score > bestScores[0].score) {
        bestScores[2] = bestScores[1]
        bestScores[1] = bestScores[0] 
        bestScores[0] = this
    }

    else if (this.score > bestScores[1].score) {
        bestScores[2] = bestScores[1]
        bestScores[1] = this
    }

    else if (this.score > bestScores[2].score) {
        bestScores[2] = this
    }

    mapper.writeValue(File("./core/score/Leaderboard.json"), bestScores)

    }

}