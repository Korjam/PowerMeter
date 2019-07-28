package com.kinwatt.powermeter.common.mathUtils

import kotlin.math.roundToInt

fun angularVelocity(rpm: Float) = (2.0 * Math.PI * rpm / 60).toFloat()

fun linealVelocity(rpm: Float, radious: Float) = radious * angularVelocity(rpm)

/**
 * Gives the best average @seg seconds for powerList
 *
 * @param powerList List of activity power, second by second
 * @param seg The number of seconds to get the best average of
 */
fun cpseg(powerList: List<Int>, seg: Int): Int {
    var sum = powerList.take(seg).sum()
    var bestSum = sum
    for (i in seg until powerList.size) {
        sum += powerList[i] - powerList[i - seg]
        if (sum > bestSum) {
            bestSum = sum
        }
    }
    return (bestSum.toFloat() / seg).roundToInt()
}

fun <T : Number, S : Number> interpolate(x1: T, y1: S, x2: T, y2: S, v: T) = interpolation(x1, y1, x2, y2)(v)

fun <T : Number, S : Number> interpolation(x1: T, y1: S, x2: T, y2: S): (T) -> S {
    val _x1 = x1.toDouble()
    val _y1 = y1.toDouble()
    val _x2 = x2.toDouble()
    val _y2 = y2.toDouble()

    if (_x1 >= _x2) throw IllegalArgumentException(String.format("'$x1' cannot be greater than '$x2'."))

    val m = (_y2 - _y1) / (_x2 - _x1)
    val n = _y1 - m * _x1

    return when (y1) {
        is Double -> { v ->  (m * v.toDouble() + n) as S }
        is Float -> { v ->  (m * v.toDouble() + n).toFloat() as S }
        is Long -> { v ->  (m * v.toDouble() + n).toLong() as S }
        is Int -> { v ->  (m * v.toDouble() + n).toInt() as S }
        else -> throw RuntimeException("This exception never should be thrown.")
    }
}