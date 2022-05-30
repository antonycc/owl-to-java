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
import kotlin.test.Test
import kotlin.test.assertEquals
import org.simpleframework.xml.Serializer
import org.simpleframework.xml.core.Persister
import org.slf4j.impl.StaticLoggerBinder
import java.io.File

import uk.co.polycode.owltojava.rdf.*
import uk.co.polycode.owltojava.*
import java.math.BigDecimal
import java.math.BigInteger
import java.net.URL
import java.time.ZonedDateTime

private val logger = KotlinLogging.logger {}

internal class OwlParserTest {

    private val srcTestResources = ".${File.separator}src${File.separator}test${File.separator}resources"
    private val minimalOwlFilePath = srcTestResources.plus("${File.separator}schemaorg-minimal-person.owl")
    private val skeletonOwlFilePath = srcTestResources.plus("${File.separator}schemaorg-skeleton.owl")
    private val lang = "en"
    private val classes = listOf(
        "https://schema.org/Person",
        "https://schema.org/City",
        "https://schema.org/Place",
        "https://schema.org/CorporationX",
        "https://schema.org/Project",
        "https://schema.org/Book",
        "https://schema.org/Article",
        "https://schema.org/Fake"
    )
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
    private val ignoredPropertyTypes = listOf(
        "https://schema.org/Role"
    )
    private val prunedPropertyTypes = listOf(
        "https://schema.org/Text",
        "https://schema.org/URL"
    )

    @Test
    fun testLogger() {
        logger.trace("Test logged using SLF4J API at level: Trace")
        logger.debug("Test logged using SLF4J API at level: Debug")
        logger.info("Test logged using SLF4J API at level: Info")
        logger.warn("The current logging implementation is ${StaticLoggerBinder.getSingleton().loggerFactory}")
        logger.warn("Test logged using SLF4J API at level: Error")
        logger.error("Test logged using SLF4J API at level: Warning")
    }

    @Test
    fun testExpectedClassInSkeletonClassMap() {

        // Expected results
        val expectedClass = "Place"
        val expectedNumberOfClasses = 1

        // Setup
        val owlFile = File(skeletonOwlFilePath)
        val serializer: Serializer = Persister()

        // Execution
        logger.debug("Working Directory = ${System.getProperty("user.dir")}}")
        val rdfDocument: RdfDocument = serializer.read(RdfDocument::class.java, owlFile, false)
        val ontologyClasses = OwlParser(
            rdfDocument = rdfDocument,
            lang = lang,
            classes = classes,
            ignoredPropertyTypes = ignoredPropertyTypes,
            prunedPropertyTypes = prunedPropertyTypes
            )
            .buildClassMap()
            .filter { it.key.id !in primitivePropertyTypes.keys }

        // Validation
        val actualNumberOfTargetClasses = ontologyClasses.filter { it.key.id.endsWith(expectedClass) }.size
        assertEquals(expectedNumberOfClasses, actualNumberOfTargetClasses)
    }

    @Test
    fun testExpectedClassInMinimalClassMap() {

        // Expected results
        val expectedClass = "Person"
        val expectedNumberOfClasses = 1

        // Setup
        val owlFile = File(minimalOwlFilePath)
        val serializer: Serializer = Persister()

        // Execution
        logger.debug("Working Directory = ${System.getProperty("user.dir")}}")
        val rdfDocument: RdfDocument = serializer.read(RdfDocument::class.java, owlFile, false)
        val ontologyClasses = OwlParser(
            rdfDocument = rdfDocument,
            lang = lang,
            classes = classes,
            ignoredPropertyTypes = ignoredPropertyTypes,
            prunedPropertyTypes = prunedPropertyTypes
        )
            .buildClassMap()
            .filter { it.key.id !in primitivePropertyTypes.keys }

        // Validation
        val actualNumberOfTargetClasses = ontologyClasses.filter { it.key.id.endsWith(expectedClass) }.size
        assertEquals(expectedNumberOfClasses, actualNumberOfTargetClasses)
    }
}
