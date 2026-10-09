package com.tinaut1986.porkilo.ui.screens

import java.util.Locale
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.tinaut1986.porkilo.model.ProductTemplate
import com.tinaut1986.porkilo.ui.components.RatingBar
import com.tinaut1986.porkilo.util.formatQuantity
import com.tinaut1986.porkilo.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tinaut1986.porkilo.model.Product
import com.tinaut1986.porkilo.model.OfferType
import kotlin.math.roundToInt

@Composable
fun CompareScreen(
    isDarkMode: Boolean,
    products: MutableList<Product>,
    templates: List<ProductTemplate> = emptyList(),
    onAddClick: () -> Unit,
    onEditClick: (Product) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences("porkilo_prefs", android.content.Context.MODE_PRIVATE) }
    var isTutorialActive by remember { mutableStateOf(prefs.getBoolean("compare_tutorial_active", false)) }
    var tutorialStep by remember { mutableStateOf(0) }
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()

    // Auto-scroll during tutorial
    LaunchedEffect(tutorialStep) {
        if (isTutorialActive) {
            when (tutorialStep) {
                1, 3 -> listState.animateScrollToItem(0)
                2 -> listState.animateScrollToItem(1)
            }
        }
    }

    val displayProducts = if (isTutorialActive) {
        listOf(
            Product(
                id = 9991,
                name = "Producto A (3x2)",
                price = 1.5,
                quantityPerUnit = 1.0,
                unit = "ud",
                compareQuantity = 6,
                offer = com.tinaut1986.porkilo.model.Offer(
                    type = OfferType.BUY_X_PAY_Y,
                    value1 = 3.0,
                    value2 = 2.0
                )
            ),
            Product(
                id = 9992,
                name = "Producto B (Sin oferta)",
                price = 1.2,
                quantityPerUnit = 1.0,
                unit = "ud",
                compareQuantity = 6
            ),
            Product(
                id = 9993,
                name = "Producto C (-20% dto)",
                price = 1.4,
                quantityPerUnit = 1.0,
                unit = "ud",
                compareQuantity = 5,
                offer = com.tinaut1986.porkilo.model.Offer(
                    type = OfferType.PERCENTAGE_DISCOUNT,
                    value1 = 20.0
                )
            )
        )
    } else {
        products
    }

    val sortedProducts = remember(displayProducts.toList()) {
        displayProducts.sortedBy { it.pricePerBaseUnit }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
        if (displayProducts.isEmpty()) {
            Spacer(modifier = Modifier.height(60.dp))
            Icon(
                Icons.Filled.ShoppingCart,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = Color(0xFF2E7D32).copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                stringResource(R.string.compare_empty_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                stringResource(R.string.compare_empty_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onAddClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(28.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2E7D32)
                )
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.compare_add_first), fontWeight = FontWeight.Medium)
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.compare_products_count, displayProducts.size),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkMode) Color.White else Color(0xFF333333)
                )
                Text(
                    stringResource(R.string.compare_ordered_by),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            
            var productToDelete by remember { mutableStateOf<Product?>(null) }
            
            if (productToDelete != null) {
                AlertDialog(
                    onDismissRequest = { productToDelete = null },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                products.removeAll { it.id == productToDelete?.id }
                                productToDelete = null
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFF44336))
                        ) {
                            Text(stringResource(R.string.delete_confirm_button))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { productToDelete = null }) {
                            Text(stringResource(R.string.delete_cancel_button))
                        }
                    },
                    title = { Text(stringResource(R.string.delete_confirm_title)) },
                    text = { Text(stringResource(R.string.delete_confirm_message, productToDelete?.name ?: "")) },
                    shape = RoundedCornerShape(20.dp),
                    containerColor = if (isDarkMode) Color(0xFF2E2E2E) else Color.White
                )
            }

            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(sortedProducts, key = { it.id }) { product: Product ->
                    ProductCard(
                        isDarkMode = isDarkMode,
                        product = product,
                        position = sortedProducts.indexOf(product),
                        bestPricePerBaseUnit = sortedProducts.firstOrNull()?.pricePerBaseUnit ?: 0.0,
                        rating = findTemplateFor(product, templates)?.rating ?: 0f,
                        onDelete = { prod ->
                            productToDelete = prod
                        },
                        onEdit = { onEditClick(product) },
                        tutorialStep = if (isTutorialActive) tutorialStep else -1
                    )
                }
            }
        }
    }

        // Tutorial Overlay
        if (isTutorialActive) {
            val configuration = androidx.compose.ui.platform.LocalConfiguration.current
            val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
            val alignment = if (tutorialStep == 2) Alignment.TopCenter else Alignment.BottomCenter
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                contentAlignment = alignment
            ) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF2E7D32),
                        contentColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier
                        .fillMaxWidth(if (isLandscape) 0.6f else 1f)
                        .padding(top = if (tutorialStep == 2) 64.dp else 0.dp, bottom = if (tutorialStep != 2) 64.dp else 0.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                when (tutorialStep) {
                                    0 -> stringResource(R.string.tutorial_compare_welcome_title)
                                    1 -> stringResource(R.string.tutorial_compare_best_title)
                                    2 -> stringResource(R.string.tutorial_compare_diff_title)
                                    else -> stringResource(R.string.tutorial_compare_actions_title)
                                },
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = {
                                prefs.edit().putBoolean("compare_tutorial_active", false).apply()
                                isTutorialActive = false
                            }) {
                                Icon(Icons.Filled.Close, contentDescription = null, tint = Color.White.copy(alpha = 0.7f))
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            when (tutorialStep) {
                                0 -> stringResource(R.string.tutorial_compare_welcome_desc)
                                1 -> stringResource(R.string.tutorial_compare_best_desc)
                                2 -> stringResource(R.string.tutorial_compare_diff_desc)
                                else -> stringResource(R.string.tutorial_compare_actions_desc)
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextBtn(onClick = {
                                prefs.edit().putBoolean("compare_tutorial_active", false).apply()
                                isTutorialActive = false
                            }) {
                                Text(stringResource(R.string.tutorial_skip), color = Color.White.copy(alpha = 0.7f))
                            }
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Button(
                                onClick = {
                                    if (tutorialStep < 3) tutorialStep++
                                    else {
                                        prefs.edit().putBoolean("compare_tutorial_active", false).apply()
                                        isTutorialActive = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                                    contentColor = Color(0xFF2E7D32)
                                )
                            ) {
                                Text(
                                    if (tutorialStep < 3) stringResource(R.string.tutorial_next) 
                                    else stringResource(R.string.tutorial_finish),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TextBtn(onClick: () -> Unit, content: @Composable () -> Unit) {
    TextButton(onClick = onClick) { content() }
}

@Composable
fun ProductCard(
    isDarkMode: Boolean,
    product: Product,
    position: Int,
    bestPricePerBaseUnit: Double,
    rating: Float = 0f,
    onDelete: (Product) -> Unit,
    onEdit: () -> Unit,
    tutorialStep: Int = -1
) {
    val isBestOption = position == 0
    val cardBgColor = when {
        isBestOption -> if (isDarkMode) Color(0xFF1B3821) else Color(0xFFE8F5E9)
        position == 1 -> if (isDarkMode) Color(0xFF1A2E44) else Color(0xFFE3F2FD)
        else -> if (isDarkMode) Color(0xFF252525) else Color.White
    }
    
    val accentColor = when {
        isBestOption -> Color(0xFF2E7D32)
        position == 1 -> Color(0xFF1976D2)
        else -> Color(0xFF757575)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardBgColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isBestOption) 6.dp else 2.dp
        ),
        border = if (isBestOption || position == 1) 
            BorderStroke(1.5.dp, accentColor.copy(alpha = 0.5f)) 
            else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    if (isBestOption) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF4CAF50))
                                .then(if (tutorialStep == 1 && isBestOption) Modifier.border(2.dp, Color(0xFFFF9800), RoundedCornerShape(12.dp)) else Modifier)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                Icons.Filled.Star,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                stringResource(R.string.compare_best_option),
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    Text(
                        product.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkMode) Color.White else Color(0xFF333333)
                    )

                    if (rating > 0f) {
                        RatingBar(rating = rating, starSize = 16.dp)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.ShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            stringResource(R.string.product_compare_quantity) + ": ${product.effectiveCompareQuantity}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }

                    if (product.offer.type != OfferType.NONE) {
                        Surface(
                            color = Color(0xFFFF9800).copy(alpha = 0.15f),
                            contentColor = Color(0xFFE65100),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    Icons.Filled.LocalOffer, 
                                    contentDescription = null, 
                                    modifier = Modifier.size(14.dp),
                                    tint = Color(0xFFE65100)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                val offerLabel = when (product.offer.type) {
                                    OfferType.PERCENTAGE_DISCOUNT -> stringResource(R.string.offer_desc_pct, 
                                        if (product.offer.value1 % 1 == 0.0) product.offer.value1.toInt().toString() else product.offer.value1.toString())
                                    OfferType.BUY_X_PAY_Y -> stringResource(R.string.offer_desc_buy_x_pay_y, 
                                        product.offer.value1.toInt().toString(), product.offer.value2.toInt().toString())
                                    OfferType.NTH_UNIT_DISCOUNT -> stringResource(R.string.offer_desc_nth_unit, 
                                        product.offer.value1.toInt().toString(),
                                        if (product.offer.value2 % 1 == 0.0) product.offer.value2.toInt().toString() else product.offer.value2.toString())
                                    OfferType.FIXED_PRICE_FOR_X -> stringResource(R.string.offer_desc_fixed_price, 
                                        product.offer.value1.toInt().toString(), 
                                        if (product.offer.value2 % 1 == 0.0) product.offer.value2.toInt().toString() else String.format("%.2f", product.offer.value2))
                                    OfferType.EXTRA_QUANTITY -> stringResource(R.string.offer_desc_extra_qty, 
                                        if (product.offer.value1 % 1 == 0.0) product.offer.value1.toInt().toString() else product.offer.value1.toString())
                                    else -> ""
                                }

                                val finalLabel = if (product.savingPercentage > 0) {
                                    "$offerLabel (-${product.savingPercentage}%)"
                                } else {
                                    offerLabel
                                }

                                Text(
                                    finalLabel, 
                                    style = MaterialTheme.typography.labelSmall, 
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.then(if (tutorialStep == 3 && position == 0) Modifier.border(2.dp, Color(0xFFFF9800), RoundedCornerShape(8.dp)) else Modifier)
                ) {
                    Text(
                        "#${position + 1}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = accentColor
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Filled.Edit,
                            contentDescription = stringResource(R.string.compare_edit_product),
                            tint = Color(0xFF2196F3),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = { onDelete(product) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = stringResource(R.string.compare_delete_product),
                            tint = Color(0xFFF44336),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val unitRes = when(product.unit) {
                    "kg" -> R.string.unit_kg
                    "g" -> R.string.unit_g
                    "l" -> R.string.unit_l
                    "ml" -> R.string.unit_ml
                    "ud" -> R.string.unit_units
                    "pack" -> R.string.unit_pack
                    "box" -> R.string.unit_box
                    else -> R.string.unit_units
                }
                val unitName = stringResource(unitRes)

                val baseUnitRes = when(product.baseUnit) {
                    "kg" -> R.string.unit_kg
                    "l" -> R.string.unit_l
                    "g" -> R.string.unit_g
                    "ml" -> R.string.unit_ml
                    "ud" -> R.string.unit_units
                    "pack" -> R.string.unit_pack
                    "box" -> R.string.unit_box
                    else -> R.string.unit_units
                }
                val baseUnitName = stringResource(baseUnitRes)

                Column {
                    val packageText = if (product.unitsPerPackage > 1) {
                        "${product.unitsPerPackage} x ${formatQuantity(product.quantityPerUnit)} $unitName"
                    } else {
                        "${formatQuantity(product.quantityPerUnit)} $unitName"
                    }
                    val quantityText = if (product.effectiveCompareQuantity > 1 || product.unitsPerPackage > 1) {
                        "$packageText (${formatQuantity(product.totalCompareQuantity)} $unitName)"
                    } else {
                        packageText
                    }
                    Text(
                        quantityText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    Text(
                        stringResource(R.string.compare_total_price, product.effectiveCompareQuantity),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Text(
                        "€${String.format(Locale.getDefault(), "%.2f", product.totalComparePrice)}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isDarkMode) Color.White else Color(0xFF1A1A1A)
                    )
                }
                
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        stringResource(R.string.compare_price_per_unit),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Text(
                        "€${String.format(Locale.getDefault(), "%.2f", product.pricePerUnitInCompare)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = accentColor
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        stringResource(R.string.compare_price_per, baseUnitName),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Text(
                        "€${String.format(Locale.getDefault(), "%.4f", product.pricePerBaseUnit)}/$baseUnitName",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = accentColor.copy(alpha = 0.8f)
                    )
                }
            }
            
            if (position > 0 && bestPricePerBaseUnit > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                val difference = product.pricePerBaseUnit - bestPricePerBaseUnit
                val percentage = (difference / bestPricePerBaseUnit * 100).roundToInt()
                
                if (percentage > 0) {
                    Text(
                        stringResource(R.string.compare_more_expensive, percentage),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFF44336),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.then(if (tutorialStep == 2 && position == 1) Modifier.border(2.dp, Color(0xFFFF9800), RoundedCornerShape(4.dp)) else Modifier)
                    )
                }
            }
        }
    }
}

// Products keep the id of the template they came from; older ones are matched by barcode or name
private fun findTemplateFor(product: Product, templates: List<ProductTemplate>): ProductTemplate? =
    product.templateId?.let { id -> templates.find { it.id == id } }
        ?: product.barcode?.let { code -> templates.find { it.barcode == code } }
        ?: templates.find { it.name.equals(product.name, ignoreCase = true) }
