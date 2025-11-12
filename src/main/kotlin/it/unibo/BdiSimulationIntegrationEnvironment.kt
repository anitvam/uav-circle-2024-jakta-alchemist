package it.unibo

import it.unibo.alchemist.model.Position
import it.unibo.jakta.agents.bdi.dsl.Builder
import it.unibo.jakta.agents.bdi.environment.Environment
import kotlin.random.Random
import kotlin.uuid.Uuid

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

//JaktaEnvironmentForAlchemist -> Builder<WrappedAgent>

// MainSwarmEnvironment -> Builder<Mas>