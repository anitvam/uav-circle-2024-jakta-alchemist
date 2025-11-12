package it.unibo.jakta.agents.bdi

import java.util.*

data class AgentID(val id: String = generateId()) {
    companion object {
        private fun generateId(): String = Random(System.currentTimeMillis()).nextInt().toString()
    }
}
