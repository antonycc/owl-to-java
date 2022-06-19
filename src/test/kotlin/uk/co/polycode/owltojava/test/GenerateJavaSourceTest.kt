package uk.co.polycode.owltojava.test

import org.simpleframework.xml.core.Persister
import uk.co.polycode.owltojava.SchemaOrgParameterSet
import uk.co.polycode.owltojava.JavaSourceBuilder
import uk.co.polycode.owltojava.OwlParser
import uk.co.polycode.owltojava.rdf.RdfDocument
import java.net.URI
import java.nio.file.Paths
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

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
internal class GenerateJavaSourceTest {

    private val srcTestResources = "./src/test/resources"
    private val skeletonOwlFilePath = Paths.get("${srcTestResources}/schemaorg-skeleton.owl")

    private val skeletonRdfDocument: RdfDocument = with(skeletonOwlFilePath.toFile()){
        Persister().read(RdfDocument::class.java, this, false)
    }
    private val owlParserWithLanguage = OwlParser(rdfDocument = skeletonRdfDocument).also {
        it.lang = SchemaOrgParameterSet.lang
    }
    private val owlParserWithIgnoredAndPrunedTypes = OwlParser(rdfDocument = skeletonRdfDocument).also {
        it.ignoredPropertyTypes = SchemaOrgParameterSet.ignoredPropertyTypes
        it.prunedPropertyTypes = SchemaOrgParameterSet.prunedPropertyTypes
    }
    private val owlParserWithSpecifiedClasses = OwlParser(rdfDocument = skeletonRdfDocument).also {
        it.lang = SchemaOrgParameterSet.lang
        it.classes = SchemaOrgParameterSet.classes
        it.ignoredPropertyTypes = SchemaOrgParameterSet.ignoredPropertyTypes
        it.prunedPropertyTypes = SchemaOrgParameterSet.prunedPropertyTypes
    }
    private val javaSourceBuilder = JavaSourceBuilder(javaBasePackage = SchemaOrgParameterSet.javaBasePackage).also {
        it.lang = SchemaOrgParameterSet.lang
        it.licenceText = SchemaOrgParameterSet.licenceText
        it.desiredClasses = SchemaOrgParameterSet.classes
        it.primitivePropertyTypes = SchemaOrgParameterSet.primitivePropertyTypes
        it.prunedPropertyTypes = SchemaOrgParameterSet.prunedPropertyTypes
        it.ignoredSuperclasses = SchemaOrgParameterSet.ignoredSuperclasses
    }
    private val javaSourceBuilderWithDefaults =
        JavaSourceBuilder(javaBasePackage = SchemaOrgParameterSet.javaBasePackage)
    private val javaSourceBuilderWithLang =
        JavaSourceBuilder(javaBasePackage = SchemaOrgParameterSet.javaBasePackage).also {
        it.lang = SchemaOrgParameterSet.lang
    }

    @Test
    fun testExpectClassNameFromPath() {

        // Expected results
        val expectedClassName = "Person"

        // Setup

        // Execution
        val actualClassName = JavaSourceBuilder.classNameForPath(expectedClassName)
        val actualClassNameConverted = JavaSourceBuilder.toTitleCase(actualClassName.uppercase())

        // Validation
        assertTrue { actualClassName.contains(expectedClassName) }
        assertEquals(actualClassNameConverted, expectedClassName)
    }

    @Test
    fun testExpectClassNameFromId() {

        // Expected results
        val expectedClassName = "Person"
        val expectedSuperclassName = "Thing"

        // Setup

        // Execution
        val ontologyClasses = owlParserWithSpecifiedClasses
            .buildMapOfClassesToFieldLists()
            .filter { it.key.id !in SchemaOrgParameterSet.primitivePropertyTypes.keys }
        val owlExpectedClass = ontologyClasses.keys.firstOrNull { it.id.contains(expectedClassName) }
        val owlExpectedProperties = ontologyClasses[owlExpectedClass]
        assertNotNull(owlExpectedClass)
        assertNotNull(owlExpectedProperties)
        val actualClassName = JavaSourceBuilder.classNameForUri(URI(owlExpectedClass.id))
        val actualSuperClassIds = owlExpectedClass.subClassesOf?.map {it.resource} ?: emptyList()
        val actualSuperclassName = JavaSourceBuilder.classNameForUri(URI(actualSuperClassIds.firstOrNull() ?: ""))

        // Validation
        assertTrue { actualClassName.contains(expectedClassName) }
        assertTrue { actualSuperclassName.contains(expectedSuperclassName) }
    }

    @Test
    fun testExpectClassToBeGenerated() {

        // Expected results
        val expectedClass = "Person"
        val expectedClassLabel = "A person (alive, dead, undead, or fictional)."

        // Setup

        // Execution
        val ontologyClasses = owlParserWithIgnoredAndPrunedTypes
            .buildMapOfClassesToFieldLists()
            .filter { it.key.id !in SchemaOrgParameterSet.primitivePropertyTypes.keys }
        val owlExpectedClass = ontologyClasses.keys.firstOrNull { it.id.contains(expectedClass)}
        val owlExpectedProperties = ontologyClasses[owlExpectedClass]
        assertNotNull(owlExpectedClass)
        assertNotNull(owlExpectedProperties)
        val javaSource = javaSourceBuilder.build(owlExpectedClass, owlExpectedProperties)

        // Validation
        assertTrue { javaSource.isNotBlank() && javaSource.contains("public class ${expectedClass}") }
        assertTrue { javaSource.isNotBlank() && javaSource.contains(expectedClassLabel) }
    }

    @Test
    fun testExpectClassToBeGeneratedWithDefaults() {

        // Expected results
        val expectedClass = "Person"
        val expectedClassLabel = "A person (alive, dead, undead, or fictional)."

        // Setup

        // Execution
        val ontologyClasses = owlParserWithIgnoredAndPrunedTypes
            .buildMapOfClassesToFieldLists()
            .filter { it.key.id !in SchemaOrgParameterSet.primitivePropertyTypes.keys }
        val owlExpectedClass = ontologyClasses.keys.firstOrNull { it.id.contains(expectedClass)}
        val owlExpectedProperties = ontologyClasses[owlExpectedClass]
        assertNotNull(owlExpectedClass)
        assertNotNull(owlExpectedProperties)
        val javaSource = javaSourceBuilderWithDefaults.build(owlExpectedClass, owlExpectedProperties)

        // Validation
        assertTrue { javaSource.isNotBlank() && javaSource.contains("public class ${expectedClass}") }
        assertTrue { javaSource.isNotBlank() && javaSource.contains(expectedClassLabel) }
    }

    @Test
    fun testExpectNoLangClassToBeGenerated() {

        // Expected results
        val expectedClass = "NoLang"

        // Setup

        // Execution
        val ontologyClasses = owlParserWithLanguage
            .buildMapOfClassesToFieldLists()
            .filter { it.key.id !in SchemaOrgParameterSet.primitivePropertyTypes.keys }
        val owlExpectedClass = ontologyClasses.keys.firstOrNull { it.id.contains(expectedClass)}
        val owlExpectedProperties = ontologyClasses[owlExpectedClass]
        assertNotNull(owlExpectedClass)
        assertNotNull(owlExpectedProperties)
        val javaSource = javaSourceBuilderWithLang.build(owlExpectedClass, owlExpectedProperties)

        // Validation
        assertTrue { javaSource.isNotBlank() && javaSource.contains("public class ${expectedClass}") }
    }

    @Test
    fun testExpectThingWithNoLangClassToBeGenerated() {

        // Expected results
        val expectedClass = "Thing"

        // Setup

        // Execution
        val ontologyClasses = owlParserWithLanguage
            .buildMapOfClassesToFieldLists(SchemaOrgParameterSet.classes)
            .filter { it.key.id !in SchemaOrgParameterSet.primitivePropertyTypes.keys }
        val owlExpectedClass = ontologyClasses.keys.firstOrNull { it.id.contains(expectedClass)}
        val owlExpectedProperties = ontologyClasses[owlExpectedClass]
        assertNotNull(owlExpectedClass)
        assertNotNull(owlExpectedProperties)
        val javaSource = javaSourceBuilderWithLang.build(owlExpectedClass, owlExpectedProperties)

        // Validation
        assertTrue { javaSource.isNotBlank() && javaSource.contains("public NoLang nolangproperty;") }
    }
}
