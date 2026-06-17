plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.secrets)
}

secrets {
    propertiesFileName = "local.properties" // Файл с секретами
    defaultPropertiesFileName = "local.properties" // Дефолтные значения для CI
}

android {
    namespace = "ru.mammoth70.wherearetheynow"
    compileSdk {
        version = release(37)
    }

    testOptions {
        unitTests {
            isReturnDefaultValues = true
            all {
                it.useJUnitPlatform()
            }
        }
    }

    /* androidResources {
        localeFilters.clear()
        localeFilters.addAll(listOf("en", "ru"))
    } */

    defaultConfig {
        val versionMajor = 5
        val versionMinor = 1
        val versionPatch = 0
        applicationId = "ru.mammoth70.wherearetheynow"
        minSdk = 31
        targetSdk = 37
        versionName = "${versionMajor}.${versionMinor}.${versionPatch}"
        versionCode = versionMajor * 10000 + versionMinor * 100 + versionPatch
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        @Suppress("DEPRECATION")
        resourceConfigurations.clear()
        @Suppress("DEPRECATION")
        resourceConfigurations.addAll(listOf("en", "ru"))
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
            )
        }
    }

    packaging {
        jniLibs {
            // useLegacyPackaging = true // меньше размер apk (в два раза, со 100 до 50 МБ), но больше размер приложения в памяти
            useLegacyPackaging = false   // больше размер apk, но меньше размер приложения в памяти
        }
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

base {
    archivesName = "WhereAreTheyNow-${android.defaultConfig.versionName}"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.google.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.play.services.location)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.yandex.maps.mobile)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.work.runtime.ktx)

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.junit.jupiter.params)
    testImplementation(libs.org.json)
    testRuntimeOnly(libs.junit.platform.launcher)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}