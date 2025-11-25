package it.unibo.jakta.examples.common

import it.unibo.jakta.agents.bdi.dsl.Builder
import it.unibo.jakta.agents.bdi.dsl.actions.ExternalActionsScope
import it.unibo.jakta.agents.bdi.environment.Environment

interface BdiSimulationIntegrationEnvironment<P: Any>: Environment {

    fun setDesiredPosition(agentName: String, position: SwarmPosition)

    fun getPosition(agentName: String): P

    fun deviceId(agentName: String): Int

    fun getTime(): Double

    fun getNeighborIds(): List<Int>
}
