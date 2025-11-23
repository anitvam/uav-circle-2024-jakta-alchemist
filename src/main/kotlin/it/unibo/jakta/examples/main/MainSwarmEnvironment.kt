package it.unibo.jakta.examples.main

import it.unibo.jakta.agents.bdi.AgentID
import it.unibo.jakta.examples.common.BdiSimulationIntegrationEnvironment
import it.unibo.jakta.agents.bdi.Mas
import it.unibo.jakta.agents.bdi.actions.ExternalAction
import it.unibo.jakta.agents.bdi.dsl.MasScope
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
import it.unibo.jakta.examples.common.randomPointOnCircumference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.sql.Date
import java.util.Calendar
import java.util.concurrent.ConcurrentHashMap

class MainSwarmEnvironment(
    override val agentIDs: ConcurrentHashMap<String, AgentID> = ConcurrentHashMap<String, AgentID>(),
    override val externalActions: ConcurrentHashMap<String, ExternalAction> = ConcurrentHashMap<String, ExternalAction>(),
    override val messageBoxes: ConcurrentHashMap<AgentID, MessageQueue> = ConcurrentHashMap<AgentID, MessageQueue>(),
    perception: Perception = Perception.empty(),
    override val data: ConcurrentHashMap<String, Any> = ConcurrentHashMap<String, Any>(),
): BdiSimulationIntegrationEnvironment<SwarmPosition, Mas, MasScope>, EnvironmentImpl(
    externalActions,
    agentIDs,
    messageBoxes,
    perception,
    data,
) {
    var mas: Mas? = null
        private set

    private val environmentLock: Mutex = Mutex()
    private val realPositions: ConcurrentHashMap<String, SwarmPosition>  = ConcurrentHashMap<String, SwarmPosition>()
    private val desiredPositions: ConcurrentHashMap<String, SwarmPosition>  = ConcurrentHashMap<String, SwarmPosition>()

    init {
        val scope = CoroutineScope(Dispatchers.Default)
        scope.launch {
            var initialTime = System.currentTimeMillis()
            while(true) {
                val now = System.currentTimeMillis()
                val maxWalk: Double = 1.0 * (now - initialTime)
                for ((agentName, desiredPosition) in desiredPositions) {
                    environmentLock.withLock {
                        val current = realPositions[agentName] ?: continue
                        val distance = computeDistance(current, desiredPosition)
                        if (distance > maxWalk) {
                            val newCoords = computeStepTowardsDestination(current, desiredPosition, maxWalk)
                            //println("Moving $agentName to $newCoords (desired position: $desiredPosition)")
                            realPositions[agentName] = current.copy(x = newCoords.x, y = newCoords.y)
                        }
                    }
                }
                initialTime = now
            }
        }

        val initTime = System.currentTimeMillis()
        val experimentTimeInMillis = 1500 * 1000
        val timedErrors: MutableMap<String, Double> = mutableMapOf()
        scope.launch{
            while(true) {
                environmentLock.withLock {
                    if (realPositions.isNotEmpty()) {
                        val e = computeDistanceError(realPositions)
                        println("Time: ${timestamp()}, error: $e")
                        timedErrors[timestamp()] = e
                    }
                }
                if (System.currentTimeMillis() - initTime > experimentTimeInMillis) {
                    saveErrorMapToCsv(timedErrors, File("data/main-execution.csv"))
                    System.exit(0)
                    return@launch
                }
                delay(1000)

            }
        }
    }

    private fun timestamp(): String {
        val date:Date = Date(System.currentTimeMillis())
        val cal = Calendar.getInstance()
        cal.time = date
        val hours = cal.get(Calendar.HOUR_OF_DAY)
        val minutes = cal.get(Calendar.MINUTE)
        val seconds = cal.get(Calendar.SECOND)
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    override fun deviceId(agentName: String): Int {
        synchronized(this) {
            val agentIds = mas?.agents?.filter { it.name == agentName }?.map { it.agentID }
            if (agentIds != null && agentIds.isNotEmpty()) {
                return agentIds.first().id.toInt()
            }
            return -1
        }
    }

    override fun getPosition(agentName: String): SwarmPosition =
        synchronized(this) {
            realPositions.getOrElse(agentName) {
                println("Unknown agent: $agentName")
                throw IllegalStateException("Unknown agent: $agentName")
            }
        }

    override fun setDesiredPosition(agentName: String, position: SwarmPosition) {
        synchronized(this) {
            desiredPositions[agentName] = position
        }
    }

    override fun getTime(): Double =
        System.currentTimeMillis().toDouble()

    override fun getNeighborIds(): List<Int> = synchronized(environmentLock) {
        mas?.agents?.map { Integer.parseInt(it.agentID.id) } ?: emptyList()
    }

    override fun addData(key: String, value: Any): Environment {
        synchronized(environmentLock) {
            data[key] = value
            return this
        }
    }

    fun device(f: MasScope.() -> Unit): Mas {
        val masScope = MasScope().also(f)
        this.externalActions.putAll(masScope.env.externalActions)
        this.realPositions.putAll(masScope.agents.map {
            when (it.name.contains("leader")) {
                true -> it.name to SwarmPosition(0.0, 0.0)
                else -> it.name to randomPointOnCircumference(ENVIRONMENT_SIZE)
            }
        })
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
        synchronized(this) {
            val myPosition = realPositions[message.from] ?: return this
            val neighbors = realPositions
                .filterNot { message.from == it.key }
                .map {
                    it to computeDistance(myPosition, it.value)
                }
                .filter { it.second < SIGHT_RADIUS }
                .map { it.first.key.substringAfter("@") }
            if (neighbors.isEmpty()) return this
            //println("Reaching: $neighbors")
            for (neighbor in neighbors) {
                messageBoxes[AgentID(neighbor)] = messageBoxes[AgentID(neighbor)]?.plus(message)
                    ?: listOf(message)
            }
            return this
        }
    }

    private fun gatherIdFromAgentName(agentName: String): AgentID =
        AgentID(agentName.substringAfter("@"))

    override fun getNextMessage(agentName: String): Message? =
        synchronized(this) {
            messageBoxes[gatherIdFromAgentName(agentName)]?.lastOrNull()
        }

    override fun popMessage(agentName: String): Environment {
        synchronized(this) {
            val message = getNextMessage(agentName)
            val agentMessageBox = messageBoxes[gatherIdFromAgentName(agentName)]
            if (agentMessageBox != null && message != null) {
                messageBoxes[gatherIdFromAgentName(agentName)] = agentMessageBox - message
            }
            return this
        }
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