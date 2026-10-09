package com.tinaut1986.porkilo.util

import java.text.NumberFormat
import java.util.Locale

fun formatQuantity(value: Double): String {
    val format = NumberFormat.getNumberInstance(Locale.getDefault())
    format.maximumFractionDigits = 3
    format.minimumFractionDigits = 0
    return format.format(value)
}
