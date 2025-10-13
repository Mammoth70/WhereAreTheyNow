package ru.mammoth70.wherearetheynow

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.SpannableString
import android.text.style.StrikethroughSpan
import android.util.TypedValue
import android.view.View
import android.widget.Button
import android.widget.TextView

import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import com.google.android.material.snackbar.Snackbar

class PermissionActivity : AppCompatActivity() {
    // Activity показывает все выданные и необходимые разрешения для работы приложения,
    // а также (для невыданных разрешений) кнопки, вызвающие код с запросом на выдачу этих разрешений
    companion object {
        private const val REQUEST_PERMISSIONS_LOCATION = 482
        private const val REQUEST_PERMISSIONS_BACKGROUND_LOCATION = 483
        private const val REQUEST_PERMISSIONS_SMS = 484
    }

    private var tvCoarseLocation: TextView? = null
    private var tvFineLocation: TextView? = null
    private var tvBackgroundLocation: TextView? = null
    private var tvReceiveSMS: TextView? = null
    private var tvSendSMS: TextView? = null
    private var tvCoarseLocation1: TextView? = null
    private var tvFineLocation1: TextView? = null
    private var tvBackgroundLocation1: TextView? = null
    private var tvReceiveSMS1: TextView? = null
    private var tvSendSMS1: TextView? = null
    private var btnLocation: Button? = null
    private var btnBackgroundLocation: Button? = null
    private var btnSMS: Button? = null

    private var colorGranted = 0
    private var colorError = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        // Метод вызывается при создании Activity.
        // Сразу вызываются методы для запроса недостающих разрешений,
        // После вызываются метод, заполняюший поля
        super.onCreate(savedInstanceState)
        this.enableEdgeToEdge()
        setContentView(R.layout.activity_permission)
        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.permissions)
        ) { v: View?, insets: WindowInsetsCompat? ->
            val systemBars = insets!!.getInsets(WindowInsetsCompat.Type.systemBars())
            v!!.setPadding(systemBars.left, systemBars.top,
                systemBars.right, systemBars.bottom)
            insets
        }
        val tvName = findViewById<TextView>(R.id.tvTitle)
        tvName.setText(R.string.titlePermissions)

        tvCoarseLocation = findViewById(R.id.tvCoarseLocation)
        tvFineLocation = findViewById(R.id.tvFineLocation)
        tvBackgroundLocation = findViewById(R.id.tvBackgroundLocation)
        tvReceiveSMS = findViewById(R.id.tvReceiveSMS)
        tvSendSMS = findViewById(R.id.tvSendSMS)
        tvCoarseLocation1 = findViewById(R.id.tvCoarseLocation1)
        tvFineLocation1 = findViewById(R.id.tvFineLocation1)
        tvBackgroundLocation1 = findViewById(R.id.tvBackgroundLocation1)
        tvReceiveSMS1 = findViewById(R.id.tvReceiveSMS1)
        tvSendSMS1 = findViewById(R.id.tvSendSMS1)
        btnLocation = findViewById(R.id.btnLocation)
        btnBackgroundLocation = findViewById(R.id.btnBackgroundLocation)
        btnSMS = findViewById(R.id.btnSMS)

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
        requestBackgroundLocationPermission(btnBackgroundLocation!!)
        viewPermissions()
    }

    private fun requestPermissions() {
        // Метод запрашивает все разрешения разом.
        val locationPermissionRequest = registerForActivityResult(
            RequestMultiplePermissions()
        ) { isGranted: Map<String, @JvmSuppressWildcards() Boolean> -> }
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.SEND_SMS
            )
        )
    }

    fun requestBackgroundLocationPermission(view: View) {
        // Метод запрашивает разрешения работы в фоновом режиме,
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
        // Метод - обработчик кнопки запроса работы в фоновом режиме.
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
        // Метод - обработчик кнопки запроса разрешений геолокации.
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
        // Метод - обработчик кнопки запроса разрешений SMS.
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

    private fun viewPermissions() {
        // Метод проверяет необходимые разрешения и выводит их.
        // Зелёный цвет, если разрешение выдано, красный цвет и зачёркнуто, если разрешение не выдано.
        // Также, если разрешения нет, делается видимой кнопка запроса.

        val spCoarseLocation = SpannableString(getString(R.string.access_coarse_location))
        val spFineLocation = SpannableString(getString(R.string.access_fine_location))
        val spBackgroundLocation = SpannableString(getString(R.string.access_background_location))
        val spReceiveSMS = SpannableString(getString(R.string.access_Receive_SMS))
        val spSendSMS = SpannableString(getString(R.string.access_Send_SMS))

        if ((ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED)
        ) {
            tvCoarseLocation!!.setText(R.string.access_coarse_location)
            tvCoarseLocation!!.setTextColor(colorGranted)
            tvCoarseLocation1!!.setText(R.string.granted1)
            tvCoarseLocation1!!.setTextColor(colorGranted)
        } else {
            spCoarseLocation.setSpan(
                StrikethroughSpan(),
                0,
                getString(R.string.access_coarse_location).length,
                0
            )
            tvCoarseLocation!!.text = spCoarseLocation
            tvCoarseLocation!!.setTextColor(colorError)
            tvCoarseLocation1!!.setText(R.string.denied1)
            tvCoarseLocation1!!.setTextColor(colorError)
        }
        if ((ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED)
        ) {
            tvFineLocation!!.setText(R.string.access_fine_location)
            tvFineLocation!!.setTextColor(colorGranted)
            tvFineLocation1!!.setText(R.string.granted1)
            tvFineLocation1!!.setTextColor(colorGranted)
            btnLocation!!.setEnabled(false)
            btnLocation!!.visibility = View.INVISIBLE
        } else {
            spFineLocation.setSpan(
                StrikethroughSpan(),
                0,
                getString(R.string.access_fine_location).length,
                0
            )
            tvFineLocation!!.text = spFineLocation
            tvFineLocation!!.setTextColor(colorError)
            tvFineLocation1!!.setText(R.string.denied1)
            tvFineLocation1!!.setTextColor(colorError)
            btnLocation!!.setEnabled(true)
            btnLocation!!.visibility = View.VISIBLE
        }
        if ((ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED)
        ) {
            tvBackgroundLocation!!.setText(R.string.access_background_location)
            tvBackgroundLocation!!.setTextColor(colorGranted)
            tvBackgroundLocation1!!.setText(R.string.granted1)
            tvBackgroundLocation1!!.setTextColor(colorGranted)
            btnBackgroundLocation!!.setEnabled(false)
            btnBackgroundLocation!!.visibility = View.INVISIBLE
        } else {
            spBackgroundLocation.setSpan(
                StrikethroughSpan(),
                0,
                getString(R.string.access_background_location).length,
                0
            )
            tvBackgroundLocation!!.text = spBackgroundLocation
            tvBackgroundLocation!!.setTextColor(colorError)
            tvBackgroundLocation1!!.setText(R.string.denied1)
            tvBackgroundLocation1!!.setTextColor(colorError)
            btnBackgroundLocation!!.setEnabled(true)
            btnBackgroundLocation!!.visibility = View.VISIBLE
        }
        if ((ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.RECEIVE_SMS
            ) == PackageManager.PERMISSION_GRANTED)
        ) {
            tvReceiveSMS!!.setText(R.string.access_Receive_SMS)
            tvReceiveSMS!!.setTextColor(colorGranted)
            tvReceiveSMS1!!.setText(R.string.granted1)
            tvReceiveSMS1!!.setTextColor(colorGranted)
        } else {
            spReceiveSMS.setSpan(
                StrikethroughSpan(),
                0,
                getString(R.string.access_Receive_SMS).length,
                0
            )
            tvReceiveSMS!!.text = spReceiveSMS
            tvReceiveSMS!!.setTextColor(colorError)
            tvReceiveSMS1!!.setText(R.string.denied1)
            tvReceiveSMS1!!.setTextColor(colorError)
        }
        if ((ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.SEND_SMS
            ) == PackageManager.PERMISSION_GRANTED)
        ) {
            tvSendSMS!!.setText(R.string.access_Send_SMS)
            tvSendSMS!!.setTextColor(colorGranted)
            tvSendSMS1!!.setText(R.string.granted1)
            tvSendSMS1!!.setTextColor(colorGranted)
        } else {
            spSendSMS.setSpan(StrikethroughSpan(), 0, getString(R.string.access_Send_SMS).length, 0)
            tvSendSMS!!.text = spSendSMS
            tvSendSMS!!.setTextColor(colorError)
            tvSendSMS1!!.setText(R.string.denied1)
            tvSendSMS1!!.setTextColor(colorError)
        }
        if ((ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.RECEIVE_SMS
            ) == PackageManager.PERMISSION_GRANTED) &&
            (ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.SEND_SMS
            ) == PackageManager.PERMISSION_GRANTED)
        ) {
            btnSMS!!.setEnabled(false)
            btnSMS!!.visibility = View.INVISIBLE
        } else {
            btnSMS!!.setEnabled(true)
            btnSMS!!.visibility = View.VISIBLE
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        // Метод обработки ответа на запрос разрешений.
        // Выводятся вплывающие сообщения, в том случае, если пришёл отказ на выдачу разрешений.
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        viewPermissions()
        when (requestCode) {
            REQUEST_PERMISSIONS_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_DENIED) {
                    val message = getString(R.string.location_request_blocked) + "\n" +
                            getString(R.string.set_access_manually)
                    Snackbar.make(btnLocation!!, message,
                        Snackbar.LENGTH_INDEFINITE).show()
                }
                return
            }
            REQUEST_PERMISSIONS_BACKGROUND_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_DENIED) {
                    val message = getString(R.string.location_bg_request_blocked) + "\n" +
                            getString(R.string.set_access_manually)
                    Snackbar.make(btnBackgroundLocation!!, message,
                        Snackbar.LENGTH_INDEFINITE)
                        .show()
                }
                return
            }
            REQUEST_PERMISSIONS_SMS -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_DENIED) {
                    val message = getString(R.string.sms_request_blocked) + "\n" +
                            getString(R.string.set_access_manually)
                    Snackbar.make(btnSMS!!, message,
                        Snackbar.LENGTH_INDEFINITE).show()
                }
            }
        }
    }

}