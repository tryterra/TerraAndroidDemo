package co.tryterra.terraandroiddemo

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import co.tryterra.terra.Connections
import co.tryterra.terra.Terra
import co.tryterra.terra.googlefit.GoogleFitRealtimeCategories
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.util.*


class MainActivity : AppCompatActivity() {
    private var terra: Terra? = null

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.O)

    private lateinit var activityDate: TextView
    private lateinit var dailyDate: TextView
    private lateinit var sleepDate: TextView
    private lateinit var bodyDate: TextView

    private lateinit var activityButton: Button
    private lateinit var sleepButton: Button
    private lateinit var dailyButton: Button
    private lateinit var bodyButton: Button
    private lateinit var athleteButton: Button

    private lateinit var disconnectButton: Button

    private var startTimeActivity: Long = LocalDate.now().atTime(LocalTime.now()).toEpochSecond(
        ZoneOffset.UTC) * 1000
    private var startTimeDaily: Long = LocalDate.now().atTime(LocalTime.now()).toEpochSecond(
        ZoneOffset.UTC) * 1000
    private var startTimeSleep: Long = LocalDate.now().atTime(LocalTime.now()).toEpochSecond(
        ZoneOffset.UTC) * 1000
    private var startTimeBody: Long = LocalDate.now().atTime(LocalTime.now()).toEpochSecond(
        ZoneOffset.UTC) * 1000




    @SuppressLint("SetTextI18n")
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView( R.layout.activity_main)
        try {
            terra = Terra(
                devId = devId,
                XAPIKey = XAPIKey,
                context = this,
                bodyTimer = 60 * 60 * 1000,
                dailyTimer = 60 * 60 * 1000,
                sleepTimer = 60 * 60 * 1000,
                nutritionTimer = 60 * 60 * 1000,
                activityTimer = 60 * 60 * 1000,
                referenceId = "testingRef",
            )
            Log.i(TAG, "${terra!!.referenceId}")
        }
        catch (exception: IllegalStateException){
            Log.e(TAG, "${exception.message}")
        }
        catch (exception: IllegalAccessError){
            Log.e(TAG, "${exception.message}")
        }

        activityDate = findViewById(R.id.activityDate)
        sleepDate = findViewById(R.id.sleepDate)
        dailyDate = findViewById(R.id.dailyDate)
        bodyDate = findViewById(R.id.bodyDate)

        activityDate.text = LocalDate.now().toString()
        sleepDate.text = LocalDate.now().toString()
        dailyDate.text = LocalDate.now().toString()
        bodyDate.text = LocalDate.now().toString()

        activityButton = findViewById(R.id.activity)
        dailyButton = findViewById(R.id.daily)
        sleepButton = findViewById(R.id.sleep)
        bodyButton = findViewById(R.id.body)
        athleteButton = findViewById(R.id.athlete)

        disconnectButton = findViewById(R.id.disconnect)

        disconnectButton.setOnClickListener {
            disconnectTerra(terra!!, REQUEST_RESOURCE)
        }

        startTimeActivity = LocalDate.parse(activityDate.text.toString()).atStartOfDay().toInstant(
            ZoneOffset.UTC).toEpochMilli()
        startTimeBody = LocalDate.parse(bodyDate.text.toString()).atTime(LocalTime.now()).toInstant(
            ZoneOffset.UTC).toEpochMilli()
        startTimeDaily = LocalDate.parse(dailyDate.text.toString()).atTime(LocalTime.now()).toInstant(
            ZoneOffset.UTC).toEpochMilli()
        startTimeSleep = LocalDate.parse(sleepDate.text.toString()).atTime(LocalTime.now()).toInstant(
            ZoneOffset.UTC).toEpochMilli()


        activityDate.setOnClickListener{
            val calendar: Calendar = Calendar.getInstance()
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val month = calendar.get(Calendar.MONTH)
            val year = calendar.get(Calendar.YEAR)
            val picker = DatePickerDialog(
                this@MainActivity,
                { _, yearDisplayed, monthOfYear, dayOfMonth ->
                    activityDate.text =
                        "$yearDisplayed-${(monthOfYear + 1).toString().padStart(2, '0')}-${dayOfMonth.toString().padStart(2, '0')}"
                    startTimeActivity = LocalDate.parse(activityDate.text.toString()).atTime(LocalTime.now()).toInstant(
                        ZoneOffset.UTC).toEpochMilli()
                    activityButton.setOnClickListener { requestActivityData(startTimeActivity- ONE_DAY, startTimeActivity, terra!!, REQUEST_RESOURCE) }

                },
                year,
                month,
                day
            )
            picker.show()
        }

        bodyDate.setOnClickListener{
            val calendar: Calendar = Calendar.getInstance()
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val month = calendar.get(Calendar.MONTH)
            val year = calendar.get(Calendar.YEAR)
            val picker = DatePickerDialog(
                this@MainActivity,
                { _, yearDisplayed, monthOfYear, dayOfMonth ->
                    bodyDate.text = "$yearDisplayed-${(monthOfYear + 1).toString().padStart(2, '0')}-${dayOfMonth.toString().padStart(2, '0')}"
                    startTimeBody =  LocalDate.parse(bodyDate.text.toString()).atTime(LocalTime.now()).toInstant(
                        ZoneOffset.UTC).toEpochMilli()
                    bodyButton.setOnClickListener{ requestBodyData(startTimeBody- ONE_DAY, startTimeBody, terra!!, REQUEST_RESOURCE) }
                },
                year,
                month,
                day
            )
            picker.show()
        }

        sleepDate.setOnClickListener{
            val calendar: Calendar = Calendar.getInstance()
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val month = calendar.get(Calendar.MONTH)
            val year = calendar.get(Calendar.YEAR)
            val picker = DatePickerDialog(
                this@MainActivity,
                { _, yearDisplayed, monthOfYear, dayOfMonth ->
                    sleepDate.text = "$yearDisplayed-${(monthOfYear + 1).toString().padStart(2, '0')}-${dayOfMonth.toString().padStart(2, '0')}"
                    startTimeSleep = LocalDate.parse(sleepDate.text.toString()).atTime(LocalTime.now()).toInstant(
                        ZoneOffset.UTC).toEpochMilli()
                    sleepButton.setOnClickListener{ requestBodyData(startTimeSleep- ONE_DAY, startTimeSleep, terra!!, REQUEST_RESOURCE) }
                },
                year,
                month,
                day
            )
            picker.show()
        }

        dailyDate.setOnClickListener{
            val calendar: Calendar = Calendar.getInstance()
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val month = calendar.get(Calendar.MONTH)
            val year = calendar.get(Calendar.YEAR)
            val picker = DatePickerDialog(
                this@MainActivity,
                { _, yearDisplayed, monthOfYear, dayOfMonth ->
                    dailyDate.text = "$yearDisplayed-${(monthOfYear + 1).toString().padStart(2, '0')}-${dayOfMonth.toString().padStart(2, '0')}"
                    startTimeDaily = LocalDate.parse(dailyDate.text.toString()).atTime(LocalTime.now()).toInstant(
                        ZoneOffset.UTC).toEpochMilli()
                    sleepButton.setOnClickListener{ requestBodyData(startTimeDaily- ONE_DAY, startTimeDaily, terra!!, REQUEST_RESOURCE) }
                },
                year,
                month,
                day
            )
            picker.show()
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
        if (item.itemId == R.id.connect_shealth) {
            terra!!.initConnection(connection = Connections.SAMSUNG, context = this)
            activityButton.setOnClickListener { requestActivityData(startTimeActivity - ONE_DAY, startTimeActivity, terra!!, Connections.SAMSUNG) }
            sleepButton.setOnClickListener { requestSleepData(startTimeSleep- ONE_DAY, startTimeSleep, terra!!, Connections.SAMSUNG) }
            dailyButton.setOnClickListener { requestDailyData(startTimeDaily- ONE_DAY, startTimeDaily, terra!!, Connections.SAMSUNG) }
            bodyButton.setOnClickListener { requestBodyData(startTimeBody- ONE_DAY, startTimeBody , terra!!, Connections.SAMSUNG) }
            athleteButton.setOnClickListener { requestAthleteData(terra!!, Connections.SAMSUNG) }
            disconnectButton.setOnClickListener { disconnectTerra(terra!!, Connections.SAMSUNG) }

        }

        if (item.itemId == R.id.connectGfit){
            terra!!.initConnection(Connections.GOOGLE_FIT, this)
            activityButton.setOnClickListener { requestActivityData(startTimeActivity- ONE_DAY, startTimeActivity , terra!!, Connections.GOOGLE_FIT) }
            sleepButton.setOnClickListener { requestSleepData(startTimeSleep- ONE_DAY, startTimeSleep , terra!!, Connections.GOOGLE_FIT) }
            dailyButton.setOnClickListener { requestDailyData(startTimeDaily- ONE_DAY, startTimeDaily , terra!!, Connections.GOOGLE_FIT) }
            bodyButton.setOnClickListener { requestBodyData(startTimeBody- ONE_DAY, startTimeBody , terra!!, Connections.GOOGLE_FIT) }
            athleteButton.setOnClickListener { requestAthleteData(terra!!, Connections.GOOGLE_FIT) }
            disconnectButton.setOnClickListener { disconnectTerra(terra!!, Connections.GOOGLE_FIT) }
        }

        if (item.itemId == R.id.connect_fsl){
            terra!!.initConnection(Connections.FREESTYLE_LIBRE, this, startIntent = "co.tryterra.terraandroiddemo.MainActivity")
        }
        if (item.itemId == R.id.startRealtime) {
            terra?.startRealtime(Connections.GOOGLE_FIT, setOf(GoogleFitRealtimeCategories.STEP_COUNT_DELTA))
        }

        if (item.itemId == R.id.stopRealtime) {
            terra?.stopRealtime(Connections.GOOGLE_FIT)
        }

        if (item.itemId == R.id.connectTerra){
            GenerateSessionID(XAPIKey, devId, "testing").getAuthUrl {
                val loginIntent = Intent(Intent.ACTION_VIEW, Uri.parse(it))
                startActivity(loginIntent)
            }

        }
        return true
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun requestActivityData(startTime: Long, endTime: Long, terra: Terra, resource: Connections) {
        terra.getActivity(
            resource,
            startDate = Date.from(Instant.ofEpochMilli(startTime)),
            endDate = Date.from(Instant.ofEpochMilli(endTime))){_, payload ->
            Log.i(TAG, "$payload")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun requestBodyData(startTime: Long, endTime: Long, terra: Terra, resource: Connections){
        terra.getBody(
            resource,
            startDate = Date.from(Instant.ofEpochMilli(startTime)),
            endDate = Date.from(Instant.ofEpochMilli(endTime))){_, payload ->
            Log.i(TAG, "$payload")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun requestDailyData(startTime: Long, endTime: Long, terra: Terra, resource: Connections) {
        terra.getDaily(
            resource,
            startDate = Date.from(Instant.ofEpochMilli(startTime)),
            endDate = Date.from(Instant.ofEpochMilli(endTime))){_, payload ->
            if (payload != null) {
                Log.i(TAG, "$payload")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun requestSleepData(startTime: Long, endTime: Long, terra: Terra, resource: Connections) {
        terra.getSleep(
            resource,
            startDate = Date.from(Instant.ofEpochMilli(startTime)),
            endDate = Date.from(Instant.ofEpochMilli(endTime))) {_, payload ->
            Log.i(TAG, "$payload")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun requestAthleteData(terra: Terra, resource: Connections) {
        terra.getAthlete(resource)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun disconnectTerra(terra: Terra, resource: Connections) {
        terra.disconnect(resource)
    }



    companion object{
        const val TAG = "Terra"
        const val ONE_DAY = 86400000
        val REQUEST_RESOURCE = Connections.SAMSUNG
    }
}