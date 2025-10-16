package ru.mammoth70.wherearetheynow

object AppColors {
    // Объект для работы с цветовыми ресурсами.

    const val COLOR_WHITE: String = "#FFFFFF"
    const val COLOR_BLACK: String = "#000000"
    const val COLOR_RED: String = "#FF001A"
    const val COLOR_ORANGE: String = "#FF5D00"
    const val COLOR_YELLOW: String = "#FFBB00"
    const val COLOR_GREEN: String = "#32FF00"
    const val COLOR_DARKGREEN: String = "#168000"
    const val COLOR_CYAN: String = "#00FFFB"
    const val COLOR_BLUE: String = "#002EFF"
    const val COLOR_VIOLET: String = "#8F00FF"
    const val COLOR_MAGENTA: String = "#FF00B1"

    val colors: ArrayList<String> = ArrayList(
        listOf(
            COLOR_RED, COLOR_ORANGE, COLOR_YELLOW, COLOR_GREEN, COLOR_DARKGREEN,
            COLOR_CYAN, COLOR_BLUE, COLOR_VIOLET, COLOR_MAGENTA, COLOR_BLACK
        )
    )

    private const val OCTOHORPE = '#'
    private const val COLOR_WHITE_8 = "#FFFFFFFF"
    private const val ALPHA_48 = "48"
    private const val ALPHA_16 = "16"

    fun getColorAlpha(color: String?): String {
        // Функция возвращает прозрачный цвет по строке с цветом.
        return addTransparenty(color, ALPHA_48)
    }

    fun getColorAlpha16(color: String?): String {
        // Функция возвращает прозрачный цвет по строке с цветом.
        return addTransparenty(color, ALPHA_16)
    }

    fun getMarker64(color: String?): Int {
        // Функция возвращает большую метку по строке с цветом.
        color?.let {
            return when (color) {
                COLOR_WHITE -> R.drawable.ic_pin_white_64
                COLOR_RED -> R.drawable.ic_pin_red_64
                COLOR_ORANGE -> R.drawable.ic_pin_orange_64
                COLOR_YELLOW -> R.drawable.ic_pin_yellow_64
                COLOR_GREEN -> R.drawable.ic_pin_green_64
                COLOR_DARKGREEN -> R.drawable.ic_pin_darkgreen_64
                COLOR_CYAN -> R.drawable.ic_pin_cyan_64
                COLOR_BLUE -> R.drawable.ic_pin_blue_64
                COLOR_VIOLET -> R.drawable.ic_pin_violet_64
                COLOR_MAGENTA -> R.drawable.ic_pin_magenta_64
                COLOR_BLACK -> R.drawable.ic_pin_black_64
                else -> R.drawable.ic_pin_empty_64
            }
        }
        return R.drawable.ic_pin_error_64
    }

    fun getMarker48(color: String?): Int {
        // Функция возвращает маленькую метку по строке с цветом.
        color?.let {
            return when (color) {
                COLOR_WHITE -> R.drawable.ic_pin_white_48
                COLOR_RED -> R.drawable.ic_pin_red_48
                COLOR_ORANGE -> R.drawable.ic_pin_orange_48
                COLOR_YELLOW -> R.drawable.ic_pin_yellow_48
                COLOR_GREEN -> R.drawable.ic_pin_green_48
                COLOR_DARKGREEN -> R.drawable.ic_pin_darkgreen_48
                COLOR_CYAN -> R.drawable.ic_pin_cyan_48
                COLOR_BLUE -> R.drawable.ic_pin_blue_48
                COLOR_VIOLET -> R.drawable.ic_pin_violet_48
                COLOR_MAGENTA -> R.drawable.ic_pin_magenta_48
                COLOR_BLACK -> R.drawable.ic_pin_black_48
                else -> R.drawable.ic_pin_empty_48
            }
        }
        return R.drawable.ic_pin_error_48
    }

    private fun addTransparenty(color: String?, tranparent: String?): String {
        // Функция добавляет прозрачность к цвету без прозрачности.
        // Если что-то не так - возвращает белый цвет.
        return if ((color != null) && (tranparent != null) && (color.length == 7) &&
            (tranparent.length == 2) && (color[0] == OCTOHORPE)
        ) {
            OCTOHORPE.toString() + tranparent + color.substring(1, 7)
        } else {
            COLOR_WHITE_8
        }
    }

}