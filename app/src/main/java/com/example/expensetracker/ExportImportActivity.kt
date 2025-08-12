package com.example.expensetracker

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.expensetracker.data.DatabaseFileManager
import com.example.expensetracker.data.ThemePreferenceManager
import com.example.expensetracker.ui.ExportImportUiEvent
import com.example.expensetracker.ui.ExportImportViewModel
import com.example.expensetracker.ui.ExportImportViewModelFactory
import com.example.expensetracker.ui.components.AppDrawer
import com.example.expensetracker.ui.components.AppTopAppBar
import com.example.expensetracker.ui.theme.ExpenseTrackerTheme

class ExportImportActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.Q)
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

@RequiresApi(Build.VERSION_CODES.Q)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportImportScreen(
    viewModel: ExportImportViewModel,
    themeManager: ThemePreferenceManager,
    isDarkTheme: Boolean,
    onImportRequest: () -> Unit
) {
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(key1 = Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is ExportImportUiEvent.ShowSnackbar -> {
                    val message = context.getString(event.messageResId, *event.formatArgs.toTypedArray())
                    snackbarHostState.showSnackbar(
                        message = message,
                        duration = SnackbarDuration.Short
                    )
                }
                is ExportImportUiEvent.RestartApp -> {
                    val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
                    intent?.let {
                        it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(it)
                    }
                }
            }
        }
    }

    AppDrawer(
        drawerState = drawerState,
        scope = scope
    ) {
        Scaffold(
            topBar = {
                AppTopAppBar(
                    title = stringResource(R.string.backup_restore),
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
                    Text(stringResource(R.string.export_database))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onImportRequest,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.import_database))
                }
            }
        }
    }
}