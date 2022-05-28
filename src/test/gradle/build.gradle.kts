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

import java.time.ZonedDateTime
import java.net.URL

plugins {
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


dependencies {
    // implementation("OwlToJava Task definition")
}

tasks {

    // Regenerate Java classes for schema.org using the OWL schema
    val regenerate by registering(RegenerateOntologyTask::class) {
        outputs.upToDateWhen { false }
        val srcMain = "".plus(projectDir).plus(File.separator).plus("src").plus(File.separator).plus("main")
        //val sourceFileName = "schemaorg-minimal-person.owl"
        val sourceFileName = "schemaorg.owl"
        lang = "en"
        src = srcMain.plus(File.separator).plus("resources").plus(File.separator).plus(sourceFileName)
        //dest = "INFO"
        dest = "".plus(buildDir).plus(File.separator).plus("generated-src")
        javaBasePackage = "com.default"
        classes = listOf(
            "http://schema.org/Person",
            "http://schema.org/City",
            "http://schema.org/CorporationX",
            "http://schema.org/Project",
            "http://schema.org/Book",
            "http://schema.org/Article",
            "http://schema.org/Fake"
        )
        primativePropertyTypes = mapOf(
            "http://schema.org/DataType" to Object::class.java.name,
            "http://schema.org/Text"     to String::class.java.name,
            "http://schema.org/Time"     to ZonedDateTime::class.java.name,
            "http://schema.org/DateTime" to ZonedDateTime::class.java.name,
            "http://schema.org/Date"     to ZonedDateTime::class.java.name,
            "http://schema.org/URL"      to URL::class.java.name,
            "http://schema.org/Integer"  to BigInteger::class.java.name,
            "http://schema.org/Float"    to BigDecimal::class.java.name,
            "http://schema.org/Number"   to BigDecimal::class.java.name,
            "http://schema.org/Boolean"  to "java.lang.Boolean", // Boolean::class.java.name, unboxes to boolean.
        )
        ignoredPropertyTypes = listOf(
            "http://schema.org/Role"
        )
        prunedPropertyTypes = listOf(
            "http://schema.org/Text",
            "http://schema.org/URL"
        )
        ignoredSuperclasses = listOf<String>(
            "http://www.w3.org/2000/01/rdf-schema#Class"
        )
    }
}

