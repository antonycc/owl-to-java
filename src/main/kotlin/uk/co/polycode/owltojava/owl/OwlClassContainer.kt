package uk.co.polycode.owltojava.owl

import org.simpleframework.xml.*

import uk.co.polycode.owltojava.rdf.*

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
    }

    class OwlUnionOf {
        @field:ElementListUnion(ElementList(entry = "Class", type=OwlClassRef::class, inline = true))
        lateinit var classes: List<OwlClassRef>
    }

    override fun toString() =
        classUnion
            .unionOf
            .classes
            .filter {
                !listOf<String>("http://schema.org/Text", "http://schema.org/URL", "http://schema.org/Role").contains(it.id)
            }
            .toString()
        //return MoreObjects.toStringHelper(this.javaClass)
        //    .add("classes", classUnion.unionOf.classes)
        //    .toString()
    //}
}
