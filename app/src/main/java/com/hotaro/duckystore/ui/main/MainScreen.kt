package com.hotaro.duckystore.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hotaro.duckystore.data.GithubAsset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToDetail: (String, String, String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MainScreenViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Ducky Store") },
                    actions = {
                        IconButton(onClick = { isSearchActive = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                    }
                )
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                val isRefreshing = state is MainScreenUiState.Loading

                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = { viewModel.loadApps() },
                    modifier = Modifier.fillMaxSize()
                ) {
                    when (val s = state) {
                        is MainScreenUiState.Loading -> {
                            // Handled by PullToRefreshBox indicator, but we can keep a placeholder if empty
                            if (searchQuery.isEmpty()) {
                                // optional loading state
                            }
                        }
                        is MainScreenUiState.Success -> {
                            val filteredApps = if (searchQuery.isNotBlank()) {
                                s.data.filter { it.name.contains(searchQuery, ignoreCase = true) }
                            } else {
                                s.data
                            }

                            if (filteredApps.isEmpty()) {
                                Text(
                                    "No apps found.", 
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(filteredApps) { asset ->
                                        AppItem(asset = asset, onClick = {
                                            val sizeMb = "%.2f MB".format(asset.size / (1024.0 * 1024.0))
                                            val readableName = asset.name.replace(".apk", "").split("-universal")[0].replaceFirstChar { it.uppercase() }.replace("_", " ")
                                            onNavigateToDetail(readableName, sizeMb, asset.downloadUrl)
                                        })
                                    }
                                }
                            }
                        }
                        is MainScreenUiState.Error -> {
                            Column(
                                modifier = Modifier.align(Alignment.Center),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "Error loading apps:",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    s.throwable.localizedMessage ?: "Unknown error",
                                    color = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(onClick = { viewModel.loadApps() }) {
                                    Text("Retry")
                                }
                            }
                        }
                    }
                }
            }
        }

        // Search Bar Overlay
        AnimatedVisibility(
            visible = isSearchActive,
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            SearchBar(
                inputField = {
                    SearchBarDefaults.InputField(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        onSearch = { isSearchActive = false },
                        expanded = isSearchActive,
                        onExpandedChange = { isSearchActive = it },
                        placeholder = { Text("Search apps...") },
                        leadingIcon = {
                            IconButton(onClick = {
                                isSearchActive = false
                                searchQuery = ""
                            }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        }
                    )
                },
                expanded = isSearchActive,
                onExpandedChange = { 
                    isSearchActive = it
                    if (!it) searchQuery = ""
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                // When expanded, the SearchBar content covers the screen.
                // We show the filtered list here as well so users see results immediately.
                val s = state
                if (s is MainScreenUiState.Success) {
                    val filteredApps = if (searchQuery.isNotBlank()) {
                        s.data.filter { it.name.contains(searchQuery, ignoreCase = true) }
                    } else {
                        s.data
                    }
                    
                    if (filteredApps.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Text("No apps found.", modifier = Modifier.align(Alignment.Center))
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredApps) { asset ->
                                AppItem(asset = asset, onClick = {
                                    val sizeMb = "%.2f MB".format(asset.size / (1024.0 * 1024.0))
                                    val readableName = asset.name.replace(".apk", "").split("-universal")[0].replaceFirstChar { it.uppercase() }.replace("_", " ")
                                    onNavigateToDetail(readableName, sizeMb, asset.downloadUrl)
                                })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppItem(asset: GithubAsset, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            val readableName = asset.name
                .replace(".apk", "")
                .split("-universal")[0]
                .replaceFirstChar { it.uppercase() }
                .replace("_", " ")
                
            Text(
                text = readableName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            val sizeMb = "%.2f".format(asset.size / (1024.0 * 1024.0))
            Text(
                text = "Size: $sizeMb MB",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
