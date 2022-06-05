package uk.co.polycode.owltojava

import mu.KotlinLogging
import uk.co.polycode.owltojava.owl.OwlClass
import uk.co.polycode.owltojava.owl.OwlProperty
import java.io.File
import java.net.URI

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
open class JavaSourceWriter {

    fun outputJavaClasses(
        outputDirFilepath: String,
        ontologyClasses: Map<OwlClass, List<OwlProperty>>,
        javaSourceBuilder: JavaSourceBuilder
    ): File? {
        val latestOutputDir =
            if (outputDirFilepath.isNotBlank() && outputDirFilepath != "INFO")
                File(outputDirFilepath)
            else
                null
        if (latestOutputDir != null) {
            writeClassMapAsJavaSource(outputDirFilepath, latestOutputDir,ontologyClasses, javaSourceBuilder)
        } else { //Log
            ontologyClasses
                .filter { it.key.id.endsWith("Place") }
                .forEach { logger.info(javaSourceBuilder.build(it.key, it.value)) }
        }
        return latestOutputDir
    }

    fun writeClassMapAsJavaSource(
        dest: String,
        outputDir: File,
        ontologyClasses: Map<OwlClass, List<OwlProperty>>,
        javaSourceBuilder: JavaSourceBuilder
    ) {
        logger.info("Writing to $dest")
        if (!outputDir.exists()) outputDir.mkdir()
        ontologyClasses
            .forEach {
                val javaSource = javaSourceBuilder.build(it.key, it.value)
                val javaFullyQualifiedName =
                    JavaSourceBuilder.fullyQualifiedNameForPackageAndUri(javaSourceBuilder.javaBasePackage, it.key)
                    val packageDir = appendAndCreatePackageDir(dest, javaFullyQualifiedName)
                    File("${packageDir}${JavaSourceBuilder.classNameForUri(URI(it.key.id))}.java")
                        .printWriter().use { out ->
                            out.println(javaSource)
                        }
            }
    }

    private fun appendAndCreatePackageDir(basePath: String, javaFullyQualifiedName: String): String {
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
