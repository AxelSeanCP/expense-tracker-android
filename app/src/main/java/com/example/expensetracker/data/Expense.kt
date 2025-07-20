package com.example.expensetracker.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "expense_name")
    val name: String,
    @ColumnInfo(name = "expense_amount")
    val amount: Double
)
