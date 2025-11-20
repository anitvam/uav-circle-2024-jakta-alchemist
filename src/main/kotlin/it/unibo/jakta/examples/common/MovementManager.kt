package it.unibo.jakta.examples.common

import it.unibo.alchemist.model.molecules.SimpleMolecule
import it.unibo.jakta.agents.bdi.Agent
import it.unibo.jakta.examples.common.SwarmPosition
import it.unibo.jakta.examples.main.MainSwarmEnvironment
import it.unibo.jakta.examples.main.MainSwarmEnvironment.Companion.ENVIRONMENT_SIZE
import org.apache.commons.math3.util.MathArrays
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

val destination = SimpleMolecule("desiredPosition")

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

fun computeDistance(source: SwarmPosition, target: SwarmPosition): Double =
    MathArrays.distance(
        doubleArrayOf(source.x, source.y),
        doubleArrayOf(target.x, target.y)
    )

fun computeStepTowardsDestination(current: SwarmPosition, target: SwarmPosition, maxWalk: Double): SwarmPosition {
    val vector = target - current
    val distance = computeDistance(vector, target)
    if (distance <= maxWalk || distance == 0.0) return target
    val angle = atan2(vector.y, vector.x)
    val dx = maxWalk * cos(angle)
    val dy = maxWalk * sin(angle)
    return current + SwarmPosition(dx, dy)
}

fun computeDistanceError(positions: Map<String, SwarmPosition>): Double {
    val (leader, leaderPosition) = positions.filter { it.key.contains("leader") }.entries.first()
    val followersPosition = positions.filter { it.key.contains("follower") }
    val angle = (2 * PI) / followersPosition.count()
    val error = followersPosition
        .entries
        .associate { it.key.substringAfter("@").toInt() to it.value }
        .toSortedMap()
        .entries
        .mapIndexed { index, (_, followerPosition) ->
        val idealAngle = index * angle
        val idealPosition = CircleMovement.positionInCircumference(
            ENVIRONMENT_SIZE,
            idealAngle,
            leaderPosition,
        )
        val diff = idealPosition - followerPosition
        hypot(diff.x, diff.y).let { it * it }
    }.sum()
    return error
}

fun randomPointOnCircumference(
    environmentSize: Double,
): SwarmPosition {
    require(environmentSize >= 0.0) { "radius must be non-negative" }
    val theta = Random.nextDouble(0.0, 2 * PI)
    val dist = Random.nextDouble(0.0, ENVIRONMENT_SIZE / 2.0)
    val x = dist * cos(theta)
    val y = dist * sin(theta)
    return SwarmPosition(x, y)
}


