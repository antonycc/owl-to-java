package uk.co.polycode.owltojava

//import mu.KotlinLogging
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

//private val logger = KotlinLogging.logger {}

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

    // TODO: Should be optional (logs to info with no output dir)
    @get:Input
    abstract var dest: String

    // TODO: Should be optional
    @get:Input
    abstract var javaBasePackage: String

    // TODO: Should be optional
    @get:Input
    abstract var licenceText: String

    // TODO: Should be optional
    @get:Input
    abstract var classes: List<String>

    // TODO: Should be optional
    @get:Input
    abstract var primitivePropertyTypes: Map<String, String>

    // TODO: Should be optional
    @get:Input
    abstract var ignoredPropertyTypes: List<String>

    // TODO: Should be optional
    @get:Input
    abstract var prunedPropertyTypes: List<String>

    // TODO: Should be optional
    @get:Input
    abstract var ignoredSuperclasses: List<String>

    @get:OutputDirectory
    @Optional
    var outputDir: File? = null

    @TaskAction
    fun regenerate() {
        logger.info("Regenerating ontology from $src to $dest with base package $javaBasePackage")

        val taskDelegate = RegenerateOntologyTaskDelegate(
            lang = lang,
            src = src,
            dest = dest,
            javaBasePackage = javaBasePackage,
            licenceText = licenceText,
            classes = classes,
            primitivePropertyTypes = primitivePropertyTypes,
            ignoredPropertyTypes = ignoredPropertyTypes,
            prunedPropertyTypes = prunedPropertyTypes,
            ignoredSuperclasses = ignoredSuperclasses
        )

        val (latestOutputDir, ontologyClasses) = taskDelegate.regenerateJavaSource()
        outputDir = latestOutputDir

        val classLabels = ontologyClasses.keys.map { it.labels }
        logger.info("Created ${ontologyClasses.size} Java classes for the following " +
                    "${classLabels.size} OWL Ontology classes (by label): ${classLabels}")
    }

 }
