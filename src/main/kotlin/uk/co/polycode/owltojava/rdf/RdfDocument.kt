package uk.co.polycode.owltojava.rdf

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.ElementListUnion
import org.simpleframework.xml.Root
import uk.co.polycode.owltojava.owl.OwlClass
import uk.co.polycode.owltojava.owl.OwlDatatypeProperty
import uk.co.polycode.owltojava.owl.OwlObjectProperty
import uk.co.polycode.owltojava.owl.OwlOntology

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
@Root(strict = false)
class RdfDocument {

    // Example: http://schema.org/
    @field:Attribute(name = "base", required = true)
    lateinit var id: String

    @field:Element(name = "Ontology", required = true)
    lateinit var owlOntology: OwlOntology

    @field:ElementListUnion(ElementList(entry = "Class", type=OwlClass::class, inline = true))
    lateinit var owlClasses: List<OwlClass>

    @field:ElementListUnion(ElementList(entry = "ObjectProperty", type=OwlObjectProperty::class, inline = true))
    lateinit var owlObjectProperties: List<OwlObjectProperty>

    @field:ElementListUnion(ElementList(entry = "DatatypeProperty", type=OwlDatatypeProperty::class, inline = true))
    lateinit var owlDataTypeProperties: List<OwlDatatypeProperty>
}
