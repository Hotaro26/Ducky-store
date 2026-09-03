import re

with open("app/src/main/java/com/hotaro/duckystore/ui/main/SettingsScreen.kt", "r") as f:
    content = f.read()

# Add themeManager param to SettingsScreen
content = content.replace("fun SettingsScreen(modifier: Modifier = Modifier)", "fun SettingsScreen(themeManager: com.hotaro.duckystore.theme.ThemeManager, modifier: Modifier = Modifier)")

# Add Customisation navigation branch
content = content.replace(
    """"Support" -> SupportScreen(onBack = { currentScreen = "Main" }, modifier = modifier)""",
    """"Support" -> SupportScreen(onBack = { currentScreen = "Main" }, modifier = modifier)
            "Customisation" -> CustomisationScreen(themeManager = themeManager, onBack = { currentScreen = "Main" }, modifier = modifier)"""
)

# Add Customisation button in SettingsMainList
custom_btn = """
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
"""
content = content.replace(
    """SegmentCard(
                onClick = { onNavigate("Developer") },
                defaultTopStart = 24.dp,
                defaultTopEnd = 24.dp,
                defaultBottomStart = 4.dp,
                defaultBottomEnd = 4.dp""",
    custom_btn
)

# Add the CustomisationScreen Composable at the end
custom_screen = """

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
"""
content += custom_screen

with open("app/src/main/java/com/hotaro/duckystore/ui/main/SettingsScreen.kt", "w") as f:
    f.write(content)
