@file:JvmName("Drone")

package it.unibo.jakta.examples.simulation

import it.unibo.alchemist.jakta.properties.JaktaEnvironmentForAlchemist
import it.unibo.alchemist.jakta.util.fix
import it.unibo.alchemist.model.Position
import it.unibo.jakta.agents.dsl.device
import it.unibo.jakta.examples.main.MainSwarmEnvironment
import it.unibo.jakta.examples.simulation.DronesLogic.followerLogic
import it.unibo.tuprolog.solve.libs.oop.ObjectRef
import kotlin.math.PI
import kotlin.random.Random

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
                    val destinationAngle = otherNodes.sorted().indexOf(deviceId) * angles
                    val destinationPosition = CircleMovement.positionInCircumference(
                        radius,
                        destinationAngle,
                        center,
                    )
                    val movement = destinationPosition - myPosition
                    // set Node property in the environment
                    addData("velocity", doubleArrayOf(movement.x, movement.y))
                    addData(destination.name, destinationPosition)
                }
            }
        }
        agent("follower") {
            addData("id", deviceId)
            addData("agent", "follower@${deviceId}")
            followerLogic()
        }
    }

fun MainSwarmEnvironment.followerMain() =
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
                    val destinationAngle = otherNodes.sorted().indexOf(deviceId) * angles
                    val destinationPosition = CircleMovement.positionInCircumference(
                        radius,
                        destinationAngle,
                        center,
                    )
                    val movement = destinationPosition - myPosition
                    // set Node property in the environment
                    addData("velocity", doubleArrayOf(movement.x, movement.y))
                    println("[$sender]: Next position $destinationPosition")
                    addData(destination.name, destinationPosition)
                }
            }
        }
        agent("follower") {
            addData("id", deviceId)
            addData("agent", "follower@${deviceId}")
            followerLogic()
        }
    }

