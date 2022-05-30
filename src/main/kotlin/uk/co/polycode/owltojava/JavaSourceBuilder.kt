package uk.co.polycode.owltojava

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.TypeSpec
import mu.KotlinLogging
import uk.co.polycode.owltojava.owl.OwlClass
import uk.co.polycode.owltojava.owl.OwlClassRef
import uk.co.polycode.owltojava.owl.OwlProperty
import uk.co.polycode.owltojava.rdf.RdfsResource
import java.net.URI
import javax.lang.model.element.Modifier

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
open class JavaSourceBuilder(
    var lang: String,
    var javaBasePackage: String,
    var licenceText: String,
    var desiredClasses: List<String>,
    var primitivePropertyTypes: Map<String, String>,
    var prunedPropertyTypes: List<String>,
    var ignoredSuperclasses: List<String>
) {

    fun build(owlClass: OwlClass, owlProperties: List<OwlProperty>): String {

        val owlClassId = URI(owlClass.id)
        val javaClassName = classNameForUri(owlClassId)
        val javaPackage = "${javaBasePackage}.${hostnameToJavaPackage(owlClassId.host)}"

        val javaClass = TypeSpec
            .classBuilder(javaClassName)
            .addModifiers(Modifier.PUBLIC)
        val javadocText = owlClass.comments
            .filter { it.lang == lang }
            .map { it.text }
            .reduce { javadoc, it -> javadoc.plus(it) }
            .plus("\n\n${licenceText}")
        if (javadocText.isNotBlank()) {
            val javadocTextEscaped = javadocText.replace("\$", "DOLLAR")
            try {
                javaClass.addJavadoc(javadocTextEscaped)
            } catch (e: Exception) {
                logger.warn("Could not add javadoc: ${javadocTextEscaped} due to: ${e.message}")
                logger.debug(e.stackTraceToString())
            }
        }
        val javaSuperclasses = owlClass.subClassesOf
        if (!javaSuperclasses.isNullOrEmpty()) {
            val filteredJavaSuperclasses = javaSuperclasses
                .filter { it.resource !in ignoredSuperclasses }
            val superclass = selectSuperclass(desiredClasses, filteredJavaSuperclasses)
            if (superclass != null) {
                // Pick one superclass to be the Java superclass
                val javaSuperclassName = classNameForUri(URI(superclass.resource))
                val javaSuperclass = ClassName.get(javaPackage, javaSuperclassName)
                javaClass.superclass(javaSuperclass)

                // Multiple superclasses are split into separate fields
                // e.g.
                // public class ClassWithMultipleSuperclasses extends SelectedSuperclass {
                //    AlternateSuperclass  alternateSuperclass;
                //    AnotherSuperclass    anotherSuperclass;
                filteredJavaSuperclasses
                    .filter { it.resource != superclass.resource }
                    .forEach {
                        addJavaFieldsForAdditionalSuperclasses(it, javaClass, javaBasePackage, primitivePropertyTypes)
                    }
            }
        }

        javaClass.addField(
            FieldSpec
                .builder(String::class.java, "isDefinedBy")
                .addModifiers(Modifier.PUBLIC)
                .addJavadoc("Where to find the definition of the OWL Class used to generate this Java class.")
                .initializer("\"${owlClass.isDefinedBy.resource}\"")
                .build()
        )

        owlProperties
            .forEach() {
                addJavaFields(it,
                    javaClass,
                    lang,
                    javaBasePackage,
                    primitivePropertyTypes,
                    desiredClasses,
                    prunedPropertyTypes)
            }

        return JavaFile.builder(javaPackage, javaClass.build())
            .build()
            .toString()
    }

    companion object {

        private const val unknownPackageName = "unknown.package"
        private const val unknownClassName = "UnknownClass"
        private const val noLabelForLanguage = "noLabelForLanguage_"

        fun addJavaFieldsForAdditionalSuperclasses(
            owlSuperclass: RdfsResource,
            javaClass: TypeSpec.Builder,
            javaBasePackage: String,
            primitivePropertyTypes: Map<String, String>
        ) {
            val superclassOwlClass = OwlClassRef()
            superclassOwlClass.id = owlSuperclass.resource
            val additionalFieldTypeName = fieldTypeForOwlProperty(
                javaBasePackage,
                superclassOwlClass,
                primitivePropertyTypes)
            val additionalFieldName = additionalFieldTypeName.toString().split(".").last()
            if (additionalFieldName.isNotBlank()) {
                val capitalisedFieldName = additionalFieldName.replaceFirst(
                    additionalFieldName[0],
                    additionalFieldName[0].toLowerCase()
                )
                javaClass.addField(FieldSpec
                    .builder(additionalFieldTypeName, capitalisedFieldName)
                    .addModifiers(Modifier.PUBLIC)
                    .build())
            }
        }

        fun addJavaFields(
            owlField: OwlProperty,
            javaClass: TypeSpec.Builder,
            lang: String,
            javaBasePackage: String,
            primitivePropertyTypes: Map<String, String>,
            desiredClasses: List<String>,
            prunedPropertyTypes: List<String>
        ) {
            val fieldType = selectType(owlField.fieldTypes, desiredClasses, prunedPropertyTypes)
            val fieldName = owlField.fieldNameForOwlProperty(lang, noLabelForLanguage)
            val fieldComments = owlField.commentsForOwlProperty(lang)
            val fieldTypeName =
                if (fieldType != null)
                    fieldTypeForOwlProperty(javaBasePackage, fieldType, primitivePropertyTypes)
                else
                    ClassName.bestGuess("${javaBasePackage}.${unknownClassName}")
            if (fieldType == null){
                val fieldNameForProperty = owlField.fieldNameForOwlProperty(lang, noLabelForLanguage)
                logger.warn("No field type found for Owl class: ${owlField.id} used by ${fieldNameForProperty}")
            }
            val fieldModifiers = listOf(
                "public",
                "private",
                "static",
                "final",
                "volatile",
                "transient",
                "abstract",
                "var",
                "val")
            val safeFieldName =
                if (fieldName in fieldModifiers)
                    "${fieldName}Field"
                else
                    fieldName
            javaClass.addField(
                FieldSpec
                    .builder(fieldTypeName, safeFieldName)
                    .addModifiers(Modifier.PUBLIC)
                    .addJavadoc(fieldComments)
                    .build()
            )
            // Multiple type fields are split into separate fields
            // e.g.
            // public class CreativeWork extends Thing {
            //    Person       creator;
            //    Organization creatorOrganization;
            owlField.fieldTypes
                .filter { it.id != fieldType?.id }
                .forEach {
                    val additionalFieldTypeName = fieldTypeForOwlProperty(javaBasePackage, it, primitivePropertyTypes)
                    val additionalFieldName = fieldName + additionalFieldTypeName.toString().split(".").last()
                    javaClass.addField(
                        FieldSpec
                            .builder(additionalFieldTypeName, additionalFieldName)
                            .addModifiers(Modifier.PUBLIC)
                            .addJavadoc(fieldComments)
                            .build()
                    )
                }
        }

        fun fieldTypeForOwlProperty(
                javaBasePackage: String,
                type: OwlClassRef,
                primitivePropertyTypes: Map<String, String>
        ): ClassName =
            if(type.id in primitivePropertyTypes.keys)
                ClassName.bestGuess(primitivePropertyTypes[type.id])
            else
                ClassName.bestGuess(fullyQualifiedNameForPackageAndUri(javaBasePackage, type))

        fun selectType(
                fieldTypes: List<OwlClassRef>,
                desiredClasses: List<String>,
                prunedPropertyTypes: List<String>): OwlClassRef? =
            if (fieldTypes.isEmpty())
                null
            else if (fieldTypes.size > 1 && fieldTypes.any { it.id in desiredClasses })
            // First look for ones that match the desired classes list and select the first matching that list
                fieldTypes.find {
                        fieldType -> fieldType.id == desiredClasses.firstOrNull {
                        desiredClass -> desiredClass in fieldTypes.map { it.id }
                    }
                }
            else if (fieldTypes.size > 1 && fieldTypes.any { it.id !in prunedPropertyTypes })
            // Next try the first in the owl list that isn't pruned (e.g. text is pruned in favour of objects)
                fieldTypes.firstOrNull { fieldType -> fieldType.id !in prunedPropertyTypes }
            else
            // Or the first in the owl list which might be a list of 1 and even 1 Text type.
                fieldTypes.firstOrNull()

        fun selectSuperclass(desiredClasses: List<String>, javaSuperclasses: List<RdfsResource>): RdfsResource? =
            if (javaSuperclasses.isEmpty())
                null
            else if (javaSuperclasses.size > 1 && javaSuperclasses.any { it.resource in desiredClasses })
            // First look for ones that match the desired classes list and select the first matching that list
                javaSuperclasses.find { superclass -> superclass.resource == desiredClasses.firstOrNull {
                        desiredClass -> desiredClass in javaSuperclasses.map { it.resource }
                    }
                }
            else
            // Or the first in the owl list which might be a list of 1.
                javaSuperclasses.firstOrNull()

        fun fullyQualifiedNameForPackageAndUri(javaBasePackage: String, type: OwlClassRef?) =
            if (type == null)
                "${javaBasePackage}.$unknownPackageName"
            else
                "${javaBasePackage}.${hostnameToJavaPackage(URI(type.id).host)}.${classNameForUri(URI(type.id))}"

        fun hostnameToJavaPackage(host: String) = host.split(".").reversed().joinToString(".")

        fun classNameForUri(uri: URI): String? {
            val className = uri.path
                .split("/")
                .filter { it.isNotBlank() }
                .reduce() { name, pathElement ->
                    name
                        .plus(pathElement.toUpperCase().subSequence(IntRange(0, 0)))
                        .plus(pathElement.toLowerCase().subSequence(IntRange(1, pathElement.length - 1)))
                }
            return if (className.isNotBlank())
                className.replaceFirst(className[0], className[0].toUpperCase())
            else
                unknownClassName
        }
    }
}
