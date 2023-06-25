package galaxyraiders.core.game

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.io.File

@DisplayName("Given a score recorder")
class ScoreRecorderTest {

    @Test
    fun `it sets the date correctly`() {
        val scoreRecorder = ScoreRecorder()
        scoreRecorder.setDate()
        assertTrue(scoreRecorder.date.isNotBlank())
    }

    @Test
    fun `it saves the score and updates the scoreboard and leaderboard`() {
        val scoreRecorder = ScoreRecorder(score = 100, asteroidsDestroyed = 5)
        val scoreboardPath = "./core/score/Scoreboard.json"
        val leaderboardPath = "./core/score/Leaderboard.json"

        scoreRecorder.saveScore()

        assertTrue(File(scoreboardPath).exists())
        assertTrue(File(leaderboardPath).exists())

        val scoreboardContent = File(scoreboardPath).readText()
        assertTrue(scoreboardContent.contains("\"score\":100"))
        assertTrue(scoreboardContent.contains("\"asteroidsDestroyed\":5"))

        val leaderboardContent = File(leaderboardPath).readText()
        assertTrue(leaderboardContent.contains("\"score\":100"))
        assertTrue(leaderboardContent.contains("\"asteroidsDestroyed\":5"))
    }

    @Test
    fun `it finds and replaces a score in the list`() {
        val scoreList = mutableListOf(
            ScoreRecorder(score = 50, asteroidsDestroyed = 3, date = "01/01/2023 12:00:00 Z"),
            ScoreRecorder(score = 75, asteroidsDestroyed = 4, date = "02/01/2023 12:00:00 Z"),
            ScoreRecorder(score = 100, asteroidsDestroyed = 5, date = "03/01/2023 12:00:00 Z")
        )

        val newScore = ScoreRecorder(score = 100, asteroidsDestroyed = 6, date = "03/01/2023 12:00:00 Z")

        val replaced = newScore.findAndReplace(scoreList)

        assertTrue(replaced)
        assertEquals(3, scoreList.size)
        assertEquals(6, scoreList[2].asteroidsDestroyed)
    }

    @Test
    fun `it does not find and replace a score not in the list`() {
        val scoreList = mutableListOf(
            ScoreRecorder(score = 50, asteroidsDestroyed = 3, date = "01/01/2023 12:00:00 Z"),
            ScoreRecorder(score = 75, asteroidsDestroyed = 4, date = "02/01/2023 12:00:00 Z"),
            ScoreRecorder(score = 100, asteroidsDestroyed = 5, date = "03/01/2023 12:00:00 Z")
        )

        val newScore = ScoreRecorder(score = 100, asteroidsDestroyed = 6, date = "04/01/2023 12:00:00 Z")

        val replaced = newScore.findAndReplace(scoreList)

        assertFalse(replaced)
    }
}
