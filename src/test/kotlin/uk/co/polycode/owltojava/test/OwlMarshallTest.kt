package uk.co.polycode.owltojava.test

import org.simpleframework.xml.core.Persister
import uk.co.polycode.owltojava.rdf.RdfDocument
import java.nio.file.Paths
import kotlin.test.Test
import kotlin.test.assertEquals
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
internal class OwlMarshallTest {

    private val srcTestResources = "./src/test/resources"
    private val skeletonOwlFilePath = Paths.get("${srcTestResources}/schemaorg-skeleton.owl")

    @Test
    fun testExpectedOntologyWithClasses() {

        // Expected results
        val expectedId = "https://schema.org/"

        // Setup

        // Execution
        val rdfDocument: RdfDocument = with(skeletonOwlFilePath.toFile()){
            Persister().read(RdfDocument::class.java, this, false)
        }

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
