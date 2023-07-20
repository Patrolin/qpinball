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
class Ball(var x: Double, var y: Double, var radius: Double = 50.0, var shaderId: Int = 0)
val balls = listOf(
    Ball(-0.2, 0.0, shaderId=0),
    Ball(0.0, 0.0, shaderId=1),
    Ball(0.2, 0.0, shaderId=2),
    Ball(0.4, 0.0, shaderId=3),
    Ball(-0.1, 0.1, 10.0, shaderId=0),
    Ball(0.0, 0.1, 10.0, shaderId=1),
    Ball(0.1, 0.1, 10.0, shaderId=2),
    Ball(0.2, 0.1, 10.0, shaderId=3),
    /*Ball(0.0, -1.0),
    Ball(0.0, 1.0),
    Ball(.5, 50*TIME_STEP),
    Ball(0.0, 50*TIME_STEP),
    Ball(0.0, 100*TIME_STEP),
    Ball(0.0, 150*TIME_STEP),*/
)
fun simulateStep() {
    for (ball in balls) {
        ball.y -= 0.01 * TIME_STEP
    }
}