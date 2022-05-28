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

import uk.co.polycode.owltojava.rdf.*

open class OwlIdRef() {

    @field:Attribute(name = "about", required = true)
    lateinit var id: String

    constructor(_id: String) : this() { id = _id }
    override fun equals(other: Any?) = id.equals(other)
    open fun copy(_id: String = id) = OwlIdRef(_id)
    override fun toString() = id
}
