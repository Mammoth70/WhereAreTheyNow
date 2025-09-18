package ru.mammoth70.wherearetheynow;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.geometry.Circle;
import com.yandex.mapkit.map.CameraListener;
import com.yandex.mapkit.map.CameraUpdateReason;
import com.yandex.mapkit.map.CircleMapObject;
import com.yandex.mapkit.map.IconStyle;
import com.yandex.mapkit.map.Map;
import com.yandex.mapkit.map.MapObjectCollection;
import com.yandex.mapkit.map.MapObjectTapListener;
import com.yandex.mapkit.map.MapWindow;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.mapkit.map.SizeChangedListener;
import com.yandex.mapkit.map.TextStyle;
import com.yandex.mapkit.mapview.MapView;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.runtime.image.ImageProvider;

import java.util.ArrayList;
import java.util.Objects;

public class YandexActivity extends LocationActivity implements CameraListener, SizeChangedListener {
    // Activity выводит yandex-карту с геолокацией, переданной через intent.

    private MapView mapView;
    private float mapZoom;
    private float mapTilt;
    private Map map;
    private FloatingActionButton fabNord;
    private FloatingActionButton fabZoomIn;
    private FloatingActionButton fabZoomOut;
    private FloatingActionButton fab2D3D;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Метод вызывается при создании Activity.
        // Из intent получается координаты и выводится карта с метками.
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        MapKitFactory.initialize(this);
        setContentView(R.layout.activity_yandex);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.yandex),
                (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        createFrameTitle(this);
        fabNord = findViewById(R.id.floatingActionButtonMapNord);
        fabZoomIn = findViewById(R.id.floatingActionButtonMapZoomIn);
        fabZoomOut = findViewById(R.id.floatingActionButtonMapZoomOut);
        fab2D3D = findViewById(R.id.floatingActionButtonMapTilt);

        mapView = findViewById(R.id.yandexview);
        mapView.getMapWindow().getMap().addCameraListener(this);
        map = mapView.getMapWindow().getMap();
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        switch (currentNightMode) {
            case Configuration.UI_MODE_NIGHT_NO:
                // ночная тема не активна, используется светлая тема
                map.setNightModeEnabled(false);
                break;
            case Configuration.UI_MODE_NIGHT_YES:
                // ночная тема активна, и она используется
                map.setNightModeEnabled(true);
                break;
        }
        MapObjectCollection mapObjectCollection = mapView.getMapWindow().getMap().getMapObjects().addCollection();

        // Стартовую точку привязываем к данным, переданным через intent.
        mapZoom = intent.getFloatExtra(Util.INTENT_EXTRA_MAP_ZOOM, MapUtil.MAP_ZOOM_DEFAULT);
        mapTilt = intent.getFloatExtra(Util.INTENT_EXTRA_MAP_TILT, MapUtil.MAP_TILT_DEFAULT);
        boolean mapCircle = intent.getBooleanExtra(Util.INTENT_EXTRA_MAP_CIRCLE, MapUtil.MAP_CIRCLE_DEFAULT);

        ArrayList<Point> points = new ArrayList<>();
        ArrayList<ImageProvider> imageProviders = new ArrayList<>();
        ArrayList<PlacemarkMapObject> placemarkMapObjects = new ArrayList<>();
        ArrayList<String> markerUserData = new ArrayList<>();
        ArrayList<CircleMapObject> circleMapObjects = new ArrayList<>();

        // Добавляем все маркеры. Цикл по списку разрешённых телефонов.
        for (String key : Util.phones) {
            if (Util.phone2record.containsKey(key)) {
                PointRecord value = Util.phone2record.get(key);
                if (value != null) {
                    points.add(new Point(value.latitude, value.longitude));
                    imageProviders.add(ImageProvider.fromBitmap
                            (getBitmapFromColor(Util.phone2color.get(key))));
                    placemarkMapObjects.add(mapObjectCollection.addPlacemark());
                    if (mapCircle) {
                        circleMapObjects.add(mapObjectCollection.addCircle(
                                new Circle(new Point(value.latitude, value.longitude), 75f)));
                    }
                    markerUserData.add(key);
                }
            }
        }

        // Стиль иконки маркера.
        IconStyle iconStyle = new IconStyle();
        iconStyle.setAnchor(new PointF(0.5f,1f));

        // Стиль текста над маркером.
        TextStyle textStyle = new TextStyle();
        textStyle.setSize(12);
        textStyle.setColor(getColor(R.color.md_theme_onBackground));
        textStyle.setOutlineColor(getColor(R.color.md_theme_background));
        textStyle.setOutlineWidth(4);
        textStyle.setPlacement(TextStyle.Placement.TOP);

        // Настраиваем свойства объектов-маркеров на карте.
        for (int i = 0; i < points.size(); i++) {
            placemarkMapObjects.get(i).setGeometry(points.get(i));
            placemarkMapObjects.get(i).setIcon(imageProviders.get(i),iconStyle);
            placemarkMapObjects.get(i).setUserData(markerUserData.get(i));
            placemarkMapObjects.get(i).addTapListener(mapObjectTapListener);
            if (mapCircle) {
                circleMapObjects.get(i).setStrokeColor(Color.parseColor(Util.phone2color.get(
                        (String) placemarkMapObjects.get(i).getUserData())));
                circleMapObjects.get(i).setStrokeWidth(1.f);
                circleMapObjects.get(i).setFillColor(Color.parseColor(AppColors.getColorAlpha(
                        Util.phone2color.get((String) placemarkMapObjects.get(i).getUserData()))));
                placemarkMapObjects.get(i).setText(Objects.requireNonNull
                        (Util.phone2name.get(markerUserData.get(i))),textStyle);
            }
        }

        reloadMapFromPoint(this, startRecord);
    }

    @Override
    protected void reloadMapFromPoint(Context context, PointRecord rec) {
        // Метод перестраивает карту по PointRecord.
        start2D3D();
        Point point = new Point(rec.latitude, rec.longitude);
        CameraPosition cameraPosition = new CameraPosition(point, mapZoom, 0f, mapTilt);
        map.move(cameraPosition);
    }

    private final MapObjectTapListener mapObjectTapListener = (mapObject, point) -> {
        // Метод, отвечающий за тапы по различным объектам на карте.
        String phone = (String)mapObject.getUserData();
        String name = Util.phone2name.get(phone);
        PointRecord rec = Util.phone2record.get(phone);
        if (rec != null) {
            CameraPosition currCameraPosition = map.getCameraPosition();
            Point newPoint = new Point(rec.latitude, rec.longitude);
            float newZoom = currCameraPosition.getZoom();
            if (newZoom < mapZoom) {
                newZoom = mapZoom;
            }
            tvName.setText(Util.phone2name.get(phone));
            tvDateTime.setText(MapUtil.timePassed(
                    Objects.requireNonNull(Util.phone2record.get(phone)).datetime, this));
            CameraPosition newCameraPosition = new CameraPosition(
                    newPoint,
                    newZoom,
                    0,
                    currCameraPosition.getTilt());
            map.move(newCameraPosition);
            /*
            String message = name + "\n" +
                    String.format(Locale.US, PointRecord.FORMAT_POINT,
                            point.getLatitude(), point.getLongitude());
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            */
        }
        return true;
    };

    private Bitmap createBitmapFromVector(int art) {
        // Функция преобразовывает векторное изображение в bitmap.
        Drawable drawable = ContextCompat.getDrawable(this, art);
        assert drawable != null;
        Bitmap bitmap = Bitmap.createBitmap(
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888
        );
        Canvas canvas =  new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    private Bitmap getBitmapFromColor(String color) {
        // Метод возвращает маркер заданного цвета.
        return createBitmapFromVector(AppColors.getColorMarkerSmall(color));
    }

    protected void onStart() {
        // Метод вызывается перед тем, как Activity будет видно пользователю.
        super.onStart();
        MapKitFactory.getInstance().onStart();
        mapView.onStart();
    }

    protected void onStop() {
        // Метод вызывается, когда Activity становится не видно пользователю.
        mapView.onStop();
        MapKitFactory.getInstance().onStop();
        super.onStop();
    }

    public void onMapNordClicked(View view) {
        // Метод поворачивает карту в положение север сверху.
        CameraPosition currCameraPosition = map.getCameraPosition();
        CameraPosition newCameraPosition = new CameraPosition(
                currCameraPosition.getTarget(),
                currCameraPosition.getZoom(),
                0,
                currCameraPosition.getTilt());
        map.move(newCameraPosition);
    }

    public void onMapZoomIn(View view) {
        // Метод приближает объекты на карте.
        CameraPosition currCameraPosition = map.getCameraPosition();
        float newZoom = currCameraPosition.getZoom() + 1f;
        if (newZoom > map.getCameraBounds().getMaxZoom()) {
            newZoom = map.getCameraBounds().getMaxZoom();
        }
        CameraPosition newCameraPosition = new CameraPosition(currCameraPosition.getTarget(),
                newZoom,
                currCameraPosition.getAzimuth(),
                currCameraPosition.getTilt());
        map.move(newCameraPosition);
    }

    public void onMapZoomOut(View view) {
        // Метод отдаляет от объектов на карте.
        CameraPosition currCameraPosition = map.getCameraPosition();
        float newZoom = currCameraPosition.getZoom() - 1f;
        if (newZoom < map.getCameraBounds().getMinZoom()) {
            newZoom = map.getCameraBounds().getMinZoom();
        }
        CameraPosition newCameraPosition = new CameraPosition(
                currCameraPosition.getTarget(),
                newZoom,
                currCameraPosition.getAzimuth(),
                currCameraPosition.getTilt());
        map.move(newCameraPosition);
    }

    private void start2D3D() {
        // Метод устанавливает в начальное состояние FAB Tilt.
        if (mapTilt == 0f) {
            fab2D3D.setContentDescription(getString(R.string.map3d));
            fab2D3D.setImageResource(R.drawable.ic_action_3d);
        } else {
            fab2D3D.setContentDescription(getString(R.string.map2d));
            fab2D3D.setImageResource(R.drawable.ic_action_2d);
        }
    }

    public void setfabStatus() {
        // Метод устанавливает в разрешенное состояние FAB-ы.
        CameraPosition currCameraPosition = map.getCameraPosition();
        float zoom = currCameraPosition.getZoom();
        if (zoom <= map.getCameraBounds().getMinZoom()) {
            fabZoomOut.hide();
            fabZoomIn.show();
        } else if (zoom >= map.getCameraBounds().getMaxZoom()) {
            fabZoomIn.hide();
            fabZoomOut.show();
        } else {
            fabZoomIn.show();
            fabZoomOut.show();
        }
        if (currCameraPosition.getTilt() == 0f) {
            fab2D3D.setContentDescription(getString(R.string.map3d));
            fab2D3D.setImageResource(R.drawable.ic_action_3d);
        } else {
            fab2D3D.setContentDescription(getString(R.string.map2d));
            fab2D3D.setImageResource(R.drawable.ic_action_2d);
        }
        if (currCameraPosition.getAzimuth() == 0f) {
            fabNord.hide();
        } else {
            fabNord.show();
        }

    }

    public void onMap2D3D(View view) {
        // Метод меняет режим карты 2D / 3D.
        CameraPosition currCameraPosition = map.getCameraPosition();
        float currTilt = currCameraPosition.getTilt();
        float newTilt;
        if (currTilt == 0f) {
            if (mapTilt == 0f) {
                newTilt = MapUtil.MAP_TILT_DEFAULT;
            } else {
                newTilt = mapTilt;
            }
        } else {
            newTilt = 0f;
        }
        CameraPosition newCameraPosition = new CameraPosition(
                currCameraPosition.getTarget(),
                currCameraPosition.getZoom(),
                currCameraPosition.getAzimuth(),
                newTilt);
        map.move(newCameraPosition);
    }

    @Override
    public void onCameraPositionChanged(@NonNull Map map, @NonNull CameraPosition cameraPosition,
                                        @NonNull CameraUpdateReason cameraUpdateReason,
                                        boolean finished) {
        // Интерфейс для подписки на изменение положения камеры.
        if (finished) {
            setfabStatus();
        }
    }

    @Override
    public void onMapWindowSizeChanged(@NonNull MapWindow mapWindow,
                                       int newWidth,
                                       int newHeight) {
        // Интерфейс для подписки на изменение размеров карты.
        setfabStatus();
    }

}