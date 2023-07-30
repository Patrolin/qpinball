package com.patrolin.qpinball.common

data class Vec2(var x: Double = 0.0, var y: Double = 0.0) {
    // scalar
    operator fun times(f: Double): Vec2 {
        return Vec2(this.x * f, this.y * f)
    }
    operator fun div(f: Double): Vec2 {
        return Vec2(this.x / f, this.y / f)
    }
    // vector
    operator fun plus(v: Vec2): Vec2 {
        return Vec2(this.x + v.x, this.y + v.y)
    }
    operator fun minus(v: Vec2): Vec2 {
        return Vec2(this.x - v.x, this.y - v.y)
    }
    operator fun times(v: Vec2): Vec2 {
        return Vec2(this.x * v.x, this.y * v.y)
    }
    operator fun div(v: Vec2): Vec2 {
        return Vec2(this.x / v.x, this.y / v.y)
    }
    fun dot(v: Vec2): Double {
        return this.x * v.x + this.y * v.y
    }
    fun exterior(v: Vec2): Bivec2 {
        return Bivec2(0.0, this.x * v.y - this.y * v.x)
    }
    fun magnitudeSquared(): Double {
        return this.dot(this)
    }
    fun inverse(): Vec2 {
        return this / this.magnitudeSquared()
    }
    fun projection(v: Vec2): Vec2 {
        return v.inverse() * v.dot(this)
    }
    fun rejection(v: Vec2): Vec2 {
        return v.inverse() * v.exterior(this)
    }
    // bivector
    operator fun times(b: Bivec2): Vec2 {
        val x = this.x * b.f - this.y * b.xy
        val y = this.y * b.f + this.x * b.xy
        return Vec2(x, y)
    }
}

data class Bivec2(var f: Double = 0.0, var xy: Double = 0.0) {
    // scalar
    operator fun times(f: Double): Bivec2 {
        return Bivec2(this.f * f, this.xy * f)
    }
    // vector
    operator fun times(v: Vec2): Vec2 {
        val x = this.f * v.x + this.xy * v.y
        val y = this.f * v.y - this.xy * v.x
        return Vec2(x, y)
    }
}