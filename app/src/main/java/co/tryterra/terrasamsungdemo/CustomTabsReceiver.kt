package co.tryterra.terrasamsungdemo
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log

class CustomTabsReceiver(): BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val uri: Uri? = intent.data
        if (uri != null){
            Log.i(TAG, uri.toString())
        }
    }

    companion object {
        const val TAG = "IntentReceiver"
    }

}