package co.tryterra.terrasamsungdemo

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import co.tryterra.terra.HealthStoreManager
import co.tryterra.terra.Terra
import com.google.android.libraries.healthdata.HealthDataClient
import com.google.android.libraries.healthdata.HealthDataService
import java.time.Instant
import java.time.LocalDate
import co.tryterra.terrasamsungdemo.databinding.ActivityMainBinding
import java.util.*
import java.time.ZoneOffset
import androidx.browser.customtabs.*


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val healthDataStore: HealthDataClient by lazy {HealthDataService.getClient(this)}
    private lateinit var store: HealthStoreManager
    private lateinit var terra: Terra
    private var mCustomTabsOpened = false

    @RequiresApi(Build.VERSION_CODES.O)
    public override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        store = HealthStoreManager(healthDataStore)
        terra = Terra("YOUR_DEV_ID", "YOUR_X_API_KEY", this, store)
        binding.startDateEdit.setText(LocalDate.now().toString())
        binding.endDateEdit.setText(LocalDate.now().toString())

        val startTime = LocalDate.parse(binding.startDateEdit.text.toString()).atStartOfDay().toInstant(
            ZoneOffset.UTC).toEpochMilli()
        val endTime = LocalDate.parse(binding.endDateEdit.text.toString()).atStartOfDay().toInstant(
            ZoneOffset.UTC).toEpochMilli()

        binding.activity.setOnClickListener{requestActivityData(startTime, endTime, terra)}
        binding.sleep.setOnClickListener{requestSleepData(startTime, endTime, terra)}
        binding.daily.setOnClickListener{requestDailyData(startTime, endTime, terra)}
        binding.body.setOnClickListener{requestBodyData(startTime, endTime, terra)}
        binding.athlete.setOnClickListener{requestAthleteData(terra)}

        binding.startDateEdit.setOnClickListener {
            val calendar: Calendar = Calendar.getInstance()
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val month = calendar.get(Calendar.MONTH)
            val year = calendar.get(Calendar.YEAR)
            val picker = DatePickerDialog(
                this@MainActivity,
                { _, yearDisplayed, monthOfYear, dayOfMonth ->
                    binding.startDateEdit.setText("$yearDisplayed-${monthOfYear + 1}-${dayOfMonth.toString().padStart(2, '0')}")
                },
                year,
                month,
                day
            )
            picker.show()
        }
        binding.endDateEdit.setOnClickListener {
            val calendar: Calendar = Calendar.getInstance()
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val month = calendar.get(Calendar.MONTH)
            val year = calendar.get(Calendar.YEAR)
            val picker = DatePickerDialog(
                this@MainActivity,
                { _, yearDisplayed, monthOfYear, dayOfMonth ->
                    binding.endDateEdit.setText("$yearDisplayed-${monthOfYear + 1}-${dayOfMonth.toString().padStart(2, '0')}")
                },
                year,
                month,
                day
            )
            picker.show()
        }
        binding.submitDate.setOnClickListener {
            updateOnClickListeners()
        }
    }


    public override fun onDestroy() {
        super.onDestroy()
    }

    public override fun onResume(){
        super.onResume()

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.connect) {
            store.requestPermission()
        }

        //Still not added to Widget
        if (item.itemId == R.id.connectTerra){
            loadCustomTab()
        }
        return true
    }

    @SuppressLint("RestrictedApi")
    private fun loadCustomTab(){
        val sendLinkIntent = Intent(this, CustomTabsReceiver::class.java)
        val customTabBuilder = CustomTabsIntent.Builder()
//        val pendingIntent = PendingIntent.getBroadcast(this, 0, sendLinkIntent, PendingIntent.FLAG_UPDATE_CURRENT)
//        AppCompatResources.getDrawable(this, R.drawable.cancel)?.let {
//            DrawableCompat.setTint(it, Color.WHITE)
//            customTabBuilder.setActionButton(it.toBitmap(),"Add this link to your dig",pendingIntent,false)
//        }
        val customTab = customTabBuilder.build()
        val sessionGenerator = GenerateSession("YOUR_X_API_KEY", "YOUR_DEV_ID", "https://tryterra.co", "https://google.com")
        sessionGenerator.generateSession().invokeOnCompletion {
            sessionGenerator.stop()
            customTab.launchUrl(this, Uri.parse(sessionGenerator.sessionurl))
            while (!customTab.intent.dataString?.contains("https://tryterra.co")!!){
                Log.i(TAG, "${customTab.intent.dataString}")
            }
            mCustomTabsOpened = true
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun requestActivityData(startTime: Long, endTime: Long, terra: Terra) {
        terra.getActivity(
            startDate = Date.from(Instant.ofEpochMilli(startTime)),
            endDate = Date.from(Instant.ofEpochMilli(endTime)))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun requestBodyData(startTime: Long, endTime: Long, terra: Terra){
        terra.getBody(
            startDate = Date.from(Instant.ofEpochMilli(startTime)),
            endDate = Date.from(Instant.ofEpochMilli(endTime)))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun requestDailyData(startTime: Long, endTime: Long, terra: Terra) {
        terra.getDaily(
            startDate = Date.from(Instant.ofEpochMilli(startTime)),
            endDate = Date.from(Instant.ofEpochMilli(endTime)))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun requestSleepData(startTime: Long, endTime: Long, terra: Terra) {
        terra.getSleep(
            startDate = Date.from(Instant.ofEpochMilli(startTime)),
            endDate = Date.from(Instant.ofEpochMilli(endTime)))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun requestAthleteData(terra: Terra) {
        terra.getAthlete()
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateOnClickListeners(){
        val startTime = LocalDate.parse(binding.startDateEdit.text.toString()).atStartOfDay().toInstant(
            ZoneOffset.UTC).toEpochMilli()
        val endTime = LocalDate.parse(binding.endDateEdit.text.toString()).atStartOfDay().toInstant(
            ZoneOffset.UTC).toEpochMilli()
        binding.activity.setOnClickListener{requestActivityData(startTime, endTime, terra)}
        binding.sleep.setOnClickListener{requestSleepData(startTime, endTime, terra)}
        binding.daily.setOnClickListener{requestDailyData(startTime, endTime, terra)}
        binding.body.setOnClickListener{requestBodyData(startTime, endTime, terra)}
        Log.i(TAG, "Resetting On-clicks")
    }
    companion object{
        const val TAG = "Terra"

    }
}