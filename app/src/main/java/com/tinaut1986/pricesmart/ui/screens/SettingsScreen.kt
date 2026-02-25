package com.tinaut1986.pricesmart.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.tinaut1986.pricesmart.R
import com.tinaut1986.pricesmart.ThemeMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    isDarkMode: Boolean,
    currentLanguage: String,
    onLanguageChange: (String) -> Unit,
    onNavigateToAdd: () -> Unit
) {
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(if (isLandscape) 16.dp else 24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(if (isLandscape) 12.dp else 16.dp)
    ) {
        Text(
            stringResource(R.string.screen_settings),
            style = if (isLandscape) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = if (isDarkMode) Color.White else Color(0xFF2E7D32)
        )
        
        if (isLandscape) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    SettingsSection(title = stringResource(R.string.settings_appearance)) {
                        SettingsThemeItem(currentMode = themeMode, onModeChange = onThemeModeChange)
                    }
                }
                Box(modifier = Modifier.weight(1f)) {
                    SettingsSection(title = stringResource(R.string.settings_language)) {
                        SettingsLanguageItem(currentLanguage = currentLanguage, onLanguageChange = onLanguageChange)
                    }
                }
            }
        } else {
            // Appearance Section
            SettingsSection(title = stringResource(R.string.settings_appearance)) {
                SettingsThemeItem(
                    currentMode = themeMode,
                    onModeChange = onThemeModeChange
                )
            }
            
            // Language Section
            SettingsSection(title = stringResource(R.string.settings_language)) {
                SettingsLanguageItem(
                    currentLanguage = currentLanguage,
                    onLanguageChange = onLanguageChange
                )
            }
        }

        // Tutorial Section
        val context = androidx.compose.ui.platform.LocalContext.current
        SettingsSection(title = stringResource(R.string.settings_tutorial)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val prefs = context.getSharedPreferences("pricesmart_prefs", android.content.Context.MODE_PRIVATE)
                        prefs.edit().putBoolean("tutorial_shown", false).apply()
                        onNavigateToAdd()
                    }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(Icons.AutoMirrored.Filled.Help, contentDescription = null, tint = Color(0xFF2E7D32))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(stringResource(R.string.settings_reset_tutorial), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        Text(stringResource(R.string.settings_reset_tutorial_desc), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
                Icon(Icons.Filled.RestartAlt, contentDescription = null, tint = Color(0xFF2E7D32))
            }
        }
        
        if (!isLandscape) Spacer(modifier = Modifier.weight(1f))
        
        val versionName = try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "1.2.0"
        } catch (e: Exception) {
            "1.2.0"
        }

        Text(
            stringResource(R.string.settings_version, versionName ?: "1.2.0"),
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(vertical = 8.dp)
        )
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2E7D32)
        )
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                content()
            }
        }
    }
}

@Composable
fun SettingsThemeItem(
    currentMode: ThemeMode,
    onModeChange: (ThemeMode) -> Unit
) {
    val modes = listOf(
        Triple(ThemeMode.SYSTEM, R.string.settings_theme_system, Icons.Filled.SettingsSuggest),
        Triple(ThemeMode.LIGHT, R.string.settings_theme_light, Icons.Filled.WbSunny),
        Triple(ThemeMode.DARK, R.string.settings_theme_dark, Icons.Filled.DarkMode)
    )
    
    Column(modifier = Modifier.padding(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Palette, contentDescription = null, tint = Color(0xFF2E7D32))
            Spacer(modifier = Modifier.width(16.dp))
            Text(stringResource(R.string.settings_theme), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            modes.forEach { (mode, resId, icon) ->
                val selected = currentMode == mode
                ThemeCard(
                    title = stringResource(resId),
                    icon = icon,
                    selected = selected,
                    onClick = { onModeChange(mode) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun ThemeCard(
    title: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(90.dp),
        shape = RoundedCornerShape(16.dp),
        color = if (selected) Color(0xFF2E7D32) else Color.Transparent,
        border = if (selected) null else BorderStroke(1.dp, Color(0xFFE0E0E0)),
        contentColor = if (selected) Color.White else Color.Gray
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon, 
                contentDescription = null, 
                modifier = Modifier.size(28.dp),
                tint = if (selected) Color.White else Color(0xFF2E7D32)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

@Composable
fun SettingsLanguageItem(
    currentLanguage: String,
    onLanguageChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val languages = listOf(
        "Español", "English", "Català", "Galego", "Euskara",
        "Français", "Português", "Deutsch", "Nederlands", "中文", "日本語"
    )
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = true }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Language, contentDescription = null, tint = Color(0xFF2E7D32))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(stringResource(R.string.settings_language), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.settings_language_desc), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
        Box {
            Text(currentLanguage, style = MaterialTheme.typography.bodyLarge, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                languages.forEach { language ->
                    DropdownMenuItem(
                        text = { Text(language) },
                        onClick = {
                            onLanguageChange(language)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
