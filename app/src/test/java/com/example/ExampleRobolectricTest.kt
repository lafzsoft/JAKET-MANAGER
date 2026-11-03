package com.example

import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.JacketRepository
import com.example.data.local.JacketDatabase
import com.example.data.local.JacketEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var db: JacketDatabase
    private lateinit var repository: JacketRepository

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, JacketDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = JacketRepository(db.jacketDao())
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun readStringFromContext() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Jacket Manager", appName)
    }

    @Test
    fun testRoomDatabaseInsertAndQuery() = runBlocking {
        repository.addJacket(
            sku = "JKT-TEST-DB-01",
            name = "Robolectric Test Bomber",
            style = "Bomber Jacket",
            size = "L",
            material = "Leather",
            color = "Black",
            quantity = 20,
            locationRack = "Rack T-01"
        )

        val jackets = repository.jackets.first()
        assertTrue(jackets.any { it.sku == "JKT-TEST-DB-01" })
        assertEquals("Robolectric Test Bomber", jackets.first { it.sku == "JKT-TEST-DB-01" }.name)
    }

    @Test
    fun testRoomDatabaseDelete() = runBlocking {
        repository.addJacket(
            sku = "JKT-DELETE-ME",
            name = "Disposable Jacket",
            style = "Denim Jacket",
            size = "S",
            material = "Cotton",
            color = "Navy",
            quantity = 5,
            locationRack = "Rack X-01"
        )

        var jackets = repository.jackets.first()
        val itemToDelete = jackets.first { it.sku == "JKT-DELETE-ME" }

        repository.deleteJacket(itemToDelete.id)

        jackets = repository.jackets.first()
        assertFalse(jackets.any { it.id == itemToDelete.id })
    }

    @Test
    fun testAddNewJacketFormDetailsToRoomDatabase() = runBlocking {
        val formName = "Alpine Winter Parka"
        val formStyle = "Parka"
        val formSize = "XXL"
        val formQuantity = 12

        repository.addJacket(
            sku = "JKT-PARKA-ALPINE",
            name = formName,
            style = formStyle,
            size = formSize,
            material = "Polyester & Goose Down",
            color = "Olive Green",
            quantity = formQuantity,
            locationRack = "Rack W-04"
        )

        val jackets = repository.jackets.first()
        val saved = jackets.firstOrNull { it.name == formName }
        assertNotNull(saved)
        assertEquals(formStyle, saved?.style)
        assertEquals(formSize, saved?.size)
        assertEquals(formQuantity, saved?.quantity)
        assertEquals(com.example.model.StockStatusLevel.IN_STOCK, saved?.status)
    }

    @Test
    fun testUpdateJacketDetailsInRoomDatabase() = runBlocking {
        repository.addJacket(
            sku = "JKT-UPDATE-TEST",
            name = "Initial Test Jacket",
            style = "Windbreaker",
            size = "M",
            material = "Nylon",
            color = "Black",
            quantity = 3,
            locationRack = "Rack U-01"
        )

        var jackets = repository.jackets.first()
        val created = jackets.first { it.sku == "JKT-UPDATE-TEST" }
        assertEquals(3, created.quantity)
        assertEquals(com.example.model.StockStatusLevel.LOW_STOCK, created.status)

        // Simulate updating via Edit Bottom Sheet: change stock to 25 and location to Rack U-99
        repository.updateJacketDetails(
            id = created.id,
            sku = created.sku,
            name = "Updated Test Jacket",
            style = "Windbreaker",
            size = "L",
            material = "Nylon",
            color = "Black",
            quantity = 25,
            locationRack = "Rack U-99"
        )

        jackets = repository.jackets.first()
        val updated = jackets.first { it.id == created.id }
        assertEquals("Updated Test Jacket", updated.name)
        assertEquals("L", updated.size)
        assertEquals(25, updated.quantity)
        assertEquals("Rack U-99", updated.locationRack)
        assertEquals(com.example.model.StockStatusLevel.IN_STOCK, updated.status)
    }
}
