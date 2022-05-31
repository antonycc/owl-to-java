// OWL to Java generates Source Code from the W3C Web Ontology Language (OWL)
// Copyright (C) 2022  Antony Cartwright, Polycode Limited
//
// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// Mozilla Public License, v. 2.0 for more details.

// Property which, if true, runs reports too expensive or distracting from the daily build and fails on error.
// Override with: gradle build -PsafeBuildMode=false
val safeBuildMode: String by project
var detektIgnoreFailuresValue: Boolean
var koverEnableAllReports: Boolean
if ( "safeBuildMode" in project.properties && "false" == project.properties["safeBuildMode"] ) {
    logger.info("Build is in non-SAFE mode with safeBuildMode: ${project.properties["safeBuildMode"]}")
    detektIgnoreFailuresValue = false
    koverEnableAllReports = true
}else{
    logger.info("Build is in SAFE mode with safeBuildMode: ${project.properties["safeBuildMode"]}")
    detektIgnoreFailuresValue = true
    koverEnableAllReports = false
}
logger.info("Gradle logging is outputting at INFO.")
logger.debug("Gradle logging is outputting at DEBUG.")

plugins {
    `kotlin-dsl`
    id("io.gitlab.arturbosch.detekt").version("1.20.0")
    id("org.jetbrains.kotlinx.kover").version("0.5.1")
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
    //sourceCompatibility = "18"
    //targetCompatibility = "18"
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(18))
        //languageVersion.set(KotlinVersion
    }
}

// TODO: Fix 'compileTestJava' task (current target is 18) and 'compileTestKotlin' task (current target is 1.8)
//   jvm target compatibility should be set to the same Java version.
//tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
//    kotlinOptions {
//        jvmTarget = "18"
//    }
//}

repositories {
    mavenCentral()
}

// Check: gradle -q dependencies --configuration compileClasspath
dependencies {

    // All logging via SLF4J
    implementation("org.slf4j:slf4j-api:1.7.25")
    //implementation("ch.qos.logback:logback-classic:1.2.10")
    implementation("io.github.microutils:kotlin-logging-jvm:2.1.20"){
        exclude("org.jetbrains.kotlin")
        exclude("org.slf4j")
    }

    // XML Parser
    implementation("org.simpleframework:simple-xml:2.7.1")

    // String generation
    implementation("com.google.guava:guava:31.1-jre"){
        exclude("com.google.code.findbugs")
    }

    // Java source generation
    implementation("com.squareup:javapoet:1.13.0")

    // Testing
    testImplementation(kotlin("test"))
    // Use JUnit Jupiter for testing.
    //testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")

    // Static analysis
    implementation("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.20.0")
}

tasks.test {
    useJUnitPlatform()
}

kover {
    isDisabled = !koverEnableAllReports
}

// See: https://detekt.dev/docs/gettingstarted/gradle/
detekt {
    config = files("$projectDir/src/test/detekt/config.yml")
    buildUponDefaultConfig = true
    baseline = file("$projectDir/src/test/detekt/baseline.xml")
    ignoreFailures = detektIgnoreFailuresValue
}
