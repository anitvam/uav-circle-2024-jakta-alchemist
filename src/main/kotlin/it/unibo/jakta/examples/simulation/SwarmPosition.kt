package it.unibo.jakta.examples.simulation

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Position2D
import it.unibo.alchemist.model.geometry.Vector2D
import it.unibo.alchemist.model.positions.AbstractEuclideanPosition
import it.unibo.alchemist.model.positions.AbstractPosition
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import kotlin.random.Random

data class SwarmPosition(
    val x: Double,
    val y: Double,
) {
    fun <P : Position<P>> toAlchemistPosition(alchemistEnvironment: Environment<*, P>): P =
        alchemistEnvironment.makePosition(x, y)

    operator fun plus(other: SwarmPosition): SwarmPosition = SwarmPosition(x + other.x, y + other.y)

    operator fun minus(other: SwarmPosition): SwarmPosition = SwarmPosition(x - other.x, y - other.y)

    companion object {
        fun fromDoubleArray(doubleArray: DoubleArray): SwarmPosition =
            SwarmPosition(doubleArray[0], doubleArray[1])

        fun random(): SwarmPosition = SwarmPosition(
            Random(System.nanoTime()).nextDouble(),
            Random(System.nanoTime()).nextDouble(),
        )

        fun fromPosition(position: Any): SwarmPosition {
            if (position is Position<*>) {
                return fromDoubleArray(position.coordinates)
            } else if (position is SwarmPosition) {
                return position
            }
            error("Argument is not a Position<*>, don't know what to do with it. position is ${position.javaClass}")
        }
    }
}