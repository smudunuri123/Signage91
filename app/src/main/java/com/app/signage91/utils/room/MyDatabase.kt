package com.app.signage91.utils.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.app.signage91.utils.room.MyDatabase.Companion.DATABASE_VERSION
import com.app.signage91.utils.room.dao.ZoneDataEntityDao
import com.app.signage91.utils.room.models.ZoneDataEntity

@Database(
    entities = arrayOf(
        ZoneDataEntity::class
    ), version = DATABASE_VERSION
)
abstract class MyDatabase : RoomDatabase() {

    abstract fun zoneDao(): ZoneDataEntityDao

    companion object {

        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "signage91.sqlite3"

        var mInstance: MyDatabase? = null
        fun getInstance(context: Context): MyDatabase? {

            if (mInstance == null) {
                mInstance = Room.databaseBuilder(context, MyDatabase::class.java, DATABASE_NAME)
                    //.fallbackToDestructiveMigration()
                    //.createFromAsset("database/crossbox.sqlite3")
                    .allowMainThreadQueries()
                    .build()
            }
            return mInstance
        }
    }
}