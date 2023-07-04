package com.patrolin.qpinball.common

import android.util.Log

fun debugPrint(message: String) {
    Log.d("AYAYA", message)
}
fun time(): Double {
    return System.currentTimeMillis().toDouble() / 1e3
}