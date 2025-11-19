package it.unibo.jakta.examples.main

import it.unibo.jakta.examples.common.BdiSimulationIntegrationEnvironment
import it.unibo.jakta.agents.bdi.Mas
import it.unibo.jakta.agents.bdi.actions.ExternalAction
import it.unibo.jakta.agents.bdi.dsl.MasScope
import it.unibo.jakta.agents.bdi.dsl.actions.ExternalActionsScope
import it.unibo.jakta.agents.bdi.environment.Environment
import it.unibo.jakta.agents.bdi.environment.impl.EnvironmentImpl
import it.unibo.jakta.agents.bdi.messages.Message
import it.unibo.jakta.agents.bdi.perception.Perception
import it.unibo.jakta.examples.common.SwarmPosition
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.apache.commons.math3.util.MathArrays
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class MainSwarmEnvironment(
): BdiSimulationIntegrationEnvironment<SwarmPosition, Mas, MasScope>, EnvironmentImpl(
    externalActions = mapOf(),
    perception = Perception.empty(),
) {
    var mas: Mas? = null
        private set
    private val realPositions: ConcurrentHashMap<String, SwarmPosition>  = ConcurrentHashMap<String, SwarmPosition>()
    private val desiredPositions: MutableMap<String, SwarmPosition>  = ConcurrentHashMap<String, SwarmPosition>()
    private val actions: MutableMap<String, ExternalAction> = mutableMapOf()
    private val envData: MutableMap<String, Any> = mutableMapOf()
    override val data: Map<String, Any>
        get() = envData
    override val externalActions: Map<String, ExternalAction>
        get() = actions

    init {
        val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
        scope.launch {
            val maxWalk: Double = 0.05 // Speed?
            while(true) {
                for ((agentName, desiredPosition) in desiredPositions) {
                    val current = realPositions[agentName] ?: continue
                    val distance = MathArrays.distance(
                        doubleArrayOf(current.x, current.y),
                        doubleArrayOf(desiredPosition.x, desiredPosition.y)
                    )
                    if (distance > maxWalk) {
                        val newCoords = stepTowards(current, desiredPosition, maxWalk)
                        println("Moving $agentName to $newCoords")
                        realPositions[agentName] = current.copy(x = newCoords.x, y = newCoords.y)
                    }
                }
            }
        }
    }

    private fun stepTowards(current: SwarmPosition, target: SwarmPosition, maxWalk: Double): SwarmPosition {
        val vector = target - current
        val distance = MathArrays.distance(doubleArrayOf(vector.x, vector.y), doubleArrayOf(target.x, target.y))
        if (distance <= maxWalk || distance == 0.0) {
            // Reached or small enough => go exactly to target
            return target
        }
        val angle = atan2(vector.y, vector.x)
        val dx = maxWalk * cos(angle)
        val dy = maxWalk * sin(angle)
        return current + SwarmPosition(dx, dy)
    }

    override fun deviceId(agentName: String): Int {
        val agentIds = mas?.agents?.filter { it.name == agentName }?.map { it.agentID }
        if (agentIds != null && agentIds.isNotEmpty()) {
            return agentIds.first().id.toInt()
        }
        return -1 // Double check
    }

    override fun getPosition(agentName: String): SwarmPosition = realPositions.getOrElse(agentName) {
        println("Unknown agent: $agentName")
        throw IllegalStateException("Unknown agent: $agentName")
    }

    override fun setDesiredPosition(agentName: String, position: SwarmPosition) {
        desiredPositions[agentName] = position
    }

    override fun getTime(): Double =
        System.currentTimeMillis().toDouble()

    override fun getNeighborIds(): List<Int> = mas?.agents?.map { Integer.parseInt(it.agentID.id) } ?: emptyList()

    override fun addData(key: String, value: Any): Environment {
        envData[key] = value
        println(data)
        return super.addData(key, value)
    }
    fun device(f: MasScope.() -> Unit): Mas {
        val masScope = MasScope().also(f)
        this.actions.putAll(masScope.env.externalActions)
        this.realPositions.putAll(masScope.agents.map { it.name to SwarmPosition.random() })
        println(realPositions)
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

    override fun broadcastMessage(message: Message): Environment {
        TODO("Implement accounting for radius of communication")
    }

    companion object{
        const val ENVIRONMENT_SIZE: Double = 10.0 // meters
        const val SIGHT_RADIUS: Double = ENVIRONMENT_SIZE / 2.0 // meters
        const val FOLLOW_RADIUS: Double = SIGHT_RADIUS / 2.0 // meters
    }
}