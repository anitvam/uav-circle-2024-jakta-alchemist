package it.unibo.jakta.examples.common

import it.unibo.jakta.agents.bdi.dsl.Builder
import it.unibo.jakta.agents.bdi.environment.Environment

interface BdiSimulationIntegrationEnvironment<
    P: Any,
    Building: Any,
    B: Builder<Building>,
>: Environment {

    fun getPosition(agentName: String): P

    val deviceId: Int

    fun getTime(): Double

    fun getNeighborIds(): List<Int>
}
