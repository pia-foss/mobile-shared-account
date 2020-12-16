/*
 *  Copyright (c) 2020 Private Internet Access, Inc.
 *
 *  This file is part of the Private Internet Access Mobile Client.
 *
 *  The Private Internet Access Mobile Client is free software: you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as published by the Free
 *  Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  The Private Internet Access Mobile Client is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 *  details.
 *
 *  You should have received a copy of the GNU General Public License along with the Private
 *  Internet Access Mobile Client.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.privateinternetaccess.account.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class MessageInformation(
    @SerialName("id")
    val id: Long,
    @SerialName("link")
    val link: Link? = null,
    @SerialName("message")
    val message: Map<String, String> = mapOf()
) {
    @Serializable
    data class Link(
        @SerialName("action")
        val action: Action,
        @SerialName("text")
        val text: Map<String, String>
    ) {
        @Serializable
        data class Action(
            @SerialName("settings")
            val settings: Map<String, Boolean> = mapOf(),
            @SerialName("uri")
            val uri: String? = null,
            @SerialName("view")
            val view: String? = null,
        )
    }
}