package com.zxerrinor.githubstarwatchdog.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = arrayOf(Repository::class, Star::class), version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun repositoryDao(): RepositoryDao
    abstract fun starDao(): StarDao
}
