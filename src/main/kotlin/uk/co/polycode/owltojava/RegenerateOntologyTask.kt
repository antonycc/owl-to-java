package uk.co.polycode.owltojava

import mu.KotlinLogging
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.*
import org.simpleframework.xml.Serializer
import org.simpleframework.xml.core.Persister
import uk.co.polycode.owltojava.owl.OwlClass
import uk.co.polycode.owltojava.owl.OwlProperty
import java.io.File
import java.net.URI

import uk.co.polycode.owltojava.rdf.*

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
abstract class RegenerateOntologyTask : DefaultTask() {

    @get:Input
    abstract var lang: String

    @get:Input
    abstract var src: String

    @get:Input
    abstract var dest: String

    @get:Input
    abstract var javaBasePackage: String

    @get:Input
    abstract var licenceText: String

    @get:Input
    abstract var classes: List<String>

    @get:Input
    abstract var primitivePropertyTypes: Map<String, String>

    @get:Input
    abstract var ignoredPropertyTypes: List<String>

    @get:Input
    abstract var prunedPropertyTypes: List<String>

    @get:Input
    abstract var ignoredSuperclasses: List<String>

    @get:OutputDirectory
    @Optional
    var outputDir: File? = null

    @TaskAction
    fun regenerate() {
        logger.info("Regenerating ontology from $src to $dest with base package $javaBasePackage")

        val owlFile = File(src)
        val serializer: Serializer = Persister()
        val rdfDocument: RdfDocument = serializer.read(RdfDocument::class.java, owlFile, false)
        logger.debug("Parsed document ${rdfDocument.toString()}")

        // Extract classes
        //val ontologyClasses = OwlParser(rdfDocument)
        //    .withLanguage(lang)
        //    .withClasses(classes)
        //    .withIgnoredPropertyTypes(ignoredPropertyTypes)
        //    .withPrunedPropertyTypes(prunedPropertyTypes)
        //    .buildClassMap()
        //    .filter { it.key.id !in primativePropertyTypes.keys }
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
            ignoredPropertyTypes = ignoredPropertyTypes,
            prunedPropertyTypes = prunedPropertyTypes,
            ignoredSuperclasses = ignoredSuperclasses
            )

        val latestOutputDir = if (dest.isNotBlank() && dest != "INFO") File(dest) else null
        outputDir = latestOutputDir
        if (latestOutputDir != null) {
            writeClassMapAsJavaSource(dest, latestOutputDir, javaBasePackage, ontologyClasses, javaSourceBuilder)
        } else {
            // Log
            ontologyClasses
                .filter { it.key.id.endsWith("Place") }
                .forEach { logger.info(javaSourceBuilder.build(it.key, it.value)) }

        }
        logger.info("Created ${ontologyClasses.size} Java classes for the OWL Ontology.: ${ontologyClasses.keys.map { it.labels }}")
    }

    companion object {

        fun writeClassMapAsJavaSource(
            dest: String,
            outputDir: File,
            javaBasePackage: String,
            ontologyClasses: Map<OwlClass, List<OwlProperty>>,
            javaSourceBuilder: JavaSourceBuilder
        ) {
            logger.info("Writing to $dest")
            if (!outputDir.exists()) outputDir.mkdir()
            ontologyClasses
                .forEach {
                    val javaSource = javaSourceBuilder.build(it.key, it.value)
                    val javaFullyQualifiedName =
                        JavaSourceBuilder.fullyQualifiedNameForPackageAndUri(javaBasePackage, it.key)
                    if (javaFullyQualifiedName.isNullOrBlank()) {
                        logger.warn("No Java source file name identified for ${it.key}")
                    } else {
                        var packageDir = "${appendAndCreatePackageDir(dest, javaFullyQualifiedName)}"
                        File("${packageDir}${JavaSourceBuilder.classNameForUri(URI(it.key.id))}.java")
                            .printWriter().use { out ->
                                out.println(javaSource)
                            }
                    }
                }
        }

        fun appendAndCreatePackageDir(basePath: String, javaFullyQualifiedName: String): String {
            var packageDir = basePath.plus(File.separator)
            if ("." in javaFullyQualifiedName) {
                packageDir += javaFullyQualifiedName
                    .subSequence(0, javaFullyQualifiedName.lastIndexOf("."))
                    .split(".")
                    .joinToString(File.separator)
                    .plus(File.separator)
                File(packageDir).mkdirs()
            }
            return packageDir
        }
    }
 }