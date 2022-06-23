package uk.co.polycode.owltojava.integrationtest

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
internal class GradleTaskWholeFileTest {

    private val srcTestResources = "./src/integrationTest/resources"
    private val wholeOwlFilePath = Paths.get("${srcTestResources}/schemaorg.owl")
    private val javaSourceDirectoryPath = Paths.get("./build/generated-sources-with-whole-file")
    private val taskDelegate = RegenerateOntologyTaskDelegate(
        src = wholeOwlFilePath.toFile(),
        dest = javaSourceDirectoryPath.toFile(),
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
     * Gradle Task with default arguments
     *
     * @since("Commit hash: 2ce90b1eff2e7746ebf96f0dbfd82e668510b505")
     */
    @Test
    fun testJavaSourceFileInOutputWithDefaults() {

        // Expected results
        val expectedClass = "Place"
        val outputFilePath = "${javaSourceDirectoryPath}/uk/co/polycode/org/schema/${expectedClass}.java"
        val outputFile = Paths.get(outputFilePath).toFile()

        // Setup

        // Execution

        // Validation
        assertNotNull(taskDelegate)
        assertTrue(outputFile.exists())
        val bufferedReader: BufferedReader = outputFile.bufferedReader()
        val javaSourceFile = bufferedReader.use { it.readText() }
        assertTrue { javaSourceFile.contains("public class ${expectedClass}") }
        assertTrue { javaSourceFile.contains("public GeoShape geoGeoShape;") }
        assertTrue { javaSourceFile.contains("public PostalAddress address;") }
        assertTrue { javaSourceFile.contains("public DefinedTerm keywords;") }
        assertTrue { javaSourceFile.contains("public BigDecimal latitude;") }
        assertTrue { javaSourceFile.contains("public BigDecimal longitude;") }
        assertTrue { javaSourceFile.contains("public OpeningHoursSpecification openingHoursSpecification;") }
        assertTrue { javaSourceFile.contains("public Photograph photo;") }
        assertTrue { javaSourceFile.contains("public ImageObject photoImageObject;") }
        assertTrue { javaSourceFile.contains("public BigInteger maximumAttendeeCapacity;") }
        assertTrue { javaSourceFile.contains("public Boolean publicAccess;") }
    }

    /**
     * Test detects duplicated field names where the field name is generated from 2 classes mapped to the same primitive
     *
     * /Users/antony/projects/libschemaorg/src/main/java/uk/co/polycode/schemaorg/org/schema/Offer.java:347:
     * error:
     * variable availabilityEndsZonedDateTime is already defined in class uk.co.polycode.schemaorg.org.schema.Offer
     * public ZonedDateTime availabilityEndsZonedDateTime;
     *                      ^
     * /Users/antony/projects/libschemaorg/src/main/java/uk/co/polycode/schemaorg/org/schema/Offer.java:362:
     * error:
     * variable availabilityStartsZonedDateTime is already defined in class uk.co.polycode.schemaorg.org.schema.Offer
     * public ZonedDateTime availabilityStartsZonedDateTime;
     *                      ^
     * @since("Commit hash: 6c00cc11c84725ca4472979a48988a031c166fdd")
     */
    @Test
    fun testNoDuplicateFieldNames() {

        // Expected results
        val expectedClass = "Offer"
        val outputFilePath = "${javaSourceDirectoryPath}/uk/co/polycode/org/schema/${expectedClass}.java"
        val outputFile = Paths.get(outputFilePath).toFile()
        val expectedSingleField = "availabilityEndsZonedDateTime"

        // Setup

        // Execution

        // Validation
        assertTrue(outputFile.exists())
        val bufferedReader: BufferedReader = outputFile.bufferedReader()
        val javaSourceFile = bufferedReader.use { it.readText() }
        assertTrue { javaSourceFile.contains("public class ${expectedClass}") }
        assertTrue { javaSourceFile.contains(expectedSingleField) }
        val matches = countMatches(javaSourceFile,expectedSingleField)
        assertTrue { matches == 1 }
    }

    /**
     * Test detects illegal Superclass (which was replaced by a Primitive so not defined but still used as a superclass)
     *
     * /Users/antony/projects/libschemaorg/src/main/java/uk/co/polycode/schemaorg/org/schema/CssSelectorType.java:17:
     * error: cannot find symbol
     * public class CssSelectorType extends Text {
     *                                      ^
     * symbol: class Text
     * /Users/antony/projects/libschemaorg/src/main/java/uk/co/polycode/schemaorg/org/schema/XPathType.java:17:
     * error: cannot find symbol
     * public class XPathType extends Text {
     *                                ^
     * symbol: class Text
     *
     * @since("Commit hash: 1f2204db95a8c3efbf2f7d62756c4b94dd0b22ee")
     */
    @Test
    fun testNoTextSuperclass() {

        // Expected results
        val expectedClass = "XPathType"
        val outputFilePath = "${javaSourceDirectoryPath}/uk/co/polycode/org/schema/${expectedClass}.java"
        val outputFile = Paths.get(outputFilePath).toFile()
        val unexpectedSingleField = "extends Text"

        // Setup

        // Execution

        // Validation
        assertTrue(outputFile.exists())
        val bufferedReader: BufferedReader = outputFile.bufferedReader()
        val javaSourceFile = bufferedReader.use { it.readText() }
        assertTrue { javaSourceFile.contains("public class ${expectedClass}") }
        assertFalse { javaSourceFile.contains(unexpectedSingleField) }
    }

    @Test
    fun testCountMatches() {

        // Expected results
        val sampleString = "AABBCCBB"
        val expectedNoMatch = "XX"
        val expectedOneMatch = "AA"
        val expectedOneMatch2 = "CC"
        val expectedTwoMatch = "BB"

        // Setup

        // Execution

        // Validation
        assertEquals(0, countMatches(sampleString, expectedNoMatch))
        assertEquals(1, countMatches(sampleString, expectedOneMatch))
        assertEquals(1, countMatches(sampleString, expectedOneMatch2))
        assertEquals(0, countMatches(expectedNoMatch, expectedOneMatch))
        assertEquals(2, countMatches(sampleString, expectedTwoMatch))
    }

    private fun countMatches(s: String, sub: String): Int = with(s.indexOf(sub)){
        if ( this == -1 )
            0
        else
            1 + countMatches(s.substring(this + sub.length), sub)
    }
}
