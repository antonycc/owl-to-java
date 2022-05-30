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
    //id("io.gitlab.arturbosch.detekt") version "8.0.2"
    id("io.gitlab.arturbosch.detekt").version("1.20.0")
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

//tasks.withType<Detekt>().configureEach {// .detekt {
//    buildUponDefaultConfig = true // preconfigure defaults
//    allRules = false // activate all available (even unstable) rules.
//    config = files("$projectDir/config/detekt.yml") // point to your custom config defining rules to run, overwriting default behavior
//    baseline = file("$projectDir/config/baseline.xml") // a way of suppressing issues before introducing detekt
//}
// hasProperty('detektIgnoreFailures') ? project.getProperty('detektIgnoreFailures') : 'hello'
//ext.detektIgnoreFailures

// Detekt excluded and non-failing by default until it is regularly passing
// Override with: gradle clean detekt -PdetektSafeMode=false
// TODO: Bring code and checks into alignment then include relatively quick tests in the check task
val detektSafeMode: String by project
var detektIgnoreFailuresValue: Boolean? = true
if ( "detektSafeMode" in project.properties ) {
    detektIgnoreFailuresValue = (true == project.properties["detektSafeMode"])
}
//tasks.named("check").configure {
//    this.setDependsOn(this.dependsOn.filterNot {
//        it is TaskProvider<*> && it.name == "detekt"
//    })
//}

// See: https://detekt.dev/docs/gettingstarted/gradle/
detekt {
    // Version of Detekt that will be used. When unspecified the latest detekt
    // version found will be used. Override to stay on the same version.
    //toolVersion = "[detekt_version]"

    // The directories where detekt looks for source files.
    // Defaults to `files("src/main/java", "src/test/java", "src/main/kotlin", "src/test/kotlin")`.
    //source = files("src/main/java", "src/main/kotlin")

    // Builds the AST in parallel. Rules are always executed in parallel.
    // Can lead to speedups in larger projects. `false` by default.
    //parallel = false

    // Define the detekt configuration(s) you want to use.
    // Defaults to the default detekt configuration.
    config = files("$projectDir/src/test/detekt/config.yml")

    // Applies the config files on top of detekt's default config file. `false` by default.
    buildUponDefaultConfig = true

    // Turns on all the rules. `false` by default.
    //allRules = false

    // Specifying a baseline file. All findings stored in this file in subsequent runs of detekt.
    baseline = file("$projectDir/src/test/detekt/baseline.xml")

    // Disables all default detekt rulesets and will only run detekt with custom rules
    // defined in plugins passed in with `detektPlugins` configuration. `false` by default.
    //disableDefaultRuleSets = false

    // Adds debug output during task execution. `false` by default.
    //debug = false

    // If set to `true` the build does not fail when the
    // maxIssues count was reached. Defaults to `false`.
    //ignoreFailures = false
    ignoreFailures = detektIgnoreFailuresValue ?: false

    // Android: Don't create tasks for the specified build types (e.g. "release")
    //ignoredBuildTypes = listOf("release")

    // Android: Don't create tasks for the specified build flavor (e.g. "production")
    //ignoredFlavors = listOf("production")

    // Android: Don't create tasks for the specified build variants (e.g. "productionRelease")
    //ignoredVariants = listOf("productionRelease")

    // Specify the base path for file paths in the formatted reports.
    // If not set, all file paths reported will be absolute file path.
    //basePath = projectDir
}