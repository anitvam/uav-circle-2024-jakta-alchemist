@file:JvmName("Drone")

package it.unibo.jakta.examples.simulation

import it.unibo.alchemist.jakta.properties.JaktaEnvironmentForAlchemist
import it.unibo.alchemist.jakta.util.fix
import it.unibo.alchemist.model.Position
import it.unibo.jakta.agents.dsl.device
import it.unibo.jakta.examples.common.CircleMovement
import it.unibo.jakta.examples.common.DronesLogic.followerLogic
import it.unibo.jakta.examples.common.SwarmPosition
import it.unibo.tuprolog.solve.libs.oop.ObjectRef
import kotlin.math.PI

fun <P: Position<P>> JaktaEnvironmentForAlchemist<P>.follower() =
    device {
        environment {
            actions {
                action("follow", 3) {
                    val center = argument<ObjectRef>(0).fix<SwarmPosition>()
                    val radius = argument<ObjectRef>(1).fix<Double>()
                    val otherNodes = argument<ObjectRef>(2).fix<Set<Int>>()
                    val myPosition = SwarmPosition.fromPosition(getPosition(sender))

                    // Compute my destination in the circle
                    val angles = (2 * PI) / otherNodes.count()
                    val destinationAngle = otherNodes.sorted().indexOf(deviceId(sender)) * angles
                    val destinationPosition = CircleMovement.positionInCircumference(
                        radius,
                        destinationAngle,
                        center,
                    )
                    val movement = destinationPosition - myPosition
                    // set Node property in the environment
                    addData("velocity", doubleArrayOf(movement.x, movement.y))
                    println("[$sender]: Next position $destinationPosition")
                    setDesiredPosition(sender, destinationPosition)
                }
            }
        }
        val agentName = "follower"
        agent(agentName) {
            addData("id", deviceId(agentName))
            addData("agent", "$agentName@${deviceId(agentName)}")
            followerLogic()
        }
    }
