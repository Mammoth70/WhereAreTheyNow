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
        version = release(36)
    }

    /* androidResources {
        localeFilters.clear()
        localeFilters.addAll(listOf("en", "ru"))
    } */

    defaultConfig {
        val versionMajor = 3
        val versionMinor = 13
        val versionPatch = 1
        applicationId = "ru.mammoth70.wherearetheynow"
        minSdk = 31
        targetSdk = 36
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
            // useLegacyPackaging = true  // меньше размер apk (в два раза, со 100 до 50 МБ), но больше размер приложения в памяти
            useLegacyPackaging = false  // больше размер apk, но меньше размер приложения в памяти
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
    implementation(libs.yandex.maps.mobile)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}