package com.fudcalc.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FudCalcApp(viewModel: AppViewModel) {
    var tab by rememberSaveable { mutableIntStateOf(0) }

    val date by viewModel.date.collectAsStateWithLifecycle()
    val entries by viewModel.entries.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val results by viewModel.results.collectAsStateWithLifecycle()
    val packageInfo by viewModel.packageInfo.collectAsStateWithLifecycle()
    val importError by viewModel.importError.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (tab) {
                            0 -> "Diário"
                            1 -> "Alimentos"
                            else -> "Sobre"
                        },
                    )
                },
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = tab == 0,
                    onClick = { tab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Diário") },
                )
                NavigationBarItem(
                    selected = tab == 1,
                    onClick = { tab = 1 },
                    icon = { Icon(Icons.Default.Search, contentDescription = null) },
                    label = { Text("Alimentos") },
                )
                NavigationBarItem(
                    selected = tab == 2,
                    onClick = { tab = 2 },
                    icon = { Icon(Icons.Default.Info, contentDescription = null) },
                    label = { Text("Sobre") },
                )
            }
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (tab) {
                0 -> DiaryScreen(
                    date = date,
                    entries = entries,
                    isToday = date == LocalDate.now(),
                    onPreviousDay = viewModel::previousDay,
                    onNextDay = viewModel::nextDay,
                    onToday = viewModel::goToToday,
                    onDelete = viewModel::deleteEntry,
                    onAdd = { tab = 1 },
                )
                1 -> FoodSearchScreen(
                    query = query,
                    results = results,
                    onQueryChange = viewModel::onQueryChange,
                    onAdd = { food, grams, meal ->
                        viewModel.addEntry(food, grams, meal)
                        tab = 0
                    },
                )
                else -> AboutScreen(packageInfo = packageInfo, importError = importError)
            }
        }
    }
}
