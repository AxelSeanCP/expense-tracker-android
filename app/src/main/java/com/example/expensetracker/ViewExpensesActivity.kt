package com.example.expensetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.expensetracker.ui.theme.ExpenseTrackerTheme
import com.example.expensetracker.data.AppDatabase
import com.example.expensetracker.data.Expense
import com.example.expensetracker.data.ThemePreferenceManager
import com.example.expensetracker.ui.ExpenseViewModel
import com.example.expensetracker.ui.ExpenseViewModelFactory
import com.example.expensetracker.ui.components.AppDrawer
import com.example.expensetracker.ui.components.AppTopAppBar
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import com.example.expensetracker.utils.formatAmount

class ViewExpensesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val themeManager = ThemePreferenceManager(this)

        setContent {
            val isDarkTheme by themeManager.isDarkTheme.collectAsState(initial = false)

            ExpenseTrackerTheme(
                darkTheme = isDarkTheme
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val context = LocalContext.current
                    // Get database and DAO instances
                    val database = AppDatabase.getDatabase(context)
                    val expenseDao = database.expenseDao()

                    // Create view model using the factory
                    val viewModel: ExpenseViewModel = viewModel(
                        factory = ExpenseViewModelFactory(expenseDao)
                    )
                    ViewExpensesScreen(viewModel = viewModel, isDarkTheme = isDarkTheme, themeManager = themeManager)
                }
            }
        }
    }
}

@Composable
fun ViewExpensesScreen(viewModel: ExpenseViewModel, isDarkTheme: Boolean, themeManager: ThemePreferenceManager) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val expenses by viewModel.expenses.collectAsState()
    val totalExpenses by viewModel.totalExpenses.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val selectedYear by viewModel.selectedYear.collectAsState()

    val showConfirmDeleteDialog = remember { mutableStateOf(false) }
    val expenseToDelete = remember { mutableStateOf<Expense?>(null) }

    AppDrawer(drawerState = drawerState, scope = scope) {
        Scaffold(
            topBar = {
                AppTopAppBar(
                    title = "View Expenses",
                    drawerState = drawerState,
                    scope = scope,
                    isDarkTheme = isDarkTheme,
                    themeManager = themeManager
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Date selector item
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { viewModel.previousMonth() }
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Month")
                        }
                        Text(
                            text = "${getMonthName(selectedMonth)} $selectedYear",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(
                            onClick = { viewModel.nextMonth() }
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Month")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Total expenses item
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Total Expenses:",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Rp. ${formatAmount(totalExpenses)}",
                                    style = MaterialTheme.typography.headlineLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    // Header item
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Your Expenses:",
                            style = MaterialTheme.typography.headlineSmall,
                            modifier= Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // list of expenses
                    if (expenses.isEmpty()) {
                        item {
                            Text("No expenses added yet.", modifier = Modifier.padding(16.dp))
                        }
                    } else {
                        items(expenses) { expense ->
                            ExpenseItem(
                                expense = expense,
                                // Pass a lambda that updates the states to show dialog
                                onDeleteClick = { item ->
                                    expenseToDelete.value = item
                                    showConfirmDeleteDialog.value = true
                                }
                            )
                        }
                    }

//                    item {
//                        Spacer(modifier = Modifier.height(16.dp))
//                        // clear all expenses button
//                        TextButton(
//                            onClick = { viewModel.clearAllExpenses() },
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .wrapContentWidth(Alignment.End)
//                        ) {
//                            Text("Clear All Expenses")
//                        }
//                        Spacer(modifier = Modifier.height(8.dp))
//                    }
                }
            }

            // Delete expense dialog
            if (showConfirmDeleteDialog.value) {
                AlertDialog(
                    onDismissRequest = {
                        showConfirmDeleteDialog.value = false
                        expenseToDelete.value = null
                    },
                    title = { Text("Confirm Deletion") },
                    text = {
                        Text("Are you sure you want to delete '${expenseToDelete.value?.name ?: "this expense"}'?")
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                expenseToDelete.value?.let { expense ->
                                    viewModel.deleteExpense(expense)
                                }
                                showConfirmDeleteDialog.value = false
                                expenseToDelete.value = null
                            }
                        ) {
                            Text("Delete")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                showConfirmDeleteDialog.value = false
                                expenseToDelete.value = null
                            }
                        ) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}


@Composable
fun ExpenseItem(expense: Expense, onDeleteClick: (Expense) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = formatDate(expense.date),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = expense.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Rp. ${formatAmount(expense.amount)}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            IconButton(onClick = { onDeleteClick(expense) }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Expense",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun getMonthName(monthIndex: Int): String {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.MONTH, monthIndex)
    return SimpleDateFormat("MMMM", Locale("in", "ID")).format(calendar.time)
}

@Composable
fun formatDate(timeStamp: Long): String {
    val date = Calendar.getInstance().apply { timeInMillis = timeStamp }.time
    return SimpleDateFormat("dd-MMM-yyyy", Locale("in", "ID")).format(date)
}
