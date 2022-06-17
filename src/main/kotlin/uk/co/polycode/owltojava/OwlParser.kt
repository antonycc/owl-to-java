package uk.co.polycode.owltojava

import mu.KotlinLogging
import uk.co.polycode.owltojava.owl.OwlClass
import uk.co.polycode.owltojava.owl.OwlClassRef
import uk.co.polycode.owltojava.owl.OwlProperty
import uk.co.polycode.owltojava.rdf.RdfDocument

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
class OwlParser(
    private val rdfDocument: RdfDocument,
    var lang: String = "en",
    var classes: List<String> = mutableListOf(),
    var ignoredPropertyTypes: List<String> = mutableListOf(),
    var prunedPropertyTypes: List<String> = mutableListOf()){

    // TODO: Consider an alternative and parameterised fuzzy match to the last 3 characters
    @field:Suppress("MagicNumber")
    private val typeFuzzyMatchLast = 3

    fun buildMapOfClassesToFieldLists() =
        buildMapOfClassesToFieldLists(emptyList<String>())

    fun buildMapOfClassesToFieldLists(desiredClassIds: List<String>) =
        buildMapOfClassesToFieldLists(
            desiredClassIds.ifEmpty { rdfDocument.owlClasses.map { it.id } },
            emptyMap<OwlClass,List<OwlProperty>>()
        )

    private fun buildMapOfClassesToFieldLists(
        desiredClassIds: List<String>,
        classesToFields: Map<OwlClass,List<OwlProperty>>
    ):Map<OwlClass,List<OwlProperty>> =
        if (desiredClassIds.isEmpty()) {
            logger.debug { "All classes have been defined in the class map." }
            classesToFields
        } else {
            logger.debug { "Desired class list has ${desiredClassIds.size} classes for the class map." }
            val definedClassIds = classesToFields.keys.map { it.id }
            val newClasses = rdfDocument.owlClasses.filter { it.id in desiredClassIds && it.id !in definedClassIds}
            logger.debug { "New classes to add to the classMap: ${newClasses.size}" }
            val newClassesToFields = classesToFields + newClasses.associateWith { fieldsForClass(it) }
            val newlyDefinedClassIds = classesToFields.keys.map { it.id }
            val classIdsForFields = newClassesToFields
                .values
                .asSequence()
                .flatten()
                .map { fieldTypesForOwlProperty(it) }
                .flatten()
                .map { it.id }
                .toList()
            logger.debug { "New classes to add for fields: ${classIdsForFields.size}" }
            val classIdsForSuperclasses = newClasses
                .map {it.subClassesOf?.map { subClass -> subClass.resource } ?: emptyList()}
                .flatten()
                .toList()
            logger.debug { "New classes to add for superclasses: ${classIdsForSuperclasses.size}" }
            val newDesiredClassIds: List<String> = (classIdsForFields + classIdsForSuperclasses)
                .distinctBy { it }
                .filter { it !in newlyDefinedClassIds }
                .toList()
            buildMapOfClassesToFieldLists(newDesiredClassIds, newClassesToFields)
        }

    // TODO: Consider looking up the class from the owlClass domain, instead of filtering all property types
    private fun fieldsForClass(owlClass: OwlClass) //: List<OwlProperty>
        = with(rdfDocument.owlClasses.map { it.id }.toHashSet()) {
            listOf(rdfDocument.owlObjectProperties, rdfDocument.owlDataTypeProperties)
                .flatten()
                .filter { owlClass.id in domainClassIdsForProperty(it) }
                .filter { owlProperty: OwlProperty -> !owlProperty.supersededBy.any { it.resource !in this } }
                .map { it.withFieldTypes(fieldTypesForOwlProperty(it)) }
        }

    private fun domainClassIdsForProperty(owlProperty: OwlProperty) =
        owlProperty.domain
            .map { it.classUnion.unionOf.classes }
            .flatten()
            .map { it.id }
            .toHashSet()

    private fun fieldTypesForOwlProperty(owlProperty: OwlProperty): List<OwlClassRef> {
        val fieldTypes = owlProperty.range
            .map { it.classUnion.unionOf.classes }
            .flatten()
            .filter { it.id !in ignoredPropertyTypes }
            .filter { it.id in rdfDocument.owlClasses.map { definedClass -> definedClass.id } }
        // If any of the field types are not in the pruned types, return this list
        return fieldTypes.filter { it.id !in prunedPropertyTypes }.ifEmpty {
            val prunedFieldTypes = fieldTypes.filter { it.id in prunedPropertyTypes }

            // If the last three characters of the field name match a pruned type use it.
            // (e.g. "url" in "myUrl" or "ext" in "myText")
            // Otherwise return the first (least disliked) of the pruned types
            // If there are no pruned types return the full list of types for the object property
            val fieldName = owlProperty.id.substringAfterLast("/") // owlProperty.fieldNameForOwlProperty()
            prunedFieldTypes
                .filter { it.id.endsWith(fieldName.takeLast(typeFuzzyMatchLast), true) }
                .ifEmpty { prunedFieldTypes.filter { it.id == prunedPropertyTypes.firstOrNull() } }
                .ifEmpty { fieldTypes }
        }
    }
}
