package com.hotaro.duckystore.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hotaro.duckystore.AppDetail
import com.hotaro.duckystore.BoxDetail
import com.hotaro.duckystore.Variant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoxDetailScreen(
    detail: BoxDetail,
    onBack: () -> Unit,
    onNavigateToDetail: (AppDetail) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MainScreenViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(detail.title, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        if (state is MainScreenUiState.Success) {
            val allApps = (state as MainScreenUiState.Success).data
            val appsInBox = allApps.filter { detail.appIds.contains(it.originalId) }
            
            LazyColumn(
                contentPadding = PaddingValues(top = innerPadding.calculateTopPadding(), bottom = innerPadding.calculateBottomPadding() + 80.dp, start = 16.dp, end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(appsInBox) { group ->
                    AppItem(group = group) {
                        val variantsList = group.variants.map {
                            Variant(name = it.name, size = "${it.size / 1048576} MB", downloadUrl = it.downloadUrl)
                        }
                        onNavigateToDetail(
                            AppDetail(
                                originalId = group.originalId,
                                name = group.metadata?.name ?: group.baseName,
                                variants = variantsList
                            )
                        )
                    }
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}
