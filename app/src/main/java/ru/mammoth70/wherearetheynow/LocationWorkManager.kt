package ru.mammoth70.wherearetheynow

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object LocationWorkManager {
    // Объект для управления LocationUploadWorker.


    const val WORKER_NAME = "PeriodicLocationUpload"

    fun startTracking(context: Context, restartIfExists: Boolean = false) {
        // Функция запускает LocationUploadWorker.

        if ((!SettingsManager.useInternet) || SettingsManager.InternetServer.isBlank() || SettingsManager.InternetToken.isBlank()) {
            return
        }

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val locationWorkRequest = PeriodicWorkRequestBuilder<LocationUploadWorker>(
            15, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()

        val policy = if (restartIfExists) {
            ExistingPeriodicWorkPolicy.REPLACE
        } else {
            ExistingPeriodicWorkPolicy.KEEP
        }

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORKER_NAME,
            policy,
            locationWorkRequest
        )
    }


    fun stopTracking(context: Context) {
        // Функция полностью останавливает LocationUploadWorker.

        WorkManager.getInstance(context).cancelUniqueWork(WORKER_NAME)
    }
}