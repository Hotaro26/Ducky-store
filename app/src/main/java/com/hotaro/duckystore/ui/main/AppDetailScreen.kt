package com.hotaro.duckystore.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.runtime.LaunchedEffect
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.lazy.LazyRow
import com.hotaro.duckystore.data.AppMetadata
import com.hotaro.duckystore.data.AppRepository
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hotaro.duckystore.AppDetail
import com.hotaro.duckystore.data.AppDownloader
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.runtime.DisposableEffect
import com.hotaro.duckystore.utils.PackageUtils
import androidx.compose.material.icons.filled.Delete
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDetailScreen(
    detail: AppDetail,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val downloader = remember { AppDownloader(context) }
    val coroutineScope = rememberCoroutineScope()

    var downloadId by remember { mutableStateOf<Long?>(null) }
    var progress by remember { mutableStateOf<Float?>(null) }
    var downloadStatus by remember { mutableStateOf("Download") }
    var showVariantsSheet by remember { mutableStateOf(false) }
    var selectedVariant by remember { mutableStateOf(detail.variants.firstOrNull()) }
    var metadata by remember { mutableStateOf<AppMetadata?>(null) }
    
    val lifecycleOwner = LocalLifecycleOwner.current
    var isInstalled by remember { mutableStateOf(false) }
    
    DisposableEffect(lifecycleOwner, metadata?.packageName) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                metadata?.packageName?.let {
                    isInstalled = PackageUtils.isPackageInstalled(context, it)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        
        metadata?.packageName?.let {
            isInstalled = PackageUtils.isPackageInstalled(context, it)
        }
        
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(isInstalled) {
        if (isInstalled && downloadId != null) {
            kotlinx.coroutines.delay(10000)
            val downloadManager = context.getSystemService(android.content.Context.DOWNLOAD_SERVICE) as android.app.DownloadManager
            downloadManager.remove(downloadId!!)
            downloadId = null
            downloadStatus = "Download" // Reset for future updates
        }
    }
    
    LaunchedEffect(detail.originalId) {
        val repo = AppRepository()
        metadata = repo.getAppMetadata(detail.originalId).getOrNull()
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO */ }) {
                        Icon(Icons.Default.FavoriteBorder, contentDescription = "Favorite")
                    }
                    IconButton(onClick = { /* TODO */ }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                    IconButton(onClick = { /* TODO */ }) {
                        Icon(Icons.Default.Flag, contentDescription = "Report")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                color = Color.Transparent
            ) {
                Column {
                    progress?.let { p ->
                        LinearProgressIndicator(
                            progress = { p },
                            modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    Button(
                        onClick = {
                            if (downloadStatus == "Downloaded" && downloadId != null) {
                                downloader.installApk(downloadId!!)
                            } else if (downloadId == null || downloadStatus == "Download Failed") {
                                if (detail.variants.size > 1) {
                                    showVariantsSheet = true
                                } else {
                                    selectedVariant = detail.variants.firstOrNull()
                                    selectedVariant?.let { variant ->
                                        val fileName = "${detail.name.replace(" ", "_")}.apk"
                                        downloadId = downloader.downloadApk(variant.downloadUrl, fileName)
                                        downloadStatus = "Downloading..."
                                        
                                        coroutineScope.launch {
                                            downloader.getDownloadProgressFlow(downloadId!!).collect { downloadProgress ->
                                                progress = downloadProgress.progressPercent / 100f
                                                if (downloadProgress.status == android.app.DownloadManager.STATUS_SUCCESSFUL) {
                                                    downloadStatus = "Downloaded"
                                                    progress = 1f
                                                    downloader.installApk(downloadId!!)
                                                } else if (downloadProgress.status == android.app.DownloadManager.STATUS_FAILED) {
                                                    downloadStatus = "Download Failed"
                                                    progress = null
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        enabled = downloadStatus == "Download" || downloadStatus == "Download Failed" || downloadStatus == "Downloaded"
                    ) {
                        if (downloadStatus == "Download" || downloadStatus == "Download Failed") {
                            Icon(Icons.Default.Download, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                        }
                        Text(
                            text = if (downloadStatus == "Downloaded") "Install" else downloadStatus,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // App Icon (First Word)
                val firstWord = detail.name.split(" ").firstOrNull() ?: "App"
                if (metadata?.icon?.isNotEmpty() == true) {
                    AsyncImage(
                        model = metadata!!.icon,
                        contentDescription = metadata?.name ?: detail.name,
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = firstWord,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            maxLines = 1
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(20.dp))
                
                // Title and Author
                Column {
                    Text(
                        text = detail.name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = metadata?.author?.takeIf { it.isNotBlank() } ?: "Hotaro26",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Verified",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            
            if (metadata?.description?.isNotEmpty() == true) {
                Text(
                    text = metadata!!.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Preview", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                if (metadata?.screenshots?.isNotEmpty() == true) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(metadata!!.screenshots) { screenshot ->
                            AsyncImage(
                                model = screenshot,
                                contentDescription = "Screenshot",
                                modifier = Modifier.height(300.dp).clip(RoundedCornerShape(16.dp)),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        repeat(3) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(0.45f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            )
                        }
                    }
                }
            }
            
            // Tags
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Text("PRODUCTIVITY", modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

            }
            
            // Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Android, contentDescription = null, tint = Color(0xFF4CAF50))
                        Text("ANDROID", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        val firstVariantName = detail.variants.firstOrNull()?.name ?: ""
                        val versionMatch = Regex("-v([\\d\\.]+)").find(firstVariantName)
                        val versionStr = versionMatch?.groupValues?.get(1) ?: "1.0"
                        Text(versionStr, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text("VERSION", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        val displaySize = selectedVariant?.size ?: detail.variants.firstOrNull()?.size ?: "0 MB"
                        Text(displaySize, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text("SIZE", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            
            // Repository Button
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                onClick = { /* TODO */ }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Code, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Show Repository", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
            }
            

            
            // Bottom padding for the FAB
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    if (showVariantsSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showVariantsSheet = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Select Variant to Download", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(detail.variants) { variant ->
                        Card(
                            onClick = {
                                selectedVariant = variant
                                showVariantsSheet = false
                                val fileName = "${variant.name.replace(" ", "_")}.apk"
                                downloadId = downloader.downloadApk(variant.downloadUrl, fileName)
                                downloadStatus = "Downloading..."
                                coroutineScope.launch {
                                    downloader.getDownloadProgressFlow(downloadId!!).collect { downloadProgress ->
                                        progress = downloadProgress.progressPercent / 100f
                                        if (downloadProgress.status == android.app.DownloadManager.STATUS_SUCCESSFUL) {
                                            downloadStatus = "Downloaded"
                                            progress = 1f
                                            downloader.installApk(downloadId!!)
                                        } else if (downloadProgress.status == android.app.DownloadManager.STATUS_FAILED) {
                                            downloadStatus = "Download Failed"
                                            progress = null
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(variant.name, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Size: ${variant.size}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
