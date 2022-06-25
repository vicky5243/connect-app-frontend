package com.connect.android.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.connect.android.models.res.User

@Database(entities = [User::class], version = 1, exportSchema = true)
@TypeConverters(Converters::class)
abstract class ConnectDatabase : RoomDatabase() {
    abstract fun connectDao(): ConnectDao
}