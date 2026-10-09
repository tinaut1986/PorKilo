package com.tinaut1986.pricesmart.ui.screens

import java.util.Locale
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.tinaut1986.pricesmart.R
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tinaut1986.pricesmart.model.Product
import com.tinaut1986.pricesmart.model.Offer
import com.tinaut1986.pricesmart.model.OfferType
import com.tinaut1986.pricesmart.util.ScannerUtils
import com.tinaut1986.pricesmart.util.formatQuantity
import com.tinaut1986.pricesmart.vms.TemplateViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tinaut1986.pricesmart.model.ProductTemplate
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProductScreen(
    isDarkMode: Boolean,
    existingProduct: Product? = null,
    onProductAction: (Product) -> Unit,
    onTutorialFinish: () -> Unit = {},
    initialBarcode: String? = null,
    initialTemplateId: Long? = null,
    templateViewModel: TemplateViewModel = viewModel(),
    isTemplateMode: Boolean = false,
    existingTemplateId: Long? = null,
    onTemplateAction: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    var existingTemplate by remember { mutableStateOf<ProductTemplate?>(null) }
    
    // Load existing template if editing template OR if we selected one to use as base
    LaunchedEffect(existingTemplateId, initialTemplateId) {
        val idToLoad = existingTemplateId ?: initialTemplateId
        if (idToLoad != null && idToLoad != -1L) {
            val template = templateViewModel.getTemplateById(idToLoad)
            existingTemplate = template
        }
    }
    var barcode by remember { mutableStateOf(existingProduct?.barcode ?: initialBarcode ?: "") }
    var saveAsTemplate by remember { mutableStateOf(false) }


    var name by remember { mutableStateOf(existingProduct?.name ?: "") }
    var price by remember {
        mutableStateOf(existingProduct?.price?.let {
            String.format(
                Locale.US,
                "%.2f",
                it
            )
        } ?: "")
    }
    var unitsPerPackage by remember {
        mutableStateOf(
            existingProduct?.unitsPerPackage?.toString() ?: "1"
        )
    }
    var compareQuantity by remember {
        mutableStateOf(
            existingProduct?.compareQuantity?.toString() ?: "1"
        )
    }
    var quantityPerUnit by remember {
        mutableStateOf(existingProduct?.quantityPerUnit?.let {
            if (it % 1 == 0.0) it.toInt().toString() else it.toString()
        } ?: "")
    }
    var offerType by remember { mutableStateOf(existingProduct?.offer?.type ?: OfferType.NONE) }
    var offerValue1 by remember {
        mutableStateOf(existingProduct?.offer?.value1?.let {
            if (it != 0.0) (if (it % 1 == 0.0) it.toInt().toString() else it.toString()) else ""
        } ?: "")
    }
    var offerValue2 by remember {
        mutableStateOf(existingProduct?.offer?.value2?.let {
            if (it != 0.0) (if (it % 1 == 0.0) it.toInt().toString() else it.toString()) else ""
        } ?: "")
    }
    val unitOptions = listOf(
        "kg" to R.string.unit_kg,
        "g" to R.string.unit_g,
        "l" to R.string.unit_l,
        "ml" to R.string.unit_ml,
        "ud" to R.string.unit_units,
        "pack" to R.string.unit_pack,
        "box" to R.string.unit_box
    )
    var selectedUnitId by remember { mutableStateOf(existingProduct?.unit ?: "kg") }

    // Update state when existingTemplate is loaded
    LaunchedEffect(existingTemplate) {
        existingTemplate?.let {
            name = it.name
            barcode = it.barcode ?: ""
            unitsPerPackage = it.unitsPerPackage.toString()
            compareQuantity = it.compareQuantity.toString()
            quantityPerUnit = it.quantityPerUnit.toString()
            selectedUnitId = it.unit
        }
    }

    // Auto-load template if initialBarcode is provided (only if NOT in template mode or editing product)
    LaunchedEffect(initialBarcode) {
        if (initialBarcode != null && existingProduct == null && !isTemplateMode) {
            val template = templateViewModel.getTemplateByBarcode(initialBarcode)
            if (template != null) {
                name = template.name
                unitsPerPackage = template.unitsPerPackage.toString()
                compareQuantity = template.compareQuantity.toString()
                quantityPerUnit = template.quantityPerUnit.toString()
                selectedUnitId = template.unit
                barcode = template.barcode ?: ""
            }
        }
    }

    val isEditing = existingProduct != null || (isTemplateMode && existingTemplateId != null)
    val defaultProductName = stringResource(R.string.product_placeholder_name)

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = if (isDarkMode) Color.White else Color(0xFF1A1A1A),
        unfocusedTextColor = if (isDarkMode) Color.White else Color(0xFF1A1A1A),
        focusedBorderColor = Color(0xFF2E7D32),
        unfocusedBorderColor = if (isDarkMode) Color(0xFF424242) else Color(0xFFBDBDBD),
        focusedLabelColor = Color(0xFF2E7D32),
        unfocusedLabelColor = if (isDarkMode) Color.LightGray else Color(0xFF757575)
    )

    var showExtraOptions by remember { mutableStateOf(isEditing || name.isNotBlank() || unitsPerPackage != "1" || offerType != OfferType.NONE) }

    // Tutorial state
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember {
        context.getSharedPreferences(
            "pricesmart_prefs",
            android.content.Context.MODE_PRIVATE
        )
    }
    var tutorialStep by remember {
        mutableStateOf(
            if (prefs.getBoolean("tutorial_shown", false)) -1 else 0
        )
    }

    val compareQuantityField: @Composable (Modifier) -> Unit = { modifier ->
        OutlinedTextField(
            value = compareQuantity,
            onValueChange = { if (it.all { c -> c.isDigit() }) compareQuantity = it },
            label = { Text(stringResource(R.string.product_compare_quantity)) },
            placeholder = { Text("1") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = modifier.then(
                if (tutorialStep == 5) Modifier.border(2.dp, Color(0xFFFF9800), RoundedCornerShape(14.dp))
                else Modifier
            ),
            shape = RoundedCornerShape(14.dp),
            leadingIcon = {
                Icon(
                    Icons.Filled.ShoppingCart,
                    contentDescription = null,
                    tint = Color(0xFF2E7D32),
                    modifier = Modifier.size(18.dp)
                )
            },
            colors = textFieldColors,
            singleLine = true
        )
    }

    val scrollState = rememberScrollState()

    // Force extra options during tutorial and auto-scroll
    LaunchedEffect(tutorialStep) {
        if (tutorialStep >= 6) {
            showExtraOptions = true
        }
        
        // Dynamic scroll based on step
        when (tutorialStep) {
            in 0..4 -> scrollState.animateScrollTo(0)
            6, 7 -> scrollState.animateScrollTo(Int.MAX_VALUE)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        val configuration = androidx.compose.ui.platform.LocalConfiguration.current
        val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(if (isLandscape) 16.dp else 24.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(if (isLandscape) 12.dp else 20.dp)
        ) {
        Column {
            val titleStr = when {
                isTemplateMode && isEditing -> stringResource(R.string.template_edit_title)
                isTemplateMode -> stringResource(R.string.template_add_title)
                isEditing -> stringResource(R.string.edit_product_title)
                else -> stringResource(R.string.add_product_title)
            }
            Text(
                titleStr,
                style = if (isLandscape) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF2E7D32)
            )
        }

        // Barcode Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = barcode,
                onValueChange = { barcode = it },
                label = { Text(stringResource(R.string.barcode_label)) },
                placeholder = { Text(stringResource(R.string.barcode_placeholder)) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                colors = textFieldColors,
                singleLine = true,
                leadingIcon = {
                    Icon(
                        Icons.Default.QrCode,
                        contentDescription = null,
                        tint = Color(0xFF2E7D32)
                    )
                }
            )
            
            IconButton(
                onClick = {
                    ScannerUtils.startScan(
                        context = context,
                        onSuccess = { scannedBarcode ->
                            barcode = scannedBarcode
                            // Try to load template
                            scope.launch {
                                val template = templateViewModel.getTemplateByBarcode(scannedBarcode)
                                if (template != null) {
                                    name = template.name
                                    unitsPerPackage = template.unitsPerPackage.toString()
                                    quantityPerUnit = template.quantityPerUnit.toString()
                                    selectedUnitId = template.unit
                                }
                            }
                        },
                        onFailure = {
                            // Handle error if needed
                        }
                    )
                },
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        if (isDarkMode) Color(0xFF2E7D32).copy(alpha = 0.2f) 
                        else Color(0xFF2E7D32).copy(alpha = 0.1f),
                        RoundedCornerShape(14.dp)
                    )
            ) {
                Icon(
                    Icons.Default.QrCodeScanner,
                    contentDescription = stringResource(R.string.barcode_scan),
                    tint = Color(0xFF2E7D32)
                )
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(if (isLandscape) 10.dp else 16.dp)
        ) {
            if (isLandscape) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(stringResource(R.string.product_name)) },
                        placeholder = { Text(stringResource(R.string.product_name_placeholder)) },
                        modifier = Modifier.weight(1f).then(
                            if (tutorialStep == 0) Modifier.border(2.dp, Color(0xFFFF9800), RoundedCornerShape(14.dp))
                            else Modifier
                        ),
                        shape = RoundedCornerShape(14.dp),
                        colors = textFieldColors,
                        singleLine = true
                    )

                    if (!isTemplateMode) {
                        OutlinedTextField(
                            value = price,
                            onValueChange = {
                                if (it.all { c -> c.isDigit() || c == '.' || c == ',' }) price =
                                    it.replace(',', '.')
                            },
                            label = { Text(stringResource(R.string.product_price)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                                .then(if (tutorialStep == 1) Modifier.border(2.dp, Color(0xFFFF9800), RoundedCornerShape(14.dp)) else Modifier),
                            shape = RoundedCornerShape(14.dp),
                            leadingIcon = {
                                Icon(
                                    Icons.Filled.Euro,
                                    contentDescription = null,
                                    tint = Color(0xFF2E7D32),
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            colors = textFieldColors,
                            singleLine = true
                        )
                        compareQuantityField(Modifier.weight(0.7f))
                    }
                }
            } else {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.product_name)) },
                    placeholder = { Text(stringResource(R.string.product_name_placeholder)) },
                    modifier = Modifier.fillMaxWidth().then(
                        if (tutorialStep == 0) Modifier.border(2.dp, Color(0xFFFF9800), RoundedCornerShape(14.dp))
                        else Modifier
                    ),
                    shape = RoundedCornerShape(14.dp),
                    colors = textFieldColors,
                    singleLine = true
                )

                if (!isTemplateMode) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = price,
                            onValueChange = {
                                if (it.all { c -> c.isDigit() || c == '.' || c == ',' }) price =
                                    it.replace(',', '.')
                            },
                            label = { Text(stringResource(R.string.product_price)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                                .then(if (tutorialStep == 1) Modifier.border(2.dp, Color(0xFFFF9800), RoundedCornerShape(14.dp)) else Modifier),
                            shape = RoundedCornerShape(14.dp),
                            leadingIcon = {
                                Icon(
                                    Icons.Filled.Euro,
                                    contentDescription = null,
                                    tint = Color(0xFF2E7D32)
                                )
                            },
                            colors = textFieldColors,
                            singleLine = true
                        )
                        compareQuantityField(Modifier.weight(0.8f))
                    }
                }
            }

            // Always-visible row: units per package | quantity per unit | unit
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = unitsPerPackage,
                    onValueChange = { if (it.all { c -> c.isDigit() }) unitsPerPackage = it },
                    label = { Text(stringResource(R.string.product_units_per_package)) },
                    placeholder = { Text("1") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f).then(
                        if (tutorialStep == 4) Modifier.border(2.dp, Color(0xFFFF9800), RoundedCornerShape(14.dp))
                        else Modifier
                    ),
                    shape = RoundedCornerShape(14.dp),
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Layers,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    colors = textFieldColors,
                    singleLine = true
                )

                if (isTemplateMode) {
                    compareQuantityField(Modifier.weight(1f))
                }

                OutlinedTextField(
                    value = quantityPerUnit,
                    onValueChange = {
                        if (it.all { c -> c.isDigit() || c == '.' || c == ',' }) quantityPerUnit =
                            it.replace(',', '.')
                    },
                    label = { Text(stringResource(R.string.product_quantity_per_unit)) },
                    placeholder = { Text(stringResource(R.string.product_quantity_placeholder)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1.3f).then(
                        if (tutorialStep == 2) Modifier.border(2.dp, Color(0xFFFF9800), RoundedCornerShape(14.dp))
                        else Modifier
                    ),
                    shape = RoundedCornerShape(14.dp),
                    colors = textFieldColors,
                    singleLine = true
                )

                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.weight(1f).then(
                        if (tutorialStep == 3) Modifier.border(2.dp, Color(0xFFFF9800), RoundedCornerShape(14.dp))
                        else Modifier
                    )
                ) {
                    OutlinedTextField(
                        value = stringResource(
                            unitOptions.find { it.first == selectedUnitId }?.second
                                ?: R.string.unit_kg
                        ),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.product_unit_label)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        colors = textFieldColors,
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
                        shape = RoundedCornerShape(14.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        unitOptions.forEach { (id, resId) ->
                            DropdownMenuItem(
                                text = { Text(stringResource(resId)) },
                                onClick = {
                                    selectedUnitId = id
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Toggle for Offer Options (product mode only)
            if (!isTemplateMode) {
                TextButton(
                    onClick = { showExtraOptions = !showExtraOptions },
                    modifier = Modifier.align(Alignment.End)
                        .then(if (tutorialStep == 6) Modifier.border(2.dp, Color(0xFFFF9800), RoundedCornerShape(4.dp)) else Modifier)
                ) {
                    Icon(
                        if (showExtraOptions) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp)
                    )
                    Text(
                        if (showExtraOptions) stringResource(R.string.product_hide_extra) else stringResource(
                            R.string.product_show_extra
                        )
                    )
                }
            }

            if (showExtraOptions && !isTemplateMode) {
                // Offer Section
                var offerExpanded by remember { mutableStateOf(false) }
                val offerOptions = listOf(
                    OfferType.NONE to R.string.offer_none,
                    OfferType.PERCENTAGE_DISCOUNT to R.string.offer_pct_discount,
                    OfferType.BUY_X_PAY_Y to R.string.offer_buy_x_pay_y,
                    OfferType.NTH_UNIT_DISCOUNT to R.string.offer_nth_unit_pct,
                    OfferType.FIXED_PRICE_FOR_X to R.string.offer_fixed_price,
                    OfferType.EXTRA_QUANTITY to R.string.offer_extra_qty
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = if (isDarkMode) Color(0xFF424242) else Color(0xFFEEEEEE)
                )

                Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.then(
                    if (tutorialStep == 7) Modifier.border(2.dp, Color(0xFFFF9800), RoundedCornerShape(14.dp)).padding(8.dp)
                    else Modifier
                )) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.LocalOffer,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            stringResource(R.string.offer_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    ExposedDropdownMenuBox(
                        expanded = offerExpanded,
                        onExpandedChange = { offerExpanded = !offerExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = stringResource(
                                offerOptions.find { it.first == offerType }?.second
                                    ?: R.string.offer_none
                            ),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(R.string.offer_title)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = offerExpanded) },
                            colors = textFieldColors,
                            modifier = Modifier.fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
                            shape = RoundedCornerShape(14.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = offerExpanded,
                            onDismissRequest = { offerExpanded = false }
                        ) {
                            offerOptions.forEach { (type, resId) ->
                                DropdownMenuItem(
                                    text = { Text(stringResource(resId)) },
                                    onClick = {
                                        offerType = type
                                        offerExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    if (offerType != OfferType.NONE) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            val label1 = when (offerType) {
                                OfferType.PERCENTAGE_DISCOUNT -> R.string.offer_value_pct
                                OfferType.BUY_X_PAY_Y -> R.string.offer_value_buy_x
                                OfferType.NTH_UNIT_DISCOUNT -> R.string.offer_value_nth_unit
                                OfferType.FIXED_PRICE_FOR_X -> R.string.offer_value_fixed_qty
                                OfferType.EXTRA_QUANTITY -> R.string.offer_value_extra_qty
                                else -> R.string.offer_none
                            }

                            OutlinedTextField(
                                value = offerValue1,
                                onValueChange = {
                                    if (it.all { c -> c.isDigit() || c == '.' || c == ',' }) {
                                        offerValue1 = it.replace(',', '.')
                                        // Default the purchase quantity to one full offer batch so the offer applies
                                        val batchSize = offerValue1.toDoubleOrNull()?.toInt() ?: 0
                                        val isBatchOffer = offerType == OfferType.BUY_X_PAY_Y ||
                                            offerType == OfferType.NTH_UNIT_DISCOUNT ||
                                            offerType == OfferType.FIXED_PRICE_FOR_X
                                        if (isBatchOffer && batchSize > 1 && (compareQuantity.toIntOrNull() ?: 1) <= 1) {
                                            compareQuantity = batchSize.toString()
                                        }
                                    }
                                },
                                label = { Text(stringResource(label1)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                colors = textFieldColors,
                                singleLine = true
                            )

                            if (offerType == OfferType.BUY_X_PAY_Y || offerType == OfferType.FIXED_PRICE_FOR_X || offerType == OfferType.NTH_UNIT_DISCOUNT) {
                                val label2 = when (offerType) {
                                    OfferType.BUY_X_PAY_Y -> R.string.offer_value_pay_y
                                    OfferType.FIXED_PRICE_FOR_X -> R.string.offer_value_fixed_price
                                    OfferType.NTH_UNIT_DISCOUNT -> R.string.offer_value_2nd_disc
                                    else -> R.string.offer_none
                                }
                                OutlinedTextField(
                                    value = offerValue2,
                                    onValueChange = {
                                        if (it.all { c -> c.isDigit() || c == '.' || c == ',' }) offerValue2 =
                                            it.replace(',', '.')
                                    },
                                    label = { Text(stringResource(label2)) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = textFieldColors,
                                    singleLine = true
                                )
                            }
                        }
                    }
                }
            }

            val priceVal = price.toDoubleOrNull() ?: 0.0
            val unitsVal = unitsPerPackage.toIntOrNull() ?: 1
            val quantVal = quantityPerUnit.toDoubleOrNull() ?: 0.0
            val compVal = (compareQuantity.toIntOrNull() ?: 1).coerceAtLeast(1)

            if (quantVal > 0 && priceVal > 0 && !isTemplateMode) {
                val tempProduct = Product(
                    name = name,
                    price = priceVal,
                    unitsPerPackage = unitsVal,
                    quantityPerUnit = quantVal,
                    unit = selectedUnitId,
                    offer = Offer(
                        type = offerType,
                        value1 = offerValue1.toDoubleOrNull() ?: 0.0,
                        value2 = offerValue2.toDoubleOrNull() ?: 0.0
                    ),
                    compareQuantity = compVal
                )

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = if (isDarkMode) Color(0xFF2E7D32).copy(alpha = 0.15f) else Color(
                        0xFF2E7D32
                    ).copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, Color(0xFF2E7D32).copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.Info,
                                contentDescription = null,
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                stringResource(R.string.product_calculation_title),
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFF2E7D32),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                val unitRes = when (tempProduct.unit) {
                                    "kg" -> R.string.unit_kg
                                    "g" -> R.string.unit_g
                                    "l" -> R.string.unit_l
                                    "ml" -> R.string.unit_ml
                                    "ud" -> R.string.unit_units
                                    "pack" -> R.string.unit_pack
                                    "box" -> R.string.unit_box
                                    else -> R.string.unit_units
                                }
                                Text(
                                    stringResource(R.string.product_total_label),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isDarkMode) Color.LightGray else Color(0xFF616161)
                                )
                                Text(
                                    "${formatQuantity(tempProduct.totalCompareQuantity)} ${stringResource(unitRes)}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDarkMode) Color.White else Color.Black
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    stringResource(R.string.product_total_price_for_qty, tempProduct.effectiveCompareQuantity),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isDarkMode) Color.LightGray else Color(0xFF616161)
                                )
                                Text(
                                    "€${String.format(Locale.getDefault(), "%.2f", tempProduct.totalComparePrice)}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDarkMode) Color.White else Color.Black
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                val baseUnitRes = when (tempProduct.baseUnit) {
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
                                Text(
                                    stringResource(R.string.compare_price_per, baseUnitName),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isDarkMode) Color.LightGray else Color(0xFF616161)
                                )
                                Text(
                                    "€${
                                        String.format(
                                            Locale.getDefault(),
                                            "%.4f",
                                            tempProduct.pricePerBaseUnit
                                        )
                                    }/$baseUnitName",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF2E7D32)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    stringResource(R.string.product_price_per_unit_for_qty),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isDarkMode) Color.LightGray else Color(0xFF616161)
                                )
                                Text(
                                    "€${String.format(Locale.getDefault(), "%.2f", tempProduct.pricePerUnitInCompare)}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32)
                                )
                                if (tempProduct.savingPercentage > 0) {
                                    Text(
                                        stringResource(
                                            R.string.offer_savings,
                                            tempProduct.savingPercentage
                                        ),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF4CAF50),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Save as Template Toggle
        if (!isEditing && !isTemplateMode) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { saveAsTemplate = !saveAsTemplate }
                    .padding(vertical = 4.dp)
                    .then(if (tutorialStep == 8) Modifier.border(2.dp, Color(0xFFFF9800), RoundedCornerShape(8.dp)) else Modifier),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = saveAsTemplate,
                    onCheckedChange = { saveAsTemplate = it },
                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF2E7D32))
                )
                Text(
                    stringResource(R.string.product_save_as_template),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                val priceValue = price.toDoubleOrNull() ?: 0.0
                val unitsValue = unitsPerPackage.toIntOrNull() ?: 1
                val quantValue = quantityPerUnit.toDoubleOrNull() ?: 0.0
                val compValue = (compareQuantity.toIntOrNull() ?: 1).coerceAtLeast(1)

                if (isTemplateMode) {
                    val template = ProductTemplate(
                        id = existingTemplateId ?: 0,
                        name = if (name.isBlank()) defaultProductName else name.trim(),
                        unitsPerPackage = unitsValue,
                        quantityPerUnit = quantValue,
                        unit = selectedUnitId,
                        barcode = barcode.ifBlank { null },
                        compareQuantity = compValue
                    )
                    templateViewModel.insertTemplate(template)
                    onTemplateAction()
                } else {
                    if (priceValue > 0 && quantValue > 0) {
                        val finalProduct = Product(
                            id = existingProduct?.id ?: System.currentTimeMillis(),
                            name = if (name.isBlank()) defaultProductName else name.trim(),
                            price = priceValue,
                            unitsPerPackage = unitsValue,
                            quantityPerUnit = quantValue,
                            unit = selectedUnitId,
                            offer = Offer(
                                type = offerType,
                                value1 = offerValue1.toDoubleOrNull() ?: 0.0,
                                value2 = offerValue2.toDoubleOrNull() ?: 0.0
                            ),
                            barcode = barcode.ifBlank { null },
                            compareQuantity = compValue
                        )

                        if (saveAsTemplate) {
                            templateViewModel.insertTemplate(
                                ProductTemplate(
                                    name = finalProduct.name,
                                    unitsPerPackage = finalProduct.unitsPerPackage,
                                    quantityPerUnit = finalProduct.quantityPerUnit,
                                    unit = finalProduct.unit,
                                    barcode = finalProduct.barcode,
                                    compareQuantity = finalProduct.compareQuantity
                                )
                            )
                        }

                        onProductAction(finalProduct)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
            enabled = (if (isTemplateMode) name.isNotBlank() else price.isNotBlank()) && quantityPerUnit.isNotBlank(),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Icon(
                if (isEditing) Icons.Filled.CheckCircle else Icons.Filled.AddCircle,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                if (isEditing) stringResource(R.string.product_save_changes) else stringResource(R.string.product_add_button),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )
        }

        }

        // Tutorial Overlay (Non-modal)
        if (tutorialStep >= 0) {
            val alignment = if (tutorialStep >= 7) Alignment.TopCenter else Alignment.BottomCenter
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
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
                        .padding(top = if (tutorialStep >= 7) 48.dp else 0.dp, bottom = if (tutorialStep < 7) 12.dp else 0.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                when (tutorialStep) {
                                    0 -> stringResource(R.string.tutorial_step_name_title)
                                    1 -> stringResource(R.string.tutorial_step_price_title)
                                    2 -> stringResource(R.string.tutorial_step_qty_title)
                                    3 -> stringResource(R.string.tutorial_step_unit_title)
                                    4 -> stringResource(R.string.tutorial_step_extra_title)
                                    5 -> stringResource(R.string.tutorial_step_compare_qty_title)
                                    6 -> stringResource(R.string.tutorial_step_toggle_title)
                                    7 -> stringResource(R.string.tutorial_step_offers_title)
                                    else -> stringResource(R.string.tutorial_step_templates_title)
                                },
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = {
                                prefs.edit().putBoolean("tutorial_shown", true).apply()
                                tutorialStep = -1
                            }) {
                                Icon(Icons.Filled.Close, contentDescription = null, tint = Color.White.copy(alpha = 0.7f))
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            when (tutorialStep) {
                                0 -> stringResource(R.string.tutorial_step_name_desc)
                                1 -> stringResource(R.string.tutorial_step_price_desc)
                                2 -> stringResource(R.string.tutorial_step_qty_desc)
                                3 -> stringResource(R.string.tutorial_step_unit_desc)
                                4 -> stringResource(R.string.tutorial_step_extra_desc)
                                5 -> stringResource(R.string.tutorial_step_compare_qty_desc)
                                6 -> stringResource(R.string.tutorial_step_toggle_desc)
                                7 -> stringResource(R.string.tutorial_step_offers_desc)
                                else -> stringResource(R.string.tutorial_step_templates_desc)
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = {
                                prefs.edit().putBoolean("tutorial_shown", true).apply()
                                tutorialStep = -1
                            }) {
                                Text(stringResource(R.string.tutorial_skip), color = Color.White.copy(alpha = 0.7f))
                            }
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Button(
                                onClick = {
                                    if (tutorialStep < 8) tutorialStep++
                                    else {
                                        prefs.edit().putBoolean("tutorial_shown", true).apply()
                                        prefs.edit().putBoolean("compare_tutorial_active", true).apply()
                                        tutorialStep = -1
                                        onTutorialFinish()
                                        // If we are in tutorial mode from Settings, we might want to go back
                                        // or just let the user save. The user usually finishes and then saves.
                                        // But if they just wanted to SEE the tutorial, maybe we should close?
                                        // Actually, let's just let them save.
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                                    contentColor = Color(0xFF2E7D32)
                                )
                            ) {
                                Text(
                                    if (tutorialStep < 8) stringResource(R.string.tutorial_next)
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
