package com.privateinternetaccess.account.internals.utils

object AccountUtils {
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