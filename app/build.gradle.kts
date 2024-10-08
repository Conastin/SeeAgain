plugins {
    autowire(libs.plugins.android.application)
    autowire(libs.plugins.kotlin.android)
    autowire(libs.plugins.com.google.devtools.ksp)
}

android {
    signingConfigs {
        getByName("debug") {
            storeFile = file("C:\\Users\\Conastin\\Desktop\\SeeAgainPackagedKey.jks")
            storePassword = "123456"
            keyAlias = "SeeAgainKeyStore"
            keyPassword = "123456"
        }
    }
    namespace = property.project.app.packageName
    compileSdk = property.project.android.compileSdk

    defaultConfig {
        applicationId = property.project.app.packageName
        minSdk = property.project.android.minSdk
        targetSdk = property.project.android.targetSdk
        versionName = property.project.app.versionName
        versionCode = property.project.app.versionCode
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        multiDexEnabled = true
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        getByName("debug") {
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs = listOf(
            "-Xno-param-assertions",
            "-Xno-call-assertions",
            "-Xno-receiver-assertions"
        )
    }
    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
}

dependencies {
    implementation(com.highcapable.betterandroid.ui.component)
    implementation(com.highcapable.betterandroid.ui.extension)
    implementation(com.highcapable.betterandroid.system.extension)
    implementation(androidx.core.core.ktx)
    implementation(androidx.appcompat.appcompat)
    implementation(com.google.android.material.material)
    implementation(androidx.constraintlayout.constraintlayout)
    implementation(androidx.room.room.runtime)
    implementation(androidx.room.room.ktx)
    implementation(androidx.swiperefreshlayout.swiperefreshlayout)
    // 高德地图-地图SDK
    implementation(files("lib/AMap3DMap_9.8.3_AMapLocation_6.4.2_20231215.jar"))
    annotationProcessor(androidx.room.room.compiler)
    // 权限请求框架
    implementation(com.github.getActivity.xxPermissions)
    compileOnly(com.amap.api.edmap)
    testImplementation(junit.junit)
    androidTestImplementation(androidx.test.ext.junit)
    androidTestImplementation(androidx.test.espresso.espresso.core)
    ksp(androidx.room.room.compiler)
}