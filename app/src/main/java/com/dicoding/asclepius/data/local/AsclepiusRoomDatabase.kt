package com.dicoding.asclepius.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

class AsclepiusRoomDatabase {
    @Database(entities = [Asclepius::class], version = 1)
    abstract class AsclepiusRoomDatabase : RoomDatabase() {
        abstract fun asclepiusDao(): AsclepiusDao
        companion object {
            @Volatile
            private var INSTANCE: AsclepiusRoomDatabase? = null
            @JvmStatic
            fun getDatabase(context: Context): AsclepiusRoomDatabase {
                if (INSTANCE == null) {
                    synchronized(AsclepiusRoomDatabase::class.java) {
                        INSTANCE = Room.databaseBuilder(context.applicationContext,
                            AsclepiusRoomDatabase::class.java, "asclepius_database")
                            .build()
                    }
                }
                return INSTANCE as AsclepiusRoomDatabase
            }
        }
    }
}