import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.kotlin.dsl.support.zipTo

plugins {
    id("java")
    id("io.github.goooler.shadow") version "8.1.7"
}

allprojects {
    group = "dev.wuason"
    version = "1.0.4.2"
}

subprojects {
    apply(plugin = "java")

    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://oss.sonatype.org/content/groups/public/")
        maven("https://jitpack.io")
        maven("https://repo.codemc.org/repository/maven-public/")
        maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
        maven("https://repo.extendedclip.com/releases/")
        maven("https://mvn.lumine.io/repository/maven-public/")
        maven("https://invesdwin.de/repo/invesdwin-oss/")
        maven("https://repo.oraxen.com/releases")
        maven("https://repo.oraxen.com/snapshots")
        maven("https://repo.nexomc.com/snapshots/")
        maven("https://repo.nexomc.com/releases/")
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    tasks.withType<ProcessResources> {
        val vars = mapOf("version" to rootProject.version, "name" to rootProject.name)
        inputs.properties(vars)
        filesMatching("**/plugin.yml") {
            expand(vars)
        }
    }
}

project(":oraxen-j21") {
    dependencies {
        compileOnly("io.papermc.paper:paper-api:1.20.6-R0.1-SNAPSHOT")
        compileOnly("io.th0rgal:oraxen:2.0-SNAPSHOT")
        compileOnly(project(":plugin"))
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
}

project(":nexo-j21") {
    dependencies {
        compileOnly("io.papermc.paper:paper-api:1.20.6-R0.1-SNAPSHOT")
        compileOnly("com.nexomc:nexo:0.4.0:dev")
        compileOnly(project(":plugin"))
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
}

project(":plugin") {

    dependencies {
        compileOnly("com.github.LoneDev6:API-ItemsAdder:3.6.2-beta-r3-b")
        compileOnly("me.clip:placeholderapi:2.11.6")
        compileOnly("io.lumine:Mythic-Dist:5.6.0-20240124.234541-47")
        compileOnly("io.lumine:MythicCrucible-Dist:2.0.0-20240122.174338-17")
        compileOnly("io.th0rgal:oraxen:1.172.0")
        compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")
        compileOnly("com.github.Wuason6x9:mechanics:1.0.3.1")
        //compileOnly(fileTree("libs").include("*.jar"))
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        //options.release.set(17)
    }

}

dependencies {
    implementation(project(":oraxen-j21"))
    implementation(project(":nexo-j21"))
    implementation(project(":plugin"))
}

tasks {
    shadowJar {
        destinationDirectory.set(file("$rootDir/target"))
        archiveFileName = "${rootProject.name}-${rootProject.version}.jar"
    }

    build {
        dependsOn(":shadowJar")
    }
}

tasks.register<Zip>("zipPlugin") {
    group = "build"
    dependsOn("build")

    archiveFileName.set("${rootProject.name}-${rootProject.version}.zip")
    destinationDirectory.set(file("$rootDir/target"))

    from("$rootDir/target/") {
        include("${rootProject.name}-${rootProject.version}.jar")
    }
    from("$rootDir/pack/") {
        include("StorageMechanic\\**")
    }

    doLast {
        println("Zip created at: ${archiveFile.get().asFile.absolutePath}")
    }

}