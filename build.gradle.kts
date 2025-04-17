plugins {
    id("com.android.application") version "8.0.2" apply false
    id("org.jetbrains.kotlin.android") version "1.8.10" apply false
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }

    dependencies{
        classpath ("com.android.tools.build:gradle:8.2.2")
    }
    // (Dependencies can be added here if needed; with version catalogs this is often not necessary.)
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}
