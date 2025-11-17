package it.unibo.jakta.examples.main

import it.unibo.jakta.examples.main.MainSwarmEnvironment.Companion.FOLLOW_RADIUS
import it.unibo.jakta.examples.main.MainSwarmEnvironment.Companion.SIGHT_RADIUS

fun main() {
    val e = MainSwarmEnvironment()
    e.leaderMain(SIGHT_RADIUS, SIGHT_RADIUS, FOLLOW_RADIUS)
    for(i in 1..5)
        e.followerMain()
    e.mas?.start()
}
