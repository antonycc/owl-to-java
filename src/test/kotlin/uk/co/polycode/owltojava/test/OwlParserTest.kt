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

    private val minimalOwlFilePath = ".${File.separator}src${File.separator}test${File.separator}resources${File.separator}schemaorg-minimal-person.owl"
    private val skeletonOwlFilePath = ".${File.separator}src${File.separator}test${File.separator}resources${File.separator}schemaorg-skeleton.owl"
    private val javaBasePackage = "com.default"
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
    private val primativePropertyTypes = mapOf(
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

    //private var latestLoggingSnapshot: Snapshot? = null
    //private val loggingLevelDuringTests = LogLevel.DEBUG

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
        val javaSourceBuilder = JavaSourceBuilder(
            lang = lang,
            javaBasePackage = javaBasePackage,
            desiredClasses = classes,
            primitivePropertyTypes = primativePropertyTypes,
            ignoredPropertyTypes = ignoredPropertyTypes,
            prunedPropertyTypes = prunedPropertyTypes,
            ignoredSuperclasses = ignoredSuperclasses
        )

        // Execution
        logger.debug("Working Directory = ${System.getProperty("user.dir")}}")
        val rdfDocument: RdfDocument = serializer.read(RdfDocument::class.java, owlFile, false)
        val ontologyClasses = OwlParser(rdfDocument)
            .withLanguage(lang)
            .withClasses(classes)
            .withIgnoredPropertyTypes(ignoredPropertyTypes)
            .withPrunedPropertyTypes(prunedPropertyTypes)
            .buildClassMap()

        // Validation
        val actualNumberOfTargetClasses = ontologyClasses
            .filter { it.key.id.endsWith(expectedClass) }
            .onEach { logger.info(javaSourceBuilder.build(it.key, it.value)) }
            .size
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
        val javaSourceBuilder = JavaSourceBuilder(
            lang = lang,
            javaBasePackage = javaBasePackage,
            desiredClasses = classes,
            primitivePropertyTypes = primativePropertyTypes,
            ignoredPropertyTypes = ignoredPropertyTypes,
            prunedPropertyTypes = prunedPropertyTypes,
            ignoredSuperclasses = ignoredSuperclasses
        )

        // Execution
        logger.debug("Working Directory = ${System.getProperty("user.dir")}}")
        val rdfDocument: RdfDocument = serializer.read(RdfDocument::class.java, owlFile, false)
        val ontologyClasses = OwlParser(rdfDocument)
            .withLanguage(lang)
            .withClasses(classes)
            .withIgnoredPropertyTypes(ignoredPropertyTypes)
            .withPrunedPropertyTypes(prunedPropertyTypes)
            .buildClassMap()

        // Validation
        val actualNumberOfTargetClasses = ontologyClasses
            .filter { it.key.id.endsWith(expectedClass) }
            .onEach { logger.info(javaSourceBuilder.build(it.key, it.value)) }
            .size
        assertEquals(expectedNumberOfClasses, actualNumberOfTargetClasses)
    }
}