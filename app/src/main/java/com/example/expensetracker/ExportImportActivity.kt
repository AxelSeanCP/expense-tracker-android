package com.example.expensetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.expensetracker.data.DatabaseFileManager
import com.example.expensetracker.data.ThemePreferenceManager
import com.example.expensetracker.ui.ExportImportViewModel
import com.example.expensetracker.ui.ExportImportViewModelFactory
import com.example.expensetracker.ui.components.AppDrawer
import com.example.expensetracker.ui.components.AppTopAppBar
import com.example.expensetracker.ui.theme.ExpenseTrackerTheme
import kotlinx.coroutines.launch


class ExportImportActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val themeManager = ThemePreferenceManager(this)

        setContent {
            val isDarkTheme by themeManager.isDarkTheme.collectAsState(initial = false)

            ExpenseTrackerTheme (
                darkTheme = isDarkTheme
            ) {
                val context = LocalContext.current
                val databaseFileManager = DatabaseFileManager(context)
                val viewModel: ExportImportViewModel = viewModel(
                    factory = ExportImportViewModelFactory(databaseFileManager)
                )
                val importLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.OpenDocument(),
                    onResult = { uri ->
                        uri?.let { viewModel.importDatabase(it) }
                    }
                )

                ExportImportScreen(
                    viewModel = viewModel,
                    themeManager = themeManager,
                    isDarkTheme = isDarkTheme,
                    onImportRequest = {
                        importLauncher.launch(arrayOf("*/*"))
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportImportScreen(
    viewModel: ExportImportViewModel,
    themeManager: ThemePreferenceManager,
    isDarkTheme: Boolean,
    onImportRequest: () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(key1 = Unit) {
        viewModel.uiEvent.collect { message ->
            snackbarHostState.showSnackbar(message = message)
        }
    }

    AppDrawer(
        drawerState = drawerState,
        scope = scope
    ) {
        Scaffold(
            topBar = {
                AppTopAppBar(
                    title = "Backup & Restore",
                    drawerState = drawerState,
                    scope = scope,
                    isDarkTheme = isDarkTheme,
                    themeManager = themeManager
                )
            },
            snackbarHost = {
                SnackbarHost(snackbarHostState) { data ->
                    Snackbar(
                        snackbarData = data,
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = { viewModel.exportDatabase() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Export Database")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onImportRequest,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Import Database")
                }
            }
        }
    }
}