package uk.co.polycode.owltojava.test

import uk.co.polycode.owltojava.*
import java.io.BufferedReader
import java.nio.file.Paths
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
internal class GradleTaskTest {

    private val srcTestResources = "./src/test/resources"
    private val minimalOwlFilePath = Paths.get("${srcTestResources}/schemaorg-minimal-person.owl")
    private val javaSourceDirectoryPath = Paths.get("./build/generated-sources-task")

    private val taskDelegate = RegenerateOntologyTaskDelegate(
        src = minimalOwlFilePath.toFile().absolutePath,
        dest = javaSourceDirectoryPath.toFile().absolutePath,
        javaBasePackage = SchemaOrgParameterSet.javaBasePackage)
        .also {
            it.lang = SchemaOrgParameterSet.lang
            it.licenceText = SchemaOrgParameterSet.licenceText
            it.classes = SchemaOrgParameterSet.classes
            it.primitivePropertyTypes = SchemaOrgParameterSet.primitivePropertyTypes
            it.ignoredPropertyTypes = SchemaOrgParameterSet.ignoredPropertyTypes
            it.prunedPropertyTypes = SchemaOrgParameterSet.prunedPropertyTypes
            it.ignoredSuperclasses = SchemaOrgParameterSet.ignoredSuperclasses
            it.regenerateJavaSource()
        }

    /**
     *  Gradle Task
     *  @since("Commit hash: c616f30060a429080fb516d1eb715af20865ce28")
     */
    @Test
    fun testJavaSourceFileInOutputWithPrimitives() {

        // Expected results
        val expectedClass = "QuantitativeValue"
        val outputFilePath = "${javaSourceDirectoryPath}/uk/co/polycode/org/schema/${expectedClass}.java"
        val outputFile = Paths.get(outputFilePath).toFile()

        // Setup

        // Execution

        // Validation
        assertNotNull(taskDelegate)
        assertNotNull(RegenerateOntologyTask::class.java)
        assertNotNull(RegenerateOntologyCli::class.java)
        assertTrue(outputFile.exists())
        val bufferedReader: BufferedReader = outputFile.bufferedReader()
        val javaSourceFile = bufferedReader.use { it.readText() }
        assertTrue { javaSourceFile.contains("public class ${expectedClass}") }
        assertTrue { javaSourceFile.contains("public String unitCode;") }
        assertTrue { javaSourceFile.contains("public PropertyValue additionalProperty;") }
        assertTrue { javaSourceFile.contains("public BigDecimal value;") }
        assertTrue { javaSourceFile.contains("public StructuredValue valueStructuredValue;") }
    }
}
