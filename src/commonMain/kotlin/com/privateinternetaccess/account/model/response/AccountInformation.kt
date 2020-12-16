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
data class AccountInformation(
    @SerialName("active")
    val active: Boolean,
    @SerialName("can_invite")
    val canInvite: Boolean,
    @SerialName("canceled")
    val canceled: Boolean,
    @SerialName("days_remaining")
    val daysRemaining: Int,
    @SerialName("email")
    val email: String,
    @SerialName("expiration_time")
    val expirationTime: Int,
    @SerialName("expire_alert")
    val expireAlert: Boolean,
    @SerialName("expired")
    val expired: Boolean,
    @SerialName("needs_payment")
    val needsPayment: Boolean,
    @SerialName("plan")
    val plan: String,
    @SerialName("product_id")
    val productId: String?,
    @SerialName("recurring")
    val recurring: Boolean,
    @SerialName("renew_url")
    val renewUrl: String,
    @SerialName("renewable")
    val renewable: Boolean,
    @SerialName("username")
    val username: String
)