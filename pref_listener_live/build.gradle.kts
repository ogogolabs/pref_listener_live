plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("maven-publish")
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.0"
//    id("com.google.devtools.ksp") version "2.1.0-1.0.29"
    id("com.ogogo_labs.pref_listener") version "0.0.1"
    id("app.cash.sqldelight") version "2.0.2"
}

android {
    namespace = "com.ogogo_labs.pref_listener_live"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

}

dependencies {

    implementation(libs.kotlinx.serialization.json)

//    implementation( libs.androidx.room.runtime)
//    implementation(libs.androidx.room.ktx)
//    ksp(libs.androidx.room.compiler)

    implementation(libs.androidx.datastore.preferences)
    implementation("app.cash.sqldelight:android-driver:2.0.2")
    implementation("app.cash.sqldelight:coroutines-extensions:2.0.2")

}

//publishing {
//    publications {
//        create<MavenPublication>("release") {
//            afterEvaluate {
//                from(components["release"])
//            }
//
//            groupId = properties["com.github.ogogo_libs"].toString()
//            artifactId = properties["pref_listener_live"].toString()
//            version = properties["0.0.1"].toString()
//        }
//    }
//}

sqldelight {
    databases {
        create("PrefWatcherDatabase") {
            packageName.set("com.ogogo_labs.pref_listener_live")
        }
    }
}
