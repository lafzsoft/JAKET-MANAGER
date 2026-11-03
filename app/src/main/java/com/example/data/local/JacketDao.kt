package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface JacketDao {
    @Query("SELECT * FROM jackets ORDER BY createdAt DESC")
    fun getAllJackets(): Flow<List<JacketEntity>>

    @Query("SELECT * FROM jackets WHERE id = :id LIMIT 1")
    suspend fun getJacketById(id: String): JacketEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJacket(jacket: JacketEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(jackets: List<JacketEntity>)

    @Query("UPDATE jackets SET quantity = :newQuantity WHERE id = :id")
    suspend fun updateQuantity(id: String, newQuantity: Int)

    @Query("UPDATE jackets SET imageUrl = :imageUrl WHERE id = :id")
    suspend fun updateImageUrl(id: String, imageUrl: String)

    @Query("UPDATE jackets SET imageUrl = :imageUrl WHERE sku = :sku")
    suspend fun updateImageUrlBySku(sku: String, imageUrl: String)

    @Query("DELETE FROM jackets WHERE id = :id")
    suspend fun deleteJacket(id: String)

    @Query("SELECT COUNT(*) FROM jackets")
    suspend fun getCount(): Int
}
