package com.example.uas_papb.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [User::class, Item::class], version = 1, exportSchema = false)
abstract class ControllerDB: RoomDatabase() {
    abstract fun UserDao(): UserDao?
    abstract fun ItemDao(): ItemDao?

    companion object {
        @Volatile
        private var INSTANCE: ControllerDB ?= null

        fun getDatabase(context: Context): ControllerDB {
            return INSTANCE ?: synchronized(ControllerDB::class.java) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ControllerDB::class.java,
                    "database"
                ).allowMainThreadQueries().fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}