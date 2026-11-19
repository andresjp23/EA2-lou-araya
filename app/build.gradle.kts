import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kover)
}

// Host donde corren los microservicios. 10.0.2.2 es el "localhost" del PC visto desde el emulador.
// Para un celular físico, define api.host=<IP del PC> en local.properties.
val localProps = Properties().apply {
    rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
}
val apiHost: String = localProps.getProperty("api.host") ?: "10.0.2.2"

// Datos de la firma del APK (archivo keystore.properties en la raíz del proyecto).
val keystoreProps = Properties().apply {
    rootProject.file("keystore.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
}

android {
    namespace = "com.example.evaluacion2"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "cl.louaraya.app"
        minSdk = 24
        targetSdk = 37
        versionCode = 2
        versionName = "2.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "USUARIOS_BASE_URL", "\"http://$apiHost:8081/\"")
        buildConfigField("String", "CATALOGO_BASE_URL", "\"http://$apiHost:8082/\"")
        buildConfigField("String", "SOLICITUDES_BASE_URL", "\"http://$apiHost:8083/\"")
    }

    signingConfigs {
        create("release") {
            if (keystoreProps.isNotEmpty()) {
                storeFile = rootProject.file(keystoreProps.getProperty("storeFile"))
                storePassword = keystoreProps.getProperty("storePassword")
                keyAlias = keystoreProps.getProperty("keyAlias")
                keyPassword = keystoreProps.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
            if (keystoreProps.isNotEmpty()) signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

// Cobertura de pruebas: se mide sobre la lógica (ViewModels, repositorios, dominio y capa remota).
// Las pantallas Compose, el tema y las clases que dependen del framework Android quedan fuera.
kover {
    reports {
        filters {
            excludes {
                packages(
                    "com.example.evaluacion2.ui",
                    "com.example.evaluacion2.ui.*",
                    "com.example.evaluacion2.notifications",
                    "com.example.evaluacion2.data.local"
                )
                classes(
                    "com.example.evaluacion2.MainActivity",
                    "com.example.evaluacion2.LouArayaApp",
                    "com.example.evaluacion2.di.*",
                    "com.example.evaluacion2.BuildConfig",
                    "com.example.evaluacion2.data.remote.ApiClient",
                    "*ComposableSingletons*"
                )
                annotatedBy("androidx.compose.runtime.Composable")
            }
        }
        verify {
            rule {
                minBound(80)
            }
        }
    }
}

dependencies {
    implementation("androidx.navigation:navigation-compose:2.10.2")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.4")
    implementation("androidx.camera:camera-camera2:1.6.2")
    implementation("androidx.camera:camera-lifecycle:1.6.2")
    implementation("androidx.camera:camera-view:1.6.2")
    implementation("com.google.mlkit:barcode-scanning:17.3.0")
    implementation("com.google.zxing:core:3.5.4")
    implementation(libs.osmdroid.android)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
