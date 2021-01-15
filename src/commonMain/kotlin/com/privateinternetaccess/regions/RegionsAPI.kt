package com.privateinternetaccess.regions

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

import com.privateinternetaccess.regions.internals.Regions
import com.privateinternetaccess.regions.model.RegionsResponse
import com.privateinternetaccess.regions.model.TranslationsGeoResponse


public enum class RegionsProtocol(val protocol: String) {
    OPENVPN_TCP("ovpntcp"),
    OPENVPN_UDP("ovpnudp"),
    WIREGUARD("wg"),
    META("meta")
}

/**
 * Const defining the timeout being used on the ping requests
 */
public const val REGIONS_PING_TIMEOUT: Int = 1500

/**
 * Interface defining the API to be offered by the common module.
 */
public interface RegionsAPI {

    /**
     * Fetch all regions information for next-gen.
     *
     * @param locale `String`. Regions locale. If unknown defaults to en-us.
     * @param callback `(response: RegionsResponse?, error: Error?)`. Invoked on the main thread.
     */
    fun fetchRegions(locale: String, callback: (response: RegionsResponse?, error: Error?) -> Unit)

    /**
     * Starts the process of ping requests and return the updated `ServerResponse` object as a
     * callback parameter.
     *
     * @param callback `(response: RegionsResponse?, error: Error?)`. Invoked on the main thread.
     */
    fun pingRequests(callback: (response: List<RegionLowerLatencyInformation>, error: Error?) -> Unit)
}

/**
 * Interface defining the client's data provider.
 */
public interface RegionClientStateProvider {

    /**
     * It returns the list of endpoints to try to reach when performing a request. Order is relevant.
     *
     * @return `List<RegionEndpoint>`
     */
    fun regionEndpoints(): List<RegionEndpoint>
}

/**
 * Builder class responsible for creating an instance of an object conforming to
 * the `RegionsAPI` interface.
 */
public class RegionsBuilder {
    private var clientStateProvider: RegionClientStateProvider? = null

    fun setClientStateProvider(clientStateProvider: RegionClientStateProvider): RegionsBuilder =
        apply { this.clientStateProvider = clientStateProvider }

    /**
     * @return `RegionsAPI` instance.
     */
    fun build(): RegionsAPI {
        val clientStateProvider = this.clientStateProvider
            ?: throw Exception("Client state provider missing.")
        return Regions(clientStateProvider)
    }
}

/**
 * Data class defining the response object for a ping request. @see `fun pingRequests(...)`.
 *
 * @param region `String`.
 * @param endpoint `String`.
 * @param latency `String`.
 */
public data class RegionLowerLatencyInformation(
    val region: String,
    val endpoint: String,
    val latency: Long
)

/**
 * Data class defining the endpoints data needed when performing a request on it.
 */
public data class RegionEndpoint(
    val endpoint: String,
    val isProxy: Boolean,
    val usePinnedCertificate: Boolean = false,
    val certificateCommonName: String? = null
)