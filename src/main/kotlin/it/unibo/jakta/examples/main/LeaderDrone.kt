package it.unibo.jakta.examples.main

import it.unibo.jakta.agents.bdi.messages.Achieve
import it.unibo.jakta.agents.bdi.messages.Message
import it.unibo.jakta.examples.common.CircleMovement.positionInCircumference
import it.unibo.jakta.examples.common.DronesLogic.leaderLogic
import it.unibo.jakta.examples.common.SwarmPosition
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.solve.libs.oop.ObjectRef
import kotlin.math.PI

fun MainSwarmEnvironment.leaderMain(radius: Double, sightRadius: Double, followRadius: Double) =
    device {
        environment {
            actions {
                action("circleMovementStep", 0) {
                    val initialPosition = SwarmPosition.fromPosition(getPosition(sender))
                    val r = data["radius"] as? Number ?: error("Missing radius as Node molecule. $data")
                    val center = SwarmPosition(0.0, 0.0)//data["centerPosition"]
                    val nextPosition = positionInCircumference(
                        r.toDouble(),
                        2 * PI * getTime() / 600,
                        SwarmPosition.fromPosition(center),
                    )

                    val movement = nextPosition - initialPosition
                    addData("velocity", doubleArrayOf(movement.x, movement.y))
                    //println("[LEADER]: Next position $nextPosition")
                    setDesiredPosition(sender, nextPosition)
                }
                action("notifyAgent", 1) {
                    val participants = getNeighborIds().toSet()
                    val payload: Struct = Struct.of(
                        "joinCircle",
                        ObjectRef.of(SwarmPosition.fromPosition(getPosition(sender))),
                        ObjectRef.of((data["followRadius"] as Number).toDouble()),
                        ObjectRef.of(participants),
                    )
                    broadcastMessage(Message(sender, Achieve, payload))
                }
            }
        }
        val agentName = "leader"
        agent(agentName) {
            addData("id", deviceId(agentName))
            addData("sightRadius", sightRadius)
            addData("radius", radius)
            addData("followRadius", followRadius)
            leaderLogic()
        }
    }