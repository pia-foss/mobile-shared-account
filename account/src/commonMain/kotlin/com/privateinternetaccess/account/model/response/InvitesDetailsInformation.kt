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
data class InvitesDetailsInformation(
    @SerialName("invites")
    val invites: List<Invite>,
    @SerialName("total_free_days_given")
    val totalFreeDaysGiven: Int,
    @SerialName("total_invites_rewarded")
    val totalInvitesRewarded: Int,
    @SerialName("total_invites_sent")
    val totalInvitesSent: Int,
    @SerialName("unique_referral_link")
    val uniqueReferralLink: String
) {
    @Serializable
    data class Invite(
        @SerialName("accepted")
        val accepted: Boolean,
        @SerialName("grace_period_remaining")
        val gracePeriodRemaining: String,
        @SerialName("obfuscated_email")
        val obfuscatedEmail: String,
        @SerialName("rewarded")
        val rewarded: Boolean
    )
}