package com.example

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import java.text.SimpleDateFormat
import java.util.Locale

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "Reminder"
        val message = intent.getStringExtra("message") ?: "Time for your task!"
        
        showNotification(context, title, message)
    }

    private fun showNotification(context: Context, title: String, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "task_reminders"
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Task Reminders", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val i = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, i, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}

object AlarmScheduler {
    fun scheduleTaskAlarm(context: Context, title: String, scheduledTimeString: String?) {
        if (scheduledTimeString == null) return
        
        try {
            // New formats might be: "HH:mm yyyy-MM-dd|REPEAT", "yyyy-MM-dd|REPEAT", "HH:mm|REPEAT", "yyyy-MM-dd", etc.
            val cleanStr = scheduledTimeString.substringBefore("|").trim()
            if (cleanStr.isEmpty()) return
            
            var taskCal = java.util.Calendar.getInstance()
            
            if (cleanStr.length == 5 && cleanStr.contains(":")) {
                // Time only
                val parts = cleanStr.split(":")
                taskCal.set(java.util.Calendar.HOUR_OF_DAY, parts[0].toIntOrNull() ?: 12)
                taskCal.set(java.util.Calendar.MINUTE, parts[1].toIntOrNull() ?: 0)
                taskCal.set(java.util.Calendar.SECOND, 0)
                if (taskCal.timeInMillis < System.currentTimeMillis()) {
                    taskCal.add(java.util.Calendar.DAY_OF_YEAR, 1)
                }
            } else if (cleanStr.length == 10) {
                // Date only
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val d = sdf.parse(cleanStr) ?: return
                taskCal.time = d
                taskCal.set(java.util.Calendar.HOUR_OF_DAY, 9) // default to 9 AM
                taskCal.set(java.util.Calendar.MINUTE, 0)
                taskCal.set(java.util.Calendar.SECOND, 0)
            } else {
                // Time and Date: "HH:mm yyyy-MM-dd"
                val sdf = SimpleDateFormat("HH:mm yyyy-MM-dd", Locale.getDefault())
                val d = sdf.parse(cleanStr)
                if (d != null) {
                    taskCal.time = d
                } else return
            }

            if (taskCal.timeInMillis < System.currentTimeMillis()) {
                 // Already passed, handle repeats manually maybe? For now just skip.
                 return
            }

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            
            // 1. Exact Alarm
            val exactIntent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra("title", "Taco: Task Reminder")
                putExtra("message", "Time for: $title")
            }
            val exactPendingIntent = PendingIntent.getBroadcast(
                context, 
                title.hashCode(), 
                exactIntent, 
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                taskCal.timeInMillis,
                exactPendingIntent
            )
            
            // 2. Morning Reminder (e.g., 9:00 AM that day)
            val morningCal = taskCal.clone() as java.util.Calendar
            morningCal.set(java.util.Calendar.HOUR_OF_DAY, 9)
            morningCal.set(java.util.Calendar.MINUTE, 0)
            morningCal.set(java.util.Calendar.SECOND, 0)
            
            if (morningCal.before(taskCal) && morningCal.timeInMillis > System.currentTimeMillis()) {
                val morningIntent = Intent(context, AlarmReceiver::class.java).apply {
                    putExtra("title", "Taco: Daily Overview")
                    putExtra("message", "Don't forget you have: $title scheduled for today.")
                }
                val morningPendingIntent = PendingIntent.getBroadcast(
                    context, 
                    title.hashCode() + 1, // Different ID
                    morningIntent, 
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    morningCal.timeInMillis,
                    morningPendingIntent
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
