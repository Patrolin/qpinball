package com.patrolin.qpinball.common

import android.util.Log

fun debugPrint(message: String) {
    Log.d("AYAYA", message)
}
fun fail(message: String): Nothing {
    throw Exception(message)
}
fun time(): Double {
    return System.currentTimeMillis().toDouble() / 1e3
}
fun reverseBytes(value: Int): Int {
    return reverseBytes(value.toUInt()).toInt()
}
fun reverseBytes(value: UInt): UInt {
    val a = value.shl(24)
    val b = value.and(0xff00U).shl(8)
    val c = value.and(0xff_0000U).shr(8)
    val d = value.and(0xff00_0000U).shr(24)
    return a.or(b).or(c).or(d)
}