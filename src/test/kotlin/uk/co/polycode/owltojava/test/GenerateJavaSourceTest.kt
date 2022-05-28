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
package uk.co.polycode.owltojava.test

import mu.KotlinLogging
import org.simpleframework.xml.Serializer
import org.simpleframework.xml.core.Persister
import uk.co.polycode.owltojava.JavaSourceBuilder
import uk.co.polycode.owltojava.OwlParser
import uk.co.polycode.owltojava.RegenerateOntologyTask
import uk.co.polycode.owltojava.rdf.RdfDocument
import java.io.File
import java.math.BigDecimal
import java.math.BigInteger
import java.net.URL
import java.time.ZonedDateTime
import kotlin.test.Test
import kotlin.test.assertTrue

private val logger = KotlinLogging.logger {}

internal class GenerateJavaSourceTest {

    private val owlFilePath = ".${File.separator}src${File.separator}test${File.separator}resources${File.separator}schemaorg.owl"
    private val javaSourceDirectoryPath = ".${File.separator}build${File.separator}generated-sources"
    private val javaBasePackage = "uk.co.polycode"
    private val lang = "en"
    private val classes = listOf(
        "http://schema.org/Person",
        "http://schema.org/City",
        "http://schema.org/Place",
        "http://schema.org/CorporationX",
        "http://schema.org/Project",
        "http://schema.org/Book",
        "http://schema.org/Article",
        "http://schema.org/Fake"
    )
    private val primitivePropertyTypes = mapOf(
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
    private val ignoredPropertyTypes = listOf(
        "http://schema.org/Role"
    )
    private val prunedPropertyTypes = listOf(
        "http://schema.org/Text",
        "http://schema.org/URL"
    )
    private val ignoredSuperclasses = listOf<String>(
        "http://www.w3.org/2000/01/rdf-schema#Class"
    )

    @Test
    fun testJavaSourceFileInOutput() {

        // Expected results
        val expectedClass = "Person"
        var expectedOutputFile = javaSourceDirectoryPath
        expectedOutputFile += "${File.separator}uk${File.separator}co${File.separator}polycode${File.separator}"
        expectedOutputFile += "${File.separator}org${File.separator}schema${File.separator}${expectedClass}.java"
        // Setup
        val owlFile = File(owlFilePath)
        val serializer: Serializer = Persister()
        val javaSourceBuilder = JavaSourceBuilder(
            lang = lang,
            javaBasePackage = javaBasePackage,
            desiredClasses = classes,
            primitivePropertyTypes = primitivePropertyTypes,
            ignoredPropertyTypes = ignoredPropertyTypes,
            prunedPropertyTypes = prunedPropertyTypes,
            ignoredSuperclasses = ignoredSuperclasses
        )

        // Execution
        val workingDirectory = System.getProperty("user.dir")
        logger.debug("Working Directory = ${workingDirectory}}")
        val rdfDocument: RdfDocument = serializer.read(RdfDocument::class.java, owlFile, false)
        val ontologyClasses = OwlParser(rdfDocument)
            .withLanguage(lang)
            .withClasses(classes)
            .withIgnoredPropertyTypes(ignoredPropertyTypes)
            .withPrunedPropertyTypes(prunedPropertyTypes)
            .buildClassMap()
        val outputDir = File(javaSourceDirectoryPath)
        RegenerateOntologyTask.writeClassMapAsJavaSource(javaSourceDirectoryPath, outputDir, javaBasePackage, ontologyClasses, javaSourceBuilder)

        // Validation
        val actualFile = File(expectedOutputFile)
        assertTrue(actualFile.exists())
    }
}