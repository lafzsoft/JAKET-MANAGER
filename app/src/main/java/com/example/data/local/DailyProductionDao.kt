package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyProductionDao {
    @Query("SELECT * FROM daily_productions ORDER BY createdAt DESC")
    fun getAllProductions(): Flow<List<DailyProductionEntity>>

    @Query("SELECT * FROM daily_productions WHERE dateString = :dateString ORDER BY createdAt DESC")
    fun getProductionsByDate(dateString: String): Flow<List<DailyProductionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduction(entity: DailyProductionEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(entities: List<DailyProductionEntity>)

    @Query("DELETE FROM daily_productions WHERE id = :id")
    suspend fun deleteProduction(id: String)

    @Query("SELECT COUNT(*) FROM daily_productions")
    suspend fun getCount(): Int
}
