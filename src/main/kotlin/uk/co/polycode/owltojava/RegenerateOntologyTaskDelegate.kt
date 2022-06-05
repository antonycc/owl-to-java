package uk.co.polycode.owltojava

import org.simpleframework.xml.Serializer
import org.simpleframework.xml.core.Persister
import uk.co.polycode.owltojava.owl.OwlClass
import uk.co.polycode.owltojava.owl.OwlProperty
import uk.co.polycode.owltojava.rdf.RdfDocument
import java.io.File

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

        val ontologyClasses = OwlParser(
            rdfDocument = rdfDocument,
            lang = lang,
            classes = classes,
            ignoredPropertyTypes = ignoredPropertyTypes,
            prunedPropertyTypes = prunedPropertyTypes
        )
            .buildClassMap()
            .filter { it.key.id !in primitivePropertyTypes.keys }

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
        return Pair(outputDir, ontologyClasses)
    }
}
