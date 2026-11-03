package com.example.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.JacketRepository
import com.example.data.local.JacketDatabase
import com.example.model.Jacket
import com.example.model.StockStatusLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Sorting options for Jacket Inventory display.
 */
enum class JacketInventorySort {
    NAME_ASC,
    NAME_DESC,
    QTY_ASC,
    QTY_DESC,
    CATEGORY_ASC,
    CATEGORY_DESC
}

/**
 * Filter parameters combined helper.
 */
private data class InventoryFilterParams(
    val query: String = "",
    val category: String? = null,
    val size: String? = null,
    val status: StockStatusLevel? = null,
    val sort: JacketInventorySort = JacketInventorySort.NAME_ASC
)

/**
 * UI State for Jacket Inventory screen and components.
 */
data class JacketInventoryUiState(
    val jackets: List<Jacket> = emptyList(),
    val filteredJackets: List<Jacket> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val userMessage: String? = null,
    val searchQuery: String = "",
    val selectedCategory: String? = null,
    val selectedSize: String? = null,
    val selectedStockStatus: StockStatusLevel? = null,
    val selectedSort: JacketInventorySort = JacketInventorySort.NAME_ASC,
    val totalJacketsInStock: Int = 0,
    val totalItemCount: Int = 0,
    val inStockCount: Int = 0,
    val lowStockCount: Int = 0,
    val outOfStockCount: Int = 0
)

/**
 * ViewModel that manages the state of the jacket inventory and handles
 * interactions between the UI and the Room database via [JacketRepository].
 *
 * Supports constructor injection for testing and a secondary Application
 * constructor / Factory for Compose integration.
 */
class JacketInventoryViewModel(
    private val repository: JacketRepository
) : ViewModel() {

    /**
     * Convenience secondary constructor when provided with an [Application] instance.
     */
    constructor(application: Application) : this(
        repository = JacketRepository(
            dao = JacketDatabase.getDatabase(application).jacketDao(),
            productionDao = JacketDatabase.getDatabase(application).dailyProductionDao()
        )
    )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _selectedSize = MutableStateFlow<String?>(null)
    val selectedSize: StateFlow<String?> = _selectedSize.asStateFlow()

    private val _selectedStockStatus = MutableStateFlow<StockStatusLevel?>(null)
    val selectedStockStatus: StateFlow<StockStatusLevel?> = _selectedStockStatus.asStateFlow()

    private val _selectedSort = MutableStateFlow(JacketInventorySort.NAME_ASC)
    val selectedSort: StateFlow<JacketInventorySort> = _selectedSort.asStateFlow()

    private val _isIndonesian = MutableStateFlow(true)
    val isIndonesian: StateFlow<Boolean> = _isIndonesian.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    fun toggleLanguage() {
        _isIndonesian.value = !_isIndonesian.value
    }

    fun setLanguageIndonesian(isId: Boolean) {
        _isIndonesian.value = isId
    }

    /**
     * Stream of all jackets directly from the Room database.
     */
    val allJackets: StateFlow<List<Jacket>> = repository.jacketList
        .catch { e ->
            _errorMessage.value = "Failed to load jackets: ${e.localizedMessage}"
            emit(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _filterParams: Flow<InventoryFilterParams> = combine(
        _searchQuery,
        _selectedCategory,
        _selectedSize,
        _selectedStockStatus,
        _selectedSort
    ) { query, category, size, status, sort ->
        InventoryFilterParams(
            query = query,
            category = category,
            size = size,
            status = status,
            sort = sort
        )
    }

    /**
     * Aggregated UI State combining Room database flow with filter, search, and sort states.
     */
    val uiState: StateFlow<JacketInventoryUiState> = combine(
        allJackets,
        _filterParams,
        _isLoading,
        _errorMessage,
        _userMessage
    ) { rawJackets, filters, loading, error, message ->
        var filtered = rawJackets

        if (!filters.category.isNullOrBlank()) {
            filtered = filtered.filter { it.category.equals(filters.category, ignoreCase = true) }
        }

        if (!filters.size.isNullOrBlank()) {
            filtered = filtered.filter { it.size.equals(filters.size, ignoreCase = true) }
        }

        if (filters.status != null) {
            filtered = filtered.filter { it.status == filters.status }
        }

        if (filters.query.isNotBlank()) {
            val q = filters.query.trim()
            filtered = filtered.filter { jacket ->
                jacket.name.contains(q, ignoreCase = true) ||
                jacket.sku.contains(q, ignoreCase = true) ||
                jacket.category.contains(q, ignoreCase = true) ||
                jacket.locationRack.contains(q, ignoreCase = true) ||
                jacket.material.contains(q, ignoreCase = true) ||
                jacket.color.contains(q, ignoreCase = true)
            }
        }

        // Sort jackets
        val sortedList = when (filters.sort) {
            JacketInventorySort.NAME_ASC -> filtered.sortedBy { it.name.lowercase() }
            JacketInventorySort.NAME_DESC -> filtered.sortedByDescending { it.name.lowercase() }
            JacketInventorySort.QTY_ASC -> filtered.sortedBy { it.quantity }
            JacketInventorySort.QTY_DESC -> filtered.sortedByDescending { it.quantity }
            JacketInventorySort.CATEGORY_ASC -> filtered.sortedWith(compareBy({ it.category.lowercase() }, { it.name.lowercase() }))
            JacketInventorySort.CATEGORY_DESC -> filtered.sortedWith(compareByDescending<Jacket> { it.category.lowercase() }.thenBy { it.name.lowercase() })
        }

        // Aggregate summary metrics
        val totalStock = rawJackets.sumOf { it.quantity }
        val inStock = rawJackets.count { it.status == StockStatusLevel.IN_STOCK }
        val lowStock = rawJackets.count { it.status == StockStatusLevel.LOW_STOCK }
        val outOfStock = rawJackets.count { it.status == StockStatusLevel.OUT_OF_STOCK }

        JacketInventoryUiState(
            jackets = rawJackets,
            filteredJackets = sortedList,
            isLoading = loading,
            errorMessage = error,
            userMessage = message,
            searchQuery = filters.query,
            selectedCategory = filters.category,
            selectedSize = filters.size,
            selectedStockStatus = filters.status,
            selectedSort = filters.sort,
            totalJacketsInStock = totalStock,
            totalItemCount = rawJackets.size,
            inStockCount = inStock,
            lowStockCount = lowStock,
            outOfStockCount = outOfStock
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = JacketInventoryUiState(isLoading = true)
    )

    // Database Actions

    /**
     * Adds a new jacket to the Room database.
     */
    fun addJacket(jacket: Jacket) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _isLoading.value = true
                repository.addJacket(jacket)
                _userMessage.value = if (_isIndonesian.value) {
                    "Jaket '${jacket.name}' berhasil ditambahkan ke inventaris."
                } else {
                    "Jacket '${jacket.name}' added successfully."
                }
            } catch (e: Exception) {
                _errorMessage.value = if (_isIndonesian.value) {
                    "Gagal menambahkan jaket: ${e.localizedMessage}"
                } else {
                    "Failed to add jacket: ${e.localizedMessage}"
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Convenience overload to create and add a jacket.
     */
    fun addJacket(
        name: String,
        category: String,
        size: String,
        quantity: Int,
        sku: String = "",
        material: String = "Cotton",
        color: String = "Black",
        locationRack: String = "Rack A-01",
        minThreshold: Int = 5,
        unitPrice: Double = 89.99,
        imageUrl: String = "",
        isSeasonal: Boolean = false
    ) {
        val jacket = Jacket(
            name = name,
            category = category,
            size = size,
            quantity = quantity,
            sku = sku,
            material = material,
            color = color,
            locationRack = locationRack,
            minThreshold = minThreshold,
            unitPrice = unitPrice,
            imageUrl = imageUrl,
            isSeasonal = isSeasonal
        )
        addJacket(jacket)
    }

    /**
     * Updates an existing jacket in the Room database.
     */
    fun updateJacket(jacket: Jacket) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _isLoading.value = true
                repository.updateJacket(jacket)
                _userMessage.value = if (_isIndonesian.value) {
                    "Data jaket '${jacket.name}' berhasil diperbarui."
                } else {
                    "Jacket '${jacket.name}' updated."
                }
            } catch (e: Exception) {
                _errorMessage.value = if (_isIndonesian.value) {
                    "Gagal memperbarui jaket: ${e.localizedMessage}"
                } else {
                    "Failed to update jacket: ${e.localizedMessage}"
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Deletes a jacket from the Room database.
     */
    fun deleteJacket(jacket: Jacket) {
        deleteJacketById(jacket.id, jacket.name)
    }

    /**
     * Deletes a jacket by its unique ID from the Room database.
     */
    fun deleteJacketById(id: String, jacketName: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _isLoading.value = true
                repository.deleteJacket(id)
                val label = jacketName ?: if (_isIndonesian.value) "Jaket" else "Jacket"
                _userMessage.value = if (_isIndonesian.value) {
                    "$label berhasil dihapus dari inventaris."
                } else {
                    "$label removed from inventory."
                }
            } catch (e: Exception) {
                _errorMessage.value = if (_isIndonesian.value) {
                    "Gagal menghapus jaket: ${e.localizedMessage}"
                } else {
                    "Failed to delete jacket: ${e.localizedMessage}"
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Adjusts the quantity of a jacket by delta (+1, -1, etc.).
     */
    fun adjustQuantity(id: String, delta: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val current = allJackets.value.firstOrNull { it.id == id }
                if (current != null) {
                    repository.adjustQuantity(id, current.quantity, delta)
                }
            } catch (e: Exception) {
                _errorMessage.value = if (_isIndonesian.value) {
                    "Gagal mengubah stok jaket: ${e.localizedMessage}"
                } else {
                    "Failed to adjust quantity: ${e.localizedMessage}"
                }
            }
        }
    }

    /**
     * Sets an explicit stock quantity for a jacket.
     */
    fun setQuantity(id: String, newQuantity: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.setQuantity(id, newQuantity)
            } catch (e: Exception) {
                _errorMessage.value = if (_isIndonesian.value) {
                    "Gagal menetapkan jumlah stok: ${e.localizedMessage}"
                } else {
                    "Failed to set quantity: ${e.localizedMessage}"
                }
            }
        }
    }

    /**
     * Restocks a jacket by adding a specified amount (defaults to 15).
     */
    fun restockJacket(id: String, amount: Int = 15) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val current = allJackets.value.firstOrNull { it.id == id }
                if (current != null) {
                    repository.restock(id, current.quantity, amount)
                    _userMessage.value = if (_isIndonesian.value) {
                        "Restok +$amount unit untuk '${current.name}' berhasil ditambahkan."
                    } else {
                        "Restocked +$amount units for '${current.name}'."
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = if (_isIndonesian.value) {
                    "Gagal restok jaket: ${e.localizedMessage}"
                } else {
                    "Failed to restock: ${e.localizedMessage}"
                }
            }
        }
    }

    /**
     * Fetches a single jacket by ID asynchronously from the Room database.
     */
    fun getJacketById(id: String, onResult: (Jacket?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val item = repository.getJacketById(id)
            onResult(item)
        }
    }

    // Filter & UI state controls

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategoryFilter(category: String?) {
        _selectedCategory.value = category
    }

    fun setSizeFilter(size: String?) {
        _selectedSize.value = size
    }

    fun setStockStatusFilter(status: StockStatusLevel?) {
        _selectedStockStatus.value = status
    }

    fun setSortOption(sort: JacketInventorySort) {
        _selectedSort.value = sort
    }

    fun clearFilters() {
        _searchQuery.value = ""
        _selectedCategory.value = null
        _selectedSize.value = null
        _selectedStockStatus.value = null
        _selectedSort.value = JacketInventorySort.NAME_ASC
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    companion object {
        fun Factory(application: Application): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val db = JacketDatabase.getDatabase(application)
                val repo = JacketRepository(db.jacketDao(), db.dailyProductionDao())
                return JacketInventoryViewModel(repo) as T
            }
        }
    }
}
