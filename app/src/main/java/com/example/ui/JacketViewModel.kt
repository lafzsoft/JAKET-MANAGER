package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.JacketRepository
import com.example.data.local.JacketDatabase
import com.example.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class AppScreen {
    HOME,
    INVENTORY,
    ADD_JACKET,
    PRODUCTION,
    REPORTS,
    JACKET_LIST
}

class JacketViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val database = JacketDatabase.getDatabase(application, viewModelScope)
    private val repository = JacketRepository(database.jacketDao(), database.dailyProductionDao())

    init {
        // Ensure initial database population if empty
        viewModelScope.launch(Dispatchers.IO) {
            if (database.jacketDao().getCount() == 0) {
                JacketDatabase.populateInitialDatabase(database.jacketDao(), database.dailyProductionDao())
            }
        }
    }

    val allJackets: StateFlow<List<JacketItem>> = repository.jackets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allJacketModels: StateFlow<List<Jacket>> = repository.jacketList
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allProductions: StateFlow<List<DailyProductionItem>> = repository.productions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _dailyTarget = MutableStateFlow(60)
    val dailyTarget: StateFlow<Int> = _dailyTarget.asStateFlow()

    private val _productionShiftFilter = MutableStateFlow("ALL")
    val productionShiftFilter: StateFlow<String> = _productionShiftFilter.asStateFlow()

    private val _isAddProductionSheetOpen = MutableStateFlow(false)
    val isAddProductionSheetOpen: StateFlow<Boolean> = _isAddProductionSheetOpen.asStateFlow()

    val filteredProductions: StateFlow<List<DailyProductionItem>> = combine(
        allProductions,
        _productionShiftFilter
    ) { list, shift ->
        if (shift == "ALL") {
            list
        } else {
            list.filter { it.shift.name.equals(shift, ignoreCase = true) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dailySummary: StateFlow<DailyProductionSummary> = combine(
        allProductions,
        _dailyTarget
    ) { list, target ->
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val todayList = list.filter { it.dateString == todayStr }

        val totalProduced = todayList.sumOf { it.quantityProduced }
        val totalDefects = todayList.sumOf { it.defectCount }
        val totalAccepted = (totalProduced - totalDefects).coerceAtLeast(0)
        val batches = todayList.size

        val fulfillmentPct = if (target > 0) {
            ((totalAccepted.toFloat() / target.toFloat()) * 100f).coerceAtMost(100f)
        } else 0f

        val avgDefectRate = if (totalProduced > 0) {
            (totalDefects.toFloat() / totalProduced.toFloat()) * 100f
        } else 0f

        DailyProductionSummary(
            totalProducedToday = totalProduced,
            totalAcceptedToday = totalAccepted,
            totalDefectsToday = totalDefects,
            totalBatchesToday = batches,
            dailyTarget = target,
            targetFulfillmentPercentage = fulfillmentPct,
            averageDefectRate = avgDefectRate
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        DailyProductionSummary(0, 0, 0, 0, 60, 0f, 0f)
    )

    private val _currentScreen = MutableStateFlow(AppScreen.HOME)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _activeFilter = MutableStateFlow<ActiveFilter>(ActiveFilter.None)
    val activeFilter: StateFlow<ActiveFilter> = _activeFilter.asStateFlow()

    private val _isAddSheetOpen = MutableStateFlow(false)
    val isAddSheetOpen: StateFlow<Boolean> = _isAddSheetOpen.asStateFlow()

    private val _isFilterSheetOpen = MutableStateFlow(false)
    val isFilterSheetOpen: StateFlow<Boolean> = _isFilterSheetOpen.asStateFlow()

    private val _isExportSheetOpen = MutableStateFlow(false)
    val isExportSheetOpen: StateFlow<Boolean> = _isExportSheetOpen.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isIndonesian = MutableStateFlow(true)
    val isIndonesian: StateFlow<Boolean> = _isIndonesian.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow("ALL")
    val selectedCategoryFilter: StateFlow<String> = _selectedCategoryFilter.asStateFlow()

    private val _selectedJacketForImage = MutableStateFlow<JacketItem?>(null)
    val selectedJacketForImage: StateFlow<JacketItem?> = _selectedJacketForImage.asStateFlow()

    fun openImagePickerForJacket(jacket: JacketItem) {
        _selectedJacketForImage.value = jacket
    }

    fun closeImagePicker() {
        _selectedJacketForImage.value = null
    }

    val filteredJackets: StateFlow<List<JacketItem>> = combine(
        allJackets,
        _activeFilter,
        _searchQuery,
        _selectedCategoryFilter
    ) { list, filter, query, catFilter ->
        val catList = if (catFilter == "ALL") list else list.filter { it.genderCategory.equals(catFilter, ignoreCase = true) }
        val afterFilter = when (filter) {
            is ActiveFilter.None -> catList
            is ActiveFilter.ByStyle -> catList.filter { it.style.equals(filter.style, ignoreCase = true) }
            is ActiveFilter.BySize -> catList.filter { it.size.equals(filter.size, ignoreCase = true) }
            is ActiveFilter.ByStatus -> catList.filter { it.status == filter.status }
            is ActiveFilter.ByMaterial -> catList.filter { it.material.equals(filter.material, ignoreCase = true) }
            is ActiveFilter.ByColor -> catList.filter { item ->
                item.color.equals(filter.color, ignoreCase = true) ||
                item.color.equals(filter.title, ignoreCase = true) ||
                CatalogData.COLORS.find { c ->
                    c.titleEn.equals(filter.color, ignoreCase = true) || c.titleId.equals(filter.color, ignoreCase = true)
                }?.let { cat ->
                    item.color.equals(cat.titleId, ignoreCase = true) || item.color.equals(cat.titleEn, ignoreCase = true)
                } ?: false
            }
            is ActiveFilter.BySeasonal -> catList.filter { it.isSeasonal }
            is ActiveFilter.ByCategory -> catList.filter { it.genderCategory.equals(filter.category, ignoreCase = true) }
        }

        if (query.isBlank()) {
            afterFilter
        } else {
            afterFilter.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.sku.contains(query, ignoreCase = true) ||
                it.locationRack.contains(query, ignoreCase = true) ||
                it.style.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleLanguage() {
        _isIndonesian.value = !_isIndonesian.value
    }

    fun selectScreen(screen: AppScreen) {
        _currentScreen.value = screen
    }

    private val _prefilledSkuForAdd = MutableStateFlow<String?>(null)
    val prefilledSkuForAdd: StateFlow<String?> = _prefilledSkuForAdd.asStateFlow()

    fun navigateToAddJacket() {
        _prefilledSkuForAdd.value = null
        _currentScreen.value = AppScreen.ADD_JACKET
    }

    fun navigateToAddJacketWithSku(sku: String) {
        _prefilledSkuForAdd.value = sku
        _currentScreen.value = AppScreen.ADD_JACKET
    }

    fun clearPrefilledSku() {
        _prefilledSkuForAdd.value = null
    }

    fun navigateToHome() {
        _currentScreen.value = AppScreen.HOME
    }

    fun navigateToInventory() {
        _currentScreen.value = AppScreen.INVENTORY
    }

    fun navigateToReports() {
        _currentScreen.value = AppScreen.REPORTS
    }

    fun navigateToJacketList() {
        _currentScreen.value = AppScreen.JACKET_LIST
    }

    fun restockAllLowStock() {
        viewModelScope.launch(Dispatchers.IO) {
            val lowStockItems = allJackets.value.filter { it.status != StockStatusLevel.IN_STOCK }
            lowStockItems.forEach { item ->
                repository.restock(item.id, item.quantity, 15)
            }
        }
    }

    fun openAddSheet() {
        _isAddSheetOpen.value = true
    }

    fun closeAddSheet() {
        _isAddSheetOpen.value = false
    }

    fun openExportSheet() {
        _isExportSheetOpen.value = true
    }

    fun closeExportSheet() {
        _isExportSheetOpen.value = false
    }

    fun openFilterByStyle(style: StyleCategory) {
        val title = if (_isIndonesian.value) style.titleId else style.titleEn
        _activeFilter.value = ActiveFilter.ByStyle(style.titleEn, title)
        _isFilterSheetOpen.value = true
    }

    fun openFilterBySize(size: SizeCategory) {
        _activeFilter.value = ActiveFilter.BySize(size.title, "Size ${size.title}")
        _isFilterSheetOpen.value = true
    }

    fun openFilterByStatus(status: StockStatusCategory) {
        val title = if (_isIndonesian.value) status.titleId else status.titleEn
        _activeFilter.value = ActiveFilter.ByStatus(status.status, title)
        _isFilterSheetOpen.value = true
    }

    fun openFilterByMaterial(material: MaterialCategory) {
        val title = if (_isIndonesian.value) material.titleId else material.titleEn
        _activeFilter.value = ActiveFilter.ByMaterial(material.titleEn, title)
        _isFilterSheetOpen.value = true
    }

    fun openFilterByColor(color: ColorCategory) {
        val title = if (_isIndonesian.value) color.titleId else color.titleEn
        _activeFilter.value = ActiveFilter.ByColor(color.titleEn, title)
        _isFilterSheetOpen.value = true
    }

    fun openFilterSeasonal() {
        val title = if (_isIndonesian.value) CatalogData.SEASONAL_BANNER.titleId else CatalogData.SEASONAL_BANNER.titleEn
        _activeFilter.value = ActiveFilter.BySeasonal(title)
        _isFilterSheetOpen.value = true
    }

    fun openFilterByCategory(category: String) {
        _selectedCategoryFilter.value = category
        _activeFilter.value = ActiveFilter.ByCategory(category, category)
        _isFilterSheetOpen.value = true
    }

    fun setSelectedCategoryFilter(category: String) {
        _selectedCategoryFilter.value = category
    }

    fun closeFilterSheet() {
        _isFilterSheetOpen.value = false
        _searchQuery.value = ""
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun adjustQuantity(id: String, delta: Int) {
        val currentItem = allJackets.value.firstOrNull { it.id == id } ?: return
        viewModelScope.launch(Dispatchers.IO) {
            repository.adjustQuantity(id, currentItem.quantity, delta)
        }
    }

    fun restock(id: String) {
        val currentItem = allJackets.value.firstOrNull { it.id == id } ?: return
        viewModelScope.launch(Dispatchers.IO) {
            repository.restock(id, currentItem.quantity, amount = 15)
        }
    }

    fun deleteJacket(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteJacket(id)
        }
    }

    fun isSkuDuplicate(sku: String, excludeId: String? = null): Boolean {
        val clean = sku.trim()
        if (clean.isBlank()) return false
        return allJackets.value.any {
            it.sku.equals(clean, ignoreCase = true) && (excludeId == null || it.id != excludeId)
        }
    }

    fun findJacketBySku(sku: String): JacketItem? {
        val clean = sku.trim()
        if (clean.isBlank()) return null
        return allJackets.value.firstOrNull { it.sku.equals(clean, ignoreCase = true) }
    }

    fun generateAutoSku(style: String, name: String, size: String): String {
        val styleCode = if (style.isNotBlank()) style.take(3).uppercase() else "JKT"
        val nameWords = name.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }
        val nameCode = when {
            nameWords.size >= 2 -> "${nameWords[0].take(2)}${nameWords[1].take(2)}".uppercase()
            nameWords.size == 1 && nameWords[0].isNotBlank() -> nameWords[0].take(3).uppercase()
            else -> "NEW"
        }
        val sizeCode = if (size.isNotBlank()) size.uppercase() else "STD"
        val base = "JKT-$styleCode-$nameCode-$sizeCode"
        return if (allJackets.value.any { it.sku.equals(base, ignoreCase = true) }) {
            "$base-${(10..99).random()}"
        } else {
            base
        }
    }

    fun generateRandomCustomSku(prefix: String = "JKT"): String {
        val cleanPrefix = prefix.trim().take(6).uppercase().ifBlank { "JKT" }
        var candidate: String
        var attempts = 0
        do {
            val randomNum = (1000..9999).random()
            candidate = "$cleanPrefix-$randomNum"
            attempts++
        } while (isSkuDuplicate(candidate) && attempts < 20)
        return candidate
    }
    fun addJacket(jacket: Jacket) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addJacket(jacket)
        }
        _isAddSheetOpen.value = false
    }

    fun updateJacket(jacket: Jacket) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateJacket(jacket)
        }
    }

    fun updateJacketDetails(
        id: String,
        sku: String,
        name: String,
        style: String,
        size: String,
        material: String,
        color: String,
        quantity: Int,
        locationRack: String,
        isSeasonal: Boolean = false
    ) {
        val existing = allJackets.value.firstOrNull { it.id == id }
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateJacketDetails(
                id = id,
                sku = sku,
                name = name,
                style = style,
                size = size,
                material = material,
                color = color,
                quantity = quantity,
                locationRack = locationRack,
                isSeasonal = isSeasonal,
                imageUrl = existing?.imageUrl ?: "",
                unitPrice = existing?.unitPrice ?: 89.99,
                genderCategory = existing?.genderCategory ?: "Jaket Pria",
                minThreshold = existing?.minThreshold ?: 5
            )
        }
    }

    fun addNewJacket(
        sku: String,
        name: String,
        style: String,
        size: String,
        material: String,
        color: String,
        quantity: Int,
        locationRack: String,
        isSeasonal: Boolean,
        genderCategory: String = "Jaket Pria",
        imageUrl: String = ""
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addJacket(
                sku = sku,
                name = name,
                style = style,
                size = size,
                material = material,
                color = color,
                quantity = quantity,
                locationRack = locationRack,
                imageUrl = imageUrl,
                isSeasonal = isSeasonal,
                genderCategory = genderCategory
            )
        }
        _isAddSheetOpen.value = false
    }

    fun updateJacketImage(id: String, imageUrl: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateJacketImage(id, imageUrl)
        }
    }

    fun updateJacketImageBySku(sku: String, imageUrl: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateJacketImageBySku(sku, imageUrl)
        }
    }

    fun navigateToProduction() {
        _currentScreen.value = AppScreen.PRODUCTION
    }

    fun openAddProductionSheet() {
        _isAddProductionSheetOpen.value = true
    }

    fun closeAddProductionSheet() {
        _isAddProductionSheetOpen.value = false
    }

    fun setProductionShiftFilter(shift: String) {
        _productionShiftFilter.value = shift
    }

    fun updateDailyTarget(target: Int) {
        _dailyTarget.value = target.coerceAtLeast(1)
    }

    fun addDailyProduction(
        jacketId: String,
        jacketName: String,
        jacketSku: String,
        batchNumber: String,
        shift: ProductionShift,
        operatorName: String,
        quantityProduced: Int,
        defectCount: Int,
        status: ProductionStatus,
        notes: String,
        addedToStock: Boolean
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(now))
            val currentJacket = allJackets.value.firstOrNull { it.id == jacketId }

            val item = DailyProductionItem(
                id = UUID.randomUUID().toString(),
                date = now,
                dateString = todayStr,
                jacketId = jacketId,
                jacketName = jacketName,
                jacketSku = jacketSku,
                batchNumber = batchNumber.ifBlank { "BATCH-${System.currentTimeMillis() % 10000}" },
                shift = shift,
                operatorName = operatorName.ifBlank { "Operator 1" },
                quantityProduced = quantityProduced,
                defectCount = defectCount,
                status = status,
                notes = notes,
                addedToStock = addedToStock,
                createdAt = now
            )

            repository.addDailyProduction(
                production = item,
                currentJacketQuantity = currentJacket?.quantity
            )
        }
        _isAddProductionSheetOpen.value = false
    }

    fun deleteDailyProduction(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteDailyProduction(id)
        }
    }

    fun seedSampleStock() {
        viewModelScope.launch(Dispatchers.IO) {
            JacketDatabase.populateInitialDatabase(database.jacketDao(), database.dailyProductionDao())
        }
    }
}
