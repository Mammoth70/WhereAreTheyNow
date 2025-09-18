package ru.mammoth70.wherearetheynow;

import android.os.Bundle;
import android.content.pm.PackageManager;
import android.Manifest;
import android.view.View;
import android.text.SpannableString;
import android.text.style.StrikethroughSpan;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.snackbar.Snackbar;

public class PermissionActivity extends AppCompatActivity {
    // Activity показывает все выданные и необходимые разрешения для работы приложения,
    // а также (для невыданных разрешений) кнопки, вызвающие код с запросом на выдачу этих разрешений
    private static final int REQUEST_PERMISSIONS_LOCATION = 482;
    private static final int REQUEST_PERMISSIONS_BACKGROUND_LOCATION = 483;
    private static final int REQUEST_PERMISSIONS_SMS = 484;
    private TextView tvCoarseLocation;
    private TextView tvFineLocation;
    private TextView tvBackgroundLocation;
    private TextView tvReceiveSMS;
    private TextView tvSendSMS;
    private TextView tvCoarseLocation1;
    private TextView tvFineLocation1;
    private TextView tvBackgroundLocation1;
    private TextView tvReceiveSMS1;
    private TextView tvSendSMS1;
    private Button btnLocation;
    private Button btnBackgroundLocation;
    private Button btnSMS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Метод вызывается при создании Activity.
        // Сразу вызываются методы для запроса недостающих разрешений,
        // После вызываются метод, заполняюший поля
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_permission);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.permissions),
                (v, insets) -> {
                    Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                   return insets;
                });
        TextView tvName = findViewById(R.id.tvTitle);
        tvName.setText(R.string.titlePermissions);

        tvCoarseLocation = findViewById(R.id.tvCoarseLocation);
        tvFineLocation = findViewById(R.id.tvFineLocation);
        tvBackgroundLocation = findViewById(R.id.tvBackgroundLocation);
        tvReceiveSMS = findViewById(R.id.tvReceiveSMS);
        tvSendSMS = findViewById(R.id.tvSendSMS);
        tvCoarseLocation1 = findViewById(R.id.tvCoarseLocation1);
        tvFineLocation1 = findViewById(R.id.tvFineLocation1);
        tvBackgroundLocation1 = findViewById(R.id.tvBackgroundLocation1);
        tvReceiveSMS1 = findViewById(R.id.tvReceiveSMS1);
        tvSendSMS1 = findViewById(R.id.tvSendSMS1);
        btnLocation = findViewById(R.id.btnLocation);
        btnBackgroundLocation = findViewById(R.id.btnBackgroundLocation);
        btnSMS = findViewById(R.id.btnSMS);

        requestPermissions();
        requestBackgroundLocationPermission(btnBackgroundLocation);
        refreshPermissions();
    }

    private void requestPermissions() {
        // Метод запрашивает все разрешения разом.
        ActivityResultLauncher<String[]> locationPermissionRequest =
                registerForActivityResult(new ActivityResultContracts
                     .RequestMultiplePermissions(), isGranted -> {}
                );
        locationPermissionRequest.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.SEND_SMS
        });
    }

    public void requestBackgroundLocationPermission(View view) {
        // Метод запрашивает разрешения работы в фоновом режиме, если выданы все разрешения геолокации.
        if ((ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) &&
            (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) &&
            (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            if ((ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION))) {
                // Предоставляет дополнительную информацию, если разрешение так и не было выдано.
                Snackbar.make(view, R.string.allow_background_location, Snackbar.LENGTH_INDEFINITE).show();
            }
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                    REQUEST_PERMISSIONS_BACKGROUND_LOCATION);
        }
    }

    public void requestBackgroundLocationPermissionButtonClick(View view) {
        // Метод - обработчик кнопки запроса работы в фоновом режиме.
        // Запрашивает разрешения работы в фоновом режиме.
        if ((ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            if ((ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION))) {
                // Предоставляет дополнительную информацию, если разрешение так и не было выдано.
                Snackbar.make(view, R.string.allow_fine_location, Snackbar.LENGTH_INDEFINITE).show();
            }
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                    REQUEST_PERMISSIONS_BACKGROUND_LOCATION);
        }
    }

    public void requestLocationPermissionButtonClick(View view) {
        // Метод - обработчик кнопки запроса разрешений геолокации.
        // Запрашивает все разрешения геолокации.
        if ((ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) ||
            (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            if ((ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) ||
                (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION))) {
                // Предоставляет дополнительную информацию, если разрешение так и не было выдано.
                Snackbar.make(view, R.string.allow_SMS, Snackbar.LENGTH_INDEFINITE).show();
            }
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                            REQUEST_PERMISSIONS_LOCATION);
        }
    }

    public void requestSMSPermissionButtonClick(View view) {
        // Метод - обработчик кнопки запроса разрешений SMS.
        // Запрашивает все разрешения SMS.
        if ((ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) ||
            (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED)) {
            if ((ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.RECEIVE_SMS)) ||
                (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.SEND_SMS))) {
                // Предоставляет дополнительную информацию, если разрешение так и не было выдано.
                Snackbar.make(view, R.string.allow_SMS, Snackbar.LENGTH_INDEFINITE).show();
            }
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECEIVE_SMS,
                            Manifest.permission.SEND_SMS},
                            REQUEST_PERMISSIONS_SMS);
        }
    }

    private void refreshPermissions() {
        // Метод проверяет необходимые разрешения и выводит их.
        // Зелёный цвет, если разрешение выдано, красный цвет и зачёркнуто, если разрешение не выдано.
        // Также, если разрешения нет, делается видимой кнопка запроса.

        SpannableString spCoarseLocation = new SpannableString(getString(R.string.access_coarse_location));
        SpannableString spFineLocation = new SpannableString(getString(R.string.access_fine_location));
        SpannableString spBackgroundLocation = new SpannableString(getString(R.string.access_background_location));
        SpannableString spReceiveSMS = new SpannableString(getString(R.string.access_Receive_SMS));
        SpannableString spSendSMS = new SpannableString(getString(R.string.access_Send_SMS));

        if ((ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
            tvCoarseLocation.setText(R.string.access_coarse_location);
            tvCoarseLocation.setTextColor(getResources().getColor(R.color.granted,null));
            tvCoarseLocation1.setText(R.string.granted1);
            tvCoarseLocation1.setTextColor(getResources().getColor(R.color.granted,null));

        } else {
            spCoarseLocation.setSpan(new StrikethroughSpan(), 0, getString(R.string.access_coarse_location).length(), 0);
            tvCoarseLocation.setText(spCoarseLocation);
            tvCoarseLocation.setTextColor(getResources().getColor(R.color.denied,null));
            tvCoarseLocation1.setText(R.string.denied1);
            tvCoarseLocation1.setTextColor(getResources().getColor(R.color.denied,null));
        }
        if ((ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
            tvFineLocation.setText(R.string.access_fine_location);
            tvFineLocation.setTextColor(getResources().getColor(R.color.granted,null));
            tvFineLocation1.setText(R.string.granted1);
            tvFineLocation1.setTextColor(getResources().getColor(R.color.granted,null));
            btnLocation.setEnabled(false);
            btnLocation.setVisibility(View.INVISIBLE);
        } else {
            spFineLocation.setSpan(new StrikethroughSpan(), 0, getString(R.string.access_fine_location).length(), 0);
            tvFineLocation.setText(spFineLocation);
            tvFineLocation.setTextColor(getResources().getColor(R.color.denied,null));
            tvFineLocation1.setText(R.string.denied1);
            tvFineLocation1.setTextColor(getResources().getColor(R.color.denied,null));
            btnLocation.setEnabled(true);
            btnLocation.setVisibility(View.VISIBLE);
        }
        if ((ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
            tvBackgroundLocation.setText(R.string.access_background_location);
            tvBackgroundLocation.setTextColor(getResources().getColor(R.color.granted,null));
            tvBackgroundLocation1.setText(R.string.granted1);
            tvBackgroundLocation1.setTextColor(getResources().getColor(R.color.granted,null));
            btnBackgroundLocation.setEnabled(false);
            btnBackgroundLocation.setVisibility(View.INVISIBLE);
        } else {
            spBackgroundLocation.setSpan(new StrikethroughSpan(), 0, getString(R.string.access_background_location).length(), 0);
            tvBackgroundLocation.setText(spBackgroundLocation);
            tvBackgroundLocation.setTextColor(getResources().getColor(R.color.denied,null));
            tvBackgroundLocation1.setText(R.string.denied1);
            tvBackgroundLocation1.setTextColor(getResources().getColor(R.color.denied,null));
            btnBackgroundLocation.setEnabled(true);
            btnBackgroundLocation.setVisibility(View.VISIBLE);
        }
        if ((ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED)) {
            tvReceiveSMS.setText(R.string.access_Receive_SMS);
            tvReceiveSMS.setTextColor(getResources().getColor(R.color.granted,null));
            tvReceiveSMS1.setText(R.string.granted1);
            tvReceiveSMS1.setTextColor(getResources().getColor(R.color.granted,null));
        } else {
            spReceiveSMS.setSpan(new StrikethroughSpan(), 0, getString(R.string.access_Receive_SMS).length(), 0);
            tvReceiveSMS.setText(spReceiveSMS);
            tvReceiveSMS.setTextColor(getResources().getColor(R.color.denied,null));
            tvReceiveSMS1.setText(R.string.denied1);
            tvReceiveSMS1.setTextColor(getResources().getColor(R.color.denied,null));
        }
        if ((ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED)) {
            tvSendSMS.setText(R.string.access_Send_SMS);
            tvSendSMS.setTextColor(getResources().getColor(R.color.granted,null));
            tvSendSMS1.setText(R.string.granted1);
            tvSendSMS1.setTextColor(getResources().getColor(R.color.granted,null));
        } else {
            spSendSMS.setSpan(new StrikethroughSpan(), 0, getString(R.string.access_Send_SMS).length(), 0);
            tvSendSMS.setText(spSendSMS);
            tvSendSMS.setTextColor(getResources().getColor(R.color.denied,null));
            tvSendSMS1.setText(R.string.denied1);
            tvSendSMS1.setTextColor(getResources().getColor(R.color.denied,null));
        }
        if ((ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED) &&
            (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED)) {
            btnSMS.setEnabled(false);
            btnSMS.setVisibility(View.INVISIBLE);
        } else {
            btnSMS.setEnabled(true);
            btnSMS.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        // Метод обработки ответа на запрос разрешений.
        // Выводятся вплывающие сообщения, в том случае, если пришёл отказ на выдачу разрешений.
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        refreshPermissions();
        switch (requestCode) {
            case REQUEST_PERMISSIONS_LOCATION: {
                if (grantResults.length > 0  && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    String message = getString(R.string.location_request_blocked) + "\n" +
                                     getString(R.string.set_access_manually) ;
		    Snackbar.make(btnLocation,  message , Snackbar.LENGTH_INDEFINITE).show();
                }
                return;
            }
            case REQUEST_PERMISSIONS_BACKGROUND_LOCATION: {
                if (grantResults.length > 0  && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    String message = getString(R.string.location_bg_request_blocked) + "\n" +
                                     getString(R.string.set_access_manually);
		    Snackbar.make(btnBackgroundLocation, message, Snackbar.LENGTH_INDEFINITE).show();
                }
                return;
            }
            case REQUEST_PERMISSIONS_SMS: {
                if (grantResults.length > 0  && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    String message = getString(R.string.sms_request_blocked) + "\n" +
                                     getString(R.string.set_access_manually);
                    Snackbar.make(btnSMS, message, Snackbar.LENGTH_INDEFINITE).show();
                }

            }
        }
    }

}