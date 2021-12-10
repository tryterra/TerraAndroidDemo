package co.tryterra.terrasamsungdemo

import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

class GenerateSession (
    private val XApiKey: String,
    private val devId: String,
    private val authSuccessUrl: String,
    private val authFailUrl: String,
): CoroutineScope {
    private val gson = Gson()
    private val job = Job()
    private val singleThreadExecutor = Executors.newSingleThreadExecutor()
    override val coroutineContext: CoroutineContext
        get() = job + singleThreadExecutor.asCoroutineDispatcher()
    var sessionurl = ""

    fun stop(){
        job.cancel()
        singleThreadExecutor.shutdown()
        Log.i(TAG,"Session Generated")
    }

    fun generateSession(XApiKey: String = this.XApiKey, devId: String = this.devId) = launch {
        val serverURL = "https://api.tryterra.co/v2/auth/generateWidgetSession"
        val url = URL(serverURL)

        val postData = """{"providers": "GOOGLE, GARMIN, FITBIT", "auth_success_redirect_url": "$authSuccessUrl","auth_failure_redirect_url": "$authFailUrl","language": "EN"}""".toByteArray(StandardCharsets.UTF_8)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.doOutput = true
        connection.doInput = true

        connection.setRequestProperty("dev-id", devId)
        connection.setRequestProperty("X-API-Key", XApiKey)
        connection.setRequestProperty("Content-Type", "application/json; utf-8")
        connection.setRequestProperty("Accept", "application/json")

        try {
            val outputStream = DataOutputStream(connection.outputStream)
            outputStream.write(postData)
            outputStream.flush()
            outputStream.close()
        } catch (exception: Exception) {
            connection.disconnect()
//             throw Exception("Error occured Output Stream, ${exception.message}")
        }
        if (connection.responseCode == HttpURLConnection.HTTP_OK) {
            try {
                val inputStream = DataInputStream(connection.inputStream)
                val reader = BufferedReader(InputStreamReader(inputStream))
                val output: String = reader.readLine()
                Log.i(TAG, output)
                val outputJson = JSONObject(output)
                sessionurl = outputJson["url"].toString()
                connection.disconnect()
            } catch (exception: Exception) {
                Log.i(TAG, "There was error reading connection msg")
                throw Exception("Exception while push the notification  $exception.message")
            }
        }
        connection.disconnect()
    }


    companion object {
        const val TAG = "SessionGenerator"
    }
}