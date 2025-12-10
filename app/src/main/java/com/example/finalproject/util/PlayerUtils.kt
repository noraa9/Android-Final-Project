package com.example.finalproject.util

import kotlin.random.Random

fun getWaveForm(): IntArray {
    val random = Random(System.currentTimeMillis())
    return IntArray(50) { 5 + random.nextInt(50) }
}

fun formatTime(seconds: Int): String = String.format("%02d:%02d", seconds / 60, seconds % 60)

