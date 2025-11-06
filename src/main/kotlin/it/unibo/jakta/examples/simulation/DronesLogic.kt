package it.unibo.jakta.examples.simulation

import it.unibo.jakta.agents.bdi.dsl.AgentScope
import it.unibo.jakta.agents.bdi.dsl.mas
import it.unibo.jakta.examples.simulation.DronesLogic.followerLogic
import it.unibo.jakta.examples.simulation.DronesLogic.leaderLogic

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
