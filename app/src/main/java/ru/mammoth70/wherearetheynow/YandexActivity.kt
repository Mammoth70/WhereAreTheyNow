package ru.mammoth70.wherearetheynow

import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PointF
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
import ru.mammoth70.wherearetheynow.MapUtil.MAP_TILT_DEFAULT

class YandexActivity : LocationActivity(), CameraListener, SizeChangedListener {
    // Activity выводит yandex-карту с геолокацией, переданной через intent.

    private val mapView: MapView by lazy { findViewById<MapView>(R.id.yandexview) }
    private val map: Map by lazy { mapView.mapWindow.map }
    private val fabNord: FloatingActionButton by lazy {
        findViewById<FloatingActionButton>(R.id.floatingActionButtonMapNord) }
    private val fabZoomIn: FloatingActionButton by lazy {
        findViewById<FloatingActionButton>(R.id.floatingActionButtonMapZoomIn) }
    private val fabZoomOut: FloatingActionButton by lazy {
        findViewById<FloatingActionButton>(R.id.floatingActionButtonMapZoomOut) }
    private val fab2D3D: FloatingActionButton by lazy {
        findViewById<FloatingActionButton>(R.id.floatingActionButtonMapTilt) }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Функция вызывается при создании Activity.
        // Получение из intent данных.
        // Обработка данных для вывода карты.
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_yandex)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.yandex))
        { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top,
                systemBars.right, systemBars.bottom)
            insets
        }

        createFrameTitle(this)

        mapView.mapWindow.map.addCameraListener(this)
        when (getResources().configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_NO ->
                // ночная тема не активна, используется светлая тема
                map.isNightModeEnabled = false
            Configuration.UI_MODE_NIGHT_YES ->
                // ночная тема активна, и она используется
                map.isNightModeEnabled = true
        }
        val mapObjectCollection = mapView.mapWindow.map.mapObjects.addCollection()
        val points = ArrayList<Point?>()
        val imageProviders = ArrayList<ImageProvider?>()
        val placemarkMapObjects = ArrayList<PlacemarkMapObject?>()
        val circleMapObjects = ArrayList<CircleMapObject?>()

        // Добавляем все метки. Цикл по списку разрешённых телефонов.
        for (key in Util.phones) {
            if (Util.phone2record.containsKey(key)) {
                val value = Util.phone2record[key]
                value?.let {
                    points.add(Point(value.latitude, value.longitude))
                    imageProviders.add(
                        ImageProvider.fromBitmap(
                            getBitmapFromColor(Util.phone2color[key])
                        )
                    )
                    placemarkMapObjects.add(mapObjectCollection.addPlacemark().apply {
                        geometry = Point(value.latitude, value.longitude)
                        setIcon(ImageProvider.fromBitmap(
                            getBitmapFromColor(Util.phone2color[key])
                        ),IconStyle().apply { anchor = PointF(0.5f, 1f) })
                        userData = key
                        addTapListener(mapObjectTapListener)
                        setText(Util.phone2name[key]!! ,markTextStyle)
                    }
                    )

                    if (MapUtil.selectedMapCircle) {
                        circleMapObjects.add(
                            mapObjectCollection.addCircle(
                                Circle(Point(value.latitude,
                                    value.longitude), MapUtil.selectedMapCircleRadius)
                            ).apply {
                                strokeColor = Util.phone2color[key]?.toColorInt()!!
                                strokeWidth = 1f
                                fillColor = AppColors.getColorAlpha(Util.phone2color[key])
                            }
                        )
                    }

                }
            }
        }

        reloadMapFromPoint(this, startRecord)
    }

    private val markTextStyle: TextStyle
        get() {
            // Функция настраивает стиль текста над меткой.
            val typedValueTextColor = TypedValue()
            val typedValueOutLineColor = TypedValue()
            getTheme().resolveAttribute(
                com.google.android.material.R.attr.colorOnBackground,
                typedValueTextColor, true
            )
            getTheme().resolveAttribute(
                android.R.attr.colorBackground,
                typedValueOutLineColor, true
            )
            return TextStyle().apply {
                size = 12f
                color = typedValueTextColor.data
                outlineColor = typedValueOutLineColor.data
                outlineWidth = 4f
                placement = TextStyle.Placement.TOP
            }
        }

    override fun reloadMapFromPoint(context: Context, rec: PointRecord) {
        // Функция передвигает карту на PointRecord.
        start2D3D()
        map.move(CameraPosition(Point(rec.latitude, rec.longitude),
            MapUtil.selectedMapZoom, 0f, MapUtil.selectedMapTilt))
    }

    private val mapObjectTapListener =
        MapObjectTapListener { mapObject: MapObject?, point: Point? ->
            // Функция, отвечающий за тапы по различным объектам на карте.
            val phone = mapObject!!.userData as String?
            val name = Util.phone2name[phone]
            val rec = Util.phone2record[phone]
            rec?.let {
                val newZoom = if (map.cameraPosition.zoom < MapUtil.selectedMapZoom) {
                    MapUtil.selectedMapZoom
                } else {
                    map.cameraPosition.zoom
                }
                tvTitle.text = name
                tvDateTime.text = MapUtil.timePassed(rec.dateTime,
                    this
                )
                map.move(CameraPosition(
                    Point(rec.latitude, rec.longitude),
                    newZoom,
                    0f,
                    map.cameraPosition.tilt
                ))
                val message = name + "\n" + String.format(
                    Locale.US, PointRecord.FORMAT_POINT,
                    rec.latitude, rec.longitude
                )
                Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
            }
            true
        }

    private fun createBitmapFromVector(art: Int): Bitmap? {
        // Функция преобразовывает векторное изображение в bitmap.
        val drawable = ContextCompat.getDrawable(this, art) ?: return null
        val bitmap = createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    private fun getBitmapFromColor(color: String?): Bitmap? {
        // Функция возвращает метку заданного цвета.
        return createBitmapFromVector(AppColors.getMarker(color))
    }

    override fun onStart() {
        // Функция вызывается перед тем, как Activity будет видно пользователю.
        super.onStart()
        MapKitFactory.getInstance().onStart()
        mapView.onStart()
    }

    override fun onStop() {
        // Функция вызывается, когда Activity становится не видно пользователю.
        mapView.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    private fun start2D3D() {
        // Функция устанавливает в начальное состояние кнопку FAB "2D/3D".
        if (MapUtil.selectedMapTilt == 0f) {
            fab2D3D.setContentDescription(getString(R.string.map3d))
            fab2D3D.setImageResource(R.drawable.ic_action_3d)
        } else {
            fab2D3D.setContentDescription(getString(R.string.map2d))
            fab2D3D.setImageResource(R.drawable.ic_action_2d)
        }
    }

    fun onMap2D3D(@Suppress("UNUSED_PARAMETER") ignored: View?) {
        // Обработчик кнопки FAB "2D/3D".
        // Функция меняет режим карты 2D / 3D.
        val newTilt = if (map.cameraPosition.tilt == 0f) {
            if (MapUtil.selectedMapTilt == 0f) {
                MAP_TILT_DEFAULT
            } else {
                MapUtil.selectedMapTilt
            }
        } else {
            0f
        }
        map.move(CameraPosition(
            map.cameraPosition.target,
            map.cameraPosition.zoom,
            map.cameraPosition.azimuth,
            newTilt
        ))
    }

    fun onMapNordClicked(@Suppress("UNUSED_PARAMETER") ignored: View?) {
        // Обработчик кнопки FAB "На север".
        // Функция поворачивает карту в положение север сверху.
        map.move(CameraPosition(
            map.cameraPosition.target,
            map.cameraPosition.zoom,
            0f,
            map.cameraPosition.tilt
        ))
    }

    fun onMapZoomIn(@Suppress("UNUSED_PARAMETER") ignored: View?) {
        // Обработчик кнопки FAB "Zoom In".
        // Функция приближает объекты на карте.
        val newZoom = if ((map.cameraPosition.zoom + 1f) > map.cameraBounds.maxZoom) {
            map.cameraBounds.maxZoom
        } else {
            map.cameraPosition.zoom + 1f
        }
        map.move(CameraPosition(
            map.cameraPosition.target,
            newZoom,
            map.cameraPosition.azimuth,
            map.cameraPosition.tilt
        ))
    }

    fun onMapZoomOut(@Suppress("UNUSED_PARAMETER") ignored: View?) {
        // Обработчик кнопки FAB "Zoom Out".
        // Функция отдаляет объекты на карте.
        val newZoom = if ((map.cameraPosition.zoom - 1f) < map.cameraBounds.minZoom) {
            map.cameraBounds.minZoom
        } else {
            map.cameraPosition.zoom - 1f
        }
        map.move(CameraPosition(
            map.cameraPosition.target,
            newZoom,
            map.cameraPosition.azimuth,
            map.cameraPosition.tilt
        ))
    }

    private fun setFabStatus() {
        // Функция устанавливает в правильное состояние все FAB-ы.
        val zoom = map.cameraPosition.zoom
        if (zoom <= map.cameraBounds.minZoom) {
            fabZoomOut.hide()
            fabZoomIn.show()
        } else if (zoom >= map.cameraBounds.maxZoom) {
            fabZoomIn.hide()
            fabZoomOut.show()
        } else {
            fabZoomIn.show()
            fabZoomOut.show()
        }
        if (map.cameraPosition.tilt == 0f) {
            fab2D3D.setContentDescription(getString(R.string.map3d))
            fab2D3D.setImageResource(R.drawable.ic_action_3d)
        } else {
            fab2D3D.setContentDescription(getString(R.string.map2d))
            fab2D3D.setImageResource(R.drawable.ic_action_2d)
        }
        if (map.cameraPosition.azimuth == 0f) {
            fabNord.hide()
        } else {
            fabNord.show()
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

    fun onCloseClicked(@Suppress("UNUSED_PARAMETER") ignored: View?) {
        // Функция - обработчик кнопки "назад".
        finish()
    }
}