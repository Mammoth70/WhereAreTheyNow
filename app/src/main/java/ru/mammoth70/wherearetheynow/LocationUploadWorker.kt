package ru.mammoth70.wherearetheynow

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.lang.Exception
import kotlin.coroutines.resume

class LocationUploadWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
// Класс периодически через Worker отправляет последнюю сохранённую геолокацию на интернет-сервер.


    override suspend fun doWork(): Result {
        // Функция запрашивает последнюю сохранённую геолокацию (если есть разрешения) и отправляет её на интернет-сервер.

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
        {
            return Result.failure()
        }

        return try {
            val isDownloadSuccess = downloadLocationsAndWait()

            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            val location: Location? = fusedLocationClient.lastLocation.await()

            val isUploadSuccess = if (location != null) {
                uploadLocationAndWait(location)
            } else {
                true
            }

            if (isUploadSuccess && isDownloadSuccess) Result.success() else Result.retry()
        } catch (_: Exception) {
            Result.retry()
        }
    }


    private suspend fun uploadLocationAndWait(location: Location): Boolean =
        // Функция обёртка для вызова updateLocalLocation и sendLocationInternetAsync.
        suspendCancellableCoroutine { continuation ->
            var isResumed = false
            updateLocalLocation(location)
            sendLocationInternetAsync(
                context = context,
                location = location,
                onFinished = {
                    if (!isResumed) {
                        isResumed = true
                        continuation.resume(false)
                    }
                },
                onResult = { result ->
                    if (!isResumed) {
                        isResumed = true
                        continuation.resume(result.isSuccess)
                    }
                }
            )
        }

    private suspend fun downloadLocationsAndWait(): Boolean =
        // Функция обёртка для вызова getLocationsInternetAsync.
        suspendCancellableCoroutine { continuation ->
            var isResumed = false
            getLocationsInternetAsync(
                onFinished = {
                    if (!isResumed) {
                        isResumed = true
                        continuation.resume(false)
                    }
                },
                onResult = { result ->
                    if (!isResumed) {
                        isResumed = true
                        continuation.resume(result.isSuccess)
                    }
                }
            )
        }

}
