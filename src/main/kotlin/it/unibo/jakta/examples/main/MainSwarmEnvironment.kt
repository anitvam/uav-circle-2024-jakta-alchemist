package it.unibo.jakta.examples.main

import it.unibo.BdiSimulationIntegrationEnvironment
import it.unibo.jakta.agents.bdi.Agent
import it.unibo.jakta.agents.bdi.Mas
import it.unibo.jakta.agents.bdi.dsl.MasScope
import it.unibo.jakta.agents.bdi.environment.impl.EnvironmentImpl
import it.unibo.jakta.agents.bdi.perception.Perception
import it.unibo.jakta.examples.simulation.SwarmPosition
import kotlinx.coroutines.MainScope

class MainSwarmEnvironment(): BdiSimulationIntegrationEnvironment<SwarmPosition, Mas, MasScope>, EnvironmentImpl(
    externalActions = mapOf(),
    perception = Perception.empty(),
) {

    override fun getPosition(): SwarmPosition {
        TODO("Not yet implemented")
    }

    override fun device(f: MasScope.() -> Unit) {
        MasScope().f()
    }

    companion object{
        const val ENVIRONMENT_SIZE: Int = 10 // meters
        const val SIGHT_RADIUS: Int = ENVIRONMENT_SIZE / 2 // meters
        const val FOLLOW_RADIUS: Int = SIGHT_RADIUS / 2 // meters
    }
}

interface BDISimulatedDSL {
    fun device()
}
