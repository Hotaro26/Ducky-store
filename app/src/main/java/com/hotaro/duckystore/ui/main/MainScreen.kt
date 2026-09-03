package com.hotaro.duckystore.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.util.lerp
import kotlin.math.absoluteValue
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.ui.semantics.Role
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Android
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.hotaro.duckystore.utils.PackageUtils
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hotaro.duckystore.R
import com.hotaro.duckystore.data.GithubAsset


enum class SortMode(val title: String) {
    RECOMMENDED("Recommended"),
    ALPHABETICAL("Alphabetical"),
    POPULAR("Popular")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToDetail: (com.hotaro.duckystore.AppDetail) -> Unit,
    onNavigateToBox: (com.hotaro.duckystore.BoxDetail) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: MainScreenViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var sortMode by remember { mutableStateOf(SortMode.RECOMMENDED) }
    var showSortSheet by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showSortSheet = true },
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(Icons.Default.Sort, contentDescription = "Sort Apps")
                }
            },
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_launcher_monochrome),
                                contentDescription = "Ducky Store Icon",
                                modifier = Modifier.size(36.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Ducky Store", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
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
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Custom Search Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Search apps...") },
                                leadingIcon = { Icon(Icons.Default.Android, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp),
                                shape = RoundedCornerShape(28.dp),
                                colors = TextFieldDefaults.colors(
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent,
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ),
                                singleLine = true
                            )
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape)
                                    .clickable { /* Handled automatically by list filtering */ },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSecondaryContainer)
                            }
                        }

                        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            when (val s = state) {
                                is MainScreenUiState.Loading -> {
                                    // Handled by PullToRefreshBox indicator, but we can keep a placeholder if empty
                                }
                                is MainScreenUiState.Success -> {
                                    val baseFiltered = if (searchQuery.isNotBlank()) {
                                        s.data.filter { it.baseName.contains(searchQuery, ignoreCase = true) }
                                    } else {
                                        s.data
                                    }
                                    
                                    val filteredGroups = remember(baseFiltered, sortMode) {
                                        when (sortMode) {
                                            SortMode.RECOMMENDED -> baseFiltered.sortedWith(compareByDescending<com.hotaro.duckystore.ui.main.AppGroup> { it.metadata?.icon?.isNotEmpty() == true }.thenByDescending { (it.metadata?.screenshots?.size ?: 0) + it.variants.size })
                                            SortMode.ALPHABETICAL -> baseFiltered.sortedBy { it.metadata?.name?.lowercase() ?: it.baseName.lowercase() }
                                            SortMode.POPULAR -> baseFiltered.sortedByDescending { (it.metadata?.screenshots?.size ?: 0) + it.variants.size }
                                        }
                                    }

                                    if (filteredGroups.isEmpty()) {
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
                                            if (searchQuery.isBlank() && s.boxes.isNotEmpty()) {
                                                item {
                                                    val pagerState = rememberPagerState(pageCount = { s.boxes.size })
                                                    HorizontalPager(
                                                        state = pagerState,
                                                        modifier = Modifier.fillMaxWidth().height(160.dp),
                                                        contentPadding = PaddingValues(horizontal = 32.dp),
                                                        pageSpacing = 16.dp
                                                    ) { page ->
                                                        val box = s.boxes[page]
                                                        Card(
                                                            shape = RoundedCornerShape(32.dp),
                                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                                                            onClick = { onNavigateToBox(com.hotaro.duckystore.BoxDetail(box.title, box.appIds)) },
                                                            modifier = Modifier
                                                                .fillMaxSize()
                                                                .graphicsLayer {
                                                                    val pageOffset = (
                                                                        (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                                                                    ).absoluteValue
                                                                    
                                                                    val scale = lerp(
                                                                        start = 0.85f,
                                                                        stop = 1f,
                                                                        fraction = 1f - pageOffset.coerceIn(0f, 1f)
                                                                    )
                                                                    val alphaLerp = lerp(
                                                                        start = 0.5f,
                                                                        stop = 1f,
                                                                        fraction = 1f - pageOffset.coerceIn(0f, 1f)
                                                                    )
                                                                    scaleX = scale
                                                                    scaleY = scale
                                                                    alpha = alphaLerp
                                                                }
                                                        ) {
                                                            Box(
                                                                modifier = Modifier.fillMaxSize(),
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                PlayfulScatteredIcons(seed = box.title)
                                                                Text(
                                                                    text = box.title,
                                                                    style = MaterialTheme.typography.headlineMedium,
                                                                    fontWeight = FontWeight.Black,
                                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            
                                            items(filteredGroups) { group ->
                                                AppItem(group = group, onClick = {
                                                    val variants = group.variants.map { asset ->
                                                        val sizeMb = "%.2f MB".format(asset.size / (1024.0 * 1024.0))
                                                        com.hotaro.duckystore.Variant(asset.name, sizeMb, asset.downloadUrl)
                                                    }
                                                    onNavigateToDetail(com.hotaro.duckystore.AppDetail(group.originalId, group.baseName, variants))
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
            }
        } // End of Scaffold content
    }

    if (showSortSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSortSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .padding(bottom = 32.dp)
                    .selectableGroup()
            ) {
                Text(
                    text = "Sort Apps By",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp, start = 8.dp)
                )
                SortMode.entries.forEach { mode ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .selectable(
                                selected = (mode == sortMode),
                                onClick = {
                                    sortMode = mode
                                    showSortSheet = false
                                },
                                role = Role.RadioButton
                            )
                            .padding(horizontal = 8.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (mode == sortMode),
                            onClick = null 
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(text = mode.title, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }
}

@Composable
fun AppItem(group: AppGroup, onClick: () -> Unit) {
    val context = LocalContext.current
    var isInstalled by remember { mutableStateOf(false) }
    var installedVersion by remember { mutableStateOf<String?>(null) }
    var remoteVersion by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(group.metadata?.packageName, group.variants) {
        group.metadata?.packageName?.let {
            isInstalled = PackageUtils.isPackageInstalled(context, it)
            if (isInstalled) {
                installedVersion = PackageUtils.getInstalledVersion(context, it)
                val firstVariantName = group.variants.firstOrNull()?.name ?: ""
                val versionMatch = Regex("-v([\\d\\.]+)").find(firstVariantName)
                remoteVersion = versionMatch?.groupValues?.get(1) ?: group.metadata.version.takeIf { it.isNotBlank() && it != "Latest" }
            }
        }
    }

    val hasUpdate = isInstalled && installedVersion != null && remoteVersion != null && installedVersion != remoteVersion

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        val readableName = group.metadata?.name ?: group.baseName
        val firstLetter = readableName.firstOrNull()?.toString()?.uppercase() ?: "A"

        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (group.metadata?.icon?.isNotEmpty() == true) {
                AsyncImage(
                    model = group.metadata.icon,
                    contentDescription = readableName,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = firstLetter,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = readableName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (hasUpdate) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.errorContainer
                        ) {
                            Text(
                                text = "UPDATE",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    if (group.metadata?.category?.isNotEmpty() == true) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = group.metadata.category,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    } else {
                        val variantsCount = group.variants.size
                        Text(
                            text = "$variantsCount variants",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            if (isInstalled && !hasUpdate) {
                androidx.compose.material3.IconButton(
                    onClick = { group.metadata?.packageName?.let { PackageUtils.openApp(context, it) } },
                    modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp))
                ) {
                    Icon(Icons.Default.OpenInNew, contentDescription = "Open", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            } else if (hasUpdate) {
                androidx.compose.material3.IconButton(
                    onClick = onClick,
                    modifier = Modifier.background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                ) {
                    Icon(Icons.Default.Update, contentDescription = "Update", tint = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    }
}
