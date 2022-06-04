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
        val javadocTextEscaped = getClassJavaDocEscaped(owlClass)
        try {
            javaClass.addJavadoc(javadocTextEscaped)
        } catch (e: Exception) {
            logger.warn("Could not add javadoc: ${javadocTextEscaped} due to: ${e.message}")
            logger.debug(e.stackTraceToString())
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

    private fun getClassJavaDocEscaped(owlClass: OwlClass): String {
        val label = owlClass.labels.firstOrNull { it.lang == lang }?.text ?: ""
        val comments = owlClass.comments
            .filter { it.lang == lang }
            .map { it.text }
            .fold("${label}\n\n") { javadoc, it -> javadoc.plus(it) }
        // TODO: Find a better way to escape "$" on the class JavaDoc than using the string "DOLLAR"
        return if (comments.isBlank())
            licenceText
        else
            "${comments}\n\n${licenceText}"
            .replace("\$", "DOLLAR")
    }

    companion object {

        private const val unknownClassName = "UnknownClass"

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
                    additionalFieldName[0].lowercaseChar()
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
            val fieldName = owlField.fieldNameForOwlProperty()
            val fieldComments = owlField.commentsForOwlProperty(lang)
            val fieldTypeName =
                if (fieldType != null)
                    fieldTypeForOwlProperty(javaBasePackage, fieldType, primitivePropertyTypes)
                else
                    ClassName.bestGuess("${javaBasePackage}.${unknownClassName}")
            if (fieldType == null){
                val fieldNameForProperty = owlField.fieldNameForOwlProperty()
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

        private fun fieldTypeForOwlProperty(
                javaBasePackage: String,
                type: OwlClassRef,
                primitivePropertyTypes: Map<String, String>
        ): ClassName =
            if(type.id in primitivePropertyTypes.keys)
                ClassName.bestGuess(primitivePropertyTypes[type.id])
            else
                ClassName.bestGuess(fullyQualifiedNameForPackageAndUri(javaBasePackage, type))

        private fun selectType(
                fieldTypes: List<OwlClassRef>,
                desiredClasses: List<String>,
                prunedPropertyTypes: List<String>): OwlClassRef? =
                fieldTypes
                    .firstOrNull { it.id in desiredClasses }
                    ?: fieldTypes.firstOrNull { it.id !in prunedPropertyTypes }
                    ?: fieldTypes.firstOrNull()

        fun selectSuperclass(desiredClasses: List<String>, javaSuperclasses: List<RdfsResource>): RdfsResource? =
            javaSuperclasses
                .firstOrNull { it.resource in desiredClasses }
                ?: javaSuperclasses.firstOrNull()

        fun fullyQualifiedNameForPackageAndUri(javaBasePackage: String, type: OwlClassRef) =
            "${javaBasePackage}.${hostnameToJavaPackage(URI(type.id).host)}.${classNameForUri(URI(type.id))}"

        fun hostnameToJavaPackage(host: String) =
            host.split(".").reversed().joinToString(".")

       // fun classNameForUri(uri: URI) =
       //     toTitleCase(uri.path)

        fun classNameForUri(uri: URI) = classNameForPath(uri.path)

        fun classNameForPath(path: String): String =
            if ( "/" !in path )
                toTitleCase(path)
            else {
                val pathElements = path
                    .split("/")
                    .filter { it.isNotBlank() }
                val className = pathElements
                    .reduce() { name, pathElement ->
                        name.plus(classNameForPath(pathElement))
                    }
                className
            }

        // TODO: Find out why the reduce is never run.
        //fun classNameForUri(uri: URI) =
        //        uri.path
        //            .split("/")
        //            //.onEach { logger.info { it } }
        //            .filter { it.isNotBlank() }
        //            .reduce() { name, pathElement ->
        //                name.plus(toTitleCase(pathElement))
        //            }

        // TODO: Find out why replaceFirstChar doesn't work
        // https://tedblob.com/kotlin-string-first-character-uppercase/
        private fun toTitleCase(pathElement: String) = ""
            .plus(pathElement.uppercase().subSequence(IntRange(0, 0)))
            .plus(pathElement.lowercase().subSequence(IntRange(1, pathElement.length - 1)))

    }

}
