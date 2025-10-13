package ru.mammoth70.wherearetheynow

import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PointF
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.graphics.toColorInt
import androidx.core.graphics.createBitmap
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Circle
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraListener
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.CameraUpdateReason
import com.yandex.mapkit.map.CircleMapObject
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.MapObject
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.map.MapWindow
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.map.SizeChangedListener
import com.yandex.mapkit.map.TextStyle
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider
import java.util.Locale
import ru.mammoth70.wherearetheynow.AppColors.getColorAlpha
import ru.mammoth70.wherearetheynow.AppColors.getColorMarkerSmall
import ru.mammoth70.wherearetheynow.Util.INTENT_EXTRA_MAP_ZOOM
import ru.mammoth70.wherearetheynow.Util.INTENT_EXTRA_MAP_TILT
import ru.mammoth70.wherearetheynow.Util.INTENT_EXTRA_MAP_CIRCLE
import ru.mammoth70.wherearetheynow.Util.INTENT_EXTRA_MAP_CIRCLE_RADIUS
import ru.mammoth70.wherearetheynow.MapUtil.MAP_TILT_DEFAULT
import ru.mammoth70.wherearetheynow.MapUtil.MAP_ZOOM_DEFAULT
import ru.mammoth70.wherearetheynow.MapUtil.MAP_CIRCLE_DEFAULT
import ru.mammoth70.wherearetheynow.MapUtil.MAP_CIRCLE_DEFAULT_RADIUS

class YandexActivity : LocationActivity(), CameraListener, SizeChangedListener {
    // Activity выводит yandex-карту с геолокацией, переданной через intent.

    private var mapView: MapView? = null
    private var mapZoom = 0f
    private var mapTilt = 0f
    private var map: Map? = null
    private var fabNord: FloatingActionButton? = null
    private var fabZoomIn: FloatingActionButton? = null
    private var fabZoomOut: FloatingActionButton? = null
    private var fab2D3D: FloatingActionButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        // Метод вызывается при создании Activity.
        // Из intent получаются координаты и выводится карта с метками.
        super.onCreate(savedInstanceState)
        this.enableEdgeToEdge()
        MapKitFactory.initialize(this)
        setContentView(R.layout.activity_yandex)
        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.yandex)
        ) { v: View?, insets: WindowInsetsCompat? ->
            val systemBars = insets!!.getInsets(WindowInsetsCompat.Type.systemBars())
            v!!.setPadding(systemBars.left, systemBars.top,
                systemBars.right, systemBars.bottom)
            insets
        }
        createFrameTitle(this)
        fabNord = findViewById<FloatingActionButton>(R.id.floatingActionButtonMapNord)
        fabZoomIn = findViewById<FloatingActionButton>(R.id.floatingActionButtonMapZoomIn)
        fabZoomOut = findViewById<FloatingActionButton>(R.id.floatingActionButtonMapZoomOut)
        fab2D3D = findViewById<FloatingActionButton>(R.id.floatingActionButtonMapTilt)

        mapView = findViewById<MapView>(R.id.yandexview)
        mapView!!.mapWindow.map.addCameraListener(this)
        map = mapView!!.mapWindow.map
        val currentNightMode =
            getResources().configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        when (currentNightMode) {
            Configuration.UI_MODE_NIGHT_NO ->
                // ночная тема не активна, используется светлая тема
                map!!.isNightModeEnabled = false
            Configuration.UI_MODE_NIGHT_YES ->
                // ночная тема активна, и она используется
                map!!.isNightModeEnabled = true
        }
        val mapObjectCollection = mapView!!.mapWindow.map.mapObjects.addCollection()

        // Стартовую точку привязываем к данным, переданным через intent.
        mapZoom = intent!!.getFloatExtra(INTENT_EXTRA_MAP_ZOOM,
            MAP_ZOOM_DEFAULT)
        mapTilt = intent!!.getFloatExtra(INTENT_EXTRA_MAP_TILT,
            MAP_TILT_DEFAULT)
        val mapCircle =
            intent!!.getBooleanExtra(INTENT_EXTRA_MAP_CIRCLE,
                MAP_CIRCLE_DEFAULT)
        val mapCircleRadius = intent!!.getFloatExtra(
            INTENT_EXTRA_MAP_CIRCLE_RADIUS,
            MAP_CIRCLE_DEFAULT_RADIUS
        )

        val points = ArrayList<Point?>()
        val imageProviders = ArrayList<ImageProvider?>()
        val placemarkMapObjects = ArrayList<PlacemarkMapObject?>()
        val circleMapObjects = ArrayList<CircleMapObject?>()

        // Стиль иконки метки.
        val iconStyle = IconStyle()
        iconStyle.anchor = PointF(0.5f, 1f)

        // Добавляем все метки. Цикл по списку разрешённых телефонов.
        for (key in Util.phones) {
            if (Util.phone2record.containsKey(key)) {
                val value = Util.phone2record[key]
                if (value != null) {
                    points.add(Point(value.latitude, value.longitude))
                    imageProviders.add(
                        ImageProvider.fromBitmap(
                            getBitmapFromColor(
                                Util.phone2color[key]
                            )
                        )
                    )
                    placemarkMapObjects.add(mapObjectCollection.addPlacemark().apply {
                        geometry = Point(value.latitude, value.longitude)
                        setIcon(ImageProvider.fromBitmap(
                            getBitmapFromColor(
                                Util.phone2color[key]
                            )
                        ),iconStyle)
                        userData = key
                        addTapListener(mapObjectTapListener)
                        setText(Util.phone2name[key].toString() ,markTextStyle)
                    }
                    )

                    if (mapCircle) {
                        circleMapObjects.add(
                            mapObjectCollection.addCircle(
                                Circle(Point(value.latitude,
                                    value.longitude), mapCircleRadius)
                            ).apply {
                                strokeColor = Util.phone2color[key]?.toColorInt()!!
                                strokeWidth = 1f
                                fillColor = getColorAlpha(Util.phone2color[key]).toColorInt()
                            }
                        )
                    }

                }
            }
        }

        checkNotNull(startRecord)
        reloadMapFromPoint(this, startRecord!!)
    }

    private val markTextStyle: TextStyle
        get() {
            // Метод настраивает стиль текста над меткой.
            val textStyle = TextStyle()
            textStyle.size = 12f
            val theme = getTheme()
            val typedValueTextColor = TypedValue()
            val typedValueOutLineColor = TypedValue()
            theme.resolveAttribute(
                com.google.android.material.R.attr.colorOnBackground,
                typedValueTextColor, true
            )
            theme.resolveAttribute(
                android.R.attr.colorBackground,
                typedValueOutLineColor, true
            )
            textStyle.color = typedValueTextColor.data
            textStyle.outlineColor = typedValueOutLineColor.data
            textStyle.outlineWidth = 4f
            textStyle.placement = TextStyle.Placement.TOP
            return textStyle
        }

    override fun reloadMapFromPoint(context: Context, rec: PointRecord) {
        // Метод перестраивает карту по PointRecord.
        start2D3D()
        val point = Point(rec.latitude, rec.longitude)
        val cameraPosition = CameraPosition(point, mapZoom, 0f, mapTilt)
        map!!.move(cameraPosition)
    }

    private val mapObjectTapListener =
        MapObjectTapListener { mapObject: MapObject?, point: Point? ->
            // Метод, отвечающий за тапы по различным объектам на карте.
            val phone = mapObject!!.userData as String?
            val name = Util.phone2name[phone]
            val rec = Util.phone2record[phone]
            if (rec != null) {
                val currCameraPosition = map!!.cameraPosition
                val newPoint = Point(rec.latitude, rec.longitude)
                var newZoom = currCameraPosition.zoom
                if (newZoom < mapZoom) {
                    newZoom = mapZoom
                }
                checkNotNull(tvName)
                tvName!!.text = Util.phone2name[phone]
                checkNotNull(tvDateTime)
                tvDateTime!!.text = MapUtil.timePassed(Util.phone2record[phone]!!.datetime,
                    this
                )
                val newCameraPosition = CameraPosition(
                    newPoint,
                    newZoom,
                    0f,
                    currCameraPosition.tilt
                )
                map!!.move(newCameraPosition)
                val message = name + "\n" + String.format(
                    Locale.US, PointRecord.FORMAT_POINT,
                    point!!.latitude, point.longitude
                )
                Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
            }
            true
        }

    private fun createBitmapFromVector(art: Int): Bitmap {
        // Функция преобразовывает векторное изображение в bitmap.
        val drawable: Drawable? = checkNotNull(ContextCompat.getDrawable(this, art))
        val bitmap = createBitmap(drawable!!.intrinsicWidth, drawable.intrinsicHeight)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    private fun getBitmapFromColor(color: String?): Bitmap {
        // Метод возвращает метку заданного цвета.
        return createBitmapFromVector(getColorMarkerSmall(color))
    }

    override fun onStart() {
        // Метод вызывается перед тем, как Activity будет видно пользователю.
        super.onStart()
        MapKitFactory.getInstance().onStart()
        mapView!!.onStart()
    }

    override fun onStop() {
        // Метод вызывается, когда Activity становится не видно пользователю.
        mapView!!.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    private fun start2D3D() {
        // Метод устанавливает в начальное состояние кнопку FAB "2D/3D".
        if (mapTilt == 0f) {
            fab2D3D!!.setContentDescription(getString(R.string.map3d))
            fab2D3D!!.setImageResource(R.drawable.ic_action_3d)
        } else {
            fab2D3D!!.setContentDescription(getString(R.string.map2d))
            fab2D3D!!.setImageResource(R.drawable.ic_action_2d)
        }
    }

    fun onMap2D3D(@Suppress("UNUSED_PARAMETER") ignored: View?) {
        // Обработчик кнопки FAB "2D/3D".
        // Метод меняет режим карты 2D / 3D.
        val currCameraPosition = map!!.cameraPosition
        val currTilt = currCameraPosition.tilt
        val newTilt: Float = if (currTilt == 0f) {
            if (mapTilt == 0f) {
                MAP_TILT_DEFAULT
            } else {
                mapTilt
            }
        } else {
            0f
        }
        val newCameraPosition = CameraPosition(
            currCameraPosition.target,
            currCameraPosition.zoom,
            currCameraPosition.azimuth,
            newTilt
        )
        map!!.move(newCameraPosition)
    }

    fun onMapNordClicked(@Suppress("UNUSED_PARAMETER") ignored: View?) {
        // Обработчик кнопки FAB "На север".
        // Метод поворачивает карту в положение север сверху.
        val currCameraPosition = map!!.cameraPosition
        val newCameraPosition = CameraPosition(
            currCameraPosition.target,
            currCameraPosition.zoom,
            0f,
            currCameraPosition.tilt
        )
        map!!.move(newCameraPosition)
    }

    fun onMapZoomIn(@Suppress("UNUSED_PARAMETER") ignored: View?) {
        // Обработчик кнопки FAB "Zoom In".
        // Метод приближает объекты на карте.
        val currCameraPosition = map!!.cameraPosition
        var newZoom = currCameraPosition.zoom + 1f
        if (newZoom > map!!.cameraBounds.maxZoom) {
            newZoom = map!!.cameraBounds.maxZoom
        }
        val newCameraPosition = CameraPosition(
            currCameraPosition.target,
            newZoom,
            currCameraPosition.azimuth,
            currCameraPosition.tilt
        )
        map!!.move(newCameraPosition)
    }

    fun onMapZoomOut(@Suppress("UNUSED_PARAMETER") ignored: View?) {
        // Обработчик кнопки FAB "Zoom Out".
        // Метод отдаляет от объектов на карте.
        val currCameraPosition = map!!.cameraPosition
        var newZoom = currCameraPosition.zoom - 1f
        if (newZoom < map!!.cameraBounds.minZoom) {
            newZoom = map!!.cameraBounds.minZoom
        }
        val newCameraPosition = CameraPosition(
            currCameraPosition.target,
            newZoom,
            currCameraPosition.azimuth,
            currCameraPosition.tilt
        )
        map!!.move(newCameraPosition)
    }

    private fun setFabStatus() {
        // Метод устанавливает в правильное состояние все FAB-ы.
        val currCameraPosition = map!!.cameraPosition
        val zoom = currCameraPosition.zoom
        if (zoom <= map!!.cameraBounds.minZoom) {
            fabZoomOut!!.hide()
            fabZoomIn!!.show()
        } else if (zoom >= map!!.cameraBounds.maxZoom) {
            fabZoomIn!!.hide()
            fabZoomOut!!.show()
        } else {
            fabZoomIn!!.show()
            fabZoomOut!!.show()
        }
        if (currCameraPosition.tilt == 0f) {
            fab2D3D!!.setContentDescription(getString(R.string.map3d))
            fab2D3D!!.setImageResource(R.drawable.ic_action_3d)
        } else {
            fab2D3D!!.setContentDescription(getString(R.string.map2d))
            fab2D3D!!.setImageResource(R.drawable.ic_action_2d)
        }
        if (currCameraPosition.azimuth == 0f) {
            fabNord!!.hide()
        } else {
            fabNord!!.show()
        }
    }

    override fun onCameraPositionChanged(
        map: Map, cameraPosition: CameraPosition,
        cameraUpdateReason: CameraUpdateReason,
        finished: Boolean
    ) {
        // Интерфейс для подписки на изменение положения камеры.
        if (finished) {
            setFabStatus()
        }
    }

    override fun onMapWindowSizeChanged(
        mapWindow: MapWindow,
        newWidth: Int,
        newHeight: Int
    ) {
        // Интерфейс для подписки на изменение размеров карты.
        setFabStatus()
    }

}