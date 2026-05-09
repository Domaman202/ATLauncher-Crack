plugins {
    id("java")
}

group = "ru.DmN.cmhack"
version = "2.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(files("ATLauncher.jar"))
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.jar {
    archiveBaseName = "ATLCrack"
    manifest {
        attributes["Main-Class"] = "ru.DmN.atlcrack2.Bootstrap"
    }
}