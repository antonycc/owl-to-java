package uk.co.polycode.owltojava.rdf

import org.simpleframework.xml.Attribute

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
open class RdfsResource {

    @field:Attribute(name = "resource", required = true)
    lateinit var resource: String

    fun clone(): RdfsResource {
        val new = RdfsResource()
        new.resource = this.resource
        return new
    }
}
