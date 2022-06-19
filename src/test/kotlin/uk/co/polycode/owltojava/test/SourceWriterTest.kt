package uk.co.polycode.owltojava.test

import org.simpleframework.xml.core.Persister
import uk.co.polycode.owltojava.*
import uk.co.polycode.owltojava.rdf.RdfDocument
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
internal class SourceWriterTest {

    private val srcTestResources = "./src/test/resources"
    private val minimalOwlFilePath = Paths.get("${srcTestResources}/schemaorg-minimal-person.owl")
    private val rdfDocument: RdfDocument = with(minimalOwlFilePath.toFile()){
        Persister().read(RdfDocument::class.java, this, false)
    }
    private val javaSourceDirectoryPath = Paths.get("./build/generated-sources-writer")
    private val owlParser = OwlParser(rdfDocument = rdfDocument)
    private val javaSourceBuilder = JavaSourceBuilder(javaBasePackage = SchemaOrgParameterSet.javaBasePackage).also {
        it.primitivePropertyTypes = SchemaOrgParameterSet.primitivePropertyTypes
    }

    @Test
    fun testJavaSourceFileInOutput() {

        // Expected results
        val expectedClassName = "QuantitativeValue"
        val expectedSuperclassName = "StructuredValue"
        val expectedOutputFilePath = "${javaSourceDirectoryPath}/uk/co/polycode/org/schema/${expectedClassName}.java"
        val expectedOutputFile = Paths.get(expectedOutputFilePath).toFile()

        // Setup
        val ontologyClasses = owlParser.also {
            it.classes = SchemaOrgParameterSet.classes
            it.ignoredPropertyTypes = SchemaOrgParameterSet.ignoredPropertyTypes
            it.prunedPropertyTypes = SchemaOrgParameterSet.prunedPropertyTypes
        }
            .buildMapOfClassesToFieldLists()
            .filter { it.key.id !in SchemaOrgParameterSet.primitivePropertyTypes.keys }
        val javaSourceBuilder = JavaSourceBuilder(javaBasePackage = SchemaOrgParameterSet.javaBasePackage).also {
            it.lang = SchemaOrgParameterSet.lang
            it.licenceText = SchemaOrgParameterSet.licenceText
            it.desiredClasses = SchemaOrgParameterSet.classes
            it.primitivePropertyTypes = SchemaOrgParameterSet.primitivePropertyTypes
            it.prunedPropertyTypes = SchemaOrgParameterSet.prunedPropertyTypes
            it.ignoredSuperclasses = SchemaOrgParameterSet.ignoredSuperclasses
        }

        // Execution
        JavaSourceWriter().writeClassMapAsJavaSource(
            javaSourceDirectoryPath.toFile().absolutePath,
            javaSourceDirectoryPath.toFile(),
            ontologyClasses,
            javaSourceBuilder
        )

        // Class validation
        assertTrue(expectedOutputFile.exists())
        val bufferedReader: BufferedReader = expectedOutputFile.bufferedReader()
        val javaSourceFile = bufferedReader.use { it.readText() }
        assertTrue { javaSourceFile.contains("public class ${expectedClassName}") }
        assertTrue {
            javaSourceFile.contains(
                "public class ${expectedClassName} extends ${expectedSuperclassName} {"
            )
        }
        assertTrue {
            javaSourceFile.contains(
                "public String isDefinedBy = \"https://schema.org/${expectedClassName}\";"
            )
        }
        assertTrue { javaSourceFile.contains("public String unitCode;") }
        assertTrue { javaSourceFile.contains("public PropertyValue additionalProperty;") }
        assertTrue { javaSourceFile.contains("public BigDecimal value;") }
        assertTrue { javaSourceFile.contains("public StructuredValue valueStructuredValue;") }
    }

    @Test
    fun testJavaSourceFileWithDefaults() {

        // Expected results
        val expectedClassName = "QuantitativeValue"
        val expectedSuperclassName = "StructuredValue"
        val expectedOutputFilePath = "${javaSourceDirectoryPath}/uk/co/polycode/org/schema/${expectedClassName}.java"
        val expectedOutputFile = Paths.get(expectedOutputFilePath).toFile()

        // Setup
        val ontologyClasses = owlParser
            .buildMapOfClassesToFieldLists()
            .filter { it.key.id !in SchemaOrgParameterSet.primitivePropertyTypes.keys }

        // Execution
        JavaSourceWriter().writeClassMapAsJavaSource(
            javaSourceDirectoryPath.toFile().absolutePath,
            javaSourceDirectoryPath.toFile(),
            ontologyClasses,
            javaSourceBuilder
        )

        // Class validation
        assertTrue(expectedOutputFile.exists())
        val bufferedReader: BufferedReader = expectedOutputFile.bufferedReader()
        val javaSourceFile = bufferedReader.use { it.readText() }
        assertTrue { javaSourceFile.contains("public class ${expectedClassName}") }
        assertTrue {
            javaSourceFile.contains(
                "public class ${expectedClassName} extends ${expectedSuperclassName} {"
            )
        }
        assertTrue {
            javaSourceFile.contains(
                "public String isDefinedBy = \"https://schema.org/${expectedClassName}\";"
            )
        }
        assertTrue { javaSourceFile.contains("public String unitCodeString;") }
        assertTrue { javaSourceFile.contains("public PropertyValue additionalProperty;") }
        assertTrue { javaSourceFile.contains("public String value;") }
        assertTrue { javaSourceFile.contains("public StructuredValue valueStructuredValue;") }
    }
}
