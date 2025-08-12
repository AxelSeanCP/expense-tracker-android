package com.example.expensetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.expensetracker.data.AppDatabase
import com.example.expensetracker.data.ThemePreferenceManager
import com.example.expensetracker.ui.SummaryViewModel
import com.example.expensetracker.ui.SummaryViewModelFactory
import com.example.expensetracker.ui.components.AppDrawer
import com.example.expensetracker.ui.components.AppTopAppBar
import com.example.expensetracker.ui.theme.ExpenseTrackerTheme
import com.example.expensetracker.utils.formatAmount

class SummaryActivity : ComponentActivity() {
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
                    val database = AppDatabase.getDatabase(context)
                    val expenseDao = database.expenseDao()

                    val viewModel: SummaryViewModel = viewModel(
                        factory = SummaryViewModelFactory(expenseDao)
                    )
                    ExpenseSummaryScreen(viewModel = viewModel, isDarkTheme = isDarkTheme, themeManager = themeManager)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseSummaryScreen(viewModel: SummaryViewModel, isDarkTheme: Boolean, themeManager: ThemePreferenceManager) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val selectedYear by viewModel.selectedYear.collectAsState()
    val totalExpenses by viewModel.totalExpenses.collectAsState()
    val averageExpenses by viewModel.averageExpenses.collectAsState()
    val expenseCount by viewModel.expenseCount.collectAsState()

    AppDrawer(drawerState = drawerState, scope = scope) {
        Scaffold(
            topBar = {
                AppTopAppBar(
                    title = stringResource(R.string.expense_summary),
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
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                // Year Selector
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
                        IconButton(onClick = { viewModel.previousYear() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.previous_year))
                        }
                        Text(
                            text = selectedYear.toString(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { viewModel.nextYear() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = stringResource(R.string.next_year))
                        }
                    }
                }

                // Summary Data Cards
                Spacer(modifier = Modifier.height(16.dp))
                SummaryCard(
                    title = stringResource(R.string.total_expenses_summary),
                    value = totalExpenses,
                    prefix = "Rp. "
                )

                Spacer(modifier = Modifier.height(8.dp))
                SummaryCard(
                    title = stringResource(R.string.average_expenses_summary),
                    value = averageExpenses,
                    prefix = "Rp. "
                )

                Spacer(modifier = Modifier.height(8.dp))
                SummaryCard(
                    title = stringResource(R.string.number_of_expenses_summary),
                    value = expenseCount.toDouble(),
                    prefix = ""
                )
            }
        }
    }
}


@Composable
fun SummaryCard(
    title: String,
    value: Double?,
    prefix: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "$prefix${formatAmount(value ?: 0.0)}",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}