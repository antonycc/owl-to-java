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
package uk.co.polycode.owltojava.owl

import com.google.common.base.MoreObjects
import org.simpleframework.xml.*
import kotlin.reflect.full.createInstance

import uk.co.polycode.owltojava.rdf.*

import uk.co.polycode.owltojava.rdf.*

/**
 *  Parses Properties containing sections such as:
 *  <pre>
 *   <rdfs:domain>
 *       <owl:Class>
 *           <owl:unionOf rdf:parseType="Collection">
 *               <owl:Class rdf:about="http://schema.org/CreativeWork"/>
 *               <owl:Class rdf:about="http://schema.org/CommunicateAction"/>
 *               <owl:Class rdf:about="http://schema.org/Event"/>
 *           </owl:unionOf>
 *       </owl:Class>
 *   </rdfs:domain>
 *   <rdfs:range>
 *       <owl:Class>
 *           <owl:unionOf rdf:parseType="Collection">
 *               <owl:Class rdf:about="http://schema.org/Thing"/>
 *               <owl:Class rdf:about="http://schema.org/Text"/>
 *               <owl:Class rdf:about="http://schema.org/URL"/>
 *               <owl:Class rdf:about="http://schema.org/Role"/>
 *           </owl:unionOf>
 *       </owl:Class>
 *   </rdfs:range>
 *  </pre>
 */
abstract class OwlProperty : OwlIdRef() {

    @field:ElementListUnion(ElementList(entry = "label", type=RdfsText::class, inline = true))
    lateinit var labels: List<RdfsText>

    @field:ElementListUnion(ElementList(entry = "comment", type=RdfsText::class, inline = true))
    lateinit var comments: List<RdfsText>

    @field:ElementList(entry = "supersededBy", type = RdfsResource::class, inline = true, required = false)
    var supersededBy = mutableListOf<RdfsResource>()

    @field:Element(name = "isDefinedBy", required = true)
    lateinit var isDefinedBy: RdfsResource

    @field:ElementList(entry = "domain", type = OwlClassContainer::class, inline = true, required = false)
    var domain = mutableListOf<OwlClassContainer>()

    @field:ElementList(entry = "range", type = OwlClassContainer::class, inline = true, required = false)
    var range = mutableListOf<OwlClassContainer>()

    var fieldTypes = listOf<OwlClassRef>()

    public fun withFieldTypes(fieldTypes: List<OwlClassRef>): OwlProperty{
        val new: OwlProperty = this::class.createInstance()
        new.labels = this.labels.map { it }
        new.comments = this.comments.map { it }
        new.supersededBy = this.supersededBy
        new.isDefinedBy = this.isDefinedBy
        new.domain = this.domain
        //new.domain.classUnion.unionOf.classes = this.domain.classUnion.unionOf.classes.map { it }
        new.range = this.range
        //new.range.classUnion.unionOf.classes = this.range.classUnion.unionOf.classes.map { it }
        new.fieldTypes = fieldTypes
        return new
    }
    public fun fieldNameForOwlProperty(lang: String, default: String) =
        labels.filter { it.lang == lang }.firstOrNull()?.text ?: "${default}${lang}"

    public fun commentsForOwlProperty(lang: String) =
        comments
            .filter { it.lang == lang }
            .map { it.text }
            .reduce { javadoc, it -> javadoc.plus(it) }
            .replace("$", "DOLLAR")

    override fun toString() =
        MoreObjects.toStringHelper(this.javaClass)
 //           .add("id", id)
            .add("labels", labels)
 //           .add("comments", comments)
 //           .add("isDefinedBy", isDefinedBy)
            .add("domain", domain)
            .add("range", range)
            .toString()
}