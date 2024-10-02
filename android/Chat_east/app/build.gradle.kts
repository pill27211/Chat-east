plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services") // Google services 플러그인 추가
}

android {
    namespace = "com.example.chat_east"
    compileSdkVersion(34) // `compileSdkVersion()` 함수를 사용하여 컴파일 SDK 버전 설정
    defaultConfig {
        applicationId = "com.example.chat_east"
        minSdkVersion(24) // `minSdkVersion()` 함수를 사용하여 최소 SDK 버전 설정
        targetSdkVersion(34) // `targetSdkVersion()` 함수를 사용하여 타겟 SDK 버전 설정
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") { // `getByName()` 함수를 사용하여 빌드 타입 설정
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

}

dependencies {
    implementation(libs.appcompat) // `implementation()` 함수를 사용하여 라이브러리 추가
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.3")

    implementation("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")
    implementation ("com.github.bumptech.glide:glide:4.14.2@aar")
    implementation ("com.github.chrisbanes:PhotoView:2.2.0")

    // `implementation()` 함수 안에 `exclude()` 함수를 사용하여 `org.json` 제외
    implementation("io.socket:socket.io-client:2.0.1") {
        exclude(group = "org.json", module = "json")
    }

    implementation("com.google.android.gms:play-services-auth:20.5.0")
    implementation(platform("com.google.firebase:firebase-bom:33.1.1"))
    implementation("com.google.firebase:firebase-analytics")
    implementation ("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-messaging")

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.3")
    implementation ("org.webrtc:google-webrtc:1.0.30039@aar")

    implementation ("de.hdodenhof:circleimageview:3.1.0")
    implementation ("androidx.appcompat:appcompat:1.4.1")
    implementation("com.google.android.material:material:1.5.0")
    implementation ("androidx.constraintlayout:constraintlayout:2.1.3")
    implementation ("androidx.recyclerview:recyclerview:1.3.2")
    testImplementation ("junit:junit:4.13.2")
    androidTestImplementation ("androidx.test.ext:junit:1.1.3")
    androidTestImplementation ("androidx.test.espresso:espresso-core:3.4.0")

}