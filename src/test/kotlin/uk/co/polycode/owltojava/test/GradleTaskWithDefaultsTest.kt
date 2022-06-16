package uk.co.polycode.owltojava.test

import uk.co.polycode.owltojava.*
import java.io.BufferedReader
import java.math.BigDecimal
import java.math.BigInteger
import java.net.URL
import java.nio.file.Paths
import java.time.ZonedDateTime
import kotlin.test.*

/**
 * OWL to Java generates Source Code from the W3C Web Ontology Language (OWL)
 * Copyright (C) 2022  Antony Cartwright, Polycode Limited
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Mozilla Public License, v. 2.0 for more details.
 */
internal class GradleTaskWithDefaultsTest {

    private val srcTestResources = "./src/test/resources"
    private val minimalOwlFilePath = Paths.get("${srcTestResources}/schemaorg-minimal-person.owl")
    private val javaSourceDirectoryPath = Paths.get("./build/generated-sources-task-with-defaults")
    private val javaBasePackage = "uk.co.polycode"
    private val primitivePropertyTypes = mapOf(
        "https://schema.org/DataType" to Object::class.java.name,
        "https://schema.org/Text"     to String::class.java.name,
        "https://schema.org/Time"     to ZonedDateTime::class.java.name,
        "https://schema.org/DateTime" to ZonedDateTime::class.java.name,
        "https://schema.org/Date"     to ZonedDateTime::class.java.name,
        "https://schema.org/URL"      to URL::class.java.name,
        "https://schema.org/Integer"  to BigInteger::class.java.name,
        "https://schema.org/Float"    to BigDecimal::class.java.name,
        "https://schema.org/Number"   to BigDecimal::class.java.name,
        "https://schema.org/Boolean"  to "java.lang.Boolean", // Boolean::class.java.name, unboxes to boolean.
    )
    private val taskDelegateWithDefaults = RegenerateOntologyTaskDelegate(
        src = minimalOwlFilePath.toFile().absolutePath,
        dest = javaSourceDirectoryPath.toFile().absolutePath,
        javaBasePackage = javaBasePackage).also {
        it.primitivePropertyTypes = this.primitivePropertyTypes
        it.regenerateJavaSource()
    }

    /**
     * Gradle Task with default arguments
     *
     * @since("Commit hash: 2ce90b1eff2e7746ebf96f0dbfd82e668510b505")
     */
    @Test
    fun testJavaSourceFileInOutputWithDefaults() {

        // Expected results
        val expectedClass = "QuantitativeValue"
        val outputFilePath = "${javaSourceDirectoryPath}/uk/co/polycode/org/schema/${expectedClass}.java"
        val outputFile = Paths.get(outputFilePath).toFile()

        // Setup

        // Execution

        // Validation
        assertNotNull(taskDelegateWithDefaults)
        assertTrue(outputFile.exists())
        val bufferedReader: BufferedReader = outputFile.bufferedReader()
        val javaSourceFile = bufferedReader.use { it.readText() }
        assertTrue { javaSourceFile.contains("public class ${expectedClass}") }
        assertTrue { javaSourceFile.contains("public String unitCodeString;") }
        assertTrue { javaSourceFile.contains("public PropertyValue additionalProperty;") }
        assertTrue { javaSourceFile.contains("public String value;") }
        assertTrue { javaSourceFile.contains("public StructuredValue valueStructuredValue;") }
    }
}
