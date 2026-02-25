package com.tinaut1986.pricesmart.data

import com.tinaut1986.pricesmart.model.ProductTemplate
import kotlinx.coroutines.flow.Flow

class TemplateRepository(private val templateDao: TemplateDao) {
    val allTemplates: Flow<List<ProductTemplate>> = templateDao.getAllTemplates()

    suspend fun getTemplateByBarcode(barcode: String): ProductTemplate? {
        return templateDao.getTemplateByBarcode(barcode)
    }

    suspend fun getTemplateById(id: Long): ProductTemplate? {
        return templateDao.getTemplateById(id)
    }

    suspend fun insert(template: ProductTemplate) {
        templateDao.insertTemplate(template)
    }

    suspend fun update(template: ProductTemplate) {
        templateDao.updateTemplate(template)
    }

    suspend fun delete(template: ProductTemplate) {
        templateDao.deleteTemplate(template)
    }
}
