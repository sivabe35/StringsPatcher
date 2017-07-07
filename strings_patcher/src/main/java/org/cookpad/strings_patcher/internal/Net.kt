/*
 * Copyright 2017 Cookpad Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cookpad.strings_patcher.internal

import android.net.Uri
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.BufferedWriter
import java.io.IOException
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL


internal fun jsonFromGETRequest(url: String): JSONObject {
    var urlConnection: HttpURLConnection? = null
    try {
        urlConnection = URL(url).openConnection() as HttpURLConnection
        val input = BufferedInputStream(urlConnection.inputStream)
        val jsonString = input.bufferedReader().use { it.readText() }
        return JSONObject(jsonString)
    } catch (e: IOException) {
        throw e
    } finally {
        urlConnection?.disconnect()
    }
}

internal fun jsonFromPOSTRequest(url: String, params: Uri.Builder): JSONObject {
    var urlConnection: HttpURLConnection? = null
    try {
        urlConnection = URL(url).openConnection() as HttpURLConnection
        urlConnection.readTimeout = 10000
        urlConnection.connectTimeout = 15000
        urlConnection.requestMethod = "POST"
        urlConnection.doInput = true
        urlConnection.doOutput = true

        val query = params.build().encodedQuery

        val os = urlConnection.outputStream
        val writer = BufferedWriter(OutputStreamWriter(os, "UTF-8"))
        writer.write(query)
        writer.flush()
        writer.close()
        os.close()

        urlConnection.connect()

        if (urlConnection.responseCode != 200) {
            val inputError = BufferedInputStream(urlConnection.errorStream)
            val string = inputError.bufferedReader().use { it.readText() }
            throw RuntimeException(string)
        }

        val input = BufferedInputStream(urlConnection.inputStream)
        val jsonString = input.bufferedReader().use { it.readText() }
        return JSONObject(jsonString)
    } catch (e: IOException) {
        throw e
    } finally {
        urlConnection?.disconnect()
    }
}