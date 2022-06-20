package uk.co.polycode.owltojava.owl

import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.ElementListUnion

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
 *
 * e.g.
 * <pre>
 * <rdfs:domain> *I am this one*
 *     <owl:Class>
 *         <owl:unionOf rdf:parseType="Collection">
 *             <owl:Class rdf:about="http://schema.org/CreativeWork"/>
 *             <owl:Class rdf:about="http://schema.org/CommunicateAction"/>
 *             <owl:Class rdf:about="http://schema.org/Event"/>
 *         </owl:unionOf>
 *     </owl:Class>
 * </rdfs:domain>
 * </pre>
 */
class OwlClassContainer {

    @field:Element(name = "Class", required = true)
    lateinit var classUnion: OwlClassUnion

    class OwlClassUnion {
        @field:Element(name = "unionOf", required = true)
        lateinit var unionOf: OwlUnionOf
        fun clone(): OwlClassUnion{
            val new = OwlClassUnion()
            new.unionOf = this.unionOf.clone()
            return new
        }
    }

    class OwlUnionOf {
        @field:ElementListUnion(ElementList(entry = "Class", type=OwlClassRef::class, inline = true))
        lateinit var classes: List<OwlClassRef>
        fun clone(): OwlUnionOf{
            val new = OwlUnionOf()
            new.classes = this.classes.map { it.clone() }
            return new
        }
    }

    fun clone(): OwlClassContainer{
        val new = OwlClassContainer()
        new.classUnion = this.classUnion.clone()
        return new
    }

    override fun toString() =
        classUnion
            .unionOf
            .classes
            .filter {
                !listOf<String>(
                    "https://schema.org/Text",
                    "https://schema.org/URL",
                    "https://schema.org/Role")
                    .contains(it.id)
            }
            .toString()
}
