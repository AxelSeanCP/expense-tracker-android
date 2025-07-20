package com.example.expensetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.expensetracker.ui.theme.ExpenseTrackerTheme
import com.example.expensetracker.data.AppDatabase
import com.example.expensetracker.data.Expense
import com.example.expensetracker.ui.ExpenseViewModel
import com.example.expensetracker.ui.ExpenseViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ExpenseTrackerTheme {
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
                    ExpenseTrackerScreen(viewModel = viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseTrackerScreen(viewModel: ExpenseViewModel) {
    val expenses by viewModel.expenses.collectAsState()
    val newExpenseName by viewModel.newExpenseName.collectAsState()
    val newExpenseAmount by viewModel.newExpenseAmount.collectAsState()
    val totalExpenses by viewModel.totalExpenses.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Expense Tracker") })
        }
    ) { paddingValues ->  
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Input field for new Expense
            OutlinedTextField(
                value = newExpenseName,
                onValueChange = { viewModel.onNewExpenseNameChange(it) },
                label = { Text("Expense Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = newExpenseAmount,
                onValueChange = { viewModel.onNewExpenseAmountChange(it) },
                label = { Text("Amount (e.g. 25000)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Add expense button
            Button(
                onClick = { viewModel.addExpense() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Expense")
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Total expenses display
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
                        text = "Rp. ${"%.2f".format(totalExpenses)}",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // list of expenses
            Text(
                text = "Your Expenses:",
                style = MaterialTheme.typography.headlineSmall,
                modifier= Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (expenses.isEmpty()) {
                Text("No expenses added yet.", modifier = Modifier.padding(16.dp))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(expenses) { expense ->
                        ExpenseItem(expense = expense, onDeleteClick = {
                            viewModel.deleteExpense(expense)
                        })
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // clear all expenses button
            TextButton(
                onClick = { viewModel.clearAllExpenses() },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Clear All Expenses")
            }
        }
    }
}

@Composable
fun ExpenseItem(expense: Expense, onDeleteClick: () -> Unit) {
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
                    text = expense.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Rp. ${"%.2f".format(expense.amount)}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Expense",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ExpenseTrackerScreenPreview() {
    ExpenseTrackerTheme {
        // For preview, we can create a dummy ViewModel or just show a placeholder
        // Full functionality requires a real database context.
        Surface(modifier = Modifier.fillMaxSize()) {
            Text("Run on device to see full Expense Tracker functionality.")
        }
    }
}