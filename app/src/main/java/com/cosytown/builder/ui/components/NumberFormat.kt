package com.cosytown.builder.ui.components

import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

private val SUFFIXES = listOf("", "K", "M", "B", "T")

/** Compact idle-game number formatting: 950, 12.3K, 4.56M, ... */
fun Double.formatCompact(): String {
    val value = this
    if (abs(value) < 1000.0) {
        return if (value == floor(value)) value.toLong().toString() else "%.1f".format(value)
    }
    val magnitude = (log10(abs(value)) / 3).toInt().coerceIn(0, SUFFIXES.size - 1)
    val scaled = value / 10.0.pow(magnitude * 3)
    return "%.2f%s".format(scaled, SUFFIXES[magnitude])
}

fun Long.formatCompact(): String = toDouble().formatCompact()
