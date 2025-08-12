package com.example.expensetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.res.stringResource
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

@OptIn(ExperimentalMaterial3Api::class)
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
    val showClearAllDialog = remember { mutableStateOf(false) }

    AppDrawer(drawerState = drawerState, scope = scope) {
        Scaffold(
            topBar = {
                AppTopAppBar(
                    title = stringResource(R.string.view_expenses),
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
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
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
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.previous_month))
                                }
                                Text(
                                    text = "${getMonthName(selectedMonth)} $selectedYear",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                IconButton(
                                    onClick = { viewModel.nextMonth() }
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = stringResource(R.string.next_month))
                                }
                            }
                        }
                    }

                    // Total expenses item
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = stringResource(R.string.total_expenses),
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
                            text = stringResource(R.string.your_expenses),
                            style = MaterialTheme.typography.headlineSmall,
                            modifier= Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // list of expenses
                    if (expenses.isEmpty()) {
                        item {
                            Text(stringResource(R.string.no_expenses_added), modifier = Modifier.padding(16.dp))
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

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        // clear all expenses button
                        TextButton(
                            onClick = { showClearAllDialog.value = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentWidth(Alignment.End)
                        ) {
                            Text(stringResource(R.string.delete_all_expenses))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            // Delete expense dialog
            if (showConfirmDeleteDialog.value) {
                AlertDialog(
                    onDismissRequest = {
                        showConfirmDeleteDialog.value = false
                        expenseToDelete.value = null
                    },
                    title = { Text(stringResource(R.string.confirm_deletion)) },
                    text = {
                        Text(stringResource(R.string.confirm_delete_message, expenseToDelete.value?.name ?: "pengeluaran ini"))
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
                            Text(stringResource(R.string.delete))
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                showConfirmDeleteDialog.value = false
                                expenseToDelete.value = null
                            }
                        ) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                )
            }

            if (showClearAllDialog.value) {
                AlertDialog(
                    onDismissRequest = { showClearAllDialog.value = false },
                    title = { Text(stringResource(R.string.confirm_clear_all)) },
                    text = { Text(stringResource(R.string.confirm_clear_all_message)) },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.clearAllExpenses()
                                showClearAllDialog.value = false
                            }
                        ) {
                            Text(stringResource(R.string.delete_all_expenses))
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showClearAllDialog.value = false }
                        ) {
                            Text(stringResource(R.string.cancel))
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
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = expense.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Rp. ${formatAmount(expense.amount)}",
                    style = MaterialTheme.typography.titleMedium,
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
