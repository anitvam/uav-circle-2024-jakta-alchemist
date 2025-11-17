package it.unibo.jakta.examples.simulation

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import it.unibo.jakta.examples.common.CircleMovement
import it.unibo.jakta.examples.common.CircleMovement.degreesToRadians
import it.unibo.jakta.examples.common.SwarmPosition
import kotlin.math.sqrt

class TestMovementManager : FreeSpec({
    "positionInCircumference generate the right position to go to" {
        CircleMovement.positionInCircumference(
            1.0,
            degreesToRadians(45),
            SwarmPosition(0.0, 0.0),
        ) shouldBe SwarmPosition(
            sqrt(2.0) / 2.0,
            sqrt(2.0) / 2.0,
        )
    }
})
