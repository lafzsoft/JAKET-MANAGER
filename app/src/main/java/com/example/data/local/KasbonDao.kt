package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface KasbonDao {

    @Query("SELECT * FROM kasbon_table ORDER BY date DESC")
    fun getAllKasbon(): Flow<List<KasbonEntity>>

    @Query("SELECT * FROM kasbon_table WHERE id = :id LIMIT 1")
    suspend fun getKasbonById(id: String): KasbonEntity?

    @Query("SELECT COUNT(*) FROM kasbon_table")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: KasbonEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<KasbonEntity>)

    @Update
    suspend fun update(entity: KasbonEntity)

    @Delete
    suspend fun delete(entity: KasbonEntity)

    @Query("DELETE FROM kasbon_table WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM kasbon_table")
    suspend fun deleteAll()

    @Query("SELECT * FROM kasbon_table ORDER BY date DESC")
    suspend fun getAllKasbonSnapshot(): List<KasbonEntity>
}
