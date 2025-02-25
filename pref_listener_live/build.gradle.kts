plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("maven-publish")
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.0"
    id("com.google.devtools.ksp") version "2.1.0-1.0.29"
    id("com.ogogo_labs.pref_listener") version "0.0.1"
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

    android {
        testOptions {
            unitTests.all {
                it.systemProperty("BUILD_TYPE_IMPLEMENTATION", "debug")
            }
        }
    }


}

val isReleaseBuild = gradle.startParameter.taskNames.any { it.contains("Release") }

dependencies {

    implementation(libs.kotlinx.serialization.json)

    implementation(libs.androidx.datastore.preferences)
    implementation(project(":pref_listener_core"))
    implementation(project(":pref_listener_prod"))
    implementation(project(":pref_listener_debug"))

}

//tasks.register("updateProperty") {
//    doLast {
//
//
//        //-----------------
//        val manifest = file("src/main/AndroidManifest.xml")
//        val outputManifest = when (isReleaseBuild) {
//            true -> manifestReleaseVersion
//            else -> manifestDebugVersion
//        }
//
//        manifest.bufferedWriter().use { it.write(outputManifest) }
//
//        //------------------
//
//        val isReleaseBuild = gradle.startParameter.taskNames.any { it.contains("Release") }
//        val file = file("src/main/java/com/ogogo_labs/pref_listener_live/PrefListener.kt")
//
//        if (file.exists()) {
//            var content = file.readText()
//
//            // Заміна імпорту залежно від типу збірки
//            if (isReleaseBuild) {
//                content = content.replace("debug.WorkerWrapper(ctx)", "prod.WorkerWrapper(ctx)")
//            } else {
//                content = content.replace("prod.WorkerWrapper(ctx)", "debug.WorkerWrapper(ctx)")
//            }
//
//            file.writeText(content)
//            println("WorkerWrapper import modified successfully.")
//        } else {
//            println("File not found: ${file.absolutePath}")
//        }
//
//        //-------------------
//
//        println("✅ gradle.properties оновлено!")
//    }
//}

//val modifyWorkerImport = tasks.register("modifyWorkerImport") {
//    doLast {
//
//    }
//}

//tasks.named("preBuild").configure {
//    onlyIf { true }
//    dependsOn("updateProperty")
//}

//tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class.java){
//    dependsOn(modifyWorkerImport)
//}

publishing {
    publications {
        create<MavenPublication>("release") {
            afterEvaluate {
                from(components["release"])
            }

            groupId = properties["com.github.ogogo_libs"].toString()
            artifactId = properties["pref_listener_live"].toString()
            version = properties["0.0.1"].toString()
        }
    }
}
