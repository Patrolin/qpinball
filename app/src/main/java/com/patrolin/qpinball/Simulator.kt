package com.patrolin.qpinball

import com.patrolin.qpinball.common.*

// common
var screenSize = Vec2()
var prev_t = time()
const val TIME_STEP = 1/240.0
fun simulate() {
    //debugPrint("balls[1]: ${balls[1]}")
    val t = time()
    while (t - prev_t > TIME_STEP) {
        simulateStep()
        prev_t += TIME_STEP
    }
}

// data
data class Ball(var pos: Vec2, var vel: Vec2 = Vec2(), var r: Double = 50.0, var shaderId: Int = 2)
val balls = listOf(
    /*Ball(Vec2(-0.2, 0.2), shaderId=0),
    Ball(Vec2(0.0, 0.2), shaderId=1),
    Ball(Vec2(0.2, 0.2), shaderId=2),
    Ball(Vec2(0.4, 0.2), shaderId=3),
    Ball(Vec2(-0.1, 0.1), r=10.0, shaderId=0),
    Ball(Vec2(0.0, 0.1), r=10.0, shaderId=1),
    Ball(Vec2(0.1, 0.1), r=10.0, shaderId=2),
    Ball(Vec2(0.2, 0.1), r=10.0, shaderId=3),*/
    Ball(Vec2(-0.1, 0.0), r=10.0),
    Ball(Vec2(0.0, 0.0), r=10.0),
    Ball(Vec2(0.1, 0.0), r=10.0),
    Ball(Vec2(0.2, 0.0), r=10.0),
)
const val GRAVITY_PX_PER_S = -350.0
fun acceleration(ball: Ball): Vec2 {
    val gravity = GRAVITY_PX_PER_S / screenSize.y
    return Vec2(0.0, gravity)
}
fun simulateStep() {
    for (ball in balls) {
        val acc = acceleration(ball)
        ball.pos += (acc * TIME_STEP * 0.5 + ball.vel) * TIME_STEP
        ball.vel += acc * TIME_STEP
        if (ball.pos.y < (-1.0 + ball.r / screenSize.y)) ball.vel.y = -ball.vel.y
    }
}