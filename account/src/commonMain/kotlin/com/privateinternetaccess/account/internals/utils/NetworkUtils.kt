package com.privateinternetaccess.account.internals.utils

import com.privateinternetaccess.account.AccountRequestError
import io.ktor.client.statement.*

object NetworkUtils {

    internal fun HttpResponse.mapStatusCodeToAccountError(): AccountRequestError =
        getRetryAfterHeaderValue()?.let { retryAfterSeconds ->
            if (status.value == 429) {
                AccountRequestError(status.value, status.description, retryAfterSeconds = retryAfterSeconds)
            }
            else {
                null
            }
        } ?: AccountRequestError(status.value, status.description)

    private fun HttpResponse.getRetryAfterHeaderValue(): Long? =
        headers["Retry-After"].toString().toLongOrNull()
}

