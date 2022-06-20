package uk.co.polycode.owltojava

import mu.KotlinLogging
import org.gradle.api.tasks.TaskAction
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
class RegenerateOntologyCli {

    var lang = "en"
    var src: String = "schemaorg.owl"
    var dest: String = ""
    var javaBasePackage = ""
    var licenceText = ""
    var classes = emptyList<String>()
    var primitivePropertyTypes = emptyMap<String, String>()
    var ignoredPropertyTypes = emptyList<String>()
    var prunedPropertyTypes = emptyList<String>()
    var ignoredSuperclasses = emptyList<String>()

    @TaskAction
    fun main(args: Array<String>) {
        logger.info("OWL to Java with args: ${args}")
        logger.info("Regenerating ontology from ${src} to ${dest} with base package ${javaBasePackage}")

        val ontologyClasses = RegenerateOntologyTaskDelegate(
            src = File(src),
            dest = File(dest),
            javaBasePackage = javaBasePackage)
            .also {
                it.lang = this.lang
                it.licenceText = this.licenceText
                it.classes = this.classes
                it.primitivePropertyTypes = this.primitivePropertyTypes
                it.ignoredPropertyTypes = this.ignoredPropertyTypes
                it.prunedPropertyTypes = this.prunedPropertyTypes
                it.ignoredSuperclasses = this.ignoredSuperclasses
            }
            .regenerateJavaSource()

        val classLabels = ontologyClasses.keys.map { it.labels }
        logger.info("Created ${ontologyClasses.size} Java classes for the following " +
                    "${classLabels.size} OWL Ontology classes (by label): ${classLabels}")
    }

 }
