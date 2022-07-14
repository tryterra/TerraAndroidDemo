package co.tryterra.terraandroiddemo

import android.util.Log
import co.tryterra.terra.BASE_URL
import co.tryterra.terra.fsl.TerraConnectionFSL
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

class GenerateAuthToken (
    private val devId: String,
    private val xAPIKey: String
): CoroutineScope{

    private val job = Job()
    private val singleThreadExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    private var token: String = ""
    override val coroutineContext: CoroutineContext
        get() = job + singleThreadExecutor.asCoroutineDispatcher()

    private fun generateAuthToken() = launch {

        val serverURL = "$BASE_URL/auth/generateAuthToken"
        val url = URL(serverURL)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.doOutput = true
        connection.doInput = true

        connection.setRequestProperty("dev-id", devId)
        connection.setRequestProperty("x-api-key", xAPIKey)
        connection.setRequestProperty("Content-Type", "application/json; utf-8")
        connection.setRequestProperty("Accept", "*/*")
        connection.setRequestProperty("Connection", "keep-alive")

        Log.i(TerraConnectionFSL.TAG, "Connecting to Terra")
        if (connection.responseCode == HttpURLConnection.HTTP_OK) {
            try {
                val inputStream = DataInputStream(connection.inputStream)
                val reader = BufferedReader(InputStreamReader(inputStream))
                val output: String = reader.readLine()
                val outputJson = JSONObject(output)
                token = outputJson["token"].toString()
                connection.disconnect()
            } catch (exception: Exception) {
                Log.e(TerraConnectionFSL.TAG, "There was error reading connection msg")
                throw IOException("$exception")
            }
        }

        if (connection.responseCode != HttpURLConnection.HTTP_OK) {
            try {
                val inputStream = DataInputStream(connection.inputStream)
                val reader = BufferedReader(InputStreamReader(inputStream))
                val output: String = reader.readLine()

                Log.i(TerraConnectionFSL.TAG, output)
                connection.disconnect()

            } catch (exception: Exception) {
                Log.e(TerraConnectionFSL.TAG, "There was error with the request")
                throw IOException("Failed: $exception with error code: ${connection.responseCode}")
            }
        }
        connection.disconnect()
    }


    fun getAuthToken(callback: (String) -> Unit){
        this.generateAuthToken().invokeOnCompletion {
            callback(this.token)
        }
    }

}