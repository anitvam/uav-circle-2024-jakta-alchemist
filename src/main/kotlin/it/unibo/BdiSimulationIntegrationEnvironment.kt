package it.unibo

import it.unibo.alchemist.model.Position
import it.unibo.jakta.agents.bdi.Agent
import it.unibo.jakta.agents.bdi.dsl.Builder
import it.unibo.jakta.agents.bdi.environment.Environment
import kotlin.random.Random
import kotlin.uuid.Uuid

interface BdiSimulationIntegrationEnvironment<
    P: Position<P>,
    Building: Any,
    B: Builder<Building>,
>: Environment {

    fun getPosition(): P

    val deviceId: Int
        get() = Random(System.currentTimeMillis()).nextInt()


    fun device(f: B.()-> SupportingType)

}

interface SupportingType {
    fun environment()
}

//JaktaEnvironmentForAlchemist -> Builder<WrappedAgent>

// MainSwarmEnvironment -> Builder<Mas>