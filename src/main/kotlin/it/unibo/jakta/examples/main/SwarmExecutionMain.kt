package it.unibo.jakta.examples.main

import it.unibo.jakta.agents.bdi.Agent
import it.unibo.jakta.agents.bdi.dsl.mas
import it.unibo.jakta.agents.bdi.environment.Environment
import it.unibo.jakta.agents.bdi.environment.impl.EnvironmentImpl
import it.unibo.jakta.agents.bdi.perception.Perception
import it.unibo.jakta.examples.simulation.DronesLogic.followerLogic
import it.unibo.jakta.examples.simulation.DronesLogic.leaderLogic

class SwarmExecutionMainEnvironment(): EnvironmentImpl(
    externalActions = mapOf(),
    perception = Perception.empty(),
) {

    fun getPositionOf(agent: Agent) {
        TODO()
    }


    companion object{
        const val ENVIRONMENT_SIZE: Int = 10 // meters
        const val SIGHT_RADIUS: Int = ENVIRONMENT_SIZE / 2 // meters
        const val FOLLOW_RADIUS: Int = SIGHT_RADIUS / 2 // meters
    }
}

fun main() {
    mas {

        environment {
            from(SwarmExecutionMainEnvironment())
            actions {
                action("circleMovementStep", 0) {
                    TODO()
                }

                action("follow", 3) {
                    TODO()
                }
            }
        }

        agent("leader") {
            leaderLogic()
        }

        for(i in 1..10) {
            agent("follower${i}") {
                followerLogic()
            }
        }
    }
}
