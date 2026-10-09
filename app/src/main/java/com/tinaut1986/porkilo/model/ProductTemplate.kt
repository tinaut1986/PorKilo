package com.tinaut1986.porkilo.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(
    tableName = "product_templates",
    indices = [Index(value = ["barcode"], unique = true)]
)
data class ProductTemplate(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val unitsPerPackage: Int = 1,
    val quantityPerUnit: Double,
    val unit: String,
    val barcode: String? = null,
    val compareQuantity: Int = 1,
    val rating: Float = 0f // 0 = not rated, otherwise 0.5 to 5 in steps of 0.5
) : Parcelable
