package it.unibo.jakta.examples.main

import it.unibo.jakta.agents.bdi.dsl.mas
import it.unibo.jakta.agents.bdi.environment.Environment
import it.unibo.jakta.agents.bdi.environment.impl.EnvironmentImpl
import it.unibo.jakta.agents.bdi.perception.Perception
import it.unibo.jakta.examples.main.MainSwarmEnvironment.Companion.FOLLOW_RADIUS
import it.unibo.jakta.examples.main.MainSwarmEnvironment.Companion.SIGHT_RADIUS
import it.unibo.jakta.examples.simulation.DronesLogic.followerLogic
import it.unibo.jakta.examples.simulation.DronesLogic.leaderLogic
import it.unibo.jakta.examples.simulation.SwarmPosition
import it.unibo.jakta.examples.simulation.followerMain
import it.unibo.jakta.examples.simulation.leaderMain


fun main() {
        val e = MainSwarmEnvironment()
        e.leaderMain(SIGHT_RADIUS, SIGHT_RADIUS, FOLLOW_RADIUS)
        for(i in 1..5)
            e.followerMain()
        e.mas?.start()
    }


