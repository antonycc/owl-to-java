package uk.co.polycode.owltojava.owl

import com.google.common.base.MoreObjects
import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Path
import org.simpleframework.xml.Root
import org.simpleframework.xml.Text

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
 */
class OwlOntology {

    @field:Attribute(name = "about", required = true)
    lateinit var id: String

    @field:Element(name = "label", required = true)
    lateinit var label: String

    @field:Element(name = "versionInfo", required = true)
    lateinit var owlVersionInfo: String

    @field:Element(name = "modified", required = true)
    lateinit var owlVersionModified: String

    override fun toString() =
        MoreObjects.toStringHelper(this.javaClass)
//            .add("id", id)
            .add("label", label)
//            .add("owlVersionInfo", owlVersionInfo)
//            .add("owlVersionModified", owlVersionModified)
            .toString()
}
