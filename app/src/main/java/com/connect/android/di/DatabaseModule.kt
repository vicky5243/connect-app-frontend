package com.connect.android.di

import android.content.Context
import androidx.room.Room
import com.connect.android.database.ConnectDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DatabaseModule {

    @Singleton
    @Provides
    fun provideConnectDatabase(@ApplicationContext context: Context): ConnectDatabase {
        return Room.databaseBuilder(
            context,
            ConnectDatabase::class.java,
            "connect_db"
        ).build()
    }

    @Provides
    fun provideConnectDao(connectDatabase: ConnectDatabase) = connectDatabase.connectDao()
}