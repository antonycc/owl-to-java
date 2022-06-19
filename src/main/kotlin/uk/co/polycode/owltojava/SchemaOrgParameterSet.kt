package uk.co.polycode.owltojava

import java.math.BigDecimal
import java.math.BigInteger
import java.net.URL
import java.time.ZonedDateTime

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

@Suppress("UtilityClassWithPublicConstructor")
class SchemaOrgParameterSet {
    companion object {
        const val javaBasePackage = "uk.co.polycode"
        val licenceText = """
        This file was generated by OWL to Java as a transformation of the Schema.org schema Version 14.0.
        Schema.org is released under the Creative Commons Attribution-ShareAlike License (version 3.0). 
        The Schema.org license is applicable to the generated source files and the license is available from 
        https://creativecommons.org/licenses/by-sa/3.0/
        """.trimIndent()
        const val lang = "en"
        val classes = listOf(
            "https://schema.org/Person",
            "https://schema.org/City",
            "https://schema.org/Place",
            "https://schema.org/Corporation",
            "https://schema.org/Project",
            "https://schema.org/Book",
            "https://schema.org/Article",
            "https://example.com/NoLang",
            "https://schema.org/Fake"
        )
        val primitivePropertyTypes = mapOf(
            "https://schema.org/DataType" to Object::class.java.name,
            "https://schema.org/Text" to String::class.java.name,
            "https://schema.org/Time" to ZonedDateTime::class.java.name,
            "https://schema.org/DateTime" to ZonedDateTime::class.java.name,
            "https://schema.org/Date" to ZonedDateTime::class.java.name,
            "https://schema.org/URL" to URL::class.java.name,
            "https://schema.org/Integer" to BigInteger::class.java.name,
            "https://schema.org/Float" to BigDecimal::class.java.name,
            "https://schema.org/Number" to BigDecimal::class.java.name,
            "https://schema.org/Boolean" to "java.lang.Boolean", // Boolean::class.java.name, unboxes to boolean.
        )
        val ignoredPropertyTypes = listOf(
            "https://schema.org/Role"
        )
        val prunedPropertyTypes = listOf(
            "https://schema.org/Text",
            "https://schema.org/URL"
        )
        val ignoredSuperclasses = listOf(
            "http://www.w3.org/2000/01/rdf-schema#Class"
        )
    }
}
