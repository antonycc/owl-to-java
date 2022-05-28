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
package uk.co.polycode.owltojava.rdf

import org.simpleframework.xml.Attribute

open class RdfsResource() {

    @field:Attribute(name = "resource", required = true)
    lateinit var resource: String

    constructor(_resource: String) : this() { resource = _resource }
    override fun equals(other: Any?) = resource.equals(other)
    open fun copy(_resource: String = resource) = RdfsResource(_resource)
    override fun toString() = resource
}
