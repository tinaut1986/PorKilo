package com.tinaut1986.porkilo.vms

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tinaut1986.porkilo.data.AppDatabase
import com.tinaut1986.porkilo.data.TemplateRepository
import com.tinaut1986.porkilo.model.ProductTemplate
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TemplateViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TemplateRepository
    val allTemplates: StateFlow<List<ProductTemplate>>

    init {
        val templateDao = AppDatabase.getDatabase(application).templateDao()
        repository = TemplateRepository(templateDao)
        allTemplates = repository.allTemplates.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun insertTemplate(template: ProductTemplate) {
        viewModelScope.launch {
            // First check if a template with this barcode already exists
            if (template.barcode != null) {
                val existing = repository.getTemplateByBarcode(template.barcode)
                if (existing != null) {
                    // Update existing template with new data, keeping its rating unless a new one was given
                    val rating = if (template.id == 0L && template.rating == 0f) existing.rating else template.rating
                    repository.insert(template.copy(id = existing.id, rating = rating))
                    return@launch
                }
            }

            // Check if there's any existing template with EXACTLY the same data
            val duplicate = allTemplates.value.find { 
                it.name == template.name &&
                it.unitsPerPackage == template.unitsPerPackage &&
                it.quantityPerUnit == template.quantityPerUnit &&
                it.unit == template.unit &&
                it.barcode == template.barcode
            }

            if (duplicate == null) {
                repository.insert(template)
            }
        }
    }

    fun updateTemplate(template: ProductTemplate) {
        viewModelScope.launch {
            repository.update(template)
        }
    }

    fun updateRating(template: ProductTemplate, rating: Float) {
        updateTemplate(template.copy(rating = rating))
    }

    fun deleteTemplate(template: ProductTemplate) {
        viewModelScope.launch {
            repository.delete(template)
        }
    }

    suspend fun getTemplateByBarcode(barcode: String): ProductTemplate? {
        return repository.getTemplateByBarcode(barcode)
    }

    suspend fun getTemplateById(id: Long): ProductTemplate? {
        return repository.getTemplateById(id)
    }
}
