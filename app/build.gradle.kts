import org.gradle.api.tasks.Exec
import java.io.ByteArrayOutputStream

plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.websocketclient"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.websocketclient"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

// Define your build variant
val buildVariant = project.properties["buildVariant"]?.toString() ?: "default"

// Execute shell scripts based on the build variant
when (buildVariant) {
    "debug" -> {
        val command = "sh ../ipAddress/Ip.py"
        tasks.register("executePreBuildScript") {
            doLast {
                project.exec {
                    commandLine("sh", "-c", command)
                }
            }
        }
    }
    "release" -> {
        val command = "sh ../ipAddress/Ip.py"
        tasks.register("executePostBuildScript") {
            doLast {
                project.exec {
                    commandLine("sh", "-c", command)
                }
            }
        }
    }
    else -> {
        println("Skipping shell script execution as the build variant is not debug_test or release")
    }
}

// Task to run Python script
tasks.register("runPythonScript") {
    doLast {
        val command = "python3 ../ipAddress/Ip.py" // Path to your Python script
        project.exec {
            commandLine("sh", "-c", command)
        }
    }
}

// Ensure tasks are executed in the correct order
tasks.named("preBuild").configure {
    dependsOn("runPythonScript")

    // Only add dependencies if the build variant is relevant
    if (buildVariant == "debug_test") {
        dependsOn("executePreBuildScript")
    } else if (buildVariant == "release") {
        dependsOn("executePostBuildScript")
    }
}


dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("tech.gusavila92:java-android-websocket-client:1.2.0")
}
