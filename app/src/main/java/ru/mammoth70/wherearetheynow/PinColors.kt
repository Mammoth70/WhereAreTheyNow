package ru.mammoth70.wherearetheynow

import androidx.core.graphics.toColorInt

object PinColors {
    // Объект для работы с цветовыми метками и цветами.

    enum class Color(val hex: String, val drawableRes: Int) {
        RED("#FF001A", R.drawable.ic_pin_red),
        ORANGE("#FF5D00", R.drawable.ic_pin_orange),
        YELLOW("#FFBB00", R.drawable.ic_pin_yellow),
        GREEN("#32FF00", R.drawable.ic_pin_green),
        DARKGREEN("#168000", R.drawable.ic_pin_darkgreen),
        CYAN("#00FFFB", R.drawable.ic_pin_cyan),
        BLUE("#002EFF", R.drawable.ic_pin_blue),
        LIGHTBLUE("#4495FF", R.drawable.ic_pin_lightblue),
        VIOLET("#8F00FF", R.drawable.ic_pin_violet),
        MAGENTA("#FF00B1", R.drawable.ic_pin_magenta),
        BROWN("#7E4301", R.drawable.ic_pin_brown),
        BLACK("#000000", R.drawable.ic_pin_black),
        ;

        companion object {
            fun fromHex(hex: String?): Color? {
                // Функция позволяет найти элемент по строке HEX.
                if (hex == null) return null
                val formattedHex = if (hex.startsWith("#")) hex.uppercase() else "#${hex.uppercase()}"
                return entries.find { it.hex == formattedHex }
            }
        }
    }


        enum class Alpha(val transparent: String) {
            ALPHA_48("#48"),
            ALPHA_16("#16"),
        }


        fun getColorAlpha(color: String?): Int {
            // Функция возвращает номер прозрачного цвета по строке с цветом.

            return addTransparenty(color, Alpha.ALPHA_48).toColorInt()
        }


        fun getColorAlpha16(color: String?): Int {
            // Функция возвращает номер прозрачного цвета по строке с цветом.

            return addTransparenty(color, Alpha.ALPHA_16).toColorInt()
        }


        fun getPin(color: String?): Int {
            // Функция возвращает метку по строке с цветом.

            return Color.fromHex(color)?.drawableRes ?: R.drawable.ic_pin_empty
        }


        private fun addTransparenty(color: String?, alpha: Alpha): String {
            // Функция добавляет прозрачность к цвету без прозрачности.
            // Если что-то не так - возвращает белый цвет.

            return if ((!color.isNullOrBlank()) && (color.length == 7) && (color[0] == '#')) {
                alpha.transparent + color.substring(1, 7)
            } else {
                "#FFFFFFFF"
            }
        }


        fun isValidColors(color: String): Boolean = Color.fromHex(color) != null
        // Функция проверяет вхождение строки в список.
}