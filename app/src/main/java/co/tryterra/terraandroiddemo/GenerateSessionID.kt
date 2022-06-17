package co.tryterra.terraandroiddemo

import android.util.Log
import android.annotation.SuppressLint
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import com.google.gson.Gson
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

data class GenerateSessionIDPayload(
    val referenceId: String,
//    val providers: String, //Add this if you want to filter specific providers
    val language: String,
    val auth_success_redirect_url: String,
)

class GenerateSessionID (
    private val XApiKey: String,
    private val devId: String,
    private val referenceId: String,
): CoroutineScope {

    private val job = Job()
    private val singleThreadExecutor = Executors.newSingleThreadExecutor()
    override val coroutineContext: CoroutineContext
        get() = job + singleThreadExecutor.asCoroutineDispatcher()

    var loginUrl: String? = null

    val gson: Gson = Gson()
    @SuppressLint("HardwareIds")

    fun stop(){
        job.cancel()
        singleThreadExecutor.shutdown()
    }

    private fun generateSessionIdPayload(): GenerateSessionIDPayload{
        return GenerateSessionIDPayload(this.referenceId,"es","https://tryterra.co")
    }

    @Throws(IOException::class)
    fun generateToken(
        XApiKey: String = this.XApiKey,
        devId: String = this.devId,
        referenceId: String = this.referenceId
    ) = launch {
        val serverURL = "https://api.tryterra.co/v2/auth/generateWidgetSession"
        val url = URL(serverURL)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.doOutput = true
        connection.doInput = true

        connection.setRequestProperty("dev-id", devId)
        connection.setRequestProperty("X-API-Key", XApiKey)
        connection.setRequestProperty("Accept", "application/json")
        connection.setRequestProperty("Connection", "keep-alive")
        connection.setRequestProperty("Content-Type", "application/json")


        val outputStream: OutputStream = connection.outputStream
        val outputStreamWriter: OutputStreamWriter = OutputStreamWriter(outputStream, "UTF-8")
        outputStreamWriter.write(gson.toJson(generateSessionIdPayload()))
        outputStreamWriter.flush()
        outputStreamWriter.close()
        outputStream.close()


        if (connection.responseCode == HttpURLConnection.HTTP_CREATED) {
            try {
                val inputStream = DataInputStream(connection.inputStream)
                val reader = BufferedReader(InputStreamReader(inputStream))
                val output: String = reader.readLine()
                val outputJson = JSONObject(output)
                loginUrl = outputJson["url"].toString()
                Log.i(TAG, "here: $loginUrl")
                connection.disconnect()
            } catch (exception: Exception) {
                Log.e(TAG, "There was error reading connection msg")
                throw Exception("$exception")
            }
        }

        if (connection.responseCode != HttpURLConnection.HTTP_CREATED) {
            try {
                val inputStream = DataInputStream(connection.inputStream)
                val reader = BufferedReader(InputStreamReader(inputStream))
                val output: String = reader.readLine()
                Log.i(TAG, output)
                connection.disconnect()

            } catch (exception: Exception) {
                Log.e(TAG, "There was error with the request")
                throw Exception("Failed: $exception with error code: ${connection.responseCode}")
            }
        }
        connection.disconnect()
    }

    fun getAuthUrl(callback: (String?) -> Unit){
        generateToken().invokeOnCompletion {
            stop()
            callback(this.loginUrl)
        }
    }


    companion object {
        private const val TAG = "SessionIDGenerator"
    }
}

