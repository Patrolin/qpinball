package com.patrolin.qpinball.common

data class Vec2(var x: Double = 0.0, var y: Double = 0.0) {
    // scalar
    operator fun times(f: Double): Vec2 {
        return Vec2(this.x * f, this.y * f)
    }
    // vector
    operator fun plus(B: Vec2): Vec2 {
        return Vec2(this.x + B.x, this.y + B.y)
    }
    operator fun minus(B: Vec2): Vec2 {
        return Vec2(this.x - B.x, this.y - B.y)
    }
    operator fun times(B: Vec2): Vec2 {
        return Vec2(this.x * B.x, this.y * B.y)
    }
    operator fun div(B: Vec2): Vec2 {
        return Vec2(this.x / B.x, this.y / B.y)
    }
}