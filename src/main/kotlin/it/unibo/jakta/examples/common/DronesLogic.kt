package it.unibo.jakta.examples.common

import it.unibo.jakta.agents.bdi.dsl.AgentScope

object DronesLogic {

    fun AgentScope.leaderLogic() {
        goals {
            achieve("move")
        }
        plans {
            +achieve("move") then {
                execute("circleMovementStep")
                execute("notifyAgent")
                achieve("move")
            }
        }
    }

    fun AgentScope.followerLogic() {
        plans {
            +achieve("joinCircle"(C, R, N)) then {
                execute("follow"(C, R, N))
            }
        }
    }
}