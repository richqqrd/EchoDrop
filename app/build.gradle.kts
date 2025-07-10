plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlinAndroidKsp)
    alias(libs.plugins.hiltAndroid)
    alias(libs.plugins.kotlin.compose)
    id("jacoco")


}

android {
    namespace = "com.example.echodrop"
    compileSdk = 35

    packaging {
        resources {
            excludes += listOf(
                "META-INF/LICENSE.md",
                "META-INF/LICENSE-notice.md",
                "META-INF/LICENSE.txt",
                "META-INF/NOTICE.txt"
            )
        }
    }
    defaultConfig {
        applicationId = "com.example.echodrop"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.room.common.jvm)
    implementation(libs.firebase.crashlytics.buildtools)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    debugImplementation("com.google.dagger:hilt-android-testing:2.51.1")
    kspDebug("com.google.dagger:hilt-compiler:2.51.1")


    implementation("com.google.code.gson:gson:2.10.1")

   testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.1")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.10.1")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.10.1")
    
    // JUnit Platform für Android Studio
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.1")


    ksp("androidx.room:room-compiler:2.7.1")

    // Room component
    implementation("androidx.room:room-runtime:2.7.1")
    // Coroutines support
    implementation("androidx.room:room-ktx:2.7.1")
    testImplementation(kotlin("test"))

    // Mockito for JUnit 5
    testImplementation("org.mockito:mockito-core:5.8.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.8.0")

    // Mockito-Kotlin
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
    testImplementation("io.mockk:mockk:1.13.8")
    androidTestImplementation("io.mockk:mockk-android:1.13.8")
    androidTestImplementation("io.mockk:mockk-agent:1.13.8")
    androidTestImplementation("org.mockito:mockito-android:5.8.0")
    androidTestImplementation("org.mockito:mockito-core:5.8.0")
    androidTestImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
    androidTestImplementation("io.mockk:mockk-android:1.13.8")    // Coroutines test
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")

    implementation("androidx.compose.material:material-icons-extended:1.6.0")

    androidTestImplementation(platform(libs.androidx.compose.bom))
androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.6.0")
androidTestImplementation("androidx.test.espresso:espresso-intents:3.5.1")
debugImplementation("androidx.compose.ui:ui-test-manifest:1.6.0")

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
    androidTestImplementation("com.google.dagger:hilt-android-testing:2.51.1")
    kspAndroidTest("com.google.dagger:hilt-compiler:2.51.1")
    androidTestImplementation(libs.hilt.android)
    kspAndroidTest(libs.hilt.compiler)
    androidTestImplementation("androidx.test:rules:1.5.0")

}

// Jacoco version configuration
jacoco {
    toolVersion = "0.8.11"
}

// --- Jacoco report task ----------------------------------------------------

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest") // ensure unit tests run first

    val execData = file("$buildDir/jacoco/testDebugUnitTest.exec")
    executionData.setFrom(execData)

    val kotlinDebugTree = fileTree("$buildDir/tmp/kotlin-classes/debug") {
        exclude(
            "**/di/**",
            "**/ui/**",
            "**/BuildConfig.*",
            "**/R.class",
            "**/R$*.class"
        )
    }

    classDirectories.setFrom(kotlinDebugTree)
    sourceDirectories.setFrom(files("src/main/java"))

    reports {
        html.required.set(true)
        xml.required.set(false)
        csv.required.set(false)
    }
}

// Configure all unit test tasks to use JUnit 5 and generate coverage afterwards
tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    finalizedBy("jacocoTestReport")
}