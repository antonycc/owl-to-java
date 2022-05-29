package uk.co.polycode.owltojava.rdf

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Text

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
class RdfsText() {

    @field:Attribute(name = "lang", required = true)
    lateinit var lang: String

    @field:Text(required = true)
    lateinit var text: String

    constructor(_lang: String, _text: String) : this() {
        lang = _lang
        text = _text
    }
    override fun equals(other: Any?) = "${lang}:${text}".equals(other.toString())
    fun copy(_lang: String, _text: String) = RdfsText(_lang, _text)

    override fun toString() = "${lang}:${text}"
}