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

package com.privateinternetaccess.account.internals.utils

import com.privateinternetaccess.account.internals.Account


object AccountUtils {

    private val DOMAIN_REGEX = Regex("^((?!-)[A-Za-z0-9-]{1,63}(?<!-)\\.)+[A-Za-z]{2,6}$")
    private val IPV4_REGEX = Regex("^(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(\\.(?!$)|$)){4}$")
    private const val STAGING_DOMAIN = "staging"
    private const val SCHEME = "https"

    internal fun isErrorStatusCode(code: Int): Boolean {
        when (code) {
            in 300..399 ->
                // Redirect response
                return true
            in 400..499 ->
                // Client error response
                return true
            in 500..599 ->
                // Server error response
                return true
        }

        if (code >= 600) {
            // Unknown error response
            return true
        }
        return false
    }

    internal fun prepareRequestUrl(ipOrRootDomain: String, path: Account.Path): String? {
        // If it's not recognized as ip or domain. Return.
        if (!ipOrRootDomain.matches(DOMAIN_REGEX) && !ipOrRootDomain.matches(IPV4_REGEX)) {
            return null
        }

        // If it's a domain but there is no subdomain definition. Return.
        if (ipOrRootDomain.matches(DOMAIN_REGEX) && !containsSubdomainForPath(path)) {
            return null
        }

        return when {
            // Order matters. The staging check needs to be prior the domain check as staging is technically a domain.
            // But, we don't want to apply the subdomain to it and rather use it as it is.
            ipOrRootDomain.contains(STAGING_DOMAIN) ||
            ipOrRootDomain.matches(IPV4_REGEX) -> {
                "$SCHEME://$ipOrRootDomain${path.url}"
            }
            ipOrRootDomain.matches(DOMAIN_REGEX) -> {
                val subdomain = Account.SUBDOMAINS.getValue(path)
                "$SCHEME://$subdomain.$ipOrRootDomain${path.url}"
            }
            else -> null
        }
    }

    // region private
    private fun containsSubdomainForPath(path: Account.Path) =
        Account.SUBDOMAINS.containsKey(path)
    // endregion
}