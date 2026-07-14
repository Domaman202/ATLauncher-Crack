plugins {
    id("java")
    id("com.gradleup.shadow") version("9.3.0+")
}

group = "ru.DmN.cmhack"
version = "2.4.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.ow2.asm:asm:9.10.1")
    implementation("com.google.code.gson:gson:2.14.0")
    compileOnly(files("ATLauncher.old.jar"))
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
