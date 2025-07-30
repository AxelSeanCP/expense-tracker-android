package com.example.expensetracker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.Expense
import com.example.expensetracker.data.ExpenseDao
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.combine
import java.util.Calendar
import kotlinx.coroutines.flow.flatMapLatest

class ExpenseViewModel(private val expenseDao: ExpenseDao) : ViewModel() {

    // State for the input fields for adding a new expense
    private val _newExpenseName = MutableStateFlow("")
    val newExpenseName: StateFlow<String> = _newExpenseName.asStateFlow()

    private val _newExpenseAmount = MutableStateFlow("")
    val newExpenseAmount: StateFlow<String> = _newExpenseAmount.asStateFlow()

    private val _uiEvent = MutableSharedFlow<String>()
    val uiEvent: SharedFlow<String> = _uiEvent.asSharedFlow()

    // State for selected month and year for filtering
    private val _selectedMonth = MutableStateFlow(Calendar.getInstance().get(Calendar.MONTH)) // 0-indexed (jan = 0)
    val selectedMonth: StateFlow<Int> = _selectedMonth.asStateFlow()

    private val _selectedYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    val selectedYear = _selectedYear.asStateFlow()

    private val _selectedExpenseDate = MutableStateFlow(System.currentTimeMillis())
    val selectedExpenseDate: StateFlow<Long> = _selectedExpenseDate.asStateFlow()

    // Combine selected month/year with the DAO query to get filtered expenses
    @OptIn(ExperimentalCoroutinesApi::class)
    val expenses: StateFlow<List<Expense>> = combine(
        selectedMonth,
        selectedYear
    ) { month, year ->
        // Convert 0-indexed month (from Calendar) to 1-indexed string (for strftime in SQLite)
        val formattedMonth = (month + 1).toString().padStart(2, '0')
        val formattedYear = year.toString()
        expenseDao.getExpensesByMonthAndYear(formattedYear, formattedMonth)
    }
        .flatMapLatest { it } // Use flatMapLatest to switch to the new Flow when parameters change
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

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
            val date = _selectedExpenseDate.value

            if (name.isNotBlank() && amount != null && amount > 0) {
                val newExpense = Expense(name = name, amount = amount, date = date)
                expenseDao.insertExpense(newExpense)

                _newExpenseName.value = ""
                _newExpenseAmount.value = ""
                _selectedExpenseDate.value = System.currentTimeMillis()

                _uiEvent.emit("Expense $name added successfully!")
            } else {
                _uiEvent.emit("Please enter a valid expense name and amount.")
            }
        }
    }

    fun updateSelectedExpenseDate(timestamp: Long) {
        _selectedExpenseDate.value = timestamp
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            expenseDao.deleteExpense(expense)
            _uiEvent.emit("Expense '${expense.name}' is deleted.")
        }
    }

    fun clearAllExpenses() {
        viewModelScope.launch {
            expenseDao.deleteAllExpenses()
            _uiEvent.emit("All expenses cleared.")
        }
    }

    fun selectMonth(year: Int, month: Int) {
        _selectedYear.value = year
        _selectedMonth.value = month
    }

    fun previousMonth() {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, _selectedYear.value)
            set(Calendar.MONTH, _selectedMonth.value)
            add(Calendar.MONTH, -1)
        }
        _selectedYear.value = calendar.get(Calendar.YEAR)
        _selectedMonth.value = calendar.get(Calendar.MONTH)
    }

    fun nextMonth() {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, _selectedYear.value)
            set(Calendar.MONTH, _selectedMonth.value)
            add(Calendar.MONTH, 1)
        }
        _selectedYear.value = calendar.get(Calendar.YEAR)
        _selectedMonth.value = calendar.get(Calendar.MONTH)
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
