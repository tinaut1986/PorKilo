package com.tinaut1986.pricesmart.model

import androidx.room.ColumnInfo
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
    @ColumnInfo(defaultValue = "1")
    val compareQuantity: Int = 1
) : Parcelable
