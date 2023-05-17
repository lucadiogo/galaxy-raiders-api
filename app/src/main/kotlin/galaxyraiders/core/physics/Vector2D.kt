@file:Suppress("UNUSED_PARAMETER") // <- REMOVE
package galaxyraiders.core.physics

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

const val RAD_TO_DEGREE = 180.0 / Math.PI

@JsonIgnoreProperties("unit", "normal", "degree", "magnitude")
data class Vector2D(val dx: Double, val dy: Double) {
  override fun toString(): String {
    return "Vector2D(dx=$dx, dy=$dy)"
  }

  val magnitude: Double
    get() = Math.sqrt(this.dx * this.dx + this.dy * this.dy)

  val radiant: Double
    get() = Math.atan2(this.dy, this.dx)

  val degree: Double
    get() = this.radiant * RAD_TO_DEGREE

  val unit: Vector2D
    get() = Vector2D(this.dx / this.magnitude, this.dy / this.magnitude)

  val normal: Vector2D
    get() = Vector2D(this.unit.dy, -this.unit.dx)

  operator fun times(scalar: Double): Vector2D {
    return Vector2D(scalar * this.dx, scalar * this.dy)
  }

  operator fun div(scalar: Double): Vector2D {
    return Vector2D(this.dx / scalar, this.dy / scalar)
  }

  operator fun times(v: Vector2D): Double {
    return this.dx * v.dx + this.dy * v.dy
  }

  operator fun plus(v: Vector2D): Vector2D {
    return Vector2D(this.dx + v.dx, this.dy + v.dy)
  }

  operator fun plus(p: Point2D): Point2D {
    return Point2D(this.dx + p.x, this.dy + p.y)
  }

  operator fun unaryMinus(): Vector2D {
    return Vector2D(-this.dx, -this.dy)
  }

  operator fun minus(v: Vector2D): Vector2D {
    return this.plus(-v)
  }

  fun scalarProject(target: Vector2D): Double {
    if ((target.magnitude) > 0) {
      return (this.times(target)) / target.magnitude
    }
    return 0.0
  }
  fun vectorProject(target: Vector2D): Vector2D {
    return this.scalarProject(target).times(target.unit)
  }
}

operator fun Double.times(v: Vector2D): Vector2D {
  return v.times(this)
}
