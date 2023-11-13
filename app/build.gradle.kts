import java.util.*

@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
}

fun String.toProperties() = Properties().apply {
    rootProject.file(this@toProperties).run {
        if (exists())
            load(inputStream())
        else
            println("Warning: ${this@toProperties} file is absent")
    }
}

val apiKeys = "api_key.properties".toProperties()

object SdkVersion {
    val javaVersion = JavaVersion.VERSION_11
    const val androidMinSdk = 24
    const val androidTargetSdk = 34
}

android {
    namespace = "com.koflox.currency_exchanger"
    compileSdk = SdkVersion.androidTargetSdk

    defaultConfig {
        applicationId = "com.koflox.currency_exchanger"
        minSdk = SdkVersion.androidMinSdk
        targetSdk = SdkVersion.androidTargetSdk
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }
    buildFeatures {
        buildConfig = true
    }
    buildTypes {
        val openExchangeRatesApiKey = apiKeys["openExchangeRatesApiKey"] ?: throw IllegalArgumentException("Missing openExchangeRatesApiKey")
        debug {
            buildConfigField("String", "API_KEY_OPEN_EXCHANGE_RATES", "$openExchangeRatesApiKey")
        }
    }
    compileOptions {
        sourceCompatibility = SdkVersion.javaVersion
        targetCompatibility = SdkVersion.javaVersion
    }
    kotlinOptions {
        jvmTarget = SdkVersion.javaVersion.majorVersion
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeKotlinCompilerExtension.get()
    }
}

dependencies {
    implementation(libs.app.compat)
    implementation(libs.android.lifecycle.viewmodel.ktx)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.material3)
    implementation(libs.compose.activity)
    implementation(libs.compose.lifecycle)
    implementation(libs.compose.constraintlayout)
    debugImplementation(libs.compose.tooling)

    implementation(libs.kotlin.coroutines)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    implementation(libs.retrofit)
    implementation(libs.retrofit.gson.converter)
    implementation(libs.okhttp)
    implementation(libs.okhttp.interceptor)

    implementation(libs.koin)
    implementation(libs.koin.android)
    implementation(libs.koin.android.compose)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlin.coroutines.test)
}
