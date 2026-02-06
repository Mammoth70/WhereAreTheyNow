package ru.mammoth70.wherearetheynow

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class) // Обязательно для инструментальных тестов.
class SimpleInstrumentedTest {

    @Test
    fun useAppContext() {
        // Получаем контекст приложения
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        // Проверяем, что package name совпадает.
        assertEquals("ru.mammoth70.wherearetheynow", appContext.packageName)
    }
}