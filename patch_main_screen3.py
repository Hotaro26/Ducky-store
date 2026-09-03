import re

with open("app/src/main/java/com/hotaro/duckystore/ui/main/MainScreen.kt", "r") as f:
    content = f.read()

# Add Android icon import
if "import androidx.compose.material.icons.filled.Android" not in content:
    content = content.replace("import androidx.compose.material.icons.filled.Search", "import androidx.compose.material.icons.filled.Search\nimport androidx.compose.material.icons.filled.Android")

# Change PlayArrow to Android in SearchBar
content = content.replace("Icon(Icons.Default.PlayArrow", "Icon(Icons.Default.Android")

# Redesign AppItem
old_app_item = """@Composable
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
}"""

new_app_item = """import androidx.compose.ui.draw.clip

@Composable
fun AppItem(asset: GithubAsset, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        val readableName = asset.name
            .replace(".apk", "")
            .split("-universal")[0]
            .replaceFirstChar { it.uppercase() }
            .replace("_", " ")
            
        val firstLetter = readableName.firstOrNull()?.toString()?.uppercase() ?: "A"

        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
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

            Spacer(modifier = Modifier.width(16.dp))

            Column {
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
}"""

# Since "import androidx.compose.ui.draw.clip" is already added to top, we can just replace old_app_item without it.
new_app_item = new_app_item.replace("import androidx.compose.ui.draw.clip\n\n", "")
if "import androidx.compose.ui.draw.clip" not in content:
    content = content.replace("import androidx.compose.ui.graphics.Color", "import androidx.compose.ui.graphics.Color\nimport androidx.compose.ui.draw.clip")

content = content.replace(old_app_item, new_app_item)

with open("app/src/main/java/com/hotaro/duckystore/ui/main/MainScreen.kt", "w") as f:
    f.write(content)
