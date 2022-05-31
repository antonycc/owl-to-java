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
