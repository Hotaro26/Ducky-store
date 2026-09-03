package com.hotaro.duckystore.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.hotaro.duckystore.AppDetail
import com.hotaro.duckystore.data.AppDownloader
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(detail.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = detail.name, style = MaterialTheme.typography.headlineLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Size: ${detail.size}", style = MaterialTheme.typography.bodyLarge)
            
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (downloadStatus == "Downloaded" && downloadId != null) {
                        downloader.installApk(downloadId!!)
                    } else if (downloadId == null) {
                        val fileName = "${detail.name.replace(" ", "_")}.apk"
                        downloadId = downloader.downloadApk(detail.downloadUrl, fileName)
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
                },
                enabled = downloadStatus == "Download" || downloadStatus == "Download Failed" || downloadStatus == "Downloaded"
            ) {
                Text(if (downloadStatus == "Downloaded") "Install" else downloadStatus)
            }

            Spacer(modifier = Modifier.height(16.dp))

            progress?.let { p ->
                LinearProgressIndicator(
                    progress = { p },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("${(p * 100).toInt()}%")
            }
        }
    }
}
