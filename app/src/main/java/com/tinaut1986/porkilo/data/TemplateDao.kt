package com.tinaut1986.porkilo.data

import androidx.room.*
import com.tinaut1986.porkilo.model.ProductTemplate
import kotlinx.coroutines.flow.Flow

@Dao
interface TemplateDao {
    @Query("SELECT * FROM product_templates ORDER BY name ASC")
    fun getAllTemplates(): Flow<List<ProductTemplate>>

    @Query("SELECT * FROM product_templates WHERE barcode = :barcode LIMIT 1")
    suspend fun getTemplateByBarcode(barcode: String): ProductTemplate?

    @Query("SELECT * FROM product_templates WHERE id = :id LIMIT 1")
    suspend fun getTemplateById(id: Long): ProductTemplate?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: ProductTemplate)

    @Update
    suspend fun updateTemplate(template: ProductTemplate)

    @Delete
    suspend fun deleteTemplate(template: ProductTemplate)
}
