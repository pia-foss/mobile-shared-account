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