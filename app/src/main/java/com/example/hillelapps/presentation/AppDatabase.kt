package com.example.hillelapps.presentation

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        MainActivity.Confirmation::class,
        PointsEntity::class  // Ajout de l'entité PointsEntity
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun confirmationDao(): MainActivity.ConfirmationDao
    abstract fun pointsDao(): PointsDao  // Ajout du PointsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .fallbackToDestructiveMigration()  // Pour gérer les changements de schéma
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}