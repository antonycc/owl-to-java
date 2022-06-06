package uk.co.polycode.owltojava

import mu.KotlinLogging
import org.simpleframework.xml.Serializer
import org.simpleframework.xml.core.Persister
import uk.co.polycode.owltojava.owl.OwlClass
import uk.co.polycode.owltojava.owl.OwlProperty
import uk.co.polycode.owltojava.rdf.RdfDocument
import java.io.File

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
class RegenerateOntologyTaskDelegate(val lang: String,
                                     val src: String,
                                     val dest: String,
                                     val javaBasePackage: String,
                                     val licenceText: String,
                                     val classes: List<String>,
                                     val primitivePropertyTypes: Map<String, String>,
                                     val ignoredPropertyTypes: List<String>,
                                     val prunedPropertyTypes: List<String>,
                                     val ignoredSuperclasses: List<String>) {

    fun regenerateJavaSource(): Pair<File?, Map<OwlClass, List<OwlProperty>>> {

        val owlFile = File(src)
        val serializer: Serializer = Persister()
        val rdfDocument = serializer.read(RdfDocument::class.java, owlFile, false)
        logger.info { "Read RDF Document with id ${rdfDocument.id} from ${src}" }
        logger.debug { "RDF Document has ${rdfDocument.owlClasses.size} classes" }

        val ontologyClassesWithPrimitives = OwlParser(
            rdfDocument = rdfDocument,
            lang = lang,
            classes = classes,
            ignoredPropertyTypes = ignoredPropertyTypes,
            prunedPropertyTypes = prunedPropertyTypes
        ).buildClassMap()
        val ontologyClasses = ontologyClassesWithPrimitives.filter { it.key.id !in primitivePropertyTypes.keys }
        logger.debug { "There are ${ontologyClassesWithPrimitives.size} classes in the classMap with primitives" }
        logger.debug { "There are ${ontologyClasses.size} classes in the classMap (after primitives were filtered)" }

        logger.info { "There are ${ontologyClasses.size} classes to write as Java source" }
        val javaSourceBuilder = JavaSourceBuilder(
            lang = lang,
            javaBasePackage = javaBasePackage,
            licenceText = licenceText,
            desiredClasses = classes,
            primitivePropertyTypes = primitivePropertyTypes,
            prunedPropertyTypes = prunedPropertyTypes,
            ignoredSuperclasses = ignoredSuperclasses
        )

        val javaSourceWriter = JavaSourceWriter()
        val outputDir = javaSourceWriter.outputJavaClasses(dest, ontologyClasses, javaSourceBuilder)
        logger.info { "Java source written to ${outputDir?.absolutePath ?: dest}" }
        return Pair(outputDir, ontologyClasses)
    }
}
