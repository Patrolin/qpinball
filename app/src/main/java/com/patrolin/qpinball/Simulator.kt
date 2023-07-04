package com.patrolin.qpinball

import com.patrolin.qpinball.common.*

// common
var prev_t = time()
const val TIME_STEP = 1/240.0
fun simulate() {
    val t = time()
    while (t - prev_t > TIME_STEP) {
        simulateStep()
        prev_t += TIME_STEP
    }
}

// data
class Ball(var x: Double, var y: Double)
val balls = listOf(Ball(0.0, 0.0))
fun simulateStep() {
    for (ball in balls) {
        ball.y -= 0.1 * TIME_STEP
    }
}