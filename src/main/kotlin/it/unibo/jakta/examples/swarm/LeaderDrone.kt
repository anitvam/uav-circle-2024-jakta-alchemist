@file:JvmName("LeaderDrone")

package it.unibo.jakta.examples.swarm

import it.unibo.alchemist.jakta.properties.JaktaEnvironmentForAlchemist
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.molecules.SimpleMolecule
import it.unibo.jakta.agents.bdi.Agent
import it.unibo.jakta.agents.bdi.messages.Achieve
import it.unibo.jakta.agents.bdi.messages.Message
import it.unibo.jakta.agents.dsl.device
import it.unibo.jakta.examples.swarm.CircleMovement.positionInCircumference
import it.unibo.jakta.examples.swarm.DronesLogic.leaderLogic
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.solve.libs.oop.ObjectRef
import kotlin.math.PI

val destination = SimpleMolecule("desiredPosition")

fun <P : Position<P>> JaktaEnvironmentForAlchemist<P>.leader(radius: Double, sightRadius: Double, followRadius: Double): Agent =
    device {
        environment {
            actions {
                action("circleMovementStep", 0) {
                    val initialPosition = SwarmPosition.fromPosition(alchemistEnvironment.getPosition(node))
                    val r = data["radius"] as? Number ?: error("Missing radius as Node molecule. $data")
                    val center = SwarmPosition(0.0, 0.0)//data["centerPosition"]
                    val nextPosition = positionInCircumference(
                        r.toDouble(),
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
            }
        }
        agent("leader") {
            addObservableProperty("id", node.id)
            addObservableProperty("sightRadius", sightRadius)
            addObservableProperty("radius", radius)
            addObservableProperty("followRadius", followRadius)
            leaderLogic()
        }
    }
