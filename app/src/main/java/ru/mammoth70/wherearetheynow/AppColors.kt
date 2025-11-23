package ru.mammoth70.wherearetheynow

import androidx.core.graphics.toColorInt

object AppColors {
    // Объект для работы с цветовыми ресурсами.

    const val COLOR_WHITE = "#FFFFFF"
    const val COLOR_BLACK = "#000000"
    const val COLOR_RED = "#FF001A"
    const val COLOR_ORANGE = "#FF5D00"
    const val COLOR_YELLOW = "#FFBB00"
    const val COLOR_GREEN = "#32FF00"
    const val COLOR_DARKGREEN = "#168000"
    const val COLOR_CYAN = "#00FFFB"
    const val COLOR_BLUE = "#002EFF"
    const val COLOR_LIGHTBLUE = "#4495FF"
    const val COLOR_VIOLET = "#8F00FF"
    const val COLOR_MAGENTA = "#FF00B1"
    const val COLOR_BROWN = "#7E4301"

    val colors =
        listOf(
            COLOR_RED, COLOR_ORANGE, COLOR_YELLOW, COLOR_GREEN, COLOR_DARKGREEN, COLOR_CYAN,
            COLOR_BLUE, COLOR_LIGHTBLUE, COLOR_VIOLET, COLOR_MAGENTA, COLOR_BROWN, COLOR_BLACK,
        )

    enum class Alpha(val tranparent: String) {
        ALPHA_48("#48"),
        ALPHA_16("#16")
    }

    fun getColorAlpha(color: String?): Int {
        // Функция возвращает номер прозрачного цвета по строке с цветом.
        return addTransparenty(color, Alpha.ALPHA_48).toColorInt()
    }

    fun getColorAlpha16(color: String?): Int {
        // Функция возвращает номер прозрачного цвета по строке с цветом.
        return addTransparenty(color, Alpha.ALPHA_16).toColorInt()
    }

    fun getMarker(color: String?): Int {
        // Функция возвращает маленькую метку по строке с цветом.
        color?.let {
            return when (color) {
                COLOR_WHITE -> R.drawable.ic_pin_white
                COLOR_RED -> R.drawable.ic_pin_red
                COLOR_ORANGE -> R.drawable.ic_pin_orange
                COLOR_YELLOW -> R.drawable.ic_pin_yellow
                COLOR_GREEN -> R.drawable.ic_pin_green
                COLOR_DARKGREEN -> R.drawable.ic_pin_darkgreen
                COLOR_CYAN -> R.drawable.ic_pin_cyan
                COLOR_BLUE -> R.drawable.ic_pin_blue
                COLOR_LIGHTBLUE -> R.drawable.ic_pin_lightblue
                COLOR_VIOLET -> R.drawable.ic_pin_violet
                COLOR_MAGENTA -> R.drawable.ic_pin_magenta
                COLOR_BROWN -> R.drawable.ic_pin_brown
                COLOR_BLACK -> R.drawable.ic_pin_black
                else -> R.drawable.ic_pin_empty
            }
        }
        return R.drawable.ic_pin_error
    }

    private fun addTransparenty(color: String?, alpha: Alpha): String {
        // Функция добавляет прозрачность к цвету без прозрачности.
        // Если что-то не так - возвращает белый цвет.
        return if ((!color.isNullOrBlank()) && (color.length == 7) && (color[0] == '#')) {
            alpha.tranparent + color.substring(1, 7)
        } else {
            "#FFFFFFFF"
        }
    }

}