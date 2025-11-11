package it.unibo.jakta.examples.simulation

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Position2D

data class SwarmPosition(
    override val x: Double,
    override val y: Double,
): Position2D<SwarmPosition> {

    override val coordinates: DoubleArray
        get() = arrayOf(x, y).toDoubleArray()

    override val dimensions: Int = 2

    override fun boundingBox(range: Double): List<SwarmPosition> {
        TODO("Not required for this example")
    }

    override fun distanceTo(other: SwarmPosition): Double {
        TODO("Not required for this example")
    }

    override fun plus(other: DoubleArray): SwarmPosition = this.plus(SwarmPosition(other[0], other[1]))

    override fun minus(other: DoubleArray): SwarmPosition = this.minus(SwarmPosition(other[0], other[1]))

    fun <P : Position<P>> toAlchemistPosition(alchemistEnvironment: Environment<*, P>): P =
        alchemistEnvironment.makePosition(x, y)

    operator fun plus(other: SwarmPosition): SwarmPosition = SwarmPosition(x + other.x, y + other.y)

    operator fun minus(other: SwarmPosition): SwarmPosition = SwarmPosition(x - other.x, y - other.y)

    @Deprecated("Access to coordinates in a 2D manifold should be performed using getX / getY")
    override fun getCoordinate(dimension: Int): Double {
        require(dimension in 0..< dimensions) {
            dimension.toString() + "is not an allowed dimension, only values between 0 and 1 are allowed."
        }
        return coordinates[dimension]
    }

    companion object {
        fun fromDoubleArray(doubleArray: DoubleArray): SwarmPosition =
            SwarmPosition(doubleArray[0], doubleArray[1])

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