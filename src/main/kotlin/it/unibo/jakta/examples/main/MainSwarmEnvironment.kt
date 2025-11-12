package it.unibo.jakta.examples.main

import it.unibo.BdiSimulationIntegrationEnvironment
import it.unibo.jakta.agents.bdi.AgentID
import it.unibo.jakta.agents.bdi.Mas
import it.unibo.jakta.agents.bdi.actions.ExternalAction
import it.unibo.jakta.agents.bdi.dsl.MasScope
import it.unibo.jakta.agents.bdi.environment.Environment
import it.unibo.jakta.agents.bdi.environment.impl.EnvironmentImpl
import it.unibo.jakta.agents.bdi.perception.Perception
import it.unibo.jakta.examples.simulation.SwarmPosition
import kotlinx.coroutines.MainScope
import kotlin.random.Random

class MainSwarmEnvironment(
): BdiSimulationIntegrationEnvironment<SwarmPosition, Mas, MasScope>, EnvironmentImpl(
    externalActions = mapOf(),
    perception = Perception.empty(),
) {
    var mas: Mas? = null
        private set
    private val positions: MutableMap<String, SwarmPosition>  = mutableMapOf()
    private val actions: MutableMap<String, ExternalAction> = mutableMapOf()
    private val id = Random(System.currentTimeMillis()).nextInt()
    private val envData: MutableMap<String, Any> = mutableMapOf()

    override val data: Map<String, Any>
        get() = envData
    override val deviceId: Int
        get() = id
    override val externalActions: Map<String, ExternalAction>
        get() = actions


    override fun getPosition(agentName: String): SwarmPosition = positions.getOrElse(agentName) {
        throw IllegalArgumentException("Unknown agent: $agentName")
    }

    fun setPosition(agentName: String, position: SwarmPosition) {
        positions[agentName] = position
    }

    override fun getTime(): Double =
        System.currentTimeMillis().toDouble()

    override fun getNeighborIds(): List<Int> = mas?.agents?.map { Integer.parseInt(it.agentID.id) } ?: emptyList()

    override fun addData(key: String, value: Any): Environment {
        envData[key] = value
        return super.addData(key, value)
    }
    fun device(f: MasScope.() -> Unit): Mas {
        val masScope = MasScope().also(f)
        this.actions.putAll(masScope.env.externalActions)
        this.positions.putAll(masScope.agents.map { it.name to SwarmPosition.random() })
        if (mas == null) {
            mas = masScope.environment(this).build()
        } else {
            val tempMas = masScope.build()
            mas = Mas.of(
                mas!!.executionStrategy,
                mas!!.environment,
                mas!!.agents + tempMas.agents,
            )
        }

        return mas!!
    }

    companion object{
        const val ENVIRONMENT_SIZE: Double = 10.0 // meters
        const val SIGHT_RADIUS: Double = ENVIRONMENT_SIZE / 2.0 // meters
        const val FOLLOW_RADIUS: Double = SIGHT_RADIUS / 2.0 // meters
    }
}