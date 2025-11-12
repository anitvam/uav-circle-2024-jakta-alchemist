package it.unibo.jakta.agents.bdi

import kotlin.random.Random


data class AgentID(val id: String = generateId()) {
    companion object {
        private fun generateId(): String = Random.nextInt(0, 1000).toString()
    }
}
