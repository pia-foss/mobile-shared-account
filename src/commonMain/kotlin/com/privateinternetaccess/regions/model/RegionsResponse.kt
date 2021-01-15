package com.privateinternetaccess.regions.model

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

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
public data class RegionsResponse(
    @SerialName("groups")
    val groups: Map<String, List<ProtocolDetails>> = mutableMapOf(),
    @SerialName("regions")
    val regions: List<Region> = listOf()
) {
    @Serializable
    data class ProtocolDetails(
        @SerialName("name")
        val name: String = "",
        @SerialName("ports")
        val ports: List<Int> = listOf()
    )

    @Serializable
    data class Region(
        @SerialName("id")
        val id: String = "",
        @SerialName("name")
        val name: String = "",
        @SerialName("country")
        val country: String = "",
        @SerialName("dns")
        val dns: String = "",
        @SerialName("geo")
        val geo: Boolean = false,
        @SerialName("offline")
        val offline: Boolean = false,
        @SerialName("latitude")
        val latitude: String? = null,
        @SerialName("longitude")
        val longitude: String? = null,
        @SerialName("auto_region")
        val autoRegion: Boolean = false,
        @SerialName("port_forward")
        val portForward: Boolean = false,
        @SerialName("proxy")
        val proxy: List<String> = listOf(),
        @SerialName("servers")
        val servers: Map<String, List<ServerDetails>> = mapOf()
    ) {
        @Serializable
        data class ServerDetails(
            @SerialName("ip")
            val ip: String = "",
            @SerialName("cn")
            val cn: String = ""
        )
    }
}