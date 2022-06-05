# owl-to-java

OWL to Java generates a Java class model from an ontology defined using the W3C Web Ontology Language (OWL). The models are built by taking a specified list of OWL classes and creating Java classes for those OWL classes and their properties.

Mission statement: Be a useful bridge between ontologies defined using open standards and object models of immediate practical use in applications 

# Done

OWL to Java currently:
* Includes Java class mappings to OWL and RDF elements.
* Parses an OWL ontology into a map of java classes.
* Generates Java source from an OWL ontology in a local file.

# Bugs

* *(a bulleted list of bugs)*

# TODO

* Separate project which runs the Owl2Java Gradle Task
* Read and action IntelliJ README wanrings and tpyos
* Gradle Task with default arguments
* Gradle Examples
* Graph DB annotations + graph example
* Graphical viewer + example
* Versioning policy, auto increment and release.
* License dependency extraction
* Consider progress against GitHub badges e.g. https://github.com/detekt/detekt
* Separate project with ready to use Schema.org library Jar
* **Publish: Release 1.0.0 to GitHub packages**
* KDoc for code
* Java Docs for output
* Public website
* Publish Detekt and Kover reports from the Quality Report Build
* Consider progress against additional GitHub README badges.
* GitHub and public website analytics
* Donate option (linked to hosting account)
* Automate library updates
* Contributor guidelines
* **Library Launch: Release to Maven Central**
* Command-line execution from a shaded Jar
* RDBMS annotations + relational example
* Command line Examples
* Command-line execution from a Docker container
* Release to DockerHub
* HATEOAS REST API
* API docs with runnable swagger
* REST API examples
* Text based viewer
* **Platform Launch: API based generation and viewer**
* Kotlin idioms
* Profiling
* Inheritance by interface (via Lombok, or better in Kotlin?) and aggregation
* Handling of plurals as collections e.g. Person.parent is a relationship with multiplicity
* Load configuration set by name e.g. (com.example.OwlToJavaConfigSetSchemaOrg.setTaskConfig(this))
* Load configuration from Kotlin Script: https://kotlinexpertise.com/run-kotlin-scripts-from-kotlin-programs/
* Extend one schema with another
* @since versioned annotation on tests and auto-generated version history from test annotations



Example of Kotlin script evaluation
```kotlin
val script = compile("""listOf(1,2,3).joinToString(":")""")
assertEquals(listOf(1, 2, 3).joinToString(":"), script.eval())

val regenerate by registering(RegenerateOntologyTask::class) {
    compile(File(Paths.get("${projectDir}/owl2java-schema.org.config").absolutePath()).load()).eval(this)
}
```

# See also

* Class and field definitions are generated based on OWL https://www.w3.org/TR/owl-guide/
* The ontology from https://schema.org/ is the primary use case for this project.

# Examples:

Build with tests which generate Java Sources:
```shell
 % gradle clean test                                                       

> Configure project :
Gradle logging an info log message which is always logged.
Gradle logging an error log message.
Gradle logging a warning log message.
Gradle logging a lifecycle info log message.

> Task :compileKotlin
'compileJava' task (current target is 18) and 'compileKotlin' task (current target is 1.8) jvm target compatibility should be set to the same Java version.
w: /Users/antony/projects/owl-to-java/src/main/kotlin/uk/co/polycode/owltojava/OwlParser.kt: (135, 60): Type inference failed. The value of the type parameter T should be mentioned in input types (argument types, receiver type or expected type). Try to specify it explicitly.

> Task :compileTestKotlin
'compileTestJava' task (current target is 18) and 'compileTestKotlin' task (current target is 1.8) jvm target compatibility should be set to the same Java version.

BUILD SUCCESSFUL in 4s
7 actionable tasks: 7 executed
 % 
```

The first 20 lines of Person Java object generated from a Parsed Schema.org OWL Schema describing humanity:
```shell
 % head -20 ./build/generated-sources/uk/co/polycode/org/schema/Person.java
```
```java
package uk.co.polycode.org.schema;

import java.lang.String;
import java.time.ZonedDateTime;

/**
 * A person (alive, dead, undead, or fictional).
 */
public class Person extends Thing {
	/**
	 * Where to find the definition of the OWL Class used to generate this Java class.
	 */
	public String isDefinedBy = "https://schema.org/Person";

	/**
	 * An additional name for a Person, can be used for a middle name.
	 */
	public String additionalName;
	
	//truncated..
}
```

# Examples:

Generating Java sources (executed via JUnit)
```kotlin
package uk.co.polycode.owltojava.test

import mu.KotlinLogging
import org.simpleframework.xml.Serializer
import org.simpleframework.xml.core.Persister
import uk.co.polycode.owltojava.JavaSourceBuilder
import uk.co.polycode.owltojava.OwlParser
import uk.co.polycode.owltojava.RegenerateOntologyTask
import uk.co.polycode.owltojava.rdf.RdfDocument
import java.io.File
import java.math.BigDecimal
import java.math.BigInteger
import java.net.URL
import java.time.ZonedDateTime
import kotlin.test.Test
import kotlin.test.assertTrue

private val logger = KotlinLogging.logger {}

internal class GenerateJavaSourceTest {

    private val owlFilePath = ".${File.separator}src${File.separator}test${File.separator}resources${File.separator}schemaorg.owl"
    private val javaSourceDirectoryPath = ".${File.separator}build${File.separator}generated-sources"
    private val javaBasePackage = "uk.co.polycode"
    private val lang = "en"
    private val classes = listOf(
        "http://schema.org/Person",
        "http://schema.org/City",
        "http://schema.org/Place",
        "http://schema.org/CorporationX",
        "http://schema.org/Project",
        "http://schema.org/Book",
        "http://schema.org/Article",
        "http://schema.org/Fake"
    )
    private val primitivePropertyTypes = mapOf(
        "http://schema.org/DataType" to Object::class.java.name,
        "http://schema.org/Text"     to String::class.java.name,
        "http://schema.org/Time"     to ZonedDateTime::class.java.name,
        "http://schema.org/DateTime" to ZonedDateTime::class.java.name,
        "http://schema.org/Date"     to ZonedDateTime::class.java.name,
        "http://schema.org/URL"      to URL::class.java.name,
        "http://schema.org/Integer"  to BigInteger::class.java.name,
        "http://schema.org/Float"    to BigDecimal::class.java.name,
        "http://schema.org/Number"   to BigDecimal::class.java.name,
        "http://schema.org/Boolean"  to "java.lang.Boolean", // Boolean::class.java.name, unboxes to boolean.
    )
    private val ignoredPropertyTypes = listOf(
        "http://schema.org/Role"
    )
    private val prunedPropertyTypes = listOf(
        "http://schema.org/Text",
        "http://schema.org/URL"
    )
    private val ignoredSuperclasses = listOf<String>(
        "http://www.w3.org/2000/01/rdf-schema#Class"
    )

    @Test
    fun testJavaSourceFileInOutput() {

        // Expected results
        val expectedClass = "Place"
        var expectedOutputFile = javaSourceDirectoryPath
        expectedOutputFile += "${File.separator}uk${File.separator}co${File.separator}polycode${File.separator}"
        expectedOutputFile += "${File.separator}org${File.separator}schema${File.separator}${expectedClass}.java"
        // Setup
        val owlFile = File(owlFilePath)
        val serializer: Serializer = Persister()
        val javaSourceBuilder = JavaSourceBuilder(
            lang = lang,
            javaBasePackage = javaBasePackage,
            desiredClasses = classes,
            primitivePropertyTypes = primitivePropertyTypes,
            ignoredPropertyTypes = ignoredPropertyTypes,
            prunedPropertyTypes = prunedPropertyTypes,
            ignoredSuperclasses = ignoredSuperclasses
        )

        // Execution
        val workingDirectory = System.getProperty("user.dir")
        logger.debug("Working Directory = ${workingDirectory}}")
        val rdfDocument: RdfDocument = serializer.read(RdfDocument::class.java, owlFile, false)
        val ontologyClasses = OwlParser(rdfDocument)
            .withLanguage(lang)
            .withClasses(classes)
            .withIgnoredPropertyTypes(ignoredPropertyTypes)
            .withPrunedPropertyTypes(prunedPropertyTypes)
            .buildClassMap()
        val outputDir = File(javaSourceDirectoryPath)
        RegenerateOntologyTask.writeClassMapAsJavaSource(javaSourceDirectoryPath, outputDir, javaBasePackage, ontologyClasses, javaSourceBuilder)

        // Validation
        val actualFile = File(expectedOutputFile)
        assertTrue(actualFile.exists())
    }
}
```

# Contributions

Owl to Java uses Trunk based Development https://www.flagship.io/git-branching-strategies/#trunk-based-development

An alternate branching strategy is likely to be required when there are multiple committers. While a low volume of
commits and committers remains we have two **paths to contribute to this project**:
* Contact me via my GitHub profile (->website -> LinkedIn) and ask to be added to this project.
* Fork the repository then create a pull request.

## Request a feature

To request a new feature:
1. Add an item to the TODO list

(see above for **paths to contribute to this project**).

## Add a feature

To add a new feature:
1. Pick a feature from this `README.md`
2. Create at least one test for the feature, cut and paste the TODO list item into the test comment
3. Build using `gradle build`
4. Commit code which passes `gradle clean check -PsafeBuildMode=false`
5. Get the commit hash using `git rev-parse HEAD` and add it to the test KDoc.

e.g.
```kotlin
    /**
     *  Support all classes as the default
     *  @since("Commit hash: e66cfd2dedd09bb496ac852a630ee1fb62466533")
     */
    @Test
    fun testExpectedClassInSkeletonClassMapWithDefaults() {
        // truncated...
    }
```



# Licence

## Licence - OWL to Java

OWL to Java is released under the Mozilla Public License, v. 2.0:
```java
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
```

## Licence - Schema.org

OWL to Java uses the Schema from Schema.org which is released under the Creative Commons Attribution-ShareAlike License (version 3.0): https://creativecommons.org/licenses/by-sa/3.0/
Schema.org Version 14.0 is currently used and this can be downloaded from https://schema.org/docs/schemaorg.owl
( Release archive: https://github.com/schemaorg/schemaorg/tree/main/data/releases/14.0/ )
The following files are copies of or derivatives of Schema.org schemas:
```shell
./src/test/resources/schemaorg.owl - Schema.org Version 14.0: copy of https://schema.org/docs/schemaorg.owl
./src/test/resources/schemaorg-minimal-person.owl - Schema.org Version 10: Cut down copy orientated aroung Person
./src/test/resources/schemaorg-skeleton.owl - Schema.org Version 10: Cut down smallest meaningful ontology
```
Fragment of these files are referenced in OWL to Java source code and tests
