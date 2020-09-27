package com.jay.rxstudyfirst.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.jay.rxstudyfirst.data.Movie

@Database(entities = [Movie::class], version = 1, exportSchema = false)
abstract class JDataBase : RoomDatabase() {
    abstract fun movieDao(): MovieDao

    companion object {
        @Volatile
        private var instance: JDataBase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) =
            instance ?: synchronized(LOCK) {
                instance ?: buildDatabase(context).also {
                    instance = it
                }
            }

        private fun buildDatabase(context: Context) = Room.databaseBuilder(
            context.applicationContext,
            JDataBase::class.java,
            "j_database"
        )
            .build()
    }
}