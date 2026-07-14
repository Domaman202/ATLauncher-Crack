plugins {
    id("java")
    id("com.gradleup.shadow") version("9.3.0+")
}

group = "ru.DmN.cmhack"
version = "2.5.0"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(files("ATLauncher.jar"))
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
