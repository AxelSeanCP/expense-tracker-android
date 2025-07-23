package com.example.expensetracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    @Insert
    suspend fun insertExpense(expense: Expense)

    @Query("SELECT * FROM expenses " +
            "WHERE strftime('%Y', expense_date / 1000, 'unixepoch') = :year " +
            "AND strftime('%m', expense_date / 1000, 'unixepoch') = :month " +
            "ORDER BY expense_date DESC")
    fun getExpensesByMonthAndYear(year: String, month: String): Flow<List<Expense>>

    @Query("SELECT * FROM expenses ORDER BY id DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Delete
    suspend fun deleteExpense(expense: Expense)

    @Query("DELETE FROM expenses")
    suspend fun deleteAllExpenses()
}