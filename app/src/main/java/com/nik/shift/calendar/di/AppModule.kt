package com.nik.shift.calendar.di

import android.content.Context
import androidx.room.Room
import com.nik.shift.calendar.database.AppDao
import com.nik.shift.calendar.database.AppDatabase
import com.nik.shift.calendar.model.ShiftsManager
import com.nik.shift.calendar.repository.ShiftsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME,
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideDao(db: AppDatabase): AppDao = db.appDao()

    /*@Provides
    fun provideSharedPrefs(@ApplicationContext context: Context): SharedPrefs =
        PreferenceManager.getDefaultSharedPreferences(context)*/

    @Provides
    fun provideShiftsRepo(dao: AppDao): ShiftsRepository = ShiftsRepository(dao)

    @Provides
    fun provideShiftsManager(repo: ShiftsRepository): ShiftsManager = ShiftsManager(repo)

}
