plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.kapt")
}

android {
    namespace = "com.protosync.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.protosync.app"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
        buildConfigField("boolean", "USE_DOH", "false")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

kapt {
    correctErrorTypes = true
}

dependencies {
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.4")

    // --- AndroidX ---
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.fragment:fragment-ktx:1.8.5")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.activity:activity-ktx:1.9.3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-savedstate:2.8.7")
    implementation("androidx.lifecycle:lifecycle-process:2.8.7")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.startup:startup-runtime:1.2.0")
    implementation("androidx.work:work-runtime-ktx:2.8.1")
    implementation("androidx.hilt:hilt-work:1.2.0")
    implementation("androidx.room:room-runtime:2.7.2")
    implementation("androidx.room:room-ktx:2.7.2")
    implementation("androidx.collection:collection-ktx:1.4.5")

    // --- Compose ---
    val composeBom = platform("androidx.compose:compose-bom:2025.01.00")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // --- Dagger / Hilt ---
    implementation("com.google.dagger:hilt-android:2.58")
    implementation("javax.inject:javax.inject:1")
    kapt("com.google.dagger:hilt-android-compiler:2.58")
    kapt("androidx.hilt:hilt-compiler:1.2.0")

    // --- Room compiler ---
    kapt("androidx.room:room-compiler:2.7.2")

    // --- Kotlinx ---
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // --- Networking ---
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")

    // --- vCard ---
    implementation("com.googlecode.ez-vcard:ez-vcard:0.11.3")

    // --- ProtonCore 36.6.2 ---
    implementation("me.proton.core:account-manager:36.6.2")
    implementation("me.proton.core:account:36.6.2")
    implementation("me.proton.core:account-recovery:36.6.2")
    implementation("me.proton.core:auth:36.6.2")
    implementation("me.proton.core:biometric:36.6.2")
    implementation("me.proton.core:challenge:36.6.2")
    implementation("me.proton.core:contact:36.6.2")
    implementation("me.proton.core:country:36.6.2")
    implementation("me.proton.core:crypto:36.6.2")
    implementation("me.proton.core:crypto-android:36.6.2")
    implementation("me.proton.core:crypto-common:36.6.2")
    implementation("me.proton.core:crypto-validator:36.6.2")
    implementation("me.proton.core:data:36.6.2")
    implementation("me.proton.core:data-room:36.6.2")
    implementation("me.proton.core:device-migration:36.6.2")
    implementation("me.proton.core:domain:36.6.2")
    implementation("me.proton.core:event-manager:36.6.2")
    implementation("me.proton.core:feature-flag:36.6.2")
    implementation("me.proton.core:human-verification:36.6.2")
    implementation("me.proton.core:key:36.6.2")
    implementation("me.proton.core:label:36.6.2")
    implementation("me.proton.core:mail-settings:36.6.2")
    implementation("me.proton.core:network:36.6.2")
    implementation("me.proton.core:notification:36.6.2")
    implementation("me.proton.core:observability:36.6.2")
    implementation("me.proton.core:payment:36.6.2")
    implementation("me.proton.core:payment-iap:36.6.2")
    implementation("me.proton.core:pass-validator:36.6.2")
    implementation("me.proton.core:plan:36.6.2")
    implementation("me.proton.core:presentation:36.6.2")
    implementation("me.proton.core:presentation-compose:36.6.2")
    implementation("me.proton.core:proguard-rules:36.6.2")
    implementation("me.proton.core:push:36.6.2")
    implementation("me.proton.core:telemetry:36.6.2")
    implementation("me.proton.core:user:36.6.2")
    implementation("me.proton.core:user-settings:36.6.2")
    implementation("me.proton.core:util-android-dagger:36.6.2")
    implementation("me.proton.core:util-kotlin:36.6.2")
    implementation("me.proton.core:configuration-data:36.6.2")
}