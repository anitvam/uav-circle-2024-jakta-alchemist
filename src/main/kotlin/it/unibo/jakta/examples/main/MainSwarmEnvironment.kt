package it.unibo.jakta.examples.main

import it.unibo.jakta.agents.bdi.AgentID
import it.unibo.jakta.examples.common.BdiSimulationIntegrationEnvironment
import it.unibo.jakta.agents.bdi.Mas
import it.unibo.jakta.agents.bdi.actions.ExternalAction
import it.unibo.jakta.agents.bdi.dsl.MasScope
import it.unibo.jakta.agents.bdi.dsl.actions.ExternalActionsScope
import it.unibo.jakta.agents.bdi.environment.Environment
import it.unibo.jakta.agents.bdi.environment.impl.EnvironmentImpl
import it.unibo.jakta.agents.bdi.executionstrategies.ExecutionStrategy
import it.unibo.jakta.agents.bdi.messages.Message
import it.unibo.jakta.agents.bdi.messages.MessageQueue
import it.unibo.jakta.agents.bdi.perception.Perception
import it.unibo.jakta.examples.common.SwarmPosition
import it.unibo.jakta.examples.common.computeDistance
import it.unibo.jakta.examples.common.computeDistanceError
import it.unibo.jakta.examples.common.computeStepTowardsDestination
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.apache.commons.math3.util.MathArrays
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class MainSwarmEnvironment(
    agentIDs: Map<String, AgentID> = emptyMap(),
    externalActions: Map<String, ExternalAction> = emptyMap(),
    override val messageBoxes: MutableMap<AgentID, MessageQueue> = mutableMapOf(),
    perception: Perception = Perception.empty(),
    override val data: MutableMap<String, Any> = mutableMapOf(),
): BdiSimulationIntegrationEnvironment<SwarmPosition, Mas, MasScope>, EnvironmentImpl(
    externalActions,
    agentIDs,
    messageBoxes,
    perception,
    data,
) {
    var mas: Mas? = null
        private set
    private val realPositions: ConcurrentHashMap<String, SwarmPosition>  = ConcurrentHashMap<String, SwarmPosition>()
    private val desiredPositions: MutableMap<String, SwarmPosition>  = ConcurrentHashMap<String, SwarmPosition>()
    private val actions: MutableMap<String, ExternalAction> = mutableMapOf()
    override val externalActions: Map<String, ExternalAction>
        get() = actions

    init {
        val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
        scope.launch {
            val maxWalk: Double = 0.05 // Speed?
            while(true) {
                for ((agentName, desiredPosition) in desiredPositions) {
                    val current = realPositions[agentName] ?: continue
                    val distance = computeDistance(current, desiredPosition)
                    if (distance > maxWalk) {
                        val newCoords = computeStepTowardsDestination(current, desiredPosition, maxWalk)
                        //println("Moving $agentName to $newCoords (desired position: $desiredPosition)")
                        realPositions[agentName] = current.copy(x = newCoords.x, y = newCoords.y)
                    }
                }
            }
            delay(500)
        }

        scope.launch{
            while(true) {
                if (realPositions.isNotEmpty()) {
                    println(
                        "Time: ${getTime()}, error: ${
                            computeDistanceError(realPositions, this@MainSwarmEnvironment)
                        }"
                    )
                }
                delay(1000)
            }
        }
    }

    override fun deviceId(agentName: String): Int {
        val agentIds = mas?.agents?.filter { it.name == agentName }?.map { it.agentID }
        if (agentIds != null && agentIds.isNotEmpty()) {
            return agentIds.first().id.toInt()
        }
        return -1
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
        data[key] = value
        return this
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
                ExecutionStrategy.oneThreadPerAgent(),
                mas!!.environment,
                mas!!.agents + tempMas.agents,
            )
        }

        return mas!!
    }

    override fun broadcastMessage(message: Message): Environment {
        val myPosition = realPositions[message.from] ?: return this
        val neighbors = realPositions
            .filterNot { message.from == it.key }
            .filter {
                val distance = MathArrays.distance(
                    doubleArrayOf(myPosition.x, myPosition.y),
                    doubleArrayOf(it.value.x, it.value.y)
                )
                distance < SIGHT_RADIUS
            }
            .map { it.key.substringAfter("@") }
            if (neighbors.isEmpty()) return this
            for (neighbor in neighbors) {
                messageBoxes[AgentID(neighbor)] = messageBoxes[AgentID(neighbor)]?.plus(message)
                    ?: listOf(message)
            }
            return this
    }

    private fun gatherIdFromAgentName(agentName: String): AgentID =
        AgentID(agentName.substringAfter("@"))

    override fun getNextMessage(agentName: String): Message? =
        messageBoxes[gatherIdFromAgentName(agentName)]?.lastOrNull()

    override fun popMessage(agentName: String): Environment {
        val message = getNextMessage(agentName)
        val agentMessageBox = messageBoxes[gatherIdFromAgentName(agentName)]
        if (agentMessageBox != null && message != null) {
            messageBoxes[gatherIdFromAgentName(agentName)] = agentMessageBox - message
        }
        return this
    }

    override fun copy(
        agentIDs: Map<String, AgentID>,
        externalActions: Map<String, ExternalAction>,
        messageBoxes: Map<AgentID, MessageQueue>,
        perception: Perception,
        data: Map<String, Any>
    ): Environment {
        return this
    }

    companion object{
        const val ENVIRONMENT_SIZE: Double = 10.0 // meters
        const val SIGHT_RADIUS: Double = ENVIRONMENT_SIZE / 2.0 // meters
        const val FOLLOW_RADIUS: Double = SIGHT_RADIUS / 2.0 // meters
    }
}