package com.example.expensetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.expensetracker.ui.theme.ExpenseTrackerTheme
import com.example.expensetracker.data.AppDatabase
import com.example.expensetracker.ui.ExpenseViewModel
import com.example.expensetracker.ui.ExpenseViewModelFactory
import com.example.expensetracker.ui.components.AppDrawer
import com.example.expensetracker.ui.components.AppTopAppBar

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

@Composable
fun ExpenseTrackerScreen(viewModel: ExpenseViewModel) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val newExpenseName by viewModel.newExpenseName.collectAsState()
    val newExpenseAmount by viewModel.newExpenseAmount.collectAsState()

    LaunchedEffect(key1 = Unit) {
        viewModel.uiEvent.collect { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
        }
    }

    AppDrawer(drawerState= drawerState, scope = scope) {
        Scaffold(
            topBar = {
                AppTopAppBar(
                    title = "Input Expenses",
                    drawerState = drawerState,
                    scope = scope
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) {data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    actionColor = MaterialTheme.colorScheme.tertiary,
                    dismissActionContentColor = MaterialTheme.colorScheme.tertiary
                )
            } }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
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
            }
        }
    }
}