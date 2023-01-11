package com.app.signage91.utils.room.dao

import androidx.room.*
import com.app.signage91.utils.room.models.ZoneDataEntity

@Dao
interface ZoneDataEntityDao {

    @Query("SELECT * FROM zone_data")
    fun getAll(): List<ZoneDataEntity>

    @Query("SELECT * FROM zone_data")
    fun loadAll(): List<ZoneDataEntity>

    @Query("SELECT * FROM zone_data WHERE `zone` LIKE :zone LIMIT 1")
    fun findByZone(zone: String): ZoneDataEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg zone: ZoneDataEntity)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(vararg zone: ZoneDataEntity)

    @Delete
    fun delete(zone: ZoneDataEntity)
}