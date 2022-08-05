package com.nik.shift.calendar

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.nik.shift.calendar.util.ApiKeys
import com.yandex.metrica.YandexMetrica
import com.yandex.metrica.YandexMetricaConfig
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

/**
 * Created by MaxSiominDev on 5/24/2022
 */
@HiltAndroidApp
class App : Application() {

    override fun onCreate() {
        super.onCreate()

        initTimber()
        initYandexMetrica()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }

    private fun initTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    private fun initYandexMetrica() {
        val config = YandexMetricaConfig
            .newConfigBuilder(ApiKeys.YANDEX_METRICA)
            .build()

        // Initializing the AppMetrica SDK.
        YandexMetrica.activate(applicationContext, config)
        // Automatic tracking of user activity.
        YandexMetrica.enableActivityAutoTracking(this)
    }
}
