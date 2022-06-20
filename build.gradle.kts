import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URI

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

buildscript {
    repositories {
        mavenCentral() // or gradlePluginPortal()
    }
    dependencies {
        classpath("com.dipien:semantic-version-gradle-plugin:1.0.0")
    }
}
apply(plugin = "com.dipien.semantic-version")

plugins {
    `kotlin-dsl`
    application
    `maven-publish`
    id("io.gitlab.arturbosch.detekt") version "1.20.0"
    id("org.jetbrains.kotlinx.kover") version "0.5.1"
    id("org.unbroken-dome.test-sets") version "4.0.0"
    //kotlin("jvm").version("1.6.21")
    id("org.jetbrains.kotlin.jvm") version "1.7.0-RC2"  // "1.5.31" // "1.6.21"
    id("com.github.jk1.dependency-license-report") version "2.0"
}

// From: https://stackoverflow.com/questions/56921833/kotlin-program-error-no-main-manifest-attribute-in-jar-file/61373175#61373175
//application {
//    mainClassName = "uk.co.polycode.owltojava.RegenerateOntologyCli"
//}

afterEvaluate {
    tasks.withType<KotlinCompile>().configureEach {
        kotlinOptions {
            apiVersion = "1.6"
            languageVersion = "1.6"
            jvmTarget = "1.8"
        }
    }
}


// ./gradlew printVersion
// ./gradlew incrementVersion --versionIncrementType=MAJOR
// ./gradlew incrementVersion --versionIncrementType=MINOR
// ./gradlew incrementVersion --versionIncrementType=PATCH
// ./gradlew printVersion -Psnapshot=false
group = "co.uk.polycode"
version = "0.0.8-SNAPSHOT"

// See: https://docs.gradle.org/current/userguide/publishing_maven.html
// ./gradlew build publishToMavenLocal
publishing {
    publications {
        //create<MavenPublication>("maven") {
        create<MavenPublication>("pluginMaven") {
            //groupId = "co.uk.polycode"
            //artifactId = "owltojava"
            //version = "0.0.1-SNAPSHOT"

            //from(components["java"])
            pom {
                name.set("OWL to Java")
                description.set("Generates a Java class model from an the W3C Web Ontology Language (OWL).")
                url.set("https://github.com/antonycc/owl-to-java")
                properties.set(mapOf(
                    "myProp" to "value",
                    "prop.with.dots" to "anotherValue"
                ))
                licenses {
                    license {
                        name.set("Mozilla Public License, v. 2.0")
                        url.set("https://mozilla.org/MPL/2.0/")
                    }
                }
                developers {
                    developer {
                        id.set("antonycc")
                        name.set("Antony Cartwright")
                        email.set("antonyccartwright@gmail.com")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/antonycc/owl-to-java.git")
                    developerConnection.set("scm:git:ssh://github.com/antonycc/owl-to-java.git")
                    url.set("https://github.com/antonycc/owl-to-java")
                }
            }
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = URI("https://maven.pkg.github.com/octocat/hello-world")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

tasks.withType<Jar> {
    // Otherwise you'll get a "No main manifest attribute" error
    //manifest {
    //    attributes["Main-Class"] = "uk.co.polycode.owltojava.RegenerateOntologyCli"
    //}

    // To add all the dependencies otherwise a "NoClassDefFoundError" error
    //from(sourceSets.main.get().output)

    //dependsOn(configurations.runtimeClasspath)
    //from({
    //    configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    //})
}

//jar {
//    manifest {
//        attributes 'Main-Class': 'com.foo.bar.MainClass'
//    }
//}

repositories {
    mavenCentral()
}

// Check: gradle -q dependencies --configuration compileClasspath
dependencies {

    // All logging via SLF4J
    implementation("org.slf4j:slf4j-api:1.7.36")
    implementation("io.github.microutils:kotlin-logging-jvm:2.1.21"){
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

    // Run as Jar in Java8+
    //compile(kotlin("stdlib-jdk8"))
    implementation(kotlin("stdlib-jdk8"))

    // Testing
    testImplementation(kotlin("test"))

    // Static analysis
    implementation("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.20.0")
}

tasks.test {
    useJUnitPlatform()
    //filter {
        //excludeTestsMatching("*ExpensiveTest")
    //}
}

kotlin {
    sourceSets {
        //val integrationTest by creating {
        create("integrationTest") {
            kotlin.srcDir("src/integrationTest/kotlin")
            resources.srcDir("src/integrationTest/resources")
        }
    }
}

val integrationTestCompile: Configuration by configurations.creating {
    extendsFrom(configurations["testImplementation"])
}
val integrationTestRuntime: Configuration by configurations.creating {
    extendsFrom(configurations["testImplementation"])
}

testSets {
//    //"integrationTest"()
    libraries {
        create("integrationTest")
    }
}

task<Test>("integrationTest"){
    description = "Runs the integration tests"
    group = "verification"
    testClassesDirs = sourceSets["integrationTest"].output.classesDirs
    classpath = sourceSets["integrationTest"].runtimeClasspath
    //mustRunAfter(tasks["check"])
    useJUnitPlatform()
}

//tasks {
//    check {
//        dependsOn("integrationTest")  // (1)
//    }
//}

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
