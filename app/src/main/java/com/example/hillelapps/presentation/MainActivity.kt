/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.example.hillelapps.presentation



import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.Dialog
import android.app.PendingIntent
import android.os.Bundle
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.hillelapps.R
import android.view.animation.AnimationUtils
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentActivity
import androidx.room.Entity
import com.google.firebase.Firebase
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.database
import java.util.Calendar
import androidx.room.PrimaryKey
import androidx.room.*
import androidx.wear.widget.WearableLinearLayoutManager
import androidx.wear.widget.WearableRecyclerView
import androidx.work.WorkManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import androidx.work.OneTimeWorkRequestBuilder
import com.google.firebase.analytics.FirebaseAnalytics



class MainActivity : FragmentActivity() {


     private lateinit var countdownText: TextView
     private lateinit var countDownTimer: CountDownTimer
     private lateinit var mediaPlayer: MediaPlayer
     private lateinit var vibrator: Vibrator
     private lateinit var confirmButton: Button
     private lateinit var startButton: Button
     private lateinit var finishButton: Button
     private lateinit var snoozeButton: Button
     private var minutesRepousser: Int = 15
     private var minutesReplay: Int = 180
     private var snoozeCount = 0
     private lateinit var database: FirebaseDatabase
    private var lastCheckedMinute = -1
    private var lasthour = -1
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var stopAlarmHandler: Handler
    private lateinit var stopAlarmRunnable: Runnable
    private lateinit var confirmButtonInChrono: Button
    private lateinit var adjustTimeButton: Button
    private var currentCountdownMinutes: Int = 180
    private var intervalRep: Int = 180



    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
     @SuppressLint("ClickableViewAccessibility")
     override fun onCreate(savedInstanceState: Bundle?) {
         super.onCreate(savedInstanceState)
         setContentView(R.layout.activity_main)

         // Initialiser Firebase Analytics
         firebaseAnalytics = FirebaseAnalytics.getInstance(this)

         // Initialiser le Handler et le Runnable
         stopAlarmHandler = Handler(Looper.getMainLooper())
         stopAlarmRunnable = Runnable {
             stopAlarm()
         }
         try {
             database = Firebase.database
             val buttonAnimation = AnimationUtils.loadAnimation(this, R.anim.button_press)

             countdownText = findViewById(R.id.countdownText)
             confirmButton = findViewById(R.id.confirmButton)
             snoozeButton = findViewById(R.id.snoozeButton)
             finishButton = findViewById(R.id.finishButton)
             startButton = findViewById(R.id.startButton)
             confirmButtonInChrono = findViewById(R.id.confirmButtonInChrono)
             adjustTimeButton = findViewById(R.id.adjustTimeButton)


            mediaPlayer =
                 MediaPlayer.create(this, Settings.System.DEFAULT_ALARM_ALERT_URI)
             vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                 val vibratorManager =
                     getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
                 vibratorManager.defaultVibrator
             } else {
                 @Suppress("DEPRECATION")
                 getSystemService(VIBRATOR_SERVICE) as Vibrator
             }

             //  la sonnerie fonctionne même si l'écran est éteint,
             window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

             WindowCompat.setDecorFitsSystemWindows(window, false)
             WindowInsetsControllerCompat(window, window.decorView).let { controller ->
                 controller.hide(WindowInsetsCompat.Type.systemBars())
                 controller.systemBarsBehavior =
                     WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
             }
             //reste reveille
             window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

             adjustTimeButton.setOnClickListener {
                 countDownTimer.cancel()
                 showSnoozeDialog(currentCountdownMinutes,true)
             }
             startButton.setOnTouchListener { v, event ->
                 when (event.action) {
                     MotionEvent.ACTION_DOWN -> v.startAnimation(buttonAnimation)
                     MotionEvent.ACTION_UP -> v.clearAnimation()
                 }
                 false
             }
             startButton.setOnClickListener {
                 Log.d("MainActivity11", "Bouton Start cliqué")
                 try{
                     mediaPlayer.stop()
                     mediaPlayer.prepare()
                     startButton.visibility = View.GONE
                     countdownText.visibility = View.VISIBLE
                     startCountdown(minutesReplay)
                     Log.i("MainActivity11", "On startButton affiché avec succès")
                 }catch (e: Exception) {
                     Log.e("MainActivity11", "Une startButton inattendue s'est produite", e)}
             }
             confirmButtonInChrono.setOnClickListener {
                 countDownTimer.cancel()
                 snoozeCount = 0  // Réinitialiser le compteur quand l'alarme est confirmée
                 updateSnoozeButton()
                 saveConfirmationTime()
                 startCountdown(minutesReplay)

             }
             confirmButton.setOnClickListener {
                 stopAlarm()
                 resetUI()
                 snoozeCount = 0  // Réinitialiser le compteur quand l'alarme est confirmée
                 updateSnoozeButton()
                 saveConfirmationTime()
                 startCountdown(minutesReplay)

             }
             snoozeButton.setOnClickListener {
                 if (snoozeCount == 0) {
                     showSnoozeDialog(intervalRep,false)
                 } else {
                     snoozeAlarm(minutesRepousser,false)
                 }
             }


             Log.i("MainActivity11", "On create affiché avec succès")
         }catch (e: Exception) {
             Log.e("MainActivity11", "Une onCreate inattendue s'est produite", e)
         }
     }

    private fun startCountdown(minutes: Int) {
        val milliseconds = minutes * 60 * 1000L

        countdownText.visibility = View.VISIBLE
        confirmButtonInChrono.visibility =View.VISIBLE
        adjustTimeButton.visibility =View.VISIBLE

        countDownTimer = object : CountDownTimer(milliseconds, 1000) { // Changé à 1000ms (1 seconde)
            @SuppressLint("DefaultLocale")
            override fun onTick(millisUntilFinished: Long) {
                val minuts = millisUntilFinished / 60000
                val seconds = (millisUntilFinished % 60000) / 1000
                val timeLeft = String.format("%02d:%02d", minuts, seconds)

                val calendar = Calendar.getInstance()
                val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
                val minut = calendar.get(Calendar.MINUTE)

                runOnUiThread {
                    // Vérifier seulement si la minute a changé
                    if (minut != lastCheckedMinute) {
                        lastCheckedMinute = minut
                        //Log.i("MainActivity11", "Heure actuelle : $hourOfDay:$minut")


                        if (hourOfDay >0  && hourOfDay <20 && finishButton.visibility == View.VISIBLE) {
                            //Log.i("MainActivity12", "startButton affiché avec succès")
                            cancel()
                            finishButton.visibility = View.GONE
                            startButton.visibility = View.VISIBLE
                            countdownText.visibility = View.GONE
                            confirmButtonInChrono.visibility =View.GONE
                            adjustTimeButton.visibility =View.GONE

                        } else if (hourOfDay > 19 && lasthour== -1) {

                            //Log.i("MainActivity111", "finishButton affiché avec succès")
                            cancel()
                            finishButton.visibility = View.VISIBLE
                            countdownText.visibility = View.GONE
                            confirmButtonInChrono.visibility =View.GONE
                            adjustTimeButton.visibility =View.GONE
                            startCountdown(4000)
                            return@runOnUiThread // Sortir après avoir démarré un nouveau compte à rebours
                        }
                    }

                    countdownText.text = timeLeft
                }
            }

            override fun onFinish() {
                runOnUiThread {
                    if(finishButton.visibility != View.VISIBLE) {
                        playAlarm()
                    }
                }
            }
        }.start()
    }
    private fun playAlarm() {
        mediaPlayer.isLooping = true
        mediaPlayer.start()

        // Ajout de vibration
        if (vibrator.hasVibrator()) {
            val vibrationPattern = longArrayOf(0, 500, 500)
            vibrator.vibrate(VibrationEffect.createWaveform(vibrationPattern, 0))
        }
        // Arrêter l'alarme après 2 minutes
        stopAlarmHandler.postDelayed(stopAlarmRunnable, 120000)

        runOnUiThread {
            if (snoozeCount < 2) {
                confirmButton.visibility = View.VISIBLE
                snoozeButton.visibility = View.VISIBLE
                snoozeButton.isEnabled = true
                snoozeButton.alpha = 1f
                countdownText.visibility = View.GONE
                confirmButtonInChrono.visibility =View.GONE
                adjustTimeButton.visibility =View.GONE
                val blinkAnimation = AnimationUtils.loadAnimation(this@MainActivity, R.anim.blink)
                confirmButton.startAnimation(blinkAnimation)
                snoozeButton.startAnimation(blinkAnimation)
                updateSnoozeButton()
            }
            else{
                confirmButton.visibility = View.VISIBLE
                snoozeButton.visibility = View.GONE
                snoozeButton.isEnabled = true
                snoozeButton.alpha = 1f
                countdownText.visibility = View.GONE
                confirmButtonInChrono.visibility =View.GONE
                adjustTimeButton.visibility =View.GONE
                updateSnoozeButton()
            }

        }
    }
    private fun stopAlarm() {
        mediaPlayer.stop()
        mediaPlayer.prepare()
        vibrator.cancel()
        confirmButton.clearAnimation()
        snoozeButton.clearAnimation()
        countdownText.visibility = View.VISIBLE
        confirmButtonInChrono.visibility =View.VISIBLE
        adjustTimeButton.visibility =View.VISIBLE
        stopAlarmHandler.removeCallbacks(stopAlarmRunnable)
        //startCountdown()
    }
    private fun snoozeAlarm(minutes: Int, isAdjustTime: Boolean ) {
        if (snoozeCount < 2) {
            stopAlarm()
            if (!isAdjustTime ) {
                snoozeCount++
            }
            startCountdown(minutes)
            updateSnoozeButton()
            confirmButton.visibility = View.GONE
            snoozeButton.visibility = View.GONE
            countdownText.visibility = View.VISIBLE
            confirmButtonInChrono.visibility =View.VISIBLE
            adjustTimeButton.visibility =View.VISIBLE
        } else {
            snoozeButton.isEnabled = false
            snoozeButton.alpha = 0.5f
        }
    }
    private fun updateSnoozeButton() {
        Log.d("MainActivity11", "211")
        snoozeButton.text = when (snoozeCount) {
            0 -> "0/2"
            1 -> "1/2"
            else -> "2/2"
        }
        Log.d("MainActivity11", "21")
    }
    private fun resetUI() {
        countdownText.visibility = View.VISIBLE
        confirmButtonInChrono.visibility =View.VISIBLE
        adjustTimeButton.visibility =View.VISIBLE
        confirmButton.visibility = View.GONE
        snoozeButton.visibility = View.GONE
        Log.d("MainActivity11", "21")
        snoozeButton.isEnabled = true  // Réactiver le bouton de report
        snoozeButton.alpha = 1f  // Restaurer l'opacité normale
        Log.d("MainActivity11", "22")
        updateSnoozeButton()
    }
     override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
        vibrator.cancel()

    }
     @SuppressLint("SuspiciousIndentation")
     private fun saveConfirmationTime() {

             val currentTime = System.currentTimeMillis()
             val confirmation = Confirmation(timestamp = currentTime)
         Log.i("MainActivity11", "AppDatabase affiché avec succès $confirmation")
                 CoroutineScope(Dispatchers.IO).launch {
                     try {
                         AppDatabase.getDatabase(applicationContext).confirmationDao()
                             .insert(confirmation)
                     Log.i("MainActivity11", "AppDatabase affiché avec succès $confirmation")
                 }catch (e: Exception) {
             Log.e("MainActivity11", "Une AppDatabase inattendue s'est produite", e)
         }

                     // Sauvegarde locale

                     try {
                         // Tentative de sauvegarde Firebase
                         val reference = database.getReference("confirmations")
                         reference.push().setValue(currentTime)
                             .addOnSuccessListener {
                                 Log.d("Firebase", "Confirmation time saved successfully")
                                 scheduleAlarm(currentTime)
                             }
                             .addOnFailureListener { e ->
                                 Log.e("Firebase", "Error saving confirmation time", e)
                                 // Planifier une synchronisation différée
                                 val syncWork = OneTimeWorkRequestBuilder<SyncWorker>().build()
                                 WorkManager.getInstance(applicationContext).enqueue(syncWork)
                             }
                         Log.i("MainActivity11", "getReference affiché avec succès")
                     } catch (e: Exception) {
                         Log.e("MainActivity11", "Une getReference inattendue s'est produite", e)
                     }

                 }


     }
    @Entity(tableName = "confirmations")
    data class Confirmation(
        @PrimaryKey(autoGenerate = true) val id: Int = 0,
        val timestamp: Long,
        var isSynced: Boolean = false
    )
    @Dao
    interface ConfirmationDao {
        @Insert
        suspend fun insert(confirmation: Confirmation)

        @Query("SELECT * FROM confirmations WHERE isSynced = 0")
        suspend fun getUnsyncedConfirmations(): List<Confirmation>

        @Query("UPDATE confirmations SET isSynced = 1 WHERE id = :id")
        suspend fun markAsSynced(id: Int)
    }
    @SuppressLint("ScheduleExactAlarm")
    private fun scheduleAlarm(triggerAtMillis: Long) {
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
    }
    private fun showSnoozeDialog(minutes: Int,isAdjustTime: Boolean) {
        val dialog = Dialog(this, android.R.style.Theme_DeviceDefault_Dialog_NoActionBar)
        dialog.setContentView(R.layout.dialog_minute_picker)

        val recyclerView = dialog.findViewById<WearableRecyclerView>(R.id.wearable_recycler_view)
        val minut = (0..minutes).toList()

        recyclerView.apply {
            layoutManager = WearableLinearLayoutManager(this@MainActivity)
            isEdgeItemsCenteringEnabled = true
            adapter = MinutePickerAdapter(minut) { selectedMinutes ->
                dialog.dismiss()
                snoozeAlarm(selectedMinutes,isAdjustTime)
            }
        }

        dialog.show()
    }
}
