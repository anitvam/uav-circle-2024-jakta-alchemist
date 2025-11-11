package it.unibo.jakta.examples.main

import it.unibo.jakta.agents.bdi.Agent
import it.unibo.jakta.agents.bdi.dsl.mas
import it.unibo.jakta.agents.bdi.environment.Environment
import it.unibo.jakta.agents.bdi.environment.impl.EnvironmentImpl
import it.unibo.jakta.agents.bdi.perception.Perception
import it.unibo.jakta.examples.simulation.DronesLogic.followerLogic
import it.unibo.jakta.examples.simulation.DronesLogic.leaderLogic


fun main() {
    mas {

        environment {
            from(MainSwarmEnvironment())
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
