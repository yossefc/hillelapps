package com.example.hillelapps.presentation

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager

class AlarmReceiver : BroadcastReceiver() {
    @SuppressLint("WearRecents")
    override fun onReceive(context: Context, intent: Intent) {
        // Déclencher l'alarme ici
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyApp:AlarmWakeLock")
        wl.acquire(10*60*1000L /*10 minutes*/)

        // Déclenchez votre sonnerie ici
        // Par exemple, vous pouvez lancer une activité ou un service pour jouer le son
        val alarmIntent = Intent(context, MainActivity::class.java)
        alarmIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(alarmIntent)

        wl.release()
    }
}