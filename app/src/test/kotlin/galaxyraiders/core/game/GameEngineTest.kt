package galaxyraiders.core.game

import galaxyraiders.helpers.AverageValueGeneratorStub
import galaxyraiders.helpers.ControllerSpy
import galaxyraiders.helpers.MaxValueGeneratorStub
import galaxyraiders.helpers.MinValueGeneratorStub
import galaxyraiders.helpers.VisualizerSpy
import galaxyraiders.core.physics.Point2D
import galaxyraiders.core.physics.Vector2D
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@DisplayName("Given a game engine")
class GameEngineTest {
  private val avgGenerator = AverageValueGeneratorStub()
  private val maxGenerator = MaxValueGeneratorStub()
  private val minGenerator = MinValueGeneratorStub()
  private val controllerSpy = ControllerSpy()
  private val visualizerSpy = VisualizerSpy()

  private val normalGame = GameEngine(
    generator = avgGenerator,
    controller = controllerSpy,
    visualizer = visualizerSpy,
  )

  private val easyGame = GameEngine(
    generator = maxGenerator,
    controller = controllerSpy,
    visualizer = visualizerSpy,
  )

  private val hardGame = GameEngine(
    generator = minGenerator,
    controller = controllerSpy,
    visualizer = visualizerSpy,
  )

  @Test
  fun `it has its parameters initialized correctly `() {
    assertAll(
      "GameEngine should initialize all its parameters correctly",
      { assertNotNull(normalGame) },
      { assertEquals(avgGenerator, normalGame.generator,) },
      { assertEquals(controllerSpy, normalGame.controller) },
      { assertEquals(visualizerSpy, normalGame.visualizer) },
    )
  }

  @Test
  fun `it creates a SpaceField injecting its generator `() {
    assertAll(
      "GameEngine should create a field correctly",
      { assertNotNull(normalGame.field) },
      { assertEquals(normalGame.generator, normalGame.field.generator) },
    )
  }

  @Test
  fun `it has an execution status that starts as true`() {
    assertEquals(true, normalGame.playing)
  }
  
  @Test
  fun `it can render its SpaceField`() {
    val initialNumRenders = visualizerSpy.numRenders
    
    normalGame.renderSpaceField()
    
    assertEquals(initialNumRenders + 1, visualizerSpy.numRenders)
  }
  
  @Test
  fun `it can process multiple PlayerCommand`() {
    val numPlayerCommands = controllerSpy.playerCommands.size
    
    // Process all available user commands
    repeat(numPlayerCommands) {
      normalGame.processPlayerInput()
    }
    
    // Should receive a null
    normalGame.processPlayerInput()
    
    assertEquals(0, controllerSpy.playerCommands.size)
  }
  
  @Test
  fun `it can handle explosions`() {
    val numAsteroids = hardGame.field.asteroids.size
    val numMissiles = hardGame.field.missiles.size
    val numExplosions = hardGame.field.explosions.size

    //these two should collide and create an explosion
    hardGame.field.asteroids += Asteroid(
                                initialPosition = Point2D(1.0, 1.0),
                                initialVelocity = Vector2D(0.0, 0.0),
                                radius = 1.0,
                                mass = 1.0,
                                )

    hardGame.field.missiles += Missile(
                                  initialPosition = Point2D(1.0, 1.0),
                                  initialVelocity = Vector2D(0.0, 0.0),
                                  radius = 1.0,
                                  mass = 1.0,
                                  )
  
    //these two shouldn't collide with anything
    hardGame.field.asteroids +=  Asteroid(
                                    initialPosition = Point2D(3.0, 3.0),
                                    initialVelocity = Vector2D(0.0, 0.0),
                                    radius = 0.5,
                                    mass = 1.0,
                                    )
    
    hardGame.field.missiles += Missile(
                                      initialPosition = Point2D(10.0, 10.0),
                                      initialVelocity = Vector2D(0.0, 0.0),
                                      radius = 0.5,
                                      mass = 1.0,
                                      )
    
    hardGame.handleExplosions()
    assertEquals(hardGame.field.asteroids.size, numAsteroids + 1)
    assertEquals(hardGame.field.missiles.size, numMissiles + 1)
    assertEquals(hardGame.field.explosions.size, numExplosions + 1)
  }

  @Test
  fun `it updates its space objects while playing`() {
    val numAsteroids = hardGame.field.asteroids.size
    
    hardGame.playing = true
    hardGame.updateSpaceObjects()
    
    assertEquals(numAsteroids + 1, hardGame.field.asteroids.size)
  }

  @Test
  fun `it does not update its space objects while paused`() {
    val numAsteroids = hardGame.field.asteroids.size

    hardGame.playing = false
    hardGame.updateSpaceObjects()

    assertEquals(numAsteroids, hardGame.field.asteroids.size)
  }

  @Test
  fun `it handle collisions between objects`() {
    // Degenerate scenario: both asteroids will be above each other
    hardGame.generateAsteroids()
    hardGame.generateAsteroids()

    val asteroidsInitialVelocity =
      hardGame.field.asteroids.map { it.velocity }

    hardGame.handleCollisions()

    val asteroidsFinalVelocity =
      hardGame.field.asteroids.map { it.velocity }

    assertNotEquals(asteroidsInitialVelocity, asteroidsFinalVelocity)
  }

  @Test
  fun `it can move its space objects`() {
    hardGame.field.generateAsteroid()
    hardGame.field.generateMissile()

    val ship = hardGame.field.ship
    repeat(2) { ship.boostRight() }
    val expectedShipPosition = ship.center + ship.velocity

    val asteroid = hardGame.field.asteroids[0]
    val expectedAsteroidPosition = asteroid.center + asteroid.velocity

    val missile = hardGame.field.missiles[0]
    val expectedMissilePosition = missile.center + missile.velocity

    hardGame.moveSpaceObjects()

    assertAll(
      "GameEngine should move all space objects",
      { assertEquals(expectedShipPosition, ship.center) },
      { assertEquals(expectedAsteroidPosition, asteroid.center) },
      { assertEquals(expectedMissilePosition, missile.center) },
    )
  }

  @Test
  fun `it can trim its space objects`() {
    hardGame.field.generateAsteroid()
    hardGame.field.generateMissile()

    val missile = hardGame.field.missiles.last()
    val missileDistanceToTopBorder =
      hardGame.field.boundaryY.endInclusive - missile.center.y
    val repetitionsToGetMissileOutOfSpaceField = Math.ceil(
      missileDistanceToTopBorder / Math.abs(missile.velocity.dy)
    ).toInt()

    val asteroid = hardGame.field.asteroids.last()
    val asteroidDistanceToBottomBorder =
      asteroid.center.y - hardGame.field.boundaryY.start
    val repetitionsToGetAsteroidOutsideOfSpaceField = Math.ceil(
      asteroidDistanceToBottomBorder / Math.abs(asteroid.velocity.dy)
    ).toInt()

    val repetitionsToGetSpaceObjectsOutOfSpaceField = Math.max(
      repetitionsToGetMissileOutOfSpaceField,
      repetitionsToGetAsteroidOutsideOfSpaceField,
    )

    repeat(repetitionsToGetSpaceObjectsOutOfSpaceField) {
      hardGame.moveSpaceObjects()
    }

    hardGame.trimSpaceObjects()

    assertAll(
      "GameEngine should trim all space objects",
      { assertEquals(-1, hardGame.field.missiles.indexOf(missile)) },
      { assertEquals(-1, hardGame.field.asteroids.indexOf(asteroid)) },
    )
  }

  @Test
  fun `it generates an Asteroid when probability is not zero`() {
    val numAsteroids = hardGame.field.asteroids.size
    hardGame.generateAsteroids()

    assertEquals(numAsteroids + 1, hardGame.field.asteroids.size)
  }

  @Test
  fun `it does not generate an Asteroid when probability is zero`() {
    val numAsteroids = easyGame.field.asteroids.size
    easyGame.generateAsteroids()

    assertEquals(numAsteroids, easyGame.field.asteroids.size)
  }

  @Test
  fun `it can execute one iteration of the game loop`() {
    val numPlayerCommands = controllerSpy.playerCommands.size
    val numAsteroids = hardGame.field.asteroids.size
    val numRenders = visualizerSpy.numRenders

    hardGame.tick()

    assertAll(
      "GameEngine should process input, update and render",
      { assertEquals(numPlayerCommands - 1, controllerSpy.playerCommands.size) },
      { assertEquals(numAsteroids + 1, hardGame.field.asteroids.size) },
      { assertEquals(numRenders + 1, visualizerSpy.numRenders) },
    )
  }

  @Test
  fun `it can execute up to max iterations`() {
    val numPlayerCommands = controllerSpy.playerCommands.size
    val numRenders = visualizerSpy.numRenders

    hardGame.execute(numPlayerCommands)

    // There is a pause in the list of commands, so the engine
    // does not process once whereas it always render
    val expectedNumRenders = numRenders + numPlayerCommands

    assertAll(
      "GameEngine should process input, update and render",
      { assertEquals(0, controllerSpy.playerCommands.size) },
      { assertEquals(expectedNumRenders, visualizerSpy.numRenders) },
      { assertTrue(hardGame.field.asteroids.size <= numPlayerCommands - 1) },
    )
  }



}
