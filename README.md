# owl-to-java

OWL to Java generates a Java class model from an ontology defined using the W3C Web Ontology Language (OWL).
The models are built by taking a specified list of OWL classes and creating Java classes for those OWL classes
and their properties.

Mission statement: Be a useful bridge between ontologies defined using open standards and object models of immediate
practical use in applications 

# Done

OWL to Java currently:
* Includes Java class mappings to OWL and RDF elements.
* Parses an OWL ontology into a map of java classes.
* Generates Java source from an OWL ontology in a local file.
* Runs as a Gradle Task
* Creates Java Source that doesn't yet compile

# Bugs

* Generated source doesn't compile
```text
Compiling with JDK Java compiler API.
/Users/antony/projects/libschemaorg/src/main/java/uk/co/polycode/schemaorg/org/schema/Offer.java:347: error: variable availabilityEndsZonedDateTime is already defined in class uk.co.polycode.schemaorg.org.schema.Offer
  public ZonedDateTime availabilityEndsZonedDateTime;
                       ^
/Users/antony/projects/libschemaorg/src/main/java/uk/co/polycode/schemaorg/org/schema/Offer.java:362: error: variable availabilityStartsZonedDateTime is already defined in class uk.co.polycode.schemaorg.org.schema.Offer
  public ZonedDateTime availabilityStartsZonedDateTime;
                       ^
/Users/antony/projects/libschemaorg/src/main/java/uk/co/polycode/schemaorg/org/schema/Demand.java:250: error: variable availabilityEndsZonedDateTime is already defined in class uk.co.polycode.schemaorg.org.schema.Demand
  public ZonedDateTime availabilityEndsZonedDateTime;
                       ^
/Users/antony/projects/libschemaorg/src/main/java/uk/co/polycode/schemaorg/org/schema/Demand.java:265: error: variable availabilityStartsZonedDateTime is already defined in class uk.co.polycode.schemaorg.org.schema.Demand
  public ZonedDateTime availabilityStartsZonedDateTime;
                       ^
/Users/antony/projects/libschemaorg/src/main/java/uk/co/polycode/schemaorg/org/schema/CssSelectorType.java:17: error: cannot find symbol
public class CssSelectorType extends Text {
                                     ^
  symbol: class Text
/Users/antony/projects/libschemaorg/src/main/java/uk/co/polycode/schemaorg/org/schema/XPathType.java:17: error: cannot find symbol
public class XPathType extends Text {
                               ^
  symbol: class Text
6 errors
```
* Perhaps if something extends Text and has no additional attributes, then it should be Text, 
* otherwise it has a field:   var text: String
* Count Matches expected: <2> but was: <1>

* Dependency org.simpleframework:simple-xml:2.7.1 is vulnerable
```text
CVE-2017-1000190 9.1 Improper Restriction of XML External Entity Reference vulnerability pending CVSS allocation
Results powered by Checkmarx(c)
```
* :jar: No valid plugin descriptors were found in META-INF/gradle-plugins

# TODO

* Separate project with ready to use Schema.org library Jar
* Sort fields in alphabetical order
* Gradle Examples
* Graph DB annotations + graph example
* Graphical viewer + example
* Versioning policy, auto-increment and release.
* License dependency extraction
* Customise POM with licence: https://docs.gradle.org/current/userguide/publishing_maven.html
* Consider progress against GitHub badges e.g. https://github.com/detekt/detekt
See: https://stackoverflow.com/questions/24827733/how-do-you-set-the-maven-artifact-id-of-a-gradle-project
* Build with Java 18
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
* Examples that scrape groups of sites using something like https://github.com/creative-mines/schemaorg4j
* Dashboard for Person data showing demographics and interpersonal connections
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
Starting a Gradle Daemon, 2 incompatible Daemons could not be reused, use --status for details

> Configure project :
WARNING: Unsupported Kotlin plugin version.
The `embedded-kotlin` and `kotlin-dsl` plugins rely on features of Kotlin `1.5.31` that might work differently than in the requested version `1.7.0-RC2`.

BUILD SUCCESSFUL in 22s
7 actionable tasks: 7 executed
 % 
```

The first 20 lines of Person Java object generated from a Parsed Schema.org OWL Schema describing humanity:
```shell
 % head -30 ./build/generated-sources/uk/co/polycode/org/schema/Person.java
```

```java
package uk.co.polycode.org.schema;

import java.lang.String;
import java.time.ZonedDateTime;

/**
 * Person
 *
 * A person (alive, dead, undead, or fictional).
 *
 * This file was generated by OWL to Java as a transformation of the Schema.org schema Version 14.0.
 * Schema.org is released under the Creative Commons Attribution-ShareAlike License (version 3.0). 
 * The Schema.org license is applicable to the generated source files and the license is available from 
 * "<a href="https://creativecommons.org/licenses/by-sa/3.0/">...</a>"
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

	/**
	 * Physical address of the item.
	 */
	public PostalAddress address;

	// truncated...
}
```

# Examples:

Generating Java sources (executed via JUnit)
```kotlin
package uk.co.polycode.owltojava.test

import mu.KotlinLogging
import org.simpleframework.xml.Serializer
import org.simpleframework.xml.core.Persister
import uk.co.polycode.owltojava.*
import uk.co.polycode.owltojava.rdf.RdfDocument
import java.io.BufferedReader
import java.io.File
import java.math.BigDecimal
import java.math.BigInteger
import java.net.URL
import java.nio.file.Paths
import java.time.ZonedDateTime
import kotlin.test.Test
import kotlin.test.assertTrue

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
internal class GradleTaskTest {

    private val srcTestResources = ".${File.separator}src${File.separator}test${File.separator}resources"
    private val wholeOwlFilePath = srcTestResources.plus("${File.separator}schemaorg.owl")
    private val javaSourceDirectoryPath = ".${File.separator}build${File.separator}generated-sources"
    private val javaBasePackage = "uk.co.polycode"
    private val licenceText = """
        This file was generated by OWL to Java as a transformation of the Schema.org schema Version 14.0.
        Schema.org is released under the Creative Commons Attribution-ShareAlike License (version 3.0). 
        The Schema.org license is applicable to the generated source files and the license is available from 
        https://creativecommons.org/licenses/by-sa/3.0/
        """.trimIndent()
    private val lang = "en"
    private val classes = listOf(
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
    private val primitivePropertyTypes = mapOf(
        "https://schema.org/DataType" to Object::class.java.name,
        "https://schema.org/Text"     to String::class.java.name,
        "https://schema.org/Time"     to ZonedDateTime::class.java.name,
        "https://schema.org/DateTime" to ZonedDateTime::class.java.name,
        "https://schema.org/Date"     to ZonedDateTime::class.java.name,
        "https://schema.org/URL"      to URL::class.java.name,
        "https://schema.org/Integer"  to BigInteger::class.java.name,
        "https://schema.org/Float"    to BigDecimal::class.java.name,
        "https://schema.org/Number"   to BigDecimal::class.java.name,
        "https://schema.org/Boolean"  to "java.lang.Boolean", // Boolean::class.java.name, unboxes to boolean.
    )
    private val ignoredPropertyTypes = listOf(
        "https://schema.org/Role"
    )
    private val prunedPropertyTypes = listOf(
        "https://schema.org/Text",
        "https://schema.org/URL"
    )
    private val ignoredSuperclasses = listOf(
        "http://www.w3.org/2000/01/rdf-schema#Class"
    )
    
    /**
     *  Gradle Task
     *  @since("Commit hash: c616f30060a429080fb516d1eb715af20865ce28")
     */
    @Test
    fun testJavaSourceFileInOutputWithPrimitives() {

        // Expected results
        val expectedClass = "Place"
        val outputFilePath = "${javaSourceDirectoryPath}/uk/co/polycode/org/schema/${expectedClass}.java"
        val outputFile = Paths.get(outputFilePath).toFile()

        // Setup
        val taskDelegate = RegenerateOntologyTaskDelegate(
            lang = lang,
            src = wholeOwlFilePath,
            dest = javaSourceDirectoryPath,
            javaBasePackage = javaBasePackage,
            licenceText = licenceText,
            classes = classes,
            primitivePropertyTypes = primitivePropertyTypes,
            ignoredPropertyTypes = ignoredPropertyTypes,
            prunedPropertyTypes = prunedPropertyTypes,
            ignoredSuperclasses = ignoredSuperclasses
        )

        // Execution
        taskDelegate.regenerateJavaSource()

        // Primitive validation
        assertTrue(outputFile.exists())
        val bufferedReader: BufferedReader = outputFile.bufferedReader()
        val javaSourceFile = bufferedReader.use { it.readText() }
        assertTrue { javaSourceFile.contains("public Boolean publicAccess;") }
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
