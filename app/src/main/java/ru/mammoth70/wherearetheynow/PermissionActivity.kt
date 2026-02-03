package ru.mammoth70.wherearetheynow

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.style.StrikethroughSpan
import android.util.TypedValue
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar

class PermissionActivity : AppActivity() {
    // Activity показывает все выданные и необходимые разрешения для работы приложения,
    // а также (для невыданных разрешений) кнопки,
    // вызвающие код с запросом на выдачу этих разрешений.

    override val idLayout = R.layout.activity_permission
    override val idActivity = R.id.framePermissionsActivity

    companion object {
        private const val REQUEST_PERMISSIONS_LOCATION = 482
        private const val REQUEST_PERMISSIONS_BACKGROUND_LOCATION = 483
        private const val REQUEST_PERMISSIONS_SMS = 484
        private const val REQUEST_PERMISSIONS_NOTIFICATIONS = 485
    }

    private val tvCoarseLocation1: TextView by lazy { findViewById(R.id.tvCoarseLocation1) }
    private val tvCoarseLocation: TextView by lazy { findViewById(R.id.tvCoarseLocation) }
    private val tvFineLocation1: TextView by lazy { findViewById(R.id.tvFineLocation1) }
    private val tvFineLocation: TextView by lazy { findViewById(R.id.tvFineLocation) }
    private val btnLocation: Button by lazy { findViewById(R.id.btnLocation)}

    private val tvBackgroundLocation1: TextView by lazy { findViewById(R.id.tvBackgroundLocation1) }
    private val tvBackgroundLocation: TextView by lazy { findViewById(R.id.tvBackgroundLocation) }
    private val btnBackgroundLocation: Button by lazy { findViewById(R.id.btnBackgroundLocation) }

    private val tvReceiveSMS1: TextView by lazy { findViewById(R.id.tvReceiveSMS1) }
    private val tvReceiveSMS: TextView by lazy { findViewById(R.id.tvReceiveSMS) }
    private val tvSendSMS1: TextView by lazy { findViewById(R.id.tvSendSMS1) }
    private val tvSendSMS: TextView by lazy { findViewById(R.id.tvSendSMS) }
    private val btnSMS: Button by lazy { findViewById(R.id.btnSMS) }

    private val cardNotifications: MaterialCardView by lazy { findViewById(R.id.cardNotifications) }
    private val tvNotifications1: TextView by lazy { findViewById(R.id.tvNotifications1) }
    private val tvNotifications: TextView by lazy { findViewById(R.id.tvNotifications) }
    private val btnNotifications: Button by lazy { findViewById(R.id.btnNotifications) }

    private var colorGranted = 0
    private var colorError = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        // Функция вызывается при создании Activity.
        // Выполнение запросов недостающих разрешений.
	    // Настройка данных для отображения разрешений и кнопок запроса недостающих разрешений.


        super.onCreate(savedInstanceState)

        topAppBar.setTitle(R.string.titlePermissions)
        topAppBar.setNavigationOnClickListener {
            finish()
        }

        val theme = getTheme()
        val typedValueColorGranted = TypedValue()
        val typedValuecolorError = TypedValue()
        theme.resolveAttribute(
            R.attr.colorGranted,
            typedValueColorGranted, true
        )
        theme.resolveAttribute(
            androidx.appcompat.R.attr.colorError,
            typedValuecolorError, true
        )
        colorGranted = typedValueColorGranted.data
        colorError = typedValuecolorError.data


        requestPermissions()
        requestBackgroundLocationPermission(btnBackgroundLocation)
        viewPermissions()

        btnLocation.setOnClickListener { view ->
            requestLocationPermissionButtonClick(view)
        }

        btnBackgroundLocation.setOnClickListener { view ->
            requestBackgroundLocationPermissionButtonClick(view)
        }

        btnSMS.setOnClickListener { view ->
            requestSMSPermissionButtonClick(view)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Обработка актуальна только для Android 13 (API 33) и выше
            btnNotifications.setOnClickListener { view ->
                requestNotificationPermissionButtonClick(view)
            }
        } else {
            cardNotifications.visibility = View.GONE
        }
    }


    private fun requestPermissions() {
        // Функция запрашивает все разрешения разом.

        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.SEND_SMS
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Проверка актуальна только для Android 13 (API 33) и выше
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val locationPermissionRequest = registerForActivityResult(
            RequestMultiplePermissions()
        ) { _: Map<String, @JvmSuppressWildcards Boolean> -> }
        locationPermissionRequest.launch(permissions.toTypedArray())
    }


    fun requestBackgroundLocationPermission(view: View) {
        // Функция запрашивает разрешения работы в фоновом режиме,
        // если выданы все разрешения геолокации.

        if ((ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED) &&
            (ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED) &&
            (ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) != PackageManager.PERMISSION_GRANTED)
        ) {

            if ((ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ))
            ) {
                // Предоставляет дополнительную информацию, если разрешение так и не было выдано.
                Snackbar.make(view, R.string.allow_background_location,
                    Snackbar.LENGTH_LONG).show()
            }

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                REQUEST_PERMISSIONS_BACKGROUND_LOCATION
            )
        }
    }


    fun requestBackgroundLocationPermissionButtonClick(view: View) {
        // Функция - обработчик кнопки запроса работы в фоновом режиме.
        // Запрашивает разрешения работы в фоновом режиме.

        if ((ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) != PackageManager.PERMISSION_GRANTED)
        ) {

            if ((ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ))
            ) {
                // Предоставляет дополнительную информацию, если разрешение так и не было выдано.
                Snackbar.make(view, R.string.allow_background_location,
                    Snackbar.LENGTH_LONG).show()
            }

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                REQUEST_PERMISSIONS_BACKGROUND_LOCATION
            )
        }
    }


    fun requestLocationPermissionButtonClick(view: View) {
        // Функция - обработчик кнопки запроса разрешений геолокации.
        // Запрашивает все разрешения геолокации.

        if ((ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) ||
            (ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED)
        ) {

            if ((ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )) ||
                (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ))
            ) {
                // Предоставляет дополнительную информацию, если разрешение так и не было выдано.
                Snackbar.make(view, R.string.allow_fine_location,
                    Snackbar.LENGTH_LONG).show()
            }

            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                REQUEST_PERMISSIONS_LOCATION
            )
        }
    }


    fun requestSMSPermissionButtonClick(view: View) {
        // Функция - обработчик кнопки запроса разрешений SMS.
        // Запрашивает все разрешения SMS.

        if ((ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.RECEIVE_SMS
            ) != PackageManager.PERMISSION_GRANTED) ||
            (ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.SEND_SMS
            ) != PackageManager.PERMISSION_GRANTED)
        ) {

            if ((ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.RECEIVE_SMS
                )) ||
                (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.SEND_SMS
                ))
            ) {
                // Предоставляет дополнительную информацию, если разрешение так и не было выдано.
                Snackbar.make(view, R.string.allow_SMS,
                    Snackbar.LENGTH_LONG).show()
            }

            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.RECEIVE_SMS,
                    Manifest.permission.SEND_SMS
                ),
                REQUEST_PERMISSIONS_SMS
            )
        }
    }


    fun requestNotificationPermissionButtonClick(view: View) {
        // Функция - обработчик кнопки запроса разрешений на уведомления.

        // Проверка актуальна только для Android 13 (API 33) и выше
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.POST_NOTIFICATIONS
                    )
                ) {
                    // Предоставляет дополнительную информацию, если разрешение так и не было выдано.
                    Snackbar.make(
                        view,
                        R.string.allow_notifications,
                        Snackbar.LENGTH_LONG
                    ).show()
                }

                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_PERMISSIONS_NOTIFICATIONS )

            }
        }
    }

    private fun viewPermissions() {
        // Функция проверяет необходимые разрешения и выводит их.
        // Зелёный цвет, если разрешение выдано, красный цвет и зачёркнуто, если разрешение не выдано.
        // Также, если разрешения нет, делается видимой кнопка запроса.

        val spCoarseLocation = SpannableString(getString(R.string.access_coarse_location))
        val spFineLocation = SpannableString(getString(R.string.access_fine_location))
        val spBackgroundLocation = SpannableString(getString(R.string.access_background_location))
        val spReceiveSMS = SpannableString(getString(R.string.access_Receive_SMS))
        val spSendSMS = SpannableString(getString(R.string.access_Send_SMS))
        val spNotifications = SpannableString(getString(R.string.access_notifications))

        // Настройка показа разрешений примерного расположения.
        if ((ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED)
        ) {
            tvCoarseLocation.setText(R.string.access_coarse_location)
            tvCoarseLocation.setTextColor(colorGranted)
            tvCoarseLocation1.setText(R.string.granted1)
            tvCoarseLocation1.setTextColor(colorGranted)
        } else {
            spCoarseLocation.setSpan(
                StrikethroughSpan(),
                0,
                getString(R.string.access_coarse_location).length,
                0
            )
            tvCoarseLocation.text = spCoarseLocation
            tvCoarseLocation.setTextColor(colorError)
            tvCoarseLocation1.setText(R.string.denied1)
            tvCoarseLocation1.setTextColor(colorError)
        }

        // Настройка показа разрешений точного расположения и кнопки запроса этих разрешений.
        if ((ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED)
        ) {
            tvFineLocation.setText(R.string.access_fine_location)
            tvFineLocation.setTextColor(colorGranted)
            tvFineLocation1.setText(R.string.granted1)
            tvFineLocation1.setTextColor(colorGranted)
            btnLocation.setEnabled(false)
            btnLocation.visibility = View.INVISIBLE
        } else {
            spFineLocation.setSpan(
                StrikethroughSpan(),
                0,
                getString(R.string.access_fine_location).length,
                0
            )
            tvFineLocation.text = spFineLocation
            tvFineLocation.setTextColor(colorError)
            tvFineLocation1.setText(R.string.denied1)
            tvFineLocation1.setTextColor(colorError)
            btnLocation.setEnabled(true)
            btnLocation.visibility = View.VISIBLE
        }

        // Настройка показа разрешений работы в фоновом режиме и кнопки запроса этих разрешений.
        if ((ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED)
        ) {
            tvBackgroundLocation.setText(R.string.access_background_location)
            tvBackgroundLocation.setTextColor(colorGranted)
            tvBackgroundLocation1.setText(R.string.granted1)
            tvBackgroundLocation1.setTextColor(colorGranted)
            btnBackgroundLocation.setEnabled(false)
            btnBackgroundLocation.visibility = View.INVISIBLE
        } else {
            spBackgroundLocation.setSpan(
                StrikethroughSpan(),
                0,
                getString(R.string.access_background_location).length,
                0
            )
            tvBackgroundLocation.text = spBackgroundLocation
            tvBackgroundLocation.setTextColor(colorError)
            tvBackgroundLocation1.setText(R.string.denied1)
            tvBackgroundLocation1.setTextColor(colorError)
            btnBackgroundLocation.setEnabled(true)
            btnBackgroundLocation.visibility = View.VISIBLE
        }

        // Настройка показа разрешений получения SMS.
        if ((ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.RECEIVE_SMS
            ) == PackageManager.PERMISSION_GRANTED)
        ) {
            tvReceiveSMS.setText(R.string.access_Receive_SMS)
            tvReceiveSMS.setTextColor(colorGranted)
            tvReceiveSMS1.setText(R.string.granted1)
            tvReceiveSMS1.setTextColor(colorGranted)
        } else {
            spReceiveSMS.setSpan(
                StrikethroughSpan(),
                0,
                getString(R.string.access_Receive_SMS).length,
                0
            )
            tvReceiveSMS.text = spReceiveSMS
            tvReceiveSMS.setTextColor(colorError)
            tvReceiveSMS1.setText(R.string.denied1)
            tvReceiveSMS1.setTextColor(colorError)
        }

        // Настройка показа разрешений отправки SMS и кнопки запроса разрешений SMS.
        if ((ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.SEND_SMS
            ) == PackageManager.PERMISSION_GRANTED)
        ) {
            tvSendSMS.setText(R.string.access_Send_SMS)
            tvSendSMS.setTextColor(colorGranted)
            tvSendSMS1.setText(R.string.granted1)
            tvSendSMS1.setTextColor(colorGranted)
        } else {
            spSendSMS.setSpan(StrikethroughSpan(), 0, getString(R.string.access_Send_SMS).length, 0)
            tvSendSMS.text = spSendSMS
            tvSendSMS.setTextColor(colorError)
            tvSendSMS1.setText(R.string.denied1)
            tvSendSMS1.setTextColor(colorError)
        }

        // Настройка кнопки запроса разрешений SMS.
        if ((ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.RECEIVE_SMS
            ) == PackageManager.PERMISSION_GRANTED) &&
            (ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.SEND_SMS
            ) == PackageManager.PERMISSION_GRANTED)
        ) {
            btnSMS.setEnabled(false)
            btnSMS.visibility = View.INVISIBLE
        } else {
            btnSMS.setEnabled(true)
            btnSMS.visibility = View.VISIBLE
        }

        // Настройка показа разрешений уведомлений и кнопки запроса этих разрешений.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Настройка актуальна только для Android 13 (API 33) и выше
            if ((ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED)
            ) {
                tvNotifications.setText(R.string.access_notifications)
                tvNotifications.setTextColor(colorGranted)
                tvNotifications1.setText(R.string.granted1)
                tvNotifications1.setTextColor(colorGranted)
                btnNotifications.setEnabled(false)
                btnNotifications.visibility = View.INVISIBLE
            } else {
                spNotifications.setSpan(
                    StrikethroughSpan(),
                    0,
                    getString(R.string.access_fine_location).length,
                    0
                )
                tvNotifications.text = spFineLocation
                tvNotifications.setTextColor(colorError)
                tvNotifications1.setText(R.string.denied1)
                tvNotifications1.setTextColor(colorError)
                btnNotifications.setEnabled(true)
                btnNotifications.visibility = View.VISIBLE
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        // Функция обработки ответа на запрос разрешений.
        // Выводятся вплывающие сообщения, в том случае, если пришёл отказ на выдачу разрешений.

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        viewPermissions()
        when (requestCode) {
            REQUEST_PERMISSIONS_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_DENIED) {
                    val message = getString(R.string.location_request_blocked) + "\n" +
                            getString(R.string.set_access_manually)
                    Snackbar.make(btnLocation, message,
                        Snackbar.LENGTH_INDEFINITE).show()
                }
                return
            }

            REQUEST_PERMISSIONS_BACKGROUND_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_DENIED) {
                    val message = getString(R.string.location_bg_request_blocked) + "\n" +
                            getString(R.string.set_access_manually)
                    Snackbar.make(btnBackgroundLocation, message,
                        Snackbar.LENGTH_INDEFINITE).show()
                }
                return
            }

            REQUEST_PERMISSIONS_SMS -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_DENIED) {
                    val message = getString(R.string.sms_request_blocked) + "\n" +
                            getString(R.string.set_access_manually)
                    Snackbar.make(btnSMS, message,
                        Snackbar.LENGTH_INDEFINITE).show()
                }
            }

            REQUEST_PERMISSIONS_NOTIFICATIONS -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_DENIED) {
                    val message = getString(R.string.notifications_request_blocked) + "\n" +
                            getString(R.string.set_access_manually)
                    Snackbar.make(btnNotifications, message,
                        Snackbar.LENGTH_INDEFINITE).show()
                }
            }
        }
    }
}