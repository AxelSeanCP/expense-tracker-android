package com.example.expensetracker.utils

import androidx.compose.runtime.Composable
import java.text.NumberFormat
import java.util.Locale

@Composable
fun formatAmount(amount: Double?): String {
    val value = amount ?: 0.0

    val formatter = NumberFormat.getNumberInstance(Locale("in", "ID"))

    if (value % 1.0 == 0.0) {
        formatter.maximumFractionDigits = 0
    } else {
        formatter.minimumFractionDigits = 2
        formatter.maximumFractionDigits = 2
    }

    return formatter.format(value)
}