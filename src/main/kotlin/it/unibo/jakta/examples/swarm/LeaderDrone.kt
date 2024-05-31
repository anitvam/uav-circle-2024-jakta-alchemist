@file:JvmName("LeaderDrone")

package it.unibo.jakta.examples.swarm

import it.unibo.alchemist.jakta.properties.JaktaEnvironmentForAlchemist
import it.unibo.alchemist.jakta.util.fix
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.molecules.SimpleMolecule
import it.unibo.jakta.agents.bdi.Agent
import it.unibo.jakta.agents.bdi.dsl.beliefs.fromPercept
import it.unibo.jakta.agents.bdi.messages.Achieve
import it.unibo.jakta.agents.bdi.messages.Message
import it.unibo.jakta.agents.dsl.mas
import it.unibo.jakta.examples.swarm.CircleMovement.createCircleCenter
import it.unibo.jakta.examples.swarm.CircleMovement.degreesToRadians
import it.unibo.jakta.examples.swarm.CircleMovement.positionInCircumference
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.solve.libs.oop.ObjectRef
import kotlin.math.PI

var leaderCircumferenceDegrees = 180

val destination = SimpleMolecule("desiredPosition")

fun <P : Position<P>> JaktaEnvironmentForAlchemist<P>.leader(): Agent =
    mas {
        environment {
            actions {
                action("circleMovementStep", 0) {
                    val initialPosition = SwarmPosition.fromPosition(alchemistEnvironment.getPosition(node))
                    val radius = data["radius"] as? Number ?: error("Missing radius as Node molecule. $data")
                    val center = SwarmPosition(0.0, 0.0)//data["centerPosition"]
//                    if (center == null) {
//                        center = createCircleCenter(SwarmPosition.fromPosition(initialPosition), radius as Double)
//                        addData("centerPosition", center)
//                    }
                    val nextPosition = positionInCircumference(
                        radius.toDouble(),
                        2 * PI * alchemistEnvironment.simulation.time.toDouble() / 600,
//                        degreesToRadians(leaderCircumferenceDegrees).also {
//                            leaderCircumferenceDegrees = ((leaderCircumferenceDegrees + 1) % 360) + 1
//                        },
                        SwarmPosition.fromPosition(center),
                    )

                    val movement = nextPosition - initialPosition
                    addData("velocity", doubleArrayOf(movement.x, movement.y))

                    node.setConcentration(destination, nextPosition)
//                    alchemistEnvironment.moveNodeToPosition(
//                        node,
//                        nextPosition.toPosition(alchemistEnvironment),
//                    )
                }
                action("notifyAgent", 1) {
                    // "drone@nodeid"
//                    var participants = (data["participants"] ?: setOf<String>()) as Set<*>
//                    participants = participants + dest.split("@")[1]
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
            }
        }
        agent("leader") {
            addData("id", node.id)
            goals {
                achieve("move")
            }
            plans {
                +achieve("move") then {
                    // execute("print"("Hi, I'm the leader at node $node.id"))
                    execute("circleMovementStep")
                    execute("notifyAgent")
                    //execute("sleep"(2000))
                    achieve("move")
                }
            }
        }
    }


