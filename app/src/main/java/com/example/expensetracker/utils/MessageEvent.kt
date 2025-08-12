package com.example.expensetracker.utils

import androidx.annotation.StringRes

data class MessageEvent(
    @StringRes val messageResId: Int,
    val formatArgs: List<Any> = emptyList()
)
