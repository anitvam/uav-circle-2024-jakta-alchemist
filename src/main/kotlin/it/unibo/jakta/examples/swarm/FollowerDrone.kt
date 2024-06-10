@file:JvmName("Drone")

package it.unibo.jakta.examples.swarm

import it.unibo.alchemist.jakta.properties.JaktaEnvironmentForAlchemist
import it.unibo.alchemist.jakta.util.fix
import it.unibo.alchemist.model.Position
import it.unibo.jakta.agents.dsl.device
import it.unibo.tuprolog.solve.libs.oop.ObjectRef
import kotlin.math.PI

fun <P : Position<P>> JaktaEnvironmentForAlchemist<P>.follower() =
    device {
        environment {
            actions {
                action("follow", 3) {
                    val center = argument<ObjectRef>(0).fix<SwarmPosition>()
                    val radius = argument<ObjectRef>(1).fix<Double>()
                    val otherNodes = argument<ObjectRef>(2).fix<Set<Int>>()
                    val myPosition = SwarmPosition.fromPosition(alchemistEnvironment.getPosition(node))

                    // Compute my destination in the circle
                    val angles = (2 * PI) / otherNodes.count()
                    val destinationAngle = otherNodes.sorted().indexOf(node.id) * angles
                    val destinationPosition = CircleMovement.positionInCircumference(
                        radius,
                        destinationAngle,
                        center,
                    )
                    val movement = destinationPosition - myPosition
                    addData("velocity", doubleArrayOf(movement.x, movement.y))
                    node.setConcentration(destination, destinationPosition)
                }
            }
        }
        agent("drone") {
            addData("id", node.id)
            addData("agent", "drone@$node.id")
            plans {
                +achieve("joinCircle"(C, R, N)) then {
                    execute("follow"(C, R, N))
                }
            }
        }
    }
