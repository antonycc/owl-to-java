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
package uk.co.polycode.owltojava

import mu.KotlinLogging

import uk.co.polycode.owltojava.owl.*
import uk.co.polycode.owltojava.rdf.*

private val logger = KotlinLogging.logger {}

class OwlParser(
    private val rdfDocument: RdfDocument,
    val lang: String = "en",
    var classes: List<String> = mutableListOf(),
    var ignoredPropertyTypes: List<String> = mutableListOf(),
    var prunedPropertyTypes: List<String> = mutableListOf()){

    private val noLabelForLanguage = "noLabelForLanguage_"

    fun withLanguage(lang: String) =
        OwlParser(rdfDocument, lang, classes, ignoredPropertyTypes, prunedPropertyTypes)

    fun withClasses(classes: List<String>) =
        OwlParser(rdfDocument, lang, classes, ignoredPropertyTypes, prunedPropertyTypes)

    fun withIgnoredPropertyTypes(ignoredPropertyTypes: List<String>) =
        OwlParser(rdfDocument, lang, classes, ignoredPropertyTypes, prunedPropertyTypes)

    fun withPrunedPropertyTypes(prunedPropertyTypes: List<String>) =
        OwlParser(rdfDocument, lang, classes, ignoredPropertyTypes, prunedPropertyTypes)

    fun buildClassMap(): MutableMap<OwlClass,List<OwlProperty>> {
        val classMap = createClassMapForClasses(classes)

        // Iterate through the map
        //      creating classes for any properties that have a class ref
        //      replace the classref with with a class
        // While there are properties with a classRef
        var undefinedCount: Int
        do {
            addClassesForClassRefs(classMap)

            classMap
                .forEach {
                    classMap.put(it.key, mapClassRefsToClasses(classMap, it))
                }

            val allOwlPropertyClassesAfterUpdate = classMap
                .values
                .flatten()
                .map { it.fieldTypes }
                .flatten()
            undefinedCount = allOwlPropertyClassesAfterUpdate.filter { it::class == OwlClassRef::class }.size
            logMappingProgress(allOwlPropertyClassesAfterUpdate, undefinedCount)

        } while (undefinedCount != 0)

        classMap.onEach { logger.debug("${it.key}${it.value}}") }
        return classMap
    }

    private fun logMappingProgress(
        allOwlPropertyClassesAfterUpdate: List<OwlClassRef>,
        undefinedCount: Int
    ) {
        val definedCount = allOwlPropertyClassesAfterUpdate.filter { it::class == OwlClass::class }.size
        logger.debug("Classes with an undefined OwlClassRef $undefinedCount vs a defined OwlClass $definedCount")
    }

    private fun createClassMapForClasses(classesToMap: List<String>) =
        appendSuperclasses(rdfDocument.owlClasses
            .filter { it.id in classesToMap })
            .associateWith { fieldsForClass(it) }
            .toMutableMap()

    private fun appendSuperclasses(currentClasses: List<OwlClass>): List<OwlClass> {
        var requiredClasses = currentClasses
        var requiredClassIds = currentClasses.map { it.id }
        var addedClasses = requiredClasses.size
        var classesWithSuperclasses = requiredClasses.filter { it.subClassesOf?.isNotEmpty() ?: false }
        while (addedClasses > 0) {
            val superclassIds = classesWithSuperclasses
                .asSequence()
                .map { it.subClassesOf?.map { it.resource } ?: emptyList() }
                .filter { it.isNotEmpty() }
                .flatten()
                .filter { it !in requiredClassIds }
                .toSet()
            val superclasses = rdfDocument.owlClasses
                .filter { it.id in superclassIds }
            addedClasses = superclasses.size
            requiredClassIds = listOf(requiredClassIds, superclassIds).flatten()
            requiredClasses = listOf(requiredClasses, superclasses).flatten()
            classesWithSuperclasses = superclasses.filter { it.subClassesOf?.isNotEmpty() ?: false }
        }
        return requiredClasses
    }

    private fun addClassesForClassRefs(classMap: MutableMap<OwlClass, List<OwlProperty>>) =
        classMap
            .values
            .flatten()
            .map { it.fieldTypes }
            .flatten()
            .filter { it::class == OwlClassRef::class }
            .forEach {
                val owlClass = findClassForClassRef(rdfDocument.owlClasses, it)
                if (owlClass != null) {
                    createClassMapForClasses(listOf(owlClass.id))
                        .forEach {
                            classMap.put(it.key, it.value)
                        }
                }
            }

    private fun mapClassRefsToClasses(classMap: MutableMap<OwlClass, List<OwlProperty>>,
                                      owlClassAndProperties: Map.Entry<OwlClass, List<OwlProperty>>) =
        owlClassAndProperties.value
            .map {
                it.withFieldTypes( it.fieldTypes.map { getClassForClassRef(classMap, it) } )
            }

    private fun getClassForClassRef(classMap: MutableMap<OwlClass,List<OwlProperty>>, classRef: OwlClassRef) =
        classMap.filter { it.key.id == classRef.id }.ifEmpty { mapOf(Pair(classRef, null)) }.keys.first()

    private fun findClassForClassRef(classList: List<OwlClass>, classRef: OwlClassRef) =
        classList.firstOrNull { it.id == classRef.id }

    private fun fieldsForClass(owlClass: OwlClass): List<OwlProperty> {
        val allOwlProperties = listOf(rdfDocument.owlObjectProperties,rdfDocument.owlDataTypeProperties).flatten()
        var filteredOwlProperties = allOwlProperties
            .filter { owlProperty ->
                owlProperty.domain.isNotEmpty() && owlClass.id in owlProperty.domain.first().classUnion.unionOf.classes.map { it.id }
            }
            .filter { owlProperty -> !owlProperty.supersededBy.any {
                    superseder: RdfsResource -> superseder !in rdfDocument.owlClasses.map { availableClass: OwlClass -> availableClass.id } }
            }
            .map { it.withFieldTypes(fieldTypesForOwlProperty(it)) }
        return filteredOwlProperties
    }

    private fun fieldTypesForOwlProperty(owlProperty: OwlProperty): List<OwlClassRef> {
        val fieldsTypes = if ( owlProperty.range.isEmpty() )
            listOf<OwlClassRef>()
        else
            owlProperty.range.first().classUnion.unionOf.classes
            .filter { it.id !in ignoredPropertyTypes }
            .filter { it.id in rdfDocument.owlClasses.map { it.id } }

        val fieldTypesWithoutPrunedTypes = fieldsTypes.filter { it.id !in prunedPropertyTypes }
        if ( fieldTypesWithoutPrunedTypes.isNotEmpty() ) {
            return fieldTypesWithoutPrunedTypes
        }

        val prunedFieldTypes = fieldsTypes.filter { it.id in prunedPropertyTypes }

        // If the last three characters of the field name match a pruned type use it.
        // (e.g. "url" in "myUrl" or "ext" in "myText")
        val fieldName = owlProperty.fieldNameForOwlProperty(lang, noLabelForLanguage)
        val fieldTypesPrunedMatchingEndOfFieldName = prunedFieldTypes
            .filter { it.id.endsWith(fieldName.takeLast(3), true) }
        if (fieldTypesPrunedMatchingEndOfFieldName.isNotEmpty() ) {
            return fieldTypesPrunedMatchingEndOfFieldName
        }

        val fieldTypesFirstPruned = prunedFieldTypes.filter { prunedPropertyTypes.isNotEmpty() && it.id == prunedPropertyTypes.first() }
        if (fieldTypesFirstPruned.isNotEmpty() ) {
            return fieldTypesFirstPruned
        }

        return fieldsTypes
    }
}