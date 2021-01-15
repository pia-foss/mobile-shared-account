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

import com.privateinternetaccess.regions.model.RegionsResponse
import kotlinx.serialization.json.Json


object RegionsUtils {

    fun stringify(regionsResponse: RegionsResponse) =
        Json.encodeToString(RegionsResponse.serializer(), regionsResponse)

    fun parse(regionsResponseString: String) =
        Json { ignoreUnknownKeys = true }.decodeFromString(RegionsResponse.serializer(), regionsResponseString)

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
}
