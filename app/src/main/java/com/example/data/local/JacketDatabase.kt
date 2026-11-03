package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Database(entities = [JacketEntity::class, DailyProductionEntity::class], version = 3, exportSchema = false)
abstract class JacketDatabase : RoomDatabase() {

    abstract fun jacketDao(): JacketDao
    abstract fun dailyProductionDao(): DailyProductionDao

    companion object {
        @Volatile
        private var INSTANCE: JacketDatabase? = null

        fun getDatabase(
            context: Context,
            scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
        ): JacketDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    JacketDatabase::class.java,
                    "jacket_inventory_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(JacketDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class JacketDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialDatabase(database.jacketDao(), database.dailyProductionDao())
                    }
                }
            }
        }

        suspend fun populateInitialDatabase(
            dao: JacketDao,
            productionDao: DailyProductionDao? = null
        ) {
            val initial = getInitialWarehouseInventoryEntities()
            dao.insertAll(initial)
            productionDao?.let { pDao ->
                val sampleProductions = getInitialProductionEntities()
                pDao.insertAll(sampleProductions)
            }
        }

        private fun getInitialProductionEntities(): List<DailyProductionEntity> {
            val now = System.currentTimeMillis()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val todayStr = dateFormat.format(Date(now))

            return listOf(
                DailyProductionEntity(
                    id = "prod-001",
                    date = now - 14400000,
                    dateString = todayStr,
                    jacketId = "jkt-001",
                    jacketName = "Tactical MA-1 Bomber",
                    jacketSku = "JKT-BMB-BLK-M",
                    batchNumber = "BATCH-BMB-01",
                    shiftName = "MORNING",
                    operatorName = "Budi Santoso",
                    quantityProduced = 25,
                    defectCount = 1,
                    statusName = "COMPLETED",
                    notes = "Finishing ritsleting lancar, jahitan ribbing rapi.",
                    addedToStock = true,
                    createdAt = now - 14400000
                ),
                DailyProductionEntity(
                    id = "prod-002",
                    date = now - 10800000,
                    dateString = todayStr,
                    jacketId = "jkt-003",
                    jacketName = "Raw Selvedge Denim Trucker",
                    jacketSku = "JKT-DNM-NVY-S",
                    batchNumber = "BATCH-DNM-02",
                    shiftName = "MORNING",
                    operatorName = "Siti Aminah",
                    quantityProduced = 20,
                    defectCount = 0,
                    statusName = "COMPLETED",
                    notes = "Bahan denim 14oz, kancing tembaga terpasang presisi.",
                    addedToStock = true,
                    createdAt = now - 10800000
                ),
                DailyProductionEntity(
                    id = "prod-003",
                    date = now - 3600000,
                    dateString = todayStr,
                    jacketId = "jkt-004",
                    jacketName = "Alpine Quilted Down Puffer",
                    jacketSku = "JKT-PFF-BLK-XL",
                    batchNumber = "BATCH-PFF-03",
                    shiftName = "AFTERNOON",
                    operatorName = "Rudi Hermawan",
                    quantityProduced = 18,
                    defectCount = 1,
                    statusName = "COMPLETED",
                    notes = "Isian dakron merata, uji tahan angin lolos QC.",
                    addedToStock = true,
                    createdAt = now - 3600000
                ),
                DailyProductionEntity(
                    id = "prod-004",
                    date = now,
                    dateString = todayStr,
                    jacketId = "jkt-011",
                    jacketName = "Expedition Arctic Parka",
                    jacketSku = "JKT-PRK-OLV-L",
                    batchNumber = "BATCH-PRK-04",
                    shiftName = "AFTERNOON",
                    operatorName = "Ahmad Zaki",
                    quantityProduced = 12,
                    defectCount = 0,
                    statusName = "IN_PROGRESS",
                    notes = "Pemasangan fur hood dan saku kargo sedang berlangsung.",
                    addedToStock = false,
                    createdAt = now
                )
            )
        }

        private fun getInitialWarehouseInventoryEntities(): List<JacketEntity> {
            val now = System.currentTimeMillis()
            return listOf(
                JacketEntity(
                    id = "jkt-001",
                    sku = "JKT-BMB-BLK-M",
                    name = "Tactical MA-1 Bomber",
                    style = "Bomber Jacket",
                    size = "M",
                    material = "Leather",
                    color = "Black",
                    quantity = 18,
                    minThreshold = 5,
                    locationRack = "Rack A-03",
                    imageUrl = "https://mediadirhub.com/vibe/black-leather-bomber-jacket-with-ribbed-cuffs-and-zipper-front.png",
                    isSeasonal = false,
                    unitPrice = 149.99,
                    genderCategory = "Jaket Pria",
                    createdAt = now - 100000
                ),
                JacketEntity(
                    id = "jkt-002",
                    sku = "JKT-TRC-BGE-L",
                    name = "Heritage Double-Breasted Trench",
                    style = "Trench Coat",
                    size = "L",
                    material = "Cotton",
                    color = "Beige",
                    quantity = 12,
                    minThreshold = 5,
                    locationRack = "Rack B-11",
                    imageUrl = "https://mediadirhub.com/vibe/beige-cotton-trench-coat-with-double-breasted-buttons-and-belt.png",
                    isSeasonal = false,
                    unitPrice = 189.50,
                    genderCategory = "Jaket Pria",
                    createdAt = now - 90000
                ),
                JacketEntity(
                    id = "jkt-003",
                    sku = "JKT-DNM-NVY-S",
                    name = "Raw Selvedge Denim Trucker",
                    style = "Denim Jacket",
                    size = "S",
                    material = "Cotton",
                    color = "Navy",
                    quantity = 3,
                    minThreshold = 5,
                    locationRack = "Rack C-02",
                    imageUrl = "https://mediadirhub.com/vibe/medium-blue-denim-jacket-with-patch-pockets-and-button-front.png",
                    isSeasonal = false,
                    unitPrice = 99.00,
                    genderCategory = "Jaket Unisex",
                    createdAt = now - 80000
                ),
                JacketEntity(
                    id = "jkt-004",
                    sku = "JKT-PFF-BLK-XL",
                    name = "Alpine Quilted Down Puffer",
                    style = "Puffer Jacket",
                    size = "XL",
                    material = "Nylon",
                    color = "Black",
                    quantity = 24,
                    minThreshold = 6,
                    locationRack = "Rack D-08",
                    imageUrl = "https://mediadirhub.com/vibe/dark-gray-quilted-puffer-jacket-with-hood-and-zip-closure.png",
                    isSeasonal = true,
                    unitPrice = 169.00,
                    genderCategory = "Jaket Unisex",
                    createdAt = now - 70000
                ),
                JacketEntity(
                    id = "jkt-005",
                    sku = "JKT-BMB-OLV-XS",
                    name = "Flight Aviator Bomber",
                    style = "Bomber Jacket",
                    size = "XS",
                    material = "Nylon",
                    color = "Olive",
                    quantity = 4,
                    minThreshold = 5,
                    locationRack = "Rack A-05",
                    imageUrl = "https://mediadirhub.com/vibe/olive-green-jacket-with-matte-finish-and-structured-silhouette.png",
                    isSeasonal = false,
                    unitPrice = 129.99,
                    genderCategory = "Jaket Pria",
                    createdAt = now - 60000
                ),
                JacketEntity(
                    id = "jkt-006",
                    sku = "JKT-TRC-BLK-XXL",
                    name = "Waterproof Storm Trench",
                    style = "Trench Coat",
                    size = "XXL",
                    material = "Polyester",
                    color = "Black",
                    quantity = 0,
                    minThreshold = 4,
                    locationRack = "Rack B-14",
                    imageUrl = "https://mediadirhub.com/vibe/solid-black-jacket-front-with-minimal-branding.png",
                    isSeasonal = false,
                    unitPrice = 179.00,
                    genderCategory = "Jaket Unisex",
                    createdAt = now - 50000
                ),
                JacketEntity(
                    id = "jkt-007",
                    sku = "JKT-PFF-OLV-M",
                    name = "Thermal Field Puffer",
                    style = "Puffer Jacket",
                    size = "M",
                    material = "Polyester",
                    color = "Olive",
                    quantity = 2,
                    minThreshold = 5,
                    locationRack = "Rack D-10",
                    imageUrl = "https://mediadirhub.com/vibe/winter-themed-display-of-puffer-and-wool-jackets-on-wooden-hangers.png",
                    isSeasonal = true,
                    unitPrice = 159.00,
                    genderCategory = "Jaket Unisex",
                    createdAt = now - 40000
                ),
                JacketEntity(
                    id = "jkt-008",
                    sku = "JKT-WOL-NVY-L",
                    name = "Sub-Zero Wool Overcoat",
                    style = "Trench Coat",
                    size = "L",
                    material = "Wool",
                    color = "Navy",
                    quantity = 15,
                    minThreshold = 5,
                    locationRack = "Rack E-01",
                    imageUrl = "https://mediadirhub.com/vibe/deep-navy-blue-jacket-with-clean-lines-and-no-patterns.png",
                    isSeasonal = true,
                    unitPrice = 219.00,
                    genderCategory = "Jaket Unisex",
                    createdAt = now - 30000
                ),
                JacketEntity(
                    id = "jkt-009",
                    sku = "JKT-FLC-BGE-XL",
                    name = "Sherpa Lined Fleece Jacket",
                    style = "Bomber Jacket",
                    size = "XL",
                    material = "Fleece",
                    color = "Beige",
                    quantity = 0,
                    minThreshold = 5,
                    locationRack = "Rack F-04",
                    imageUrl = "https://mediadirhub.com/vibe/light-beige-jacket-with-soft-texture-and-neutral-tone.png",
                    isSeasonal = true,
                    unitPrice = 119.00,
                    genderCategory = "Jaket Wanita",
                    createdAt = now - 20000
                ),
                JacketEntity(
                    id = "jkt-010",
                    sku = "JKT-DNM-BLK-M",
                    name = "Washed Black Rider Denim",
                    style = "Denim Jacket",
                    size = "M",
                    material = "Cotton",
                    color = "Black",
                    quantity = 9,
                    minThreshold = 5,
                    locationRack = "Rack C-06",
                    imageUrl = "https://mediadirhub.com/vibe/medium-blue-denim-jacket-with-patch-pockets-and-button-front.png",
                    isSeasonal = false,
                    unitPrice = 105.00,
                    genderCategory = "Jaket Pria",
                    createdAt = now - 10000
                ),
                JacketEntity(
                    id = "jkt-011",
                    sku = "JKT-PRK-OLV-L",
                    name = "Expedition Arctic Parka",
                    style = "Parka",
                    size = "L",
                    material = "Nylon",
                    color = "Olive",
                    quantity = 8,
                    minThreshold = 4,
                    locationRack = "Rack G-02",
                    imageUrl = "https://mediadirhub.com/vibe/olive-green-jacket-with-matte-finish-and-structured-silhouette.png",
                    isSeasonal = true,
                    unitPrice = 199.00,
                    genderCategory = "Jaket Wanita",
                    createdAt = now - 5000
                ),
                JacketEntity(
                    id = "jkt-012",
                    sku = "JKT-BMB-PNK-S",
                    name = "Pastel Windbreaker Pink",
                    style = "Bomber Jacket",
                    size = "S",
                    material = "Nylon",
                    color = "Pink",
                    quantity = 14,
                    minThreshold = 5,
                    locationRack = "Rack H-01",
                    imageUrl = "https://mediadirhub.com/vibe/light-beige-jacket-with-soft-texture-and-neutral-tone.png",
                    isSeasonal = false,
                    unitPrice = 119.00,
                    genderCategory = "Jaket Wanita",
                    createdAt = now - 4000
                ),
                JacketEntity(
                    id = "jkt-013",
                    sku = "JKT-PRK-MRN-L",
                    name = "Burgundy Classic Parka",
                    style = "Parka",
                    size = "L",
                    material = "Wool",
                    color = "Maroon",
                    quantity = 7,
                    minThreshold = 4,
                    locationRack = "Rack H-03",
                    imageUrl = "https://mediadirhub.com/vibe/deep-navy-blue-jacket-with-clean-lines-and-no-patterns.png",
                    isSeasonal = true,
                    unitPrice = 189.00,
                    genderCategory = "Jaket Wanita",
                    createdAt = now - 3500
                ),
                JacketEntity(
                    id = "jkt-014",
                    sku = "JKT-BMB-MRH-M",
                    name = "Crimson Speed Bomber Junior",
                    style = "Bomber Jacket",
                    size = "M",
                    material = "Leather",
                    color = "Merah",
                    quantity = 11,
                    minThreshold = 5,
                    locationRack = "Rack H-05",
                    imageUrl = "https://mediadirhub.com/vibe/red-x-icon-on-white-background-with-jacket-silhouette.png",
                    isSeasonal = false,
                    unitPrice = 95.00,
                    genderCategory = "Jaket Anak",
                    createdAt = now - 3000
                ),
                JacketEntity(
                    id = "jkt-015",
                    sku = "JKT-TRC-ABU-XL",
                    name = "Minimalist Heather Trench",
                    style = "Trench Coat",
                    size = "XL",
                    material = "Cotton",
                    color = "Abu",
                    quantity = 9,
                    minThreshold = 5,
                    locationRack = "Rack H-07",
                    imageUrl = "https://mediadirhub.com/vibe/dark-gray-quilted-puffer-jacket-with-hood-and-zip-closure.png",
                    isSeasonal = false,
                    unitPrice = 165.00,
                    genderCategory = "Jaket Wanita",
                    createdAt = now - 2500
                ),
                JacketEntity(
                    id = "jkt-016",
                    sku = "JKT-PRK-HJU-M",
                    name = "Forest Canopy Parka Kids",
                    style = "Parka",
                    size = "M",
                    material = "Nylon",
                    color = "Hijau",
                    quantity = 16,
                    minThreshold = 5,
                    locationRack = "Rack H-09",
                    imageUrl = "https://mediadirhub.com/vibe/olive-green-jacket-with-matte-finish-and-structured-silhouette.png",
                    isSeasonal = false,
                    unitPrice = 115.00,
                    genderCategory = "Jaket Anak",
                    createdAt = now - 2000
                ),
                JacketEntity(
                    id = "jkt-017",
                    sku = "JKT-BMB-CKL-L",
                    name = "Rustic Vintage Leather Brown",
                    style = "Bomber Jacket",
                    size = "L",
                    material = "Leather",
                    color = "Coklat",
                    quantity = 6,
                    minThreshold = 4,
                    locationRack = "Rack H-11",
                    imageUrl = "https://mediadirhub.com/vibe/close-up-of-smooth-brown-leather-with-natural-grain-pattern.png",
                    isSeasonal = false,
                    unitPrice = 195.00,
                    genderCategory = "Jaket Pria",
                    createdAt = now - 1500
                ),
                JacketEntity(
                    id = "jkt-018",
                    sku = "JKT-DNM-CRM-M",
                    name = "Caramel Corduroy Trucker",
                    style = "Denim Jacket",
                    size = "M",
                    material = "Cotton",
                    color = "Caramel",
                    quantity = 10,
                    minThreshold = 5,
                    locationRack = "Rack H-13",
                    imageUrl = "https://mediadirhub.com/vibe/beige-cotton-trench-coat-with-double-breasted-buttons-and-belt.png",
                    isSeasonal = false,
                    unitPrice = 125.00,
                    genderCategory = "Jaket Unisex",
                    createdAt = now - 1000
                ),
                JacketEntity(
                    id = "jkt-019",
                    sku = "JKT-PFF-PTH-S",
                    name = "Pure Alpine Snow Puffer Kids",
                    style = "Puffer Jacket",
                    size = "S",
                    material = "Nylon",
                    color = "Putih",
                    quantity = 5,
                    minThreshold = 5,
                    locationRack = "Rack H-15",
                    imageUrl = "https://mediadirhub.com/vibe/solid-black-jacket-front-with-minimal-branding.png",
                    isSeasonal = true,
                    unitPrice = 120.00,
                    genderCategory = "Jaket Anak",
                    createdAt = now - 500
                )
            )
        }
    }
}
