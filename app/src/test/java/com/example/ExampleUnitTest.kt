package com.example

import com.example.data.local.JacketEntity
import com.example.model.Jacket
import com.example.model.JacketItem
import com.example.model.StockStatusLevel
import com.example.model.toJacket
import com.example.ui.AppScreen
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testStockStatusCalculations() {
        val inStock = JacketItem(
            id = "1",
            sku = "JKT-01",
            name = "Bomber",
            style = "Bomber Jacket",
            size = "M",
            material = "Leather",
            color = "Black",
            quantity = 15,
            minThreshold = 5,
            locationRack = "Rack A-01",
            imageUrl = ""
        )
        assertEquals(StockStatusLevel.IN_STOCK, inStock.status)

        val lowStock = inStock.copy(quantity = 3)
        assertEquals(StockStatusLevel.LOW_STOCK, lowStock.status)

        val outOfStock = inStock.copy(quantity = 0)
        assertEquals(StockStatusLevel.OUT_OF_STOCK, outOfStock.status)
    }

    @Test
    fun testTotalStockCalculation() {
        val jackets = listOf(
            JacketItem("1", "SKU1", "Jacket A", "Bomber", "M", "Leather", "Black", 10, 5, "Rack A", ""),
            JacketItem("2", "SKU2", "Jacket B", "Trench", "L", "Cotton", "Beige", 5, 5, "Rack B", ""),
            JacketItem("3", "SKU3", "Jacket C", "Denim", "S", "Cotton", "Navy", 0, 5, "Rack C", "")
        )

        val totalUnits = jackets.sumOf { it.quantity }
        val inStockCount = jackets.count { it.status == StockStatusLevel.IN_STOCK }
        val lowStockCount = jackets.count { it.status == StockStatusLevel.LOW_STOCK }
        val outOfStockCount = jackets.count { it.status == StockStatusLevel.OUT_OF_STOCK }

        assertEquals(15, totalUnits)
        assertEquals(1, inStockCount)
        assertEquals(1, lowStockCount)
        assertEquals(1, outOfStockCount)
    }

    @Test
    fun testEntityConversion() {
        val entity = JacketEntity(
            id = "test-123",
            sku = "JKT-TEST-L",
            name = "Test Trench",
            style = "Trench Coat",
            size = "L",
            material = "Cotton",
            color = "Beige",
            quantity = 10,
            locationRack = "Rack B-01",
            imageUrl = "http://example.com/test.png"
        )

        val model = entity.toModel()
        assertEquals(entity.id, model.id)
        assertEquals(entity.sku, model.sku)
        assertEquals(entity.name, model.name)
        assertEquals(entity.style, model.style)
        assertEquals(entity.quantity, model.quantity)

        val roundTrip = JacketEntity.fromModel(model, entity.createdAt)
        assertEquals(entity, roundTrip)
    }

    @Test
    fun testAppScreensEnumValues() {
        val screens = AppScreen.values()
        assertTrue(screens.contains(AppScreen.HOME))
        assertTrue(screens.contains(AppScreen.INVENTORY))
        assertTrue(screens.contains(AppScreen.ADD_JACKET))
        assertTrue(screens.contains(AppScreen.REPORTS))
    }

    @Test
    fun testStyleFilteringAndSorting() {
        val jackets = listOf(
            JacketItem("1", "SKU1", "Alpha Parka", "Parka", "L", "Polyester", "Green", 12, 5, "Rack P-01", ""),
            JacketItem("2", "SKU2", "Classic Bomber", "Bomber Jacket", "M", "Leather", "Black", 25, 5, "Rack B-01", ""),
            JacketItem("3", "SKU3", "Vintage Denim", "Denim Jacket", "S", "Denim", "Blue", 8, 5, "Rack D-01", ""),
            JacketItem("4", "SKU4", "Winter Parka", "Parka", "XL", "Nylon", "Navy", 5, 5, "Rack P-02", "")
        )

        // Filter by Parka
        val parkas = jackets.filter { it.style.equals("Parka", ignoreCase = true) }
        assertEquals(2, parkas.size)
        assertTrue(parkas.all { it.style == "Parka" })

        // Filter by Bomber Jacket
        val bombers = jackets.filter { it.style.equals("Bomber Jacket", ignoreCase = true) }
        assertEquals(1, bombers.size)
        assertEquals("Classic Bomber", bombers[0].name)

        // Filter by Denim Jacket
        val denims = jackets.filter { it.style.equals("Denim Jacket", ignoreCase = true) }
        assertEquals(1, denims.size)
        assertEquals("Vintage Denim", denims[0].name)

        // Sort by Style Ascending (Bomber -> Denim -> Parka)
        val sortedByStyle = jackets.sortedWith(compareBy({ it.style }, { it.name }))
        assertEquals("Bomber Jacket", sortedByStyle[0].style)
        assertEquals("Denim Jacket", sortedByStyle[1].style)
        assertEquals("Parka", sortedByStyle[2].style)
        assertEquals("Alpha Parka", sortedByStyle[2].name)
        assertEquals("Winter Parka", sortedByStyle[3].name)

        // Sort by Style Descending (Parka -> Denim -> Bomber)
        val sortedByStyleDesc = jackets.sortedWith(compareByDescending<JacketItem> { it.style }.thenBy { it.name })
        assertEquals("Parka", sortedByStyleDesc[0].style)
        assertEquals("Parka", sortedByStyleDesc[1].style)
        assertEquals("Denim Jacket", sortedByStyleDesc[2].style)
        assertEquals("Bomber Jacket", sortedByStyleDesc[3].style)
    }

    @Test
    fun testEmptyInventoryStateDetection() {
        val emptyInventory = emptyList<JacketItem>()
        assertTrue(emptyInventory.isEmpty())

        // In empty inventory, total items and stock are 0
        val totalUnits = emptyInventory.sumOf { it.quantity }
        assertEquals(0, totalUnits)

        val isDatabaseEmpty = emptyInventory.isEmpty()
        assertTrue(isDatabaseEmpty)
    }

    @Test
    fun testFilteredEmptyStateDetection() {
        val inventory = listOf(
            JacketItem("1", "SKU1", "Alpha Parka", "Parka", "L", "Polyester", "Green", 12, 5, "Rack P-01", ""),
            JacketItem("2", "SKU2", "Classic Bomber", "Bomber Jacket", "M", "Leather", "Black", 25, 5, "Rack B-01", "")
        )

        assertFalse(inventory.isEmpty())

        // Filter for a style not present (e.g. "Puffer" or "Fleece")
        val filtered = inventory.filter { it.style.equals("Fleece Jacket", ignoreCase = true) }
        assertTrue(filtered.isEmpty())

        // Filter state: DB is not empty, but filtered result is empty
        val isDatabaseEmpty = inventory.isEmpty()
        val isFilteredEmpty = filtered.isEmpty() && !isDatabaseEmpty

        assertFalse(isDatabaseEmpty)
        assertTrue(isFilteredEmpty)
    }

    @Test
    fun testProductionScreenEnumPresence() {
        val screens = AppScreen.values()
        assertTrue(screens.contains(AppScreen.PRODUCTION))
    }

    @Test
    fun testDailyProductionCalculations() {
        val item = com.example.model.DailyProductionItem(
            id = "prod-test-1",
            date = System.currentTimeMillis(),
            dateString = "2026-10-31",
            jacketId = "jkt-001",
            jacketName = "Tactical MA-1 Bomber",
            jacketSku = "JKT-BMB-BLK-M",
            batchNumber = "BATCH-01",
            shift = com.example.model.ProductionShift.MORNING,
            operatorName = "Budi Santoso",
            quantityProduced = 25,
            defectCount = 2,
            status = com.example.model.ProductionStatus.COMPLETED,
            notes = "Jahitan rapi",
            addedToStock = true
        )

        assertEquals(23, item.netAcceptedQuantity)
        assertEquals(8.0f, item.defectRatePercentage, 0.01f)
    }

    @Test
    fun testDailyProductionSummaryAggregation() {
        val batch1 = com.example.model.DailyProductionItem(
            id = "1",
            date = 1000L,
            dateString = "2026-10-31",
            jacketId = "jkt-01",
            jacketName = "Bomber",
            jacketSku = "BMB-1",
            batchNumber = "B-1",
            shift = com.example.model.ProductionShift.MORNING,
            operatorName = "Op 1",
            quantityProduced = 30,
            defectCount = 1,
            status = com.example.model.ProductionStatus.COMPLETED,
            notes = ""
        )
        val batch2 = com.example.model.DailyProductionItem(
            id = "2",
            date = 2000L,
            dateString = "2026-10-31",
            jacketId = "jkt-02",
            jacketName = "Denim",
            jacketSku = "DNM-1",
            batchNumber = "B-2",
            shift = com.example.model.ProductionShift.AFTERNOON,
            operatorName = "Op 2",
            quantityProduced = 20,
            defectCount = 1,
            status = com.example.model.ProductionStatus.COMPLETED,
            notes = ""
        )

        val totalProduced = batch1.quantityProduced + batch2.quantityProduced
        val totalDefects = batch1.defectCount + batch2.defectCount
        val totalAccepted = (totalProduced - totalDefects).coerceAtLeast(0)
        val target = 60
        val fulfillmentPct = ((totalAccepted.toFloat() / target.toFloat()) * 100f)

        assertEquals(50, totalProduced)
        assertEquals(2, totalDefects)
        assertEquals(48, totalAccepted)
        assertEquals(80.0f, fulfillmentPct, 0.01f)
    }

    @Test
    fun testDailyProductionEntityRoundtrip() {
        val entity = com.example.data.local.DailyProductionEntity(
            id = "entity-prod-1",
            date = 1700000000000L,
            dateString = "2026-10-31",
            jacketId = "jkt-100",
            jacketName = "Overcoat",
            jacketSku = "OVC-01",
            batchNumber = "BATCH-OVC-1",
            shiftName = "AFTERNOON",
            operatorName = "Siti",
            quantityProduced = 40,
            defectCount = 2,
            statusName = "COMPLETED",
            notes = "Bahan wol tebal",
            addedToStock = true,
            createdAt = 1700000000000L
        )

        val model = entity.toModel()
        assertEquals("entity-prod-1", model.id)
        assertEquals(com.example.model.ProductionShift.AFTERNOON, model.shift)
        assertEquals(com.example.model.ProductionStatus.COMPLETED, model.status)
        assertEquals(38, model.netAcceptedQuantity)

        val backToEntity = com.example.data.local.DailyProductionEntity.fromModel(model)
        assertEquals(entity, backToEntity)
    }

    @Test
    fun testManualSkuInputFormattingAndSanitization() {
        val rawInput = "whr-bomber 001 #special!"
        val sanitized = rawInput.uppercase().filter { it.isLetterOrDigit() || it == '-' || it == '_' }
        assertEquals("WHR-BOMBER001SPECIAL", sanitized)

        val cleanBarcode = "whr-custom-99".uppercase().filter { it.isLetterOrDigit() || it == '-' || it == '_' }
        assertEquals("WHR-CUSTOM-99", cleanBarcode)
    }

    @Test
    fun testManualSkuDuplicateDetection() {
        val inventory = listOf(
            JacketItem("1", "JKT-BOM-NEW-M", "Bomber Jacket", "Bomber Jacket", "M", "Leather", "Black", 10, 5, "Rack A", ""),
            JacketItem("2", "WHR-0099", "Parka Winter", "Parka", "L", "Polyester", "Green", 5, 5, "Rack B", "")
        )

        // Existing SKU (case-insensitive)
        val duplicateInput = "jkt-bom-new-m"
        val isDuplicate = inventory.any { it.sku.equals(duplicateInput.trim(), ignoreCase = true) }
        assertTrue(isDuplicate)

        val duplicateInput2 = "WHR-0099"
        val isDuplicate2 = inventory.any { it.sku.equals(duplicateInput2.trim(), ignoreCase = true) }
        assertTrue(isDuplicate2)

        // New unique manual SKU
        val uniqueInput = "WHR-CUSTOM-001"
        val isUniqueDuplicate = inventory.any { it.sku.equals(uniqueInput.trim(), ignoreCase = true) }
        assertFalse(isUniqueDuplicate)
    }

    @Test
    fun testAutoVsManualSkuSelection() {
        var isManualMode = false
        var manualSkuText = ""
        val autoGeneratedSku = "JKT-BOM-TAC-M"

        var effectiveSku = if (isManualMode) manualSkuText.trim() else autoGeneratedSku
        assertEquals("JKT-BOM-TAC-M", effectiveSku)

        // Switch to manual mode
        isManualMode = true
        manualSkuText = "WHR-CUSTOM-BARCODE-888"
        effectiveSku = if (isManualMode) manualSkuText.trim() else autoGeneratedSku
        assertEquals("WHR-CUSTOM-BARCODE-888", effectiveSku)
    }

    @Test
    fun testJacketDataClassInstantiationAndProperties() {
        val jacket = com.example.model.Jacket(
            id = "jkt-001",
            name = "Flight Bomber",
            category = "Bomber",
            size = "L",
            quantity = 20
        )

        assertEquals("jkt-001", jacket.id)
        assertEquals("Flight Bomber", jacket.name)
        assertEquals("Bomber", jacket.category)
        assertEquals("L", jacket.size)
        assertEquals(20, jacket.quantity)
        assertEquals(StockStatusLevel.IN_STOCK, jacket.status)
        assertEquals("Cotton", jacket.material)
        assertEquals("Black", jacket.color)
        assertEquals("Rack A-01", jacket.locationRack)
    }

    @Test
    fun testJacketDataClassStatusAndDefaults() {
        val baseJacket = com.example.model.Jacket(
            name = "Winter Parka",
            category = "Parka",
            size = "XL",
            quantity = 4
        )

        // ID should be generated non-empty UUID
        assertTrue(baseJacket.id.isNotBlank())
        // 4 <= minThreshold (5) -> LOW_STOCK
        assertEquals(StockStatusLevel.LOW_STOCK, baseJacket.status)

        // 0 -> OUT_OF_STOCK
        val outOfStockJacket = baseJacket.copy(quantity = 0)
        assertEquals(StockStatusLevel.OUT_OF_STOCK, outOfStockJacket.status)

        // 10 -> IN_STOCK
        val inStockJacket = baseJacket.copy(quantity = 10)
        assertEquals(StockStatusLevel.IN_STOCK, inStockJacket.status)
    }

    @Test
    fun testJacketConversionHelpers() {
        val jacket = com.example.model.Jacket(
            id = "jkt-test",
            name = "Retro Denim",
            category = "Denim",
            size = "M",
            quantity = 15,
            sku = "JKT-DNM-BLU-M",
            material = "Denim",
            color = "Blue",
            locationRack = "Rack D-02",
            unitPrice = 79.99
        )

        // Test conversion to JacketItem
        val jacketItem = jacket.toJacketItem()
        assertEquals(jacket.id, jacketItem.id)
        assertEquals(jacket.name, jacketItem.name)
        assertEquals(jacket.category, jacketItem.style)
        assertEquals(jacket.size, jacketItem.size)
        assertEquals(jacket.quantity, jacketItem.quantity)
        assertEquals(jacket.sku, jacketItem.sku)
        assertEquals(jacket.material, jacketItem.material)
        assertEquals(jacket.color, jacketItem.color)

        // Test conversion back to Jacket
        val convertedBack = com.example.model.Jacket.fromJacketItem(jacketItem)
        assertEquals(jacket, convertedBack)

        // Test JacketItem extension toJacket()
        val extensionConverted = jacketItem.toJacket()
        assertEquals(jacket, extensionConverted)
    }

    @Test
    fun testJacketInventoryUiStateFiltering() {
        val jackets = listOf(
            Jacket(id = "1", name = "Alpha Bomber", category = "Bomber", size = "M", quantity = 10, sku = "JKT-BMB-01"),
            Jacket(id = "2", name = "Bravo Parka", category = "Parka", size = "L", quantity = 3, sku = "JKT-PRK-01"),
            Jacket(id = "3", name = "Charlie Denim", category = "Denim", size = "S", quantity = 0, sku = "JKT-DNM-01"),
            Jacket(id = "4", name = "Delta Bomber", category = "Bomber", size = "XL", quantity = 25, sku = "JKT-BMB-02")
        )

        // Category filter
        val bomberOnly = jackets.filter { it.category.equals("Bomber", ignoreCase = true) }
        assertEquals(2, bomberOnly.size)

        // Size filter
        val mOnly = jackets.filter { it.size.equals("M", ignoreCase = true) }
        assertEquals(1, mOnly.size)
        assertEquals("Alpha Bomber", mOnly.first().name)

        // Status filter: LOW_STOCK
        val lowStockOnly = jackets.filter { it.status == StockStatusLevel.LOW_STOCK }
        assertEquals(1, lowStockOnly.size)
        assertEquals("Bravo Parka", lowStockOnly.first().name)

        // Status filter: OUT_OF_STOCK
        val outOfStockOnly = jackets.filter { it.status == StockStatusLevel.OUT_OF_STOCK }
        assertEquals(1, outOfStockOnly.size)
        assertEquals("Charlie Denim", outOfStockOnly.first().name)
    }

    @Test
    fun testJacketInventorySortingOptions() {
        val jackets = listOf(
            Jacket(id = "1", name = "Zeta Bomber", category = "Bomber", size = "M", quantity = 5),
            Jacket(id = "2", name = "Alpha Parka", category = "Parka", size = "L", quantity = 30),
            Jacket(id = "3", name = "Beta Denim", category = "Denim", size = "S", quantity = 15)
        )

        // NAME_ASC
        val nameAsc = jackets.sortedBy { it.name.lowercase() }
        assertEquals("Alpha Parka", nameAsc[0].name)
        assertEquals("Beta Denim", nameAsc[1].name)
        assertEquals("Zeta Bomber", nameAsc[2].name)

        // NAME_DESC
        val nameDesc = jackets.sortedByDescending { it.name.lowercase() }
        assertEquals("Zeta Bomber", nameDesc[0].name)
        assertEquals("Alpha Parka", nameDesc[2].name)

        // QTY_ASC
        val qtyAsc = jackets.sortedBy { it.quantity }
        assertEquals(5, qtyAsc[0].quantity)
        assertEquals(30, qtyAsc[2].quantity)

        // QTY_DESC
        val qtyDesc = jackets.sortedByDescending { it.quantity }
        assertEquals(30, qtyDesc[0].quantity)
        assertEquals(5, qtyDesc[2].quantity)

        // CATEGORY_ASC
        val catAsc = jackets.sortedWith(compareBy({ it.category.lowercase() }, { it.name.lowercase() }))
        assertEquals("Bomber", catAsc[0].category)
        assertEquals("Denim", catAsc[1].category)
        assertEquals("Parka", catAsc[2].category)
    }

    @Test
    fun testJacketInventoryUiStateSummaryMetrics() {
        val jackets = listOf(
            Jacket(id = "1", name = "Jacket A", category = "Bomber", size = "M", quantity = 10, minThreshold = 5),
            Jacket(id = "2", name = "Jacket B", category = "Parka", size = "L", quantity = 4, minThreshold = 5),
            Jacket(id = "3", name = "Jacket C", category = "Denim", size = "S", quantity = 0, minThreshold = 5),
            Jacket(id = "4", name = "Jacket D", category = "Windbreaker", size = "XL", quantity = 2, minThreshold = 5)
        )

        val totalUnits = jackets.sumOf { it.quantity }
        val inStockCount = jackets.count { it.status == StockStatusLevel.IN_STOCK }
        val lowStockCount = jackets.count { it.status == StockStatusLevel.LOW_STOCK }
        val outOfStockCount = jackets.count { it.status == StockStatusLevel.OUT_OF_STOCK }

        assertEquals(16, totalUnits)
        assertEquals(4, jackets.size)
        assertEquals(1, inStockCount)
        assertEquals(2, lowStockCount)
        assertEquals(1, outOfStockCount)

        val state = com.example.ui.JacketInventoryUiState(
            jackets = jackets,
            filteredJackets = jackets,
            totalJacketsInStock = totalUnits,
            totalItemCount = jackets.size,
            inStockCount = inStockCount,
            lowStockCount = lowStockCount,
            outOfStockCount = outOfStockCount
        )

        assertEquals(16, state.totalJacketsInStock)
        assertEquals(4, state.totalItemCount)
        assertEquals(1, state.inStockCount)
        assertEquals(2, state.lowStockCount)
        assertEquals(1, state.outOfStockCount)
    }

    @Test
    fun testStandardIndonesianLanguageSupport() {
        val styles = com.example.model.CatalogData.STYLES
        assertTrue(styles.isNotEmpty())
        // Verify Indonesian titles and descriptions exist and are standard Indonesian
        assertTrue(styles.all { it.titleId.isNotBlank() && it.descId.isNotBlank() })

        val sizes = com.example.model.CatalogData.SIZES
        assertTrue(sizes.all { it.descId.contains("dada", ignoreCase = true) })
        val sizeTitles = sizes.map { it.title }
        assertTrue(sizeTitles.contains("XXL"))
        assertTrue(sizeTitles.contains("3XL"))

        val statuses = com.example.model.CatalogData.STOCK_STATUSES
        val inStock = statuses.first { it.status == StockStatusLevel.IN_STOCK }
        val lowStock = statuses.first { it.status == StockStatusLevel.LOW_STOCK }
        val outOfStock = statuses.first { it.status == StockStatusLevel.OUT_OF_STOCK }

        assertEquals("Stok Tersedia", inStock.titleId)
        assertEquals("Stok Sedikit", lowStock.titleId)
        assertEquals("Habis Stok", outOfStock.titleId)
    }

    @Test
    fun testNewColorsAddedToCatalog() {
        val colors = com.example.model.CatalogData.COLORS
        val colorTitles = colors.map { it.titleId.lowercase() } + colors.map { it.titleEn.lowercase() }

        val requestedColors = listOf(
            "pink",
            "beige",
            "maroon",
            "merah",
            "abu",
            "hijau",
            "coklat",
            "caramel",
            "putih"
        )

        for (requested in requestedColors) {
            assertTrue(
                "Color '$requested' should be present in CatalogData.COLORS",
                colorTitles.any { it.contains(requested, ignoreCase = true) }
            )
        }

        // Verify specific color hex values and attributes
        val pink = colors.first { it.titleId.equals("Pink", ignoreCase = true) }
        val white = colors.first { it.titleId.equals("Putih", ignoreCase = true) }
        val red = colors.first { it.titleId.equals("Merah", ignoreCase = true) }
        val brown = colors.first { it.titleId.equals("Coklat", ignoreCase = true) }
        val caramel = colors.first { it.titleId.equals("Caramel", ignoreCase = true) }

        assertEquals(0xFFFFFFFF, white.hexColor)
        assertEquals(0xFFF472B6, pink.hexColor)
        assertEquals(0xFFDC2626, red.hexColor)
        assertEquals(0xFF78350F, brown.hexColor)
        assertEquals(0xFFC27835, caramel.hexColor)
    }

    @Test
    fun testJacketListSortingByStockAndAlphabetical() {
        val jackets = listOf(
            JacketItem("1", "SKU1", "Zeta Bomber", "Bomber Jacket", "M", "Leather", "Black", 5, 5, "Rack A", ""),
            JacketItem("2", "SKU2", "Alpha Parka", "Parka", "L", "Cotton", "Green", 30, 5, "Rack B", ""),
            JacketItem("3", "SKU3", "Bravo Denim", "Denim Jacket", "S", "Denim", "Blue", 12, 5, "Rack C", ""),
            JacketItem("4", "SKU4", "Delta Trench", "Trench Coat", "XL", "Wool", "Navy", 1, 5, "Rack D", "")
        )

        // Sort by Stock Quantity: High to Low (QTY_DESC)
        val sortedQtyDesc = jackets.sortedByDescending { it.quantity }
        assertEquals(30, sortedQtyDesc[0].quantity)
        assertEquals("Alpha Parka", sortedQtyDesc[0].name)
        assertEquals(12, sortedQtyDesc[1].quantity)
        assertEquals(5, sortedQtyDesc[2].quantity)
        assertEquals(1, sortedQtyDesc[3].quantity)

        // Sort by Stock Quantity: Low to High (QTY_ASC)
        val sortedQtyAsc = jackets.sortedBy { it.quantity }
        assertEquals(1, sortedQtyAsc[0].quantity)
        assertEquals("Delta Trench", sortedQtyAsc[0].name)
        assertEquals(30, sortedQtyAsc[3].quantity)

        // Sort Alphabetically by Name: A to Z (NAME_ASC)
        val sortedNameAsc = jackets.sortedBy { it.name.lowercase() }
        assertEquals("Alpha Parka", sortedNameAsc[0].name)
        assertEquals("Bravo Denim", sortedNameAsc[1].name)
        assertEquals("Delta Trench", sortedNameAsc[2].name)
        assertEquals("Zeta Bomber", sortedNameAsc[3].name)

        // Sort Alphabetically by Name: Z to A (NAME_DESC)
        val sortedNameDesc = jackets.sortedByDescending { it.name.lowercase() }
        assertEquals("Zeta Bomber", sortedNameDesc[0].name)
        assertEquals("Delta Trench", sortedNameDesc[1].name)
        assertEquals("Bravo Denim", sortedNameDesc[2].name)
        assertEquals("Alpha Parka", sortedNameDesc[3].name)
    }

    @Test
    fun testDemographicCategoryFiltering() {
        val jackets = listOf(
            JacketItem("1", "SKU1", "Mens Leather Bomber", "Bomber Jacket", "L", "Leather", "Black", 15, 5, "Rack A-01", "", genderCategory = "Jaket Pria"),
            JacketItem("2", "SKU2", "Mens Trench Coat", "Trench Coat", "XL", "Cotton", "Khaki", 8, 5, "Rack A-02", "", genderCategory = "Jaket Pria"),
            JacketItem("3", "SKU3", "Womens Crop Denim", "Denim Jacket", "S", "Denim", "Blue", 20, 5, "Rack B-01", "", genderCategory = "Jaket Wanita"),
            JacketItem("4", "SKU4", "Womens Elegant Parka", "Parka", "M", "Nylon", "Pink", 12, 5, "Rack B-02", "", genderCategory = "Jaket Wanita"),
            JacketItem("5", "SKU5", "Kids Dino Puffer", "Puffer Jacket", "XS", "Polyester", "Green", 25, 5, "Rack C-01", "", genderCategory = "Jaket Anak"),
            JacketItem("6", "SKU6", "Kids Warm Parka", "Parka", "S", "Fleece", "Red", 10, 5, "Rack C-02", "", genderCategory = "Jaket Anak"),
            JacketItem("7", "SKU7", "Unisex Minimalist Bomber", "Bomber Jacket", "M", "Cotton", "Abu", 18, 5, "Rack D-01", "", genderCategory = "Jaket Unisex")
        )

        // Filter by Jaket Pria
        val priaJackets = jackets.filter { it.genderCategory.equals("Jaket Pria", ignoreCase = true) }
        assertEquals(2, priaJackets.size)
        assertTrue(priaJackets.all { it.genderCategory == "Jaket Pria" })

        // Filter by Jaket Wanita
        val wanitaJackets = jackets.filter { it.genderCategory.equals("Jaket Wanita", ignoreCase = true) }
        assertEquals(2, wanitaJackets.size)
        assertTrue(wanitaJackets.all { it.genderCategory == "Jaket Wanita" })

        // Filter by Jaket Anak
        val anakJackets = jackets.filter { it.genderCategory.equals("Jaket Anak", ignoreCase = true) }
        assertEquals(2, anakJackets.size)
        assertTrue(anakJackets.all { it.genderCategory == "Jaket Anak" })

        // Filter by Jaket Unisex
        val unisexJackets = jackets.filter { it.genderCategory.equals("Jaket Unisex", ignoreCase = true) }
        assertEquals(1, unisexJackets.size)
        assertEquals("Unisex Minimalist Bomber", unisexJackets[0].name)

        // Combined filtering: Jaket Wanita + Denim Jacket
        val wanitaDenim = jackets.filter {
            it.genderCategory.equals("Jaket Wanita", ignoreCase = true) &&
            it.style.equals("Denim Jacket", ignoreCase = true)
        }
        assertEquals(1, wanitaDenim.size)
        assertEquals("Womens Crop Denim", wanitaDenim[0].name)
    }

    @Test
    fun testDemographicCategoryPersistence() {
        val entity = JacketEntity(
            id = "test-cat-1",
            sku = "JKT-CAT-01",
            name = "Jaket Anak Lucu",
            style = "Puffer Jacket",
            size = "XS",
            material = "Polyester",
            color = "Kuning",
            quantity = 14,
            locationRack = "Rack K-01",
            imageUrl = "",
            genderCategory = "Jaket Anak"
        )

        val model = entity.toModel()
        assertEquals("Jaket Anak", model.genderCategory)

        val jacket = model.toJacket()
        assertEquals("Jaket Anak", jacket.genderCategory)

        val jacketItem = jacket.toJacketItem()
        assertEquals("Jaket Anak", jacketItem.genderCategory)

        val backEntity = JacketEntity.fromModel(model, entity.createdAt)
        assertEquals("Jaket Anak", backEntity.genderCategory)
    }

    @Test
    fun testCatalogDemographicCategories() {
        val categories = com.example.model.CatalogData.GENDER_CATEGORIES
        assertEquals(4, categories.size)

        val titles = categories.map { it.titleId }
        assertTrue(titles.contains("Jaket Pria"))
        assertTrue(titles.contains("Jaket Wanita"))
        assertTrue(titles.contains("Jaket Anak"))
        assertTrue(titles.contains("Jaket Unisex"))
    }

    @Test
    fun testCsvGenerationFormatting() {
        val jackets = listOf(
            JacketItem(
                id = "jkt-001",
                sku = "JKT-BMB-01",
                name = "Tactical Bomber",
                style = "Bomber Jacket",
                size = "L",
                material = "Leather",
                color = "Black",
                quantity = 15,
                minThreshold = 5,
                locationRack = "Rack A-01",
                imageUrl = "",
                genderCategory = "Jaket Pria",
                unitPrice = 119.99
            ),
            JacketItem(
                id = "jkt-002",
                sku = "JKT-DNM-02",
                name = "Crop Denim",
                style = "Denim Jacket",
                size = "S",
                material = "Denim",
                color = "Blue",
                quantity = 8,
                minThreshold = 5,
                locationRack = "Rack B-01",
                imageUrl = "",
                genderCategory = "Jaket Wanita",
                unitPrice = 89.99
            )
        )

        val csv = com.example.util.CsvExportHelper.generateCsv(jackets, isIndonesian = true)
        assertNotNull(csv)
        assertTrue(csv.startsWith("\uFEFF")) // UTF-8 BOM

        val lines = csv.trimStart('\uFEFF').lines().filter { it.isNotBlank() }
        assertEquals(3, lines.size) // 1 Header + 2 Data rows

        // Check header column names
        val header = lines[0]
        assertTrue(header.contains("ID"))
        assertTrue(header.contains("SKU"))
        assertTrue(header.contains("Nama Produk"))
        assertTrue(header.contains("Kategori Demografis"))
        assertTrue(header.contains("Jumlah Stok"))
        assertTrue(header.contains("Lokasi Rak"))

        // Check row 1 content
        val row1 = lines[1]
        assertTrue(row1.contains("jkt-001"))
        assertTrue(row1.contains("JKT-BMB-01"))
        assertTrue(row1.contains("Tactical Bomber"))
        assertTrue(row1.contains("Jaket Pria"))
        assertTrue(row1.contains("15"))
        assertTrue(row1.contains("119.99"))

        // Check row 2 content
        val row2 = lines[2]
        assertTrue(row2.contains("jkt-002"))
        assertTrue(row2.contains("JKT-DNM-02"))
        assertTrue(row2.contains("Crop Denim"))
        assertTrue(row2.contains("Jaket Wanita"))
        assertTrue(row2.contains("8"))
        assertTrue(row2.contains("89.99"))
    }

    @Test
    fun testCsvEscapingSpecialCharacters() {
        // Test escapeCsv with comma, quotes, and newlines
        val normalText = "Bomber Jacket"
        assertEquals("Bomber Jacket", com.example.util.CsvExportHelper.escapeCsv(normalText))

        val textWithComma = "Leather, Premium"
        assertEquals("\"Leather, Premium\"", com.example.util.CsvExportHelper.escapeCsv(textWithComma))

        val textWithQuotes = "Classic \"MA-1\" Bomber"
        assertEquals("\"Classic \"\"MA-1\"\" Bomber\"", com.example.util.CsvExportHelper.escapeCsv(textWithQuotes))

        val textWithBoth = "MA-1, \"Special\" Edition"
        assertEquals("\"MA-1, \"\"Special\"\" Edition\"", com.example.util.CsvExportHelper.escapeCsv(textWithBoth))
    }

    @Test
    fun testCsvEnglishHeadersGeneration() {
        val jackets = listOf(
            JacketItem(
                id = "jkt-01",
                sku = "SKU-01",
                name = "Winter Parka",
                style = "Parka",
                size = "XL",
                material = "Nylon",
                color = "Green",
                quantity = 10,
                minThreshold = 5,
                locationRack = "Rack C-01",
                imageUrl = "",
                genderCategory = "Jaket Anak"
            )
        )

        val csvEn = com.example.util.CsvExportHelper.generateCsv(jackets, isIndonesian = false)
        val header = csvEn.trimStart('\uFEFF').lines().first()

        assertTrue(header.contains("Product Name"))
        assertTrue(header.contains("Demographic Category"))
        assertTrue(header.contains("Stock Quantity"))
        assertTrue(header.contains("Estimated Total Value (USD)"))
    }

    @Test
    fun testSkuImageAssignmentAndPersistence() {
        val testUrl = "https://example.com/custom-sku-image.png"
        val entity = JacketEntity(
            id = "test-img-01",
            sku = "JKT-IMG-001",
            name = "Parka Bergambar Khusus",
            style = "Parka",
            size = "L",
            material = "Cotton",
            color = "Army",
            quantity = 8,
            locationRack = "Rack B-04",
            imageUrl = testUrl,
            genderCategory = "Jaket Pria"
        )

        assertEquals(testUrl, entity.imageUrl)

        val model = entity.toModel()
        assertEquals(testUrl, model.imageUrl)

        val jacket = model.toJacket()
        assertEquals(testUrl, jacket.imageUrl)

        val item = jacket.toJacketItem()
        assertEquals(testUrl, item.imageUrl)

        // Updated image on entity
        val updatedUrl = "content://media/external/images/media/12345"
        val updatedEntity = entity.copy(imageUrl = updatedUrl)
        assertEquals(updatedUrl, updatedEntity.imageUrl)
    }

    @Test
    fun testCuratedPresetsCatalog() {
        val presets = com.example.ui.dialogs.CURATED_PRESETS
        assertTrue(presets.isNotEmpty())
        presets.forEach { preset ->
            assertTrue(preset.id.isNotBlank())
            assertTrue(preset.nameId.isNotBlank())
            assertTrue(preset.nameEn.isNotBlank())
            assertTrue(preset.imageUrl.startsWith("http"))
        }
    }

    @Test
    fun testSearchFilterByNameAndSku() {
        val jackets = listOf(
            JacketItem(
                id = "jkt-01",
                sku = "JKT-BOM-BLK-M",
                name = "Classic Pilot Bomber",
                style = "Bomber Jacket",
                size = "M",
                material = "Leather",
                color = "Black",
                quantity = 10,
                locationRack = "Rack A-01",
                imageUrl = ""
            ),
            JacketItem(
                id = "jkt-02",
                sku = "JKT-TRN-BEI-L",
                name = "Heritage Trench Coat",
                style = "Trench Coat",
                size = "L",
                material = "Cotton",
                color = "Beige",
                quantity = 5,
                locationRack = "Rack B-02",
                imageUrl = ""
            ),
            JacketItem(
                id = "jkt-03",
                sku = "JKT-DNM-BLU-XXL",
                name = "Vintage Denim Trucker",
                style = "Denim Jacket",
                size = "XXL",
                material = "Cotton",
                color = "Blue",
                quantity = 8,
                locationRack = "Rack C-03",
                imageUrl = ""
            )
        )

        // Search by partial SKU
        val skuQuery = "trn"
        val skuResults = jackets.filter { it.sku.contains(skuQuery, ignoreCase = true) || it.name.contains(skuQuery, ignoreCase = true) }
        assertEquals(1, skuResults.size)
        assertEquals("JKT-TRN-BEI-L", skuResults[0].sku)

        // Search by partial Name
        val nameQuery = "denim"
        val nameResults = jackets.filter { it.sku.contains(nameQuery, ignoreCase = true) || it.name.contains(nameQuery, ignoreCase = true) }
        assertEquals(1, nameResults.size)
        assertEquals("Vintage Denim Trucker", nameResults[0].name)

        // Search by full SKU
        val fullSku = "JKT-BOM-BLK-M"
        val fullSkuResults = jackets.filter { it.sku.contains(fullSku, ignoreCase = true) || it.name.contains(fullSku, ignoreCase = true) }
        assertEquals(1, fullSkuResults.size)
        assertEquals("Classic Pilot Bomber", fullSkuResults[0].name)
    }

    @Test
    fun testTotalAvailableStockAndUniqueVariantsCalculations() {
        val jackets = listOf(
            JacketItem(
                id = "jkt-01",
                sku = "JKT-BOM-BLK-M",
                name = "Classic Bomber",
                style = "Bomber Jacket",
                size = "M",
                material = "Leather",
                color = "Black",
                quantity = 15,
                locationRack = "Rack A-01",
                imageUrl = ""
            ),
            JacketItem(
                id = "jkt-02",
                sku = "JKT-BOM-BLK-L",
                name = "Classic Bomber",
                style = "Bomber Jacket",
                size = "L",
                material = "Leather",
                color = "Black",
                quantity = 10,
                locationRack = "Rack A-02",
                imageUrl = ""
            ),
            JacketItem(
                id = "jkt-03",
                sku = "JKT-TRN-BEI-S",
                name = "Heritage Trench",
                style = "Trench Coat",
                size = "S",
                material = "Cotton",
                color = "Beige",
                quantity = 0,
                locationRack = "Rack B-01",
                imageUrl = ""
            )
        )

        val totalStock = jackets.sumOf { it.quantity }
        val uniqueVariants = jackets.map { it.sku.ifBlank { it.id } }.distinct().size

        assertEquals(25, totalStock)
        assertEquals(3, uniqueVariants)
    }

    @Test
    fun testInventoryGridCardStockLevelsAndStatus() {
        val inStockItem = JacketItem(
            id = "g-1",
            sku = "JKT-BMB-001",
            name = "Urban Bomber Jacket",
            style = "Bomber Jacket",
            size = "M",
            material = "Nylon",
            color = "Navy",
            quantity = 24,
            minThreshold = 5,
            locationRack = "Rack A-01",
            imageUrl = "https://images.unsplash.com/photo-1551028719-00167b16eac5"
        )

        val lowStockItem = inStockItem.copy(id = "g-2", sku = "JKT-BMB-002", quantity = 3)
        val outOfStockItem = inStockItem.copy(id = "g-3", sku = "JKT-BMB-003", quantity = 0)

        assertEquals(StockStatusLevel.IN_STOCK, inStockItem.status)
        assertEquals(StockStatusLevel.LOW_STOCK, lowStockItem.status)
        assertEquals(StockStatusLevel.OUT_OF_STOCK, outOfStockItem.status)

        // Verify stock formatting strings for grid presentation
        val inStockStr = if (inStockItem.quantity > 0) "${inStockItem.quantity} Unit" else "Kosong"
        val outOfStockStr = if (outOfStockItem.quantity > 0) "${outOfStockItem.quantity} Unit" else "Kosong"

        assertEquals("24 Unit", inStockStr)
        assertEquals("Kosong", outOfStockStr)

        // Verify product image URL and name are accessible for grid presentation
        assertTrue(inStockItem.imageUrl.isNotBlank())
        assertEquals("Urban Bomber Jacket", inStockItem.name)
        assertEquals("JKT-BMB-001", inStockItem.sku)
    }

    @Test
    fun testProductDetailModalBreakdownAndColorVariants() {
        val allJackets = listOf(
            JacketItem("1", "SKU-BLK-M", "Classic Bomber", "Bomber Jacket", "M", "Leather", "Black", 15, 5, "Rack A-01", "url1"),
            JacketItem("2", "SKU-NVY-M", "Classic Bomber", "Bomber Jacket", "M", "Leather", "Navy", 8, 5, "Rack A-02", "url2"),
            JacketItem("3", "SKU-OLV-M", "Classic Bomber", "Bomber Jacket", "M", "Leather", "Olive", 0, 5, "Rack A-03", "url3"),
            JacketItem("4", "SKU-BLK-L", "Classic Bomber", "Bomber Jacket", "L", "Leather", "Black", 12, 5, "Rack A-01", "url1"),
            JacketItem("5", "SKU-BLK-XL", "Classic Bomber", "Bomber Jacket", "XL", "Leather", "Black", 4, 5, "Rack A-01", "url1")
        )

        val targetJacket = allJackets[0]

        // 1. Verify Color Variants calculation for this model
        val sameModelJackets = allJackets.filter { it.name == targetJacket.name && it.style == targetJacket.style }
        val distinctColors = sameModelJackets.map { it.color }.distinct()
        assertTrue(distinctColors.contains("Black"))
        assertTrue(distinctColors.contains("Navy"))
        assertTrue(distinctColors.contains("Olive"))

        // 2. Verify Size breakdown for this model
        val sizeBreakdown = listOf("XS", "S", "M", "L", "XL", "XXL", "3XL").associateWith { size ->
            sameModelJackets.filter { it.size == size }.sumOf { it.quantity }
        }
        assertEquals(23, sizeBreakdown["M"]) // 15 + 8 + 0
        assertEquals(12, sizeBreakdown["L"])
        assertEquals(4, sizeBreakdown["XL"])
        assertEquals(0, sizeBreakdown["XS"])

        // 3. Verify Stock Breakdown Metrics
        val currentStock = targetJacket.quantity
        val threshold = targetJacket.minThreshold
        val isSafe = currentStock > threshold
        assertTrue(isSafe)
        assertEquals(15, currentStock)
        assertEquals(5, threshold)
        assertEquals("Rack A-01", targetJacket.locationRack)
    }

    @Test
    fun testSearchFilterByNameOrStyle() {
        val catalog = listOf(
            JacketItem("1", "SKU-BMB-01", "Urban Pilot", "Bomber Jacket", "L", "Leather", "Black", 10, 5, "Rack A-01", ""),
            JacketItem("2", "SKU-PRK-02", "Winter Arctic", "Parka", "XL", "Nylon", "Olive", 8, 5, "Rack B-02", ""),
            JacketItem("3", "SKU-DNM-03", "Vintage Trucker", "Denim Jacket", "M", "Denim", "Blue", 14, 5, "Rack C-01", ""),
            JacketItem("4", "SKU-TRN-04", "Classic London", "Trench Coat", "S", "Cotton", "Beige", 6, 5, "Rack D-01", ""),
            JacketItem("5", "SKU-PFR-05", "Summit Down", "Puffer Jacket", "XXL", "Polyester", "Navy", 20, 5, "Rack E-01", "")
        )

        // 1. Filter by Name: "Arctic"
        val queryName = "Arctic"
        val nameResults = catalog.filter { jacket ->
            jacket.name.contains(queryName, ignoreCase = true) || jacket.style.contains(queryName, ignoreCase = true)
        }
        assertEquals(1, nameResults.size)
        assertEquals("Winter Arctic", nameResults[0].name)

        // 2. Filter by Style: "Bomber"
        val queryStyle = "Bomber"
        val styleResults = catalog.filter { jacket ->
            jacket.name.contains(queryStyle, ignoreCase = true) || jacket.style.contains(queryStyle, ignoreCase = true)
        }
        assertEquals(1, styleResults.size)
        assertEquals("Urban Pilot", styleResults[0].name)
        assertEquals("Bomber Jacket", styleResults[0].style)

        // 3. Filter by Style: "Parka"
        val parkaResults = catalog.filter { jacket ->
            jacket.name.contains("Parka", ignoreCase = true) || jacket.style.contains("Parka", ignoreCase = true)
        }
        assertEquals(1, parkaResults.size)
        assertEquals("Parka", parkaResults[0].style)

        // 4. Filter by Multi-token query matching Name and Style
        val multiQuery = "Urban Bomber"
        val tokens = multiQuery.split(" ")
        val multiResults = catalog.filter { jacket ->
            tokens.all { token ->
                jacket.name.contains(token, ignoreCase = true) || jacket.style.contains(token, ignoreCase = true)
            }
        }
        assertEquals(1, multiResults.size)
        assertEquals("Urban Pilot", multiResults[0].name)
    }

    @Test
    fun testQrBarcodeScannerIdentifiesExistingJacket() {
        val inventory = listOf(
            JacketItem("j-1", "JKT-BMB-BLK-L", "Urban Pilot Bomber", "Bomber Jacket", "L", "Leather", "Black", 18, 5, "Rack A-01", ""),
            JacketItem("j-2", "JKT-PRK-NAV-M", "Arctic Expedition", "Parka", "M", "Gore-Tex", "Navy", 8, 4, "Rack B-03", "")
        )

        val scannedExactSku = "JKT-BMB-BLK-L"
        val matched1 = inventory.firstOrNull {
            it.sku.equals(scannedExactSku.trim(), ignoreCase = true) ||
            it.id.equals(scannedExactSku.trim(), ignoreCase = true)
        }
        assertNotNull(matched1)
        assertEquals("Urban Pilot Bomber", matched1?.name)
        assertEquals(18, matched1?.quantity)

        // Case-insensitive & trimmed barcode scan
        val scannedLowerWithSpace = "  jkt-prk-nav-m  "
        val matched2 = inventory.firstOrNull {
            it.sku.equals(scannedLowerWithSpace.trim(), ignoreCase = true) ||
            it.id.equals(scannedLowerWithSpace.trim(), ignoreCase = true)
        }
        assertNotNull(matched2)
        assertEquals("Arctic Expedition", matched2?.name)
    }

    @Test
    fun testQrBarcodeScannerDetectsNewUnknownSku() {
        val inventory = listOf(
            JacketItem("j-1", "JKT-BMB-BLK-L", "Urban Pilot Bomber", "Bomber Jacket", "L", "Leather", "Black", 18, 5, "Rack A-01", "")
        )

        val scannedNewSku = "JKT-DNM-BLU-XL"
        val matched = inventory.firstOrNull {
            it.sku.equals(scannedNewSku.trim(), ignoreCase = true)
        }
        assertNull(matched)

        // Parsing scanned SKU into style and size suggestions
        val parts = scannedNewSku.split("-")
        assertEquals(4, parts.size)
        assertEquals("JKT", parts[0])
        assertEquals("DNM", parts[1])
        assertEquals("BLU", parts[2])
        assertEquals("XL", parts[3])

        val resolvedStyle = when (parts[1].uppercase()) {
            "BMB" -> "Bomber Jacket"
            "PRK" -> "Parka"
            "DNM" -> "Denim Jacket"
            "TRN" -> "Trench Coat"
            "PUF" -> "Puffer Jacket"
            else -> "Other"
        }
        assertEquals("Denim Jacket", resolvedStyle)
        assertEquals("XL", parts[3].uppercase())
    }

    @Test
    fun testDashboardStockDistributionByStyle() {
        val inventory = listOf(
            JacketItem("1", "SKU1", "Jacket 1", "Bomber Jacket", "M", "Leather", "Black", 15, 5, "Rack A", ""),
            JacketItem("2", "SKU2", "Jacket 2", "Bomber Jacket", "L", "Leather", "Brown", 10, 5, "Rack A", ""),
            JacketItem("3", "SKU3", "Jacket 3", "Parka", "L", "Nylon", "Green", 20, 5, "Rack B", ""),
            JacketItem("4", "SKU4", "Jacket 4", "Denim Jacket", "S", "Denim", "Blue", 8, 5, "Rack C", "")
        )

        val totalStock = inventory.sumOf { it.quantity }
        assertEquals(53, totalStock)

        val bomberStock = inventory.filter { it.style == "Bomber Jacket" }.sumOf { it.quantity }
        val parkaStock = inventory.filter { it.style == "Parka" }.sumOf { it.quantity }
        val denimStock = inventory.filter { it.style == "Denim Jacket" }.sumOf { it.quantity }

        assertEquals(25, bomberStock)
        assertEquals(20, parkaStock)
        assertEquals(8, denimStock)

        // Percentage calculations
        val bomberPct = (bomberStock.toFloat() / totalStock.toFloat()) * 100f
        assertTrue(bomberPct > 47f && bomberPct < 48f)
    }

    @Test
    fun testDashboardStockDistributionBySize() {
        val inventory = listOf(
            JacketItem("1", "SKU1", "Jacket 1", "Bomber Jacket", "S", "Leather", "Black", 5, 5, "Rack A", ""),
            JacketItem("2", "SKU2", "Jacket 2", "Parka", "M", "Nylon", "Green", 12, 5, "Rack B", ""),
            JacketItem("3", "SKU3", "Jacket 3", "Denim Jacket", "L", "Denim", "Blue", 25, 5, "Rack C", ""),
            JacketItem("4", "SKU4", "Jacket 4", "Puffer Jacket", "XL", "Polyester", "Red", 8, 5, "Rack D", "")
        )

        val sizes = listOf("XS", "S", "M", "L", "XL", "XXL", "3XL")
        val distribution = sizes.associateWith { sizeCode ->
            inventory.filter { it.size.equals(sizeCode, ignoreCase = true) }.sumOf { it.quantity }
        }

        assertEquals(0, distribution["XS"])
        assertEquals(5, distribution["S"])
        assertEquals(12, distribution["M"])
        assertEquals(25, distribution["L"])
        assertEquals(8, distribution["XL"])
        assertEquals(0, distribution["XXL"])
        assertEquals(0, distribution["3XL"])

        val maxVal = distribution.values.maxOrNull() ?: 1
        assertEquals(25, maxVal)
    }

    @Test
    fun testRoomJacketLazyColumnStockStatusMapping() {
        val jackets = listOf(
            JacketItem("j-1", "SKU-IN", "Bomber 1", "Bomber Jacket", "L", "Leather", "Black", 20, 5, "Rack A-01", ""),
            JacketItem("j-2", "SKU-LOW", "Parka 2", "Parka", "M", "Nylon", "Olive", 3, 5, "Rack B-02", ""),
            JacketItem("j-3", "SKU-OUT", "Denim 3", "Denim Jacket", "S", "Denim", "Blue", 0, 5, "Rack C-01", "")
        )

        assertEquals(StockStatusLevel.IN_STOCK, jackets[0].status)
        assertEquals(StockStatusLevel.LOW_STOCK, jackets[1].status)
        assertEquals(StockStatusLevel.OUT_OF_STOCK, jackets[2].status)

        // Verify counts for status filter chips
        val inStockCount = jackets.count { it.status == StockStatusLevel.IN_STOCK }
        val lowStockCount = jackets.count { it.status == StockStatusLevel.LOW_STOCK }
        val outOfStockCount = jackets.count { it.status == StockStatusLevel.OUT_OF_STOCK }

        assertEquals(1, inStockCount)
        assertEquals(1, lowStockCount)
        assertEquals(1, outOfStockCount)
    }

    @Test
    fun testRoomJacketListStatusFiltering() {
        val allJackets = listOf(
            JacketItem("1", "SKU-1", "Alpha Jacket", "Bomber Jacket", "M", "Cotton", "Black", 12, 5, "Rack A", ""),
            JacketItem("2", "SKU-2", "Beta Jacket", "Parka", "L", "Polyester", "Navy", 4, 5, "Rack B", ""),
            JacketItem("3", "SKU-3", "Gamma Jacket", "Trench Coat", "XL", "Wool", "Gray", 0, 5, "Rack C", ""),
            JacketItem("4", "SKU-4", "Delta Jacket", "Denim Jacket", "S", "Denim", "Blue", 1, 5, "Rack D", "")
        )

        // Filter: IN_STOCK
        val inStock = allJackets.filter { it.status == StockStatusLevel.IN_STOCK }
        assertEquals(1, inStock.size)
        assertEquals("Alpha Jacket", inStock[0].name)

        // Filter: LOW_STOCK
        val lowStock = allJackets.filter { it.status == StockStatusLevel.LOW_STOCK }
        assertEquals(2, lowStock.size)
        assertTrue(lowStock.any { it.name == "Beta Jacket" })
        assertTrue(lowStock.any { it.name == "Delta Jacket" })

        // Filter: OUT_OF_STOCK
        val outOfStock = allJackets.filter { it.status == StockStatusLevel.OUT_OF_STOCK }
        assertEquals(1, outOfStock.size)
        assertEquals("Gamma Jacket", outOfStock[0].name)
    }

    @Test
    fun testInputNewJacketDetailsToRoomEntity() {
        val inputName = "Varsity Vintage Retro"
        val inputStyle = "Varsity Jacket"
        val inputSize = "XL"
        val inputQuantity = 24

        val entity = JacketEntity(
            id = "new-uuid-123",
            sku = "JKT-VAR-NEW-XL",
            name = inputName,
            style = inputStyle,
            size = inputSize,
            material = "Wool & Leather",
            color = "Navy Blue",
            quantity = inputQuantity,
            minThreshold = 5,
            locationRack = "Rack B-03",
            imageUrl = ""
        )

        assertEquals("Varsity Vintage Retro", entity.name)
        assertEquals("Varsity Jacket", entity.style)
        assertEquals("XL", entity.size)
        assertEquals(24, entity.quantity)

        val model = entity.toModel()
        assertEquals(StockStatusLevel.IN_STOCK, model.status)
        assertEquals("Varsity Vintage Retro", model.name)
        assertEquals("XL", model.size)
        assertEquals(24, model.quantity)
    }

    @Test
    fun testNewJacketQuantityStockStatusThresholds() {
        // Quantity = 0 -> OUT_OF_STOCK
        val outOfStockEntity = JacketEntity(
            id = "test-0",
            sku = "JKT-0",
            name = "Zero Jacket",
            style = "Windbreaker",
            size = "M",
            material = "Nylon",
            color = "Black",
            quantity = 0,
            locationRack = "Rack Z-01",
            imageUrl = ""
        ).toModel()
        assertEquals(StockStatusLevel.OUT_OF_STOCK, outOfStockEntity.status)

        // Quantity = 3 (<= 5) -> LOW_STOCK
        val lowStockEntity = JacketEntity(
            id = "test-3",
            sku = "JKT-3",
            name = "Low Jacket",
            style = "Bomber Jacket",
            size = "S",
            material = "Leather",
            color = "Brown",
            quantity = 3,
            minThreshold = 5,
            locationRack = "Rack L-02",
            imageUrl = ""
        ).toModel()
        assertEquals(StockStatusLevel.LOW_STOCK, lowStockEntity.status)

        // Quantity = 18 (> 5) -> IN_STOCK
        val inStockEntity = JacketEntity(
            id = "test-18",
            sku = "JKT-18",
            name = "Plentiful Jacket",
            style = "Parka",
            size = "XXL",
            material = "Down",
            color = "Olive",
            quantity = 18,
            minThreshold = 5,
            locationRack = "Rack P-05",
            imageUrl = ""
        ).toModel()
        assertEquals(StockStatusLevel.IN_STOCK, inStockEntity.status)
    }

    @Test
    fun testDeleteItemFromRoomDatabaseList() {
        val initialList = mutableListOf(
            JacketItem("1", "SKU-1", "Alpha Bomber", "Bomber", "M", "Leather", "Black", 10, 5, "Rack A", ""),
            JacketItem("2", "SKU-2", "Beta Parka", "Parka", "L", "Cotton", "Navy", 8, 5, "Rack B", ""),
            JacketItem("3", "SKU-3", "Gamma Denim", "Denim", "XL", "Denim", "Blue", 4, 5, "Rack C", "")
        )

        assertEquals(3, initialList.size)

        // Simulate swipe-to-delete or delete button action removing item with id "2"
        val itemToDelete = initialList.first { it.id == "2" }
        initialList.remove(itemToDelete)

        assertEquals(2, initialList.size)
        assertFalse(initialList.any { it.id == "2" })
        assertTrue(initialList.any { it.id == "1" })
        assertTrue(initialList.any { it.id == "3" })
    }

    @Test
    fun testSwipeToDeleteFilterConsistency() {
        val initialList = listOf(
            JacketItem("1", "SKU-1", "Alpha Bomber", "Bomber", "M", "Leather", "Black", 10, 5, "Rack A", ""),
            JacketItem("2", "SKU-2", "Beta Parka", "Parka", "L", "Cotton", "Navy", 2, 5, "Rack B", "")
        )

        // Verify low stock item status
        val lowStockItem = initialList.first { it.id == "2" }
        assertEquals(StockStatusLevel.LOW_STOCK, lowStockItem.status)

        // After deletion, low stock list is empty
        val updatedList = initialList.filter { it.id != "2" }
        val lowStockAfterDelete = updatedList.filter { it.status == StockStatusLevel.LOW_STOCK }
        assertTrue(lowStockAfterDelete.isEmpty())
        assertEquals(1, updatedList.size)
    }

    @Test
    fun testEditJacketStockLevelAndAttributes() {
        val original = JacketItem(
            id = "item-edit-1",
            sku = "JKT-BOM-001",
            name = "Classic Leather Bomber",
            style = "Bomber Jacket",
            size = "M",
            material = "Leather",
            color = "Black",
            quantity = 4, // Initially LOW_STOCK (<= 5)
            minThreshold = 5,
            locationRack = "Rack A-01",
            imageUrl = ""
        )
        assertEquals(StockStatusLevel.LOW_STOCK, original.status)

        // User updates stock level to 20 and changes rack & size via Edit Bottom Sheet
        val updated = original.copy(
            quantity = 20,
            size = "L",
            locationRack = "Rack B-05"
        )

        assertEquals("L", updated.size)
        assertEquals(20, updated.quantity)
        assertEquals("Rack B-05", updated.locationRack)
        assertEquals(StockStatusLevel.IN_STOCK, updated.status)
    }
}




