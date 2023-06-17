@file:Suppress("MatchingDeclarationName")
package galaxyraiders

import galaxyraiders.adapters.BasicRandomGenerator
import galaxyraiders.adapters.tui.TextUserInterface
import galaxyraiders.adapters.web.WebUserInterface
import galaxyraiders.core.game.GameEngine
import kotlin.concurrent.thread
import kotlin.random.Random
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

import java.io.File

object AppConfig {
  val config = Config("GR__APP__")

  val randomSeed = config.get<Int>("RANDOM_SEED")
  val operationMode = config.get<OperationMode>("OPERATION_MODE")
}

private fun saveScore(date: ZonedDateTime, score: Int, destroyed: Int) {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:SS Z") 
    val current = date.format(formatter)

    val output = score.toString() + ' ' + destroyed.toString() + ' ' + current.toString()
    File("../../../../src/main/kotlin/galaxyraiders/core/score/Scoreboard.json").appendText(output)
    updateLeaderboard(score, output)
}

private fun getScorefromLine(line: String): Int {
  return line.substring(0, line.indexOf(' ')).toInt()
}


private fun updateLeaderboard(score: Int, info: String) {
   val bestScores = mutableListOf<String>();
   File("../../../../src/main/kotlin/galaxyraiders/core/score/Scoreboard.json").forEachLine {bestScores.add(it)}
  
   if (score > getScorefromLine(bestScores[0])) {
    bestScores[2] = bestScores[1]
    bestScores[1] = bestScores[0] 
    bestScores[0] = info
   }

   else if (score > getScorefromLine(bestScores[1])) {
    bestScores[2] = bestScores[1]
    bestScores[1] = info
   }

   else if (score > getScorefromLine(bestScores[2])) {
    bestScores[2] = info
   }

   File("../../../../src/main/kotlin/galaxyraiders/core/score/Scoreboard.json").writeText(bestScores[0] + '\n' + bestScores[1] + '\n' + bestScores[2])


}

fun main() {
  val generator = BasicRandomGenerator(
    rng = Random(seed = AppConfig.randomSeed)
  )

  val ui = when (AppConfig.operationMode) {
    OperationMode.Text -> TextUserInterface()
    OperationMode.Web -> WebUserInterface()
  }

  val (controller, visualizer) = ui.build()

  val gameEngine = GameEngine(
    generator, controller, visualizer
  )

  val date = ZonedDateTime.now()
  
  println(System.getProperty("java.class.path"))
  thread { gameEngine.execute() }

  ui.start()

  saveScore(date, gameEngine.score, gameEngine.asteroidsDestroyed)
}
