package com.app.signage91.utils.room.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "zone_data")
data class ZoneDataEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "zone") val zone: String?,
    @ColumnInfo(name = "json") val json: String?,
    @ColumnInfo(name = "date") val date: Long?
)

