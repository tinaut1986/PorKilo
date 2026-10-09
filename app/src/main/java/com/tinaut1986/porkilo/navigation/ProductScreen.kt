package com.tinaut1986.porkilo.navigation

import androidx.annotation.StringRes
import com.tinaut1986.porkilo.R

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class ProductScreen(val route: String, @StringRes val titleRes: Int, val icon: ImageVector) {
    object Compare : ProductScreen("compare", R.string.screen_compare, Icons.Filled.CompareArrows)
    object Add : ProductScreen("add?barcode={barcode}&templateId={templateId}", R.string.screen_add, Icons.Filled.AddCircle) {
        fun createRoute(barcode: String? = null, templateId: Long? = null) =
            "add?barcode=${barcode ?: ""}&templateId=${templateId ?: -1L}"
    }
    object Settings : ProductScreen("settings", R.string.screen_settings, Icons.Filled.Settings)
    object Templates : ProductScreen("templates", R.string.screen_templates, Icons.Filled.Inventory)
    object AddTemplate : ProductScreen("add_template?barcode={barcode}", R.string.template_add_title, Icons.Filled.AddCircle) {
        fun createRoute(barcode: String? = null) = if (barcode != null) "add_template?barcode=$barcode" else "add_template"
    }
    object EditTemplate : ProductScreen("edit_template/{templateId}", R.string.template_edit_title, Icons.Filled.Edit) {
        fun createRoute(templateId: Long) = "edit_template/$templateId"
    }
    object Edit : ProductScreen("edit/{productId}", R.string.screen_edit, Icons.Filled.Edit) {
        fun createRoute(productId: Long) = "edit/$productId"
    }
}
