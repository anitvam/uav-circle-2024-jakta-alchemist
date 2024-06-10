@file:JvmName("LeaderDrone")

package it.unibo.jakta.examples.swarm

import it.unibo.alchemist.jakta.properties.JaktaEnvironmentForAlchemist
import it.unibo.alchemist.jakta.util.fix
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.molecules.SimpleMolecule
import it.unibo.jakta.agents.bdi.Agent
import it.unibo.jakta.agents.bdi.messages.Achieve
import it.unibo.jakta.agents.bdi.messages.Message
import it.unibo.jakta.agents.dsl.device
import it.unibo.jakta.examples.swarm.CircleMovement.positionInCircumference
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.solve.libs.oop.ObjectRef
import kotlin.math.PI

val destination = SimpleMolecule("desiredPosition")

fun <P : Position<P>> JaktaEnvironmentForAlchemist<P>.leader(): Agent =
    device {
        environment {
            actions {
                action("circleMovementStep", 0) {
                    val initialPosition = SwarmPosition.fromPosition(alchemistEnvironment.getPosition(node))
                    val radius = data["radius"] as? Number ?: error("Missing radius as Node molecule. $data")
                    val center = SwarmPosition(0.0, 0.0)//data["centerPosition"]
                    val nextPosition = positionInCircumference(
                        radius.toDouble(),
                        2 * PI * alchemistEnvironment.simulation.time.toDouble() / 600,
                        SwarmPosition.fromPosition(center),
                    )

                    val movement = nextPosition - initialPosition
                    addData("velocity", doubleArrayOf(movement.x, movement.y))

                    node.setConcentration(destination, nextPosition)
                }
                action("notifyAgent", 1) {
                    val participants = alchemistEnvironment.getNeighborhood(node)
                        .map { it.id }.toSet()
                    val payload: Struct = Struct.of(
                        "joinCircle",
                        ObjectRef.of(SwarmPosition.fromPosition(alchemistEnvironment.getPosition(node))),
                        ObjectRef.of((data["followRadius"] as Number).toDouble()),
                        ObjectRef.of(participants),
                    )
                    broadcastMessage(Message(sender, Achieve, payload))
                }
                action("storeId", 1) {
                    val id = argument<ObjectRef>(0).fix<Int>()
                    addData("id", id)
                }
            }
        }
        agent("leader") {
            goals {
                achieve("init")
                achieve("move")
            }
            plans {
                +achieve("init") then {
                    execute("storeId"(node.id))
                }
                +achieve("move") then {
                    execute("circleMovementStep")
                    execute("notifyAgent")
                    achieve("move")
                }
            }
        }
    }


