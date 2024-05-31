//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//
package it.unibo.alchemist.model.timedistributions

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.times.DoubleTime
import org.apache.commons.math3.distribution.WeibullDistribution
import org.apache.commons.math3.random.RandomGenerator
import org.apache.commons.math3.special.Gamma
import org.apache.commons.math3.util.FastMath

open class WeibullTime<T> private constructor(
    private val rand: RandomGenerator,
    private val dist: WeibullDistribution,
    private val offset: Double,
    start: Time,
) : AbstractDistribution<T?>(start) {

    constructor(mean: Double, deviation: Double, random: RandomGenerator) : this(
        mean,
        deviation,
        DoubleTime(random.nextDouble() * mean),
        random
    )

    constructor(mean: Double, deviation: Double, start: Time, random: RandomGenerator) : this(
        random,
        weibullFromMean(mean, deviation, random),
        0.0,
        start
    )

    constructor(
        shapeParameter: Double,
        scaleParameter: Double,
        offsetParameter: Double,
        start: Time,
        random: RandomGenerator
    ) : this(random, WeibullDistribution(random, shapeParameter, scaleParameter, 1.0E-9), offsetParameter, start)

    override fun updateStatus(currentTime: Time, executed: Boolean, param: Double, environment: Environment<T?, *>?) {
        if (executed) {
            this.setNextOccurrence(currentTime.plus(DoubleTime(1.0 / this.genSample())))
        }
    }

    protected fun genSample(): Double {
        return dist.inverseCumulativeProbability(rand.nextDouble()) + this.offset
    }

    val mean: Double
        get() = dist.numericalMean + this.offset

    val deviation: Double
        get() = FastMath.sqrt(dist.numericalVariance)

    override fun getRate(): Double {
        return this.mean
    }

    override fun cloneOnNewNode(
        destination: Node<T?>,
        currentTime: Time,
    ): WeibullTime<T?> {
        return WeibullTime(
            this.rand,
            this.dist,
            this.offset, currentTime
        )
    }

    companion object {
        private const val serialVersionUID = 5216987069271114818L
        protected fun weibullFromMean(mean: Double, deviation: Double, random: RandomGenerator?): WeibullDistribution {
            val t = FastMath.log(deviation * deviation / (mean * mean) + 1.0)
            var kmin = 0.0

            var kmax: Double
            kmax = 1.0
            while (Gamma.logGamma(1.0 + 2.0 * kmax) - 2.0 * Gamma.logGamma(1.0 + kmax) < t) {
                kmin = kmax
                kmax *= 2.0
            }

            var k: Double
            k = (kmin + kmax) / 2.0
            while (kmin < k && k < kmax) {
                if (Gamma.logGamma(1.0 + 2.0 * k) - 2.0 * Gamma.logGamma(1.0 + k) < t) {
                    kmin = k
                } else {
                    kmax = k
                }
                k = (kmin + kmax) / 2.0
            }

            val shapeParameter = 1.0 / k
            val scaleParameter = mean / FastMath.exp(Gamma.logGamma(1.0 + k))
            return WeibullDistribution(random, shapeParameter, scaleParameter, 1.0E-9)
        }
    }
}