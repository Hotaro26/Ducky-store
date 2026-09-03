package com.hotaro.duckystore.ui.main

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SettingsScreen(themeManager: com.hotaro.duckystore.theme.ThemeManager, modifier: Modifier = Modifier) {
    var currentScreen by remember { mutableStateOf("Main") }

    BackHandler(enabled = currentScreen != "Main") {
        currentScreen = "Main"
    }

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = {
            if (targetState != "Main") {
                (slideInHorizontally(animationSpec = spring(stiffness = Spring.StiffnessMedium)) { it } + fadeIn()).togetherWith(
                    slideOutHorizontally(animationSpec = spring(stiffness = Spring.StiffnessMedium)) { -it } + fadeOut()
                )
            } else {
                (slideInHorizontally(animationSpec = spring(stiffness = Spring.StiffnessMedium)) { -it } + fadeIn()).togetherWith(
                    slideOutHorizontally(animationSpec = spring(stiffness = Spring.StiffnessMedium)) { it } + fadeOut()
                )
            }.using(SizeTransform(clip = false))
        },
        label = "settings_nav"
    ) { screen ->
        when (screen) {
            "Main" -> SettingsMainList(onNavigate = { currentScreen = it }, modifier = modifier)
            "Developer" -> DeveloperScreen(onBack = { currentScreen = "Main" }, modifier = modifier)
            "Support" -> SupportScreen(onBack = { currentScreen = "Main" }, modifier = modifier)
            "Customisation" -> CustomisationScreen(themeManager = themeManager, onBack = { currentScreen = "Main" }, modifier = modifier)
        }
    }
}

@Composable
fun SettingsMainList(
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
            .padding(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            
            SegmentCard(
                onClick = { onNavigate("Customisation") },
                defaultTopStart = 24.dp,
                defaultTopEnd = 24.dp,
                defaultBottomStart = 4.dp,
                defaultBottomEnd = 4.dp
            ) {
                SettingsListItem(
                    icon = Icons.Default.Palette,
                    iconColor = Color(0xFF6B4C8B),
                    title = "Customisation",
                    subtitle = "App Theme, Dark Mode"
                )
            }

            SegmentCard(
                onClick = { onNavigate("Developer") },
                defaultTopStart = 4.dp,
                defaultTopEnd = 4.dp,
                defaultBottomStart = 4.dp,
                defaultBottomEnd = 4.dp

            ) {
                SettingsListItem(
                    icon = Icons.Default.Code,
                    iconColor = Color(0xFF4C6B8B),
                    title = "Developer",
                    subtitle = "App Info, Platforms, Licenses"
                )
            }

            SegmentCard(
                onClick = { onNavigate("Support") },
                defaultTopStart = 4.dp,
                defaultTopEnd = 4.dp,
                defaultBottomStart = 24.dp,
                defaultBottomEnd = 24.dp
            ) {
                SettingsListItem(
                    icon = Icons.Default.Favorite,
                    iconColor = Color(0xFF7A4F5C),
                    title = "Support",
                    subtitle = "Help keep Ducky Store alive"
                )
            }
        }
    }
}

@Composable
fun SegmentCard(
    onClick: () -> Unit,
    defaultTopStart: Dp,
    defaultTopEnd: Dp,
    defaultBottomStart: Dp,
    defaultBottomEnd: Dp,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(
            topStart = defaultTopStart,
            topEnd = defaultTopEnd,
            bottomStart = defaultBottomStart,
            bottomEnd = defaultBottomEnd
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        content()
    }
}

@Composable
fun SettingsListItem(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(iconColor.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconColor)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun DeveloperScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val profileImageRequest = remember(context) {
        coil.request.ImageRequest.Builder(context)
            .data("https://github.com/Hotaro26.png")
            .crossfade(true)
            .build()
    }

    var autoCheckUpdates by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
            .padding(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
            ) { Icon(Icons.Default.ArrowBack, "Back") }
            Spacer(Modifier.width(16.dp))
            Text("Developer", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = profileImageRequest,
                            contentDescription = "Profile Picture",
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Column {
                        Text("Hotaro", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text("Building crisp, fast, and secure apps", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Hotaro26"))) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Icon(Icons.Default.Code, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("GitHub")
                    }
                    OutlinedButton(
                        onClick = { Toast.makeText(context, "Discord: oi.hotaro", Toast.LENGTH_LONG).show() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Icon(Icons.Default.Chat, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Discord")
                    }
                }
            }
        }
        
        Spacer(Modifier.height(8.dp))
        Text("App Info & Updates", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 4.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.primary)
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Ducky Store v1.0", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                        Text("Powered by Jetpack Compose", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Auto Check Updates", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                        Text("Notify on launch when new release is out", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = autoCheckUpdates,
                        onCheckedChange = { autoCheckUpdates = it }
                    )
                }
            }
        }
    }
}

@Composable
fun SupportScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val myUpiId = "9693703723@fam"
    var showKofiDialog by remember { mutableStateOf(false) }
    
    if (showKofiDialog) {
        AlertDialog(
            onDismissRequest = { showKofiDialog = false },
            title = { Text("Support via Coffee", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedCard(
                        onClick = {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://ko-fi.com/oi.hotaro")))
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("ko-fi.com/oi.hotaro", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showKofiDialog = false }) { Text("Close") }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(24.dp)
        )
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
            .padding(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
            ) { Icon(Icons.Default.ArrowBack, "Back") }
            Spacer(Modifier.width(16.dp))
            Text("Support", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }
        
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
        ) {
            Column(Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Favorite, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(12.dp))
                    Text("Support Ducky Store", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(8.dp))
                Text("Help keep Ducky Store alive and fast",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = {
                            val payeeName = "Hotaro"
                            val transactionNote = "Support Ducky Store"
                            val uri = Uri.parse("upi://pay").buildUpon()
                                .appendQueryParameter("pa", myUpiId)
                                .appendQueryParameter("pn", payeeName)
                                .appendQueryParameter("tn", transactionNote)
                                .appendQueryParameter("am", "0")
                                .appendQueryParameter("cu", "INR")
                                .build()
                            val intent = Intent(Intent.ACTION_VIEW).apply { data = uri }
                            val chooser = Intent.createChooser(intent, "Pay with...")
                            try {
                                context.startActivity(chooser)
                            } catch (e: Exception) {
                                Toast.makeText(context, "No UPI app found", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.AttachMoney, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("UPI")
                    }
                    Button(
                        onClick = { showKofiDialog = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color(0xFFFFB6C1))
                    ) {
                        Icon(Icons.Default.LocalCafe, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Coffee")
                    }
                }
            }
        }
        
        Spacer(Modifier.height(8.dp))
        
        OutlinedCard(
            onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Hotaro26/Ducky-store"))) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                Column {
                    Text("Star the project", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Support on GitHub", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}


@Composable
fun CustomisationScreen(
    themeManager: com.hotaro.duckystore.theme.ThemeManager,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val themeMode by themeManager.themeMode.collectAsState()
    val appTheme by themeManager.appTheme.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
            .padding(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
            ) { Icon(Icons.Default.ArrowBack, "Back") }
            Spacer(Modifier.width(16.dp))
            Text("Customisation", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }
        
        Spacer(Modifier.height(8.dp))
        
        Text("Theme Mode", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 4.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    val options = listOf("System", "Light", "Dark")
                    options.forEachIndexed { index, label ->
                        val isSelected = themeMode == index
                        val shape = RoundedCornerShape(
                            topStart = if (index == 0) 100.dp else 8.dp,
                            bottomStart = if (index == 0) 100.dp else 8.dp,
                            topEnd = if (index == options.lastIndex) 100.dp else 8.dp,
                            bottomEnd = if (index == options.lastIndex) 100.dp else 8.dp
                        )
                        val colors = if (isSelected) {
                            ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.secondary,
                                contentColor = MaterialTheme.colorScheme.onSecondary
                            )
                        } else {
                            ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Button(
                            onClick = { themeManager.setThemeMode(index) },
                            shape = shape,
                            colors = colors,
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            Text(label, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                        }
                    }
                }
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        Text("Color Scheme", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 4.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val themes = com.hotaro.duckystore.theme.AppTheme.values().toList()
                themes.chunked(3).forEach { rowThemes ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        rowThemes.forEachIndexed { index, theme ->
                            val isSelected = appTheme == theme
                            val shape = RoundedCornerShape(
                                topStart = if (index == 0) 100.dp else 8.dp,
                                bottomStart = if (index == 0) 100.dp else 8.dp,
                                topEnd = if (index == rowThemes.lastIndex) 100.dp else 8.dp,
                                bottomEnd = if (index == rowThemes.lastIndex) 100.dp else 8.dp
                            )
                            val colors = if (isSelected) {
                                ButtonDefaults.filledTonalButtonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary,
                                    contentColor = MaterialTheme.colorScheme.onSecondary
                                )
                            } else {
                                ButtonDefaults.filledTonalButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Button(
                                onClick = { themeManager.setAppTheme(theme) },
                                shape = shape,
                                colors = colors,
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp)
                            ) {
                                Text(theme.label, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                            }
                        }
                        // Fill empty spaces if chunk is less than 3
                        if (rowThemes.size < 3) {
                            repeat(3 - rowThemes.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}
