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

    // TODO: Consider if this could be improved by:
    //  gathering all desired and necessary classes, de-duping, then translating to classes
    fun buildClassMap(): MutableMap<OwlClass,List<OwlProperty>> {
        logger.debug { "Desired class list has ${classes.size} classes for the class map." }
        val classMap = createClassMapForClasses(classes.ifEmpty { rdfDocument.owlClasses.map { it.id } })

        logger.debug { "Initial classMap has ${classMap.size} classes" }

        // TODO: Consider if a recursive version will be more expressive
        // Iterate through the map
        //      creating classes for any properties that have a class ref
        //      replace the OwlClassRef with an OwlClass
        // While there are properties with a classRef
        var undefinedCount: Int
        do {
            addClassesForFieldTypes(classMap)
            logger.debug { "Classes in classMap after adding classes for fields: ${classMap.size}" }
            transformAnyOwlClassRefsToOwlClasses(classMap)
            logger.debug { "Classes in classMap after translating OwlClassRefs to OwlClasses: ${classMap.size}" }
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

    // TODO: Do this with immutable lists
    private fun transformAnyOwlClassRefsToOwlClasses(classMap: MutableMap<OwlClass, List<OwlProperty>>) {
        classMap
            .forEach {
                classMap.put(it.key, mapClassRefsToClasses(classMap, it))
            }
    }

    private fun logMappingProgress(
        allOwlPropertyClassesAfterUpdate: List<OwlClassRef>,
        undefinedCount: Int
    ) {
        val definedCount = allOwlPropertyClassesAfterUpdate.filter { it::class == OwlClass::class }.size
        logger.debug("Classes with an undefined OwlClassRef $undefinedCount vs a defined OwlClass $definedCount")
    }

    // TODO: AppendSuperClasses looks awkward by wrapping the class list.
    private fun createClassMapForClasses(classesToMap: List<String>): MutableMap<OwlClass, List<OwlProperty>> {
        logger.debug { "There are ${classesToMap.size} classes to map to a list of fields" }
        // TODO: Break this down and add classes for fields and superclasses separately
        return appendSuperclasses(rdfDocument.owlClasses
            .filter { it.id in classesToMap })
            .associateWith { fieldsForClass(it) }
            .toMutableMap()
    }

    private fun appendSuperclasses(currentClasses: List<OwlClass>): List<OwlClass> {
        logger.debug { "There are ${currentClasses.size} classes to check for a superclass" }
        var requiredClasses = currentClasses
        var requiredClassIds = currentClasses.map { it.id }
        var addedClasses = requiredClasses.size
        var classesWithSuperclasses = requiredClasses.filter { it.subClassesOf?.isNotEmpty() ?: false }
        logger.debug { "There are ${classesWithSuperclasses.size} classes classes with superclasses" }
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
            logger.debug { "Added ${addedClasses} for superclasses" }
            requiredClassIds = listOf(requiredClassIds, superclassIds).flatten()
            requiredClasses = listOf(requiredClasses, superclasses).flatten()
            classesWithSuperclasses = superclasses.filter { it.subClassesOf?.isNotEmpty() ?: false }
        }
        logger.debug { "There are ${requiredClasses.size} classes required as desired classes and superclasses" }
        return requiredClasses
    }

    // TODO: Dedupe this list after flattening and check any other usages of flatten
    private fun addClassesForFieldTypes(classMap: MutableMap<OwlClass, List<OwlProperty>>) =
        classMap
            .values
            .flatten()
            .map { it.fieldTypes }
            .flatten()
            .filter { it::class == OwlClassRef::class }
            .forEach {fieldType ->
                val owlClass = findClassForClassRef(rdfDocument.owlClasses, fieldType)
                if (owlClass != null) {
                    createClassMapForClasses(listOf(owlClass.id))
                        .forEach {
                            classMap[it.key] = it.value
                        }
                }
            }

    // TODO: Do this with something immutable
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

    // TODO: Could we look up the class from the owlClass domain, instead of filtering all property types
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
