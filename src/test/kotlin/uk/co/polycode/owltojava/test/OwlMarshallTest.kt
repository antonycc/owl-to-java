package uk.co.polycode.owltojava.test

import mu.KotlinLogging
import org.simpleframework.xml.Serializer
import org.simpleframework.xml.core.Persister
import org.slf4j.impl.StaticLoggerBinder
import uk.co.polycode.owltojava.OwlParser
import uk.co.polycode.owltojava.rdf.RdfDocument
import java.io.File
import java.math.BigDecimal
import java.math.BigInteger
import java.net.URL
import java.time.ZonedDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

private val logger = KotlinLogging.logger {}

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
internal class OwlMarshallTest {

    private val srcTestResources = ".${File.separator}src${File.separator}test${File.separator}resources"
    private val skeletonOwlFilePath = srcTestResources.plus("${File.separator}schemaorg-skeleton.owl")

    @Test
    fun testExpectedOntologyWithClasses() {

        // Expected results
        val expectedId = "https://schema.org/"

        // Setup
        val owlFile = File(skeletonOwlFilePath)
        val serializer: Serializer = Persister()

        // Execution
        logger.debug("Working Directory = ${System.getProperty("user.dir")}}")
        val rdfDocument: RdfDocument = serializer.read(RdfDocument::class.java, owlFile, false)

        // Validation
        assertEquals(expectedId, rdfDocument.id)
        assertTrue { rdfDocument.owlClasses.isNotEmpty() }
        assertEquals(expectedId, rdfDocument.owlOntology.id)
        assertTrue { rdfDocument.owlOntology.label.isNotBlank() }
        assertTrue { rdfDocument.owlOntology.toString().contains(rdfDocument.owlOntology.label) }
        assertTrue { rdfDocument.owlOntology.owlVersionInfo.isNotBlank() }
        assertTrue { rdfDocument.owlOntology.owlVersionModified.isNotBlank() }
        assertTrue { rdfDocument.owlObjectProperties.first().labels.isNotEmpty() }
        assertTrue { rdfDocument.owlObjectProperties.first().comments.isNotEmpty() }
        assertTrue { rdfDocument.owlObjectProperties.first().isDefinedBy.resource.isNotBlank() }
    }

}
