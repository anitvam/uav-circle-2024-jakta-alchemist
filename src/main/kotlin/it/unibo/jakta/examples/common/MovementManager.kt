package it.unibo.jakta.examples.common

import it.unibo.jakta.examples.common.SwarmPosition
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object CircleMovement {
    fun positionInCircumference(
        radius: Double,
        radians: Double,
        center: SwarmPosition,
    ): SwarmPosition = SwarmPosition(
        radius * cos(radians) + center.x,
        radius * sin(radians) + center.y,
    )

    fun degreesToRadians(degrees: Int): Double {
        return degrees * PI / 180
    }
}
