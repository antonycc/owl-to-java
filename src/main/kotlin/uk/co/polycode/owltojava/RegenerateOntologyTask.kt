package uk.co.polycode.owltojava

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
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
abstract class RegenerateOntologyTask : DefaultTask() {

    // TODO: Consider using InputFile
    @get:Input
    abstract var src: String

    // TODO: Consider using InputDir
    @get:Input
    abstract var dest: String

    @get:Input
    abstract var javaBasePackage: String

    @get:Input
    @Optional
    var lang: String? = null

    @get:Input
    @Optional
    var licenceText: String? = null

    @get:Input
    @Optional
    var classes: List<String>? = null

    @get:Input
    @Optional
    var primitivePropertyTypes: Map<String, String>? = null

    @get:Input
    @Optional
    var ignoredPropertyTypes: List<String>? = null

    @get:Input
    @Optional
    var prunedPropertyTypes: List<String>? = null

    @get:Input
    @Optional
    var ignoredSuperclasses: List<String>? = null

    @get:OutputDirectory
    @Optional
    var outputDir: File? = null

    @TaskAction
    fun regenerate() {
        logger.info("Regenerating ontology from $src to $dest with base package $javaBasePackage")

        val (latestOutputDir, ontologyClasses) = RegenerateOntologyTaskDelegate(
            src = src,
            dest = dest,
            javaBasePackage = javaBasePackage)
            .also {
                it.lang = this.lang ?: it.lang
                it.licenceText = this.licenceText ?: it.licenceText
                it.classes = this.classes ?: it.classes
                it.primitivePropertyTypes = this.primitivePropertyTypes ?: it.primitivePropertyTypes
                it.ignoredPropertyTypes = this.ignoredPropertyTypes ?: it.ignoredPropertyTypes
                it.prunedPropertyTypes = this.prunedPropertyTypes ?: it.prunedPropertyTypes
                it.ignoredSuperclasses = this.ignoredSuperclasses ?: it.ignoredSuperclasses
            }
            .regenerateJavaSource()
        outputDir = latestOutputDir

        val classLabels = ontologyClasses.keys.map { it.labels }
        logger.info("Created ${ontologyClasses.size} Java classes for the following " +
                    "${classLabels.size} OWL Ontology classes (by label): ${classLabels}")
    }

 }
