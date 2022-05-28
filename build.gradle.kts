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

plugins {
    `kotlin-dsl`
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
    //sourceCompatibility = "18"
    //targetCompatibility = "18"
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(18))
    }
}

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
}

logger.quiet("Gradle logging an info log message which is always logged.")
logger.error("Gradle logging an error log message.")
logger.warn("Gradle logging a warning log message.")
logger.lifecycle("Gradle logging a lifecycle info log message.")
logger.info("Gradle logging an info log message.")
logger.debug("Gradle logging a debug log message.")
logger.trace("Gradle logging a trace log message.") // Gradle never logs TRACE level logs

tasks.test {
    useJUnitPlatform()
}
//tasks.named<Test>("test") {
    // Use JUnit Platform for unit tests.
//    useJUnitPlatform()
//}
