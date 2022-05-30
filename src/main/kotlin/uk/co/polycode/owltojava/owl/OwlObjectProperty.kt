package uk.co.polycode.owltojava.owl

import com.google.common.base.MoreObjects

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
class OwlObjectProperty : OwlProperty() {

    override fun toString() =
        MoreObjects.toStringHelper(this.javaClass)
            .add("owlProperty", super.toString())
            .toString().plus("\n")
}
