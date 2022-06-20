package uk.co.polycode.owltojava.owl

import com.google.common.base.MoreObjects
import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.ElementListUnion
import uk.co.polycode.owltojava.rdf.RdfsResource
import uk.co.polycode.owltojava.rdf.RdfsText

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
open class OwlClass : OwlClassRef() {

    @field:ElementListUnion(ElementList(entry = "label", type=RdfsText::class, inline = true))
    lateinit var labels: List<RdfsText>

    @field:ElementListUnion(ElementList(entry = "comment", type=RdfsText::class, inline = true))
    lateinit var comments: List<RdfsText>

    @field:Element(name = "isDefinedBy", required = true)
    lateinit var isDefinedBy: RdfsResource

    // http://schema.org/Thing - Does not have a subclass
    @field:ElementListUnion(
        ElementList(entry = "subClassOf", type=RdfsResource::class, inline = true, required = false)
    )
    var subClassesOf: List<RdfsResource>? = null

    override fun toString() =
        MoreObjects.toStringHelper(this.javaClass)
            .add("labels", labels)
            .toString().plus("\n")
}
