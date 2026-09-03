import re

with open("app/src/main/java/com/hotaro/duckystore/ui/main/MainScreen.kt", "r") as f:
    content = f.read()

# Add R import
content = content.replace("import com.hotaro.duckystore.data.GithubAsset", "import com.hotaro.duckystore.data.GithubAsset\nimport com.hotaro.duckystore.R\nimport androidx.compose.ui.res.painterResource\nimport androidx.compose.foundation.shape.CircleShape\nimport androidx.compose.foundation.shape.RoundedCornerShape\nimport androidx.compose.foundation.background\nimport androidx.compose.ui.graphics.Color")

# Replace TopAppBar
old_top_bar = """                TopAppBar(
                    title = { Text("Ducky Store") },
                    actions = {
                        IconButton(onClick = { isSearchActive = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                    }
                )"""

new_top_bar = """                TopAppBar(
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
                )"""
content = content.replace(old_top_bar, new_top_bar)

# Remove M3 SearchBar overlay
start_search_bar = content.find("// Search Bar Overlay")
end_search_bar = content.find("}\n}\n\n@Composable\nfun AppItem")
if start_search_bar != -1 and end_search_bar != -1:
    content = content[:start_search_bar] + content[end_search_bar:]

# Modify PullToRefreshBox content to include the custom search bar
old_pull_content = """                    when (val s = state) {"""

new_pull_content = """                    Column(modifier = Modifier.fillMaxSize()) {
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
                                leadingIcon = { Icon(Icons.Default.PlayArrow, contentDescription = null) },
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
                                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                                    .clickable { /* Search action if needed, though list filters instantly */ },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            when (val s = state) {"""
content = content.replace(old_pull_content, new_pull_content)

# Close the new Column and Box
content = content.replace("}\n                }\n            }\n        }\n    }\n}", "}\n                        }\n                    }\n                }\n            }\n        }\n    }\n}")

# Fix imports if needed
content = content.replace("import androidx.compose.material.icons.filled.Search", "import androidx.compose.material.icons.filled.Search\nimport androidx.compose.material.icons.filled.PlayArrow")

with open("app/src/main/java/com/hotaro/duckystore/ui/main/MainScreen.kt", "w") as f:
    f.write(content)
