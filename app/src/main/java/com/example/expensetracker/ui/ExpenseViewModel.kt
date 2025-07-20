package com.example.expensetracker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.Expense
import com.example.expensetracker.data.ExpenseDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ExpenseViewModel(private val expenseDao: ExpenseDao) : ViewModel() {

    // state for the list of expenses displayed in the UI
    // it collects from the room flow and converts it to a state flow
    val expenses: StateFlow<List<Expense>> = expenseDao.getAllExpenses()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // State for the input fields for adding a new expense
    private val _newExpenseName = MutableStateFlow("")
    val newExpenseName: StateFlow<String> = _newExpenseName.asStateFlow()

    private val _newExpenseAmount = MutableStateFlow("")
    val newExpenseAmount: StateFlow<String> = _newExpenseAmount.asStateFlow()

    // State for the total expense amount
    val totalExpenses: StateFlow<Double> = expenses
        .map { expenseList ->
            expenseList.sumOf { it.amount }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    fun onNewExpenseNameChange(name: String) {
        _newExpenseName.value = name
    }

    fun onNewExpenseAmountChange(amount: String) {
        // only allow numeric input
        if (amount.all { it.isDigit() || it == '.' }) {
            _newExpenseAmount.value = amount
        }
    }

    fun addExpense() {
        viewModelScope.launch {
            val name = _newExpenseName.value.trim()
            val amount = _newExpenseAmount.value.toDoubleOrNull()

            if (name.isNotBlank() && amount != null && amount > 0) {
                val newExpense = Expense(name = name, amount = amount)
                expenseDao.insertExpense(newExpense)

                _newExpenseName.value = ""
                _newExpenseAmount.value = ""
            }
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            expenseDao.deleteExpense(expense)
        }
    }

    fun clearAllExpenses() {
        viewModelScope.launch {
            expenseDao.deleteAllExpenses()
        }
    }
}

class ExpenseViewModelFactory(private val expenseDao: ExpenseDao) : ViewModelProvider.Factory {
    override fun <T: ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExpenseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExpenseViewModel(expenseDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
