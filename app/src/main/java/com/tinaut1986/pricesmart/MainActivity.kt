package com.tinaut1986.pricesmart

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.tinaut1986.pricesmart.model.Product
import com.tinaut1986.pricesmart.navigation.ProductScreen
import com.tinaut1986.pricesmart.ui.screens.AddEditProductScreen
import com.tinaut1986.pricesmart.ui.screens.CompareScreen
import com.tinaut1986.pricesmart.ui.screens.SettingsScreen
import com.tinaut1986.pricesmart.ui.screens.TemplatesScreen
import com.tinaut1986.pricesmart.vms.TemplateViewModel
import com.tinaut1986.pricesmart.util.ScannerUtils
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.navigation.NavType
import androidx.navigation.navArgument
import kotlinx.coroutines.launch
import com.tinaut1986.pricesmart.ui.theme.PriceSmartTheme
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.platform.LocalConfiguration
import android.content.res.Configuration
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PriceSmartTheme {
                PriceComparatorApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PriceComparatorApp() {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    val templateViewModel: TemplateViewModel = viewModel()
    val products = rememberSaveable(
        saver = listSaver(
            save = { it.toList() },
            restore = { it.toMutableStateList() }
        )
    ) { mutableStateListOf<Product>() }
    
    // Global states
    val themeModeState = rememberSaveable { mutableStateOf(ThemeMode.SYSTEM) }
    val isDarkMode = when (themeModeState.value) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    
    val languageState = rememberSaveable { 
        mutableStateOf(
            run {
                val appLocales = AppCompatDelegate.getApplicationLocales()
                val currentLocale = if (!appLocales.isEmpty) {
                    appLocales.get(0)
                } else {
                    java.util.Locale.getDefault()
                }

                when (currentLocale?.language) {
                    "en" -> "English"
                    "ca" -> "Català"
                    "gl" -> "Galego"
                    "eu" -> "Euskara"
                    "fr" -> "Français"
                    "pt" -> "Português"
                    "de" -> "Deutsch"
                    "nl" -> "Nederlands"
                    "zh" -> "中文"
                    "ja" -> "日本語"
                    "es" -> "Español"
                    else -> "Español"
                }
            }       
        )
    }

    PriceSmartTheme(darkTheme = isDarkMode) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        val configuration = LocalConfiguration.current
        val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        // Root Row handles safe areas for the side navigation
        Row(modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing) // This is the key fix for the notch!
        ) {
            if (isLandscape) {
                NavigationRail(
                    containerColor = if (isDarkMode) Color(0xFF1A1A1A) else Color(0xFFF5F5F5)
                ) {
                    listOf(ProductScreen.Compare, ProductScreen.Templates, ProductScreen.Settings).forEach { screen ->
                        NavigationRailItem(
                            icon = { 
                                Icon(
                                    screen.icon, 
                                    contentDescription = null,
                                    tint = if (currentRoute == screen.route) Color(0xFF2E7D32) else Color.Gray
                                ) 
                            },
                            label = { 
                                Text(
                                    stringResource(screen.titleRes),
                                    color = if (currentRoute == screen.route) Color(0xFF2E7D32) else Color.Gray
                                ) 
                            },
                            selected = currentRoute == screen.route,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { 
                            Text(
                                stringResource(R.string.app_name), 
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            ) 
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color(0xFF2E7D32),
                            titleContentColor = Color.White
                        )
                    )
                },
                bottomBar = {
                    if (!isLandscape) {
                        NavigationBar(
                            containerColor = if (isDarkMode) Color(0xFF1A1A1A) else Color(0xFFF5F5F5)
                        ) {
                            listOf(ProductScreen.Compare, ProductScreen.Templates, ProductScreen.Settings).forEach { screen ->
                                NavigationBarItem(
                                    icon = { 
                                        Icon(
                                            screen.icon, 
                                            contentDescription = null,
                                            tint = if (currentRoute == screen.route) Color(0xFF2E7D32) else Color.Gray
                                        ) 
                                    },
                                    label = { 
                                        Text(
                                            stringResource(screen.titleRes),
                                            color = if (currentRoute == screen.route) Color(0xFF2E7D32) else Color.Gray
                                        ) 
                                    },
                                    selected = currentRoute == screen.route,
                                    onClick = {
                                        navController.navigate(screen.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                            }
                        }
                    }
                },
                floatingActionButton = {
                    if (currentRoute == ProductScreen.Compare.route) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            SmallFloatingActionButton(
                                onClick = {
                                    ScannerUtils.startScan(
                                        context = context,
                                        onSuccess = { scannedBarcode ->
                                            scope.launch {
                                                val template = templateViewModel.getTemplateByBarcode(scannedBarcode)
                                                if (template != null) {
                                                    // Load template into add screen
                                                    navController.navigate(ProductScreen.Add.createRoute(scannedBarcode))
                                                } else {
                                                    // No template, just go with barcode
                                                    navController.navigate(ProductScreen.Add.createRoute(scannedBarcode))
                                                }
                                            }
                                        }
                                    )
                                },
                                containerColor = if (isDarkMode) Color(0xFF388E3C) else Color(0xFFE8F5E9),
                                contentColor = if (isDarkMode) Color.White else Color(0xFF2E7D32),
                                shape = androidx.compose.foundation.shape.CircleShape
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QrCodeScanner,
                                    contentDescription = stringResource(R.string.barcode_scan)
                                )
                            }
                            
                            FloatingActionButton(
                                onClick = {
                                    navController.navigate(ProductScreen.Add.createRoute(null))
                                },
                                containerColor = Color(0xFF2E7D32),
                                contentColor = Color.White,
                                shape = androidx.compose.foundation.shape.CircleShape
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = stringResource(R.string.add_product_title)
                                )
                            }
                        }
                    }
                },
                // We use WindowInsets.none() because we already handle safeDrawing in the outer Row
                contentWindowInsets = WindowInsets(0, 0, 0, 0)
            ) { padding ->
                Box(modifier = Modifier.padding(padding)) {
                    NavHost(
                        navController = navController, 
                        startDestination = ProductScreen.Compare.route,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        composable(ProductScreen.Compare.route) { 
                            val templates by templateViewModel.allTemplates.collectAsState()
                            CompareScreen(
                                isDarkMode = isDarkMode,
                                products = products,
                                templates = templates,
                                onAddClick = { 
                                    navController.navigate(ProductScreen.Add.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                onEditClick = { product -> 
                                    navController.navigate(ProductScreen.Edit.createRoute(product.id))
                                }
                            ) 
                        }
                        composable(
                            route = ProductScreen.Add.route,
                            arguments = listOf(
                                navArgument("barcode") { 
                                    type = NavType.StringType
                                    nullable = true
                                    defaultValue = null
                                },
                                navArgument("templateId") {
                                    type = NavType.LongType
                                    defaultValue = -1L
                                }
                            )
                        ) { backStackEntry ->
                            val barcode = backStackEntry.arguments?.getString("barcode")
                            val templateId = backStackEntry.arguments?.getLong("templateId") ?: -1L
                            AddEditProductScreen(
                                isDarkMode = isDarkMode,
                                initialBarcode = if (barcode.isNullOrBlank()) null else barcode,
                                initialTemplateId = if (templateId == -1L) null else templateId,
                                templateViewModel = templateViewModel,
                                onProductAction = { product ->
                                    products.add(product)
                                    navController.navigate(ProductScreen.Compare.route) {
                                        popUpTo(ProductScreen.Compare.route) { inclusive = true }
                                    }
                                },
                                onTutorialFinish = {
                                    navController.navigate(ProductScreen.Compare.route) {
                                        popUpTo(ProductScreen.Compare.route) { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable(ProductScreen.Templates.route) {
                            TemplatesScreen(
                                isDarkMode = isDarkMode,
                                viewModel = templateViewModel,
                                onTemplateSelected = { template ->
                                    navController.navigate(ProductScreen.Add.createRoute(template.barcode, template.id))
                                },
                                onEditTemplate = { template ->
                                    navController.navigate(ProductScreen.EditTemplate.createRoute(template.id))
                                },
                                onAddTemplate = {
                                    navController.navigate(ProductScreen.AddTemplate.createRoute(null))
                                }
                            )
                        }
                        composable(
                            route = ProductScreen.AddTemplate.route,
                            arguments = listOf(
                                navArgument("barcode") {
                                    type = NavType.StringType
                                    nullable = true
                                    defaultValue = null
                                }
                            )
                        ) { backStackEntry ->
                            val barcode = backStackEntry.arguments?.getString("barcode")
                            AddEditProductScreen(
                                isDarkMode = isDarkMode,
                                initialBarcode = barcode,
                                templateViewModel = templateViewModel,
                                isTemplateMode = true,
                                onProductAction = {},
                                onTemplateAction = {
                                    navController.popBackStack()
                                }
                            )
                        }
                        composable(
                            route = ProductScreen.EditTemplate.route,
                            arguments = listOf(
                                navArgument("templateId") { type = NavType.LongType }
                            )
                        ) { backStackEntry ->
                            val templateId = backStackEntry.arguments?.getLong("templateId")
                            AddEditProductScreen(
                                isDarkMode = isDarkMode,
                                templateViewModel = templateViewModel,
                                isTemplateMode = true,
                                existingTemplateId = templateId,
                                onProductAction = {},
                                onTemplateAction = {
                                    navController.popBackStack()
                                }
                            )
                        }
                        composable(ProductScreen.Settings.route) {
                            SettingsScreen(
                                themeMode = themeModeState.value,
                                onThemeModeChange = { themeModeState.value = it },
                                isDarkMode = isDarkMode,
                                currentLanguage = languageState.value,
                                onLanguageChange = { newLanguage ->
                                    languageState.value = newLanguage
                                    val localeTag = when (newLanguage) {
                                        "English" -> "en"
                                        "Català" -> "ca"
                                        "Galego" -> "gl"
                                        "Euskara" -> "eu"
                                        "Français" -> "fr"
                                        "Português" -> "pt"
                                        "Deutsch" -> "de"
                                        "Nederlands" -> "nl"
                                        "中文" -> "zh"
                                        "日本語" -> "ja"
                                        else -> "es"
                                    }
                                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(localeTag))
                                },
                                onNavigateToAdd = {
                                    navController.navigate(ProductScreen.Add.route) {
                                        popUpTo(ProductScreen.Settings.route) {
                                            inclusive = true
                                        }
                                    }
                                }
                            )
                        }
                        composable(ProductScreen.Edit.route) { backStackEntry ->
                            val productId = backStackEntry.arguments?.getString("productId")?.toLongOrNull()
                            val product = products.find { it.id == productId }
                            if (product != null) {
                                AddEditProductScreen(
                                    isDarkMode = isDarkMode,
                                    existingProduct = product,
                                    onProductAction = { updatedProduct ->
                                        val index = products.indexOfFirst { it.id == updatedProduct.id }
                                        if (index != -1) {
                                            products[index] = updatedProduct
                                        }
                                        navController.popBackStack()
                                    },
                                    onTutorialFinish = {
                                        navController.navigate(ProductScreen.Compare.route) {
                                            popUpTo(ProductScreen.Compare.route) { inclusive = true }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
