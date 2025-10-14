package ru.mammoth70.wherearetheynow;

import java.util.ArrayList;
import java.util.Arrays;

public class AppColors {
    // Класс для работы с цветовыми ресурсами.

    public static final String COLOR_WHITE = "#FFFFFF";
    public static final String COLOR_BLACK = "#000000";
    public static final String COLOR_RED = "#FF001A";
    public static final String COLOR_ORANGE = "#FF5D00";
    public static final String COLOR_YELLOW = "#FFBB00";
    public static final String COLOR_GREEN = "#32FF00";
    public static final String COLOR_DARKGREEN = "#168000";
    public static final String COLOR_CYAN = "#00FFFB";
    public static final String COLOR_BLUE = "#002EFF";
    public static final String COLOR_VIOLET = "#8F00FF";
    public static final String COLOR_MAGENTA = "#FF00B1";

    public static final ArrayList<String> colors =
            new ArrayList<>(Arrays.asList(COLOR_RED, COLOR_ORANGE, COLOR_YELLOW, COLOR_GREEN, COLOR_DARKGREEN,
                    COLOR_CYAN, COLOR_BLUE, COLOR_VIOLET, COLOR_MAGENTA, COLOR_BLACK));

    private static final char OCTOHORPE = '#';
    private static final String COLOR_WHITE_8 = "#FFFFFFFF";
    private static final String ALPHA_48 = "48";
    private static final String ALPHA_16 = "16";

    public static String getColorAlpha(String color) {
        // Метод возвращает прозрачный цвет по строке с цветом.
        return addTransparenty(color, ALPHA_48);
    }

    public static String getColorAlpha16(String color) {
        // Метод возвращает прозрачный цвет по строке с цветом.
        return addTransparenty(color, ALPHA_16);
    }

    public static int getColorMarker(String color) {
        // Метод возвращает большую метку по строке с цветом.
        if (color != null) {
            switch (color) {
                case (COLOR_WHITE):
                    return R.drawable.ic_pin_white_64;
                case (COLOR_RED):
                    return R.drawable.ic_pin_red_64;
                case (COLOR_ORANGE):
                    return R.drawable.ic_pin_orange_64;
                case (COLOR_YELLOW):
                    return R.drawable.ic_pin_yellow_64;
                case (COLOR_GREEN):
                    return R.drawable.ic_pin_green_64;
                case (COLOR_DARKGREEN):
                    return R.drawable.ic_pin_darkgreen_64;
                case (COLOR_CYAN):
                    return R.drawable.ic_pin_cyan_64;
                case (COLOR_BLUE):
                    return R.drawable.ic_pin_blue_64;
                case (COLOR_VIOLET):
                    return R.drawable.ic_pin_violet_64;
                case (COLOR_MAGENTA):
                    return R.drawable.ic_pin_magenta_64;
                case (COLOR_BLACK):
                    return R.drawable.ic_pin_black_64;
                default:
                    return R.drawable.ic_pin_empty_64;
            }
        } return R.drawable.ic_pin_error_64;
    }

    public static int getColorMarkerSmall(String color) {
        // Метод возвращает маленькую метку по строке с цветом.
        if (color != null) {
            switch (color) {
                case (COLOR_WHITE):
                    return R.drawable.ic_pin_white_48;
                case (COLOR_RED):
                    return R.drawable.ic_pin_red_48;
                case (COLOR_ORANGE):
                    return R.drawable.ic_pin_orange_48;
                case (COLOR_YELLOW):
                    return R.drawable.ic_pin_yellow_48;
                case (COLOR_GREEN):
                    return R.drawable.ic_pin_green_48;
                case (COLOR_DARKGREEN):
                    return R.drawable.ic_pin_darkgreen_48;
                case (COLOR_CYAN):
                    return R.drawable.ic_pin_cyan_48;
                case (COLOR_BLUE):
                    return R.drawable.ic_pin_blue_48;
                case (COLOR_VIOLET):
                    return R.drawable.ic_pin_violet_48;
                case (COLOR_MAGENTA):
                    return R.drawable.ic_pin_magenta_48;
                case (COLOR_BLACK):
                    return R.drawable.ic_pin_black_48;
                default:
                    return R.drawable.ic_pin_empty_48;
            }
        } return R.drawable.ic_pin_error_48;
    }

    private static String addTransparenty(String color, String tranparent){
        // Функция добавляет прозрачность к цвету без прозрачности.
        // Если что-то не так - возвращает белый цвет.
        if ((color != null) && (tranparent != null) && (color.length() == 7) &&
                (tranparent.length() == 2) && (color.charAt(0) == OCTOHORPE)) {
            return OCTOHORPE + tranparent + color.substring(1, 7);
        } else {
            return COLOR_WHITE_8;
        }
    }

}