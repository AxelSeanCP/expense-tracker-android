package com.example.expensetracker.ui.components

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.expensetracker.ViewExpensesActivity
import com.example.expensetracker.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDrawer(
    drawerState: DrawerState,
    scope: CoroutineScope,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    ModalNavigationDrawer(
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.fillMaxWidth(0.75f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Expense Tracker Menu",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = {
                        scope.launch { drawerState.close() }
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Close Menu")
                    }
                }
                HorizontalDivider()

                NavigationDrawerItem(
                    label = { Text("Input Expenses") },
                    selected = context is MainActivity,
                    onClick = {
                        scope.launch { drawerState.close() }
                        if (context !is MainActivity) {
                            context.startActivity(Intent(context, MainActivity::class.java))
                        }
                    },
                    icon = { Icon(Icons.Default.Create, contentDescription = "Input Expense") }
                )

                NavigationDrawerItem(
                    label = { Text("View Expenses") },
                    selected = context is ViewExpensesActivity,
                    onClick = {
                        scope.launch { drawerState.close() }
                        if (context !is ViewExpensesActivity) {
                            context.startActivity(Intent(context, ViewExpensesActivity::class.java))
                        }
                    },
                    icon = { Icon(Icons.Default.Search, contentDescription = "View Expenses")}
                )
            }
        },
        drawerState = drawerState,
        content = content
    )
}