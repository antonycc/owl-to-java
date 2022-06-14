package uk.co.polycode.owltojava.test

import kotlin.test.Test
import kotlin.test.assertEquals
import org.simpleframework.xml.core.Persister
import uk.co.polycode.owltojava.rdf.*
import uk.co.polycode.owltojava.*
import java.nio.file.Paths

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
internal class OwlParserTest {

    private val srcTestResources = "./src/test/resources"
    private val skeletonOwlFilePath = Paths.get("${srcTestResources}/schemaorg-skeleton.owl")
    private val rdfDocument: RdfDocument = with(skeletonOwlFilePath.toFile()){
        Persister().read(RdfDocument::class.java, this, false)
    }
    private val lang = "en"
    private val classes = listOf(
        "https://schema.org/Person",
        "https://schema.org/City",
        "https://schema.org/Place",
        "https://schema.org/CorporationX",
        "https://schema.org/Project",
        "https://schema.org/Book",
        "https://schema.org/Article",
        "https://example.com/NoLang",
        "https://schema.org/Fake"
    )
    private val primitivePropertyTypes = listOf(
        "https://schema.org/DataType",
        "https://schema.org/Text",
        "https://schema.org/Time",
        "https://schema.org/DateTime",
        "https://schema.org/Date",
        "https://schema.org/URL",
        "https://schema.org/Integer",
        "https://schema.org/Float",
        "https://schema.org/Number",
        "https://schema.org/Boolean"
    )
    private val ignoredPropertyTypes = listOf(
        "https://schema.org/Role"
    )
    private val prunedPropertyTypes = listOf(
        "https://schema.org/Text",
        "https://schema.org/URL"
    )

    @Test
    fun testExpectedClassInSkeletonClassMap() {

        // Expected results
        val expectedClass = "Place"
        val expectedNumberOfClasses = 1

        // Setup

        // Execution
        val ontologyClasses = OwlParser(rdfDocument = rdfDocument).also {
            it.lang = this.lang
            it.classes = this.classes
            it.ignoredPropertyTypes = this.ignoredPropertyTypes
        }
            .buildClassMap()
            .filter { it.key.id !in primitivePropertyTypes }

        // Validation
        val actualNumberOfTargetClasses = ontologyClasses.filter { it.key.id.endsWith(expectedClass) }.size
        assertEquals(expectedNumberOfClasses, actualNumberOfTargetClasses)
    }

    /**
     *  Support all classes as the default
     *  @since("Commit hash: e66cfd2dedd09bb496ac852a630ee1fb62466533")
     */
    @Test
    fun testExpectedClassInSkeletonClassMapWithDefaults() {

        // Expected results
        val expectedClass = "Place"
        val expectedNumberOfClasses = 1

        // Setup

        // Execution
        val ontologyClasses = OwlParser(rdfDocument = rdfDocument)
            .buildClassMap()
            .filter { it.key.id !in primitivePropertyTypes }

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

        // Execution
        val ontologyClasses = OwlParser(rdfDocument = rdfDocument).also {
            it.lang = this.lang
            it.classes = this.classes
            it.ignoredPropertyTypes = this.ignoredPropertyTypes
            it.prunedPropertyTypes = this.prunedPropertyTypes
        }
            .buildClassMap()
            .filter { it.key.id !in primitivePropertyTypes }

        // Validation
        val actualNumberOfTargetClasses = ontologyClasses.filter { it.key.id.endsWith(expectedClass) }.size
        assertEquals(expectedNumberOfClasses, actualNumberOfTargetClasses)
    }

    @Test
    fun testExpectedNoLanguageClassInMinimalClassMap() {

        // Expected results
        val expectedClass = "NoLang"
        val expectedNumberOfClasses = 1

        // Setup

        // Execution
        val ontologyClasses = OwlParser(rdfDocument = rdfDocument)
            .buildClassMap()
            .filter { it.key.id !in primitivePropertyTypes }

        // Validation
        val actualNumberOfTargetClasses = ontologyClasses.filter { it.key.id.endsWith(expectedClass) }.size
        assertEquals(expectedNumberOfClasses, actualNumberOfTargetClasses)
    }
}
