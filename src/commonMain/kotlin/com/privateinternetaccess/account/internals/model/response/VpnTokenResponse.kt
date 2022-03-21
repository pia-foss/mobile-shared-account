package com.privateinternetaccess.account.internals.model.response

/*
 *  Copyright (c) 2021 Private Internet Access, Inc.
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
internal data class VpnTokenResponse(
    @SerialName("vpn_secret1")
    val vpnUsernameToken: String,

    @SerialName("vpn_secret2")
    val vpnPasswordToken: String,

    /**
     * ISO 8601 string representation.
     */
    @SerialName("expires_at")
    val expiresAt: String
)
