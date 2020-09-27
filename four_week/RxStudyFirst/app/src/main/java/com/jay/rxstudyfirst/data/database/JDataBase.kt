package com.jay.rxstudyfirst.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.jay.rxstudyfirst.data.Movie

@Database(entities = [Movie::class], version = 1, exportSchema = false)
abstract class JDataBase : RoomDatabase() {
    abstract fun movieDao(): MovieDao

    object Factory {
        private const val DATABASE_NAME = "jmovie.db"

        fun create(context: Context): JDataBase {
            return Room.databaseBuilder(
                context.applicationContext,
                JDataBase::class.java,
                DATABASE_NAME
            )
                .build()
        }

    }
}