package com.example.expensetracker.ui.components

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.expensetracker.ExportImportActivity
import com.example.expensetracker.ViewExpensesActivity
import com.example.expensetracker.MainActivity
import com.example.expensetracker.SummaryActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import com.example.expensetracker.R

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
                modifier = Modifier.fillMaxWidth(0.75f),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,

                ) {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = {
                        scope.launch { drawerState.close() }
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Close Menu", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }

                HorizontalDivider()

                Spacer(modifier = Modifier.height(8.dp) )

                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.input_expenses)) },
                    selected = context is MainActivity,
                    onClick = {
                        scope.launch { drawerState.close() }
                        if (context !is MainActivity) {
                            context.startActivity(Intent(context, MainActivity::class.java))
                        }
                    },
                    icon = { Icon(Icons.Default.Create, contentDescription = stringResource(R.string.input_expenses)) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.view_expenses)) },
                    selected = context is ViewExpensesActivity,
                    onClick = {
                        scope.launch { drawerState.close() }
                        if (context !is ViewExpensesActivity) {
                            context.startActivity(Intent(context, ViewExpensesActivity::class.java))
                        }
                    },
                    icon = { Icon(Icons.Default.Search, contentDescription = stringResource(R.string.view_expenses))}
                )

                Spacer(modifier = Modifier.height(8.dp))

                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.expense_summary)) },
                    selected = context is SummaryActivity,
                    onClick = {
                        scope.launch { drawerState.close() }
                        if (context !is SummaryActivity) {
                            context.startActivity(Intent(context, SummaryActivity::class.java))
                        }
                    },
                    icon = { Icon(Icons.Default.Info, contentDescription = stringResource(R.string.expense_summary))}
                )

                Spacer(modifier = Modifier.height(8.dp))

                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.backup_restore)) },
                    selected = context is ExportImportActivity,
                    onClick = {
                        scope.launch { drawerState.close() }
                        if (context !is ExportImportActivity) {
                            context.startActivity(Intent(context, ExportImportActivity::class.java))
                        }
                    },
                    icon = { Icon(Icons.Default.Download , contentDescription = stringResource(R.string.backup_restore))}
                )
            }
        },
        drawerState = drawerState,
        content = content
    )
}